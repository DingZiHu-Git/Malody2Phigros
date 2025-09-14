package com.dzh.m2p;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MalodyChart extends Chart {
	private JSONArray time;
	private JSONArray effect;
	private int modeInt;
	private JSONObject modeExt;
	private JSONArray note;
	public MalodyChart(File file) throws IOException, JSONException {
		String json = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		json = br.readLine();
		br.close();
		JSONObject main = new JSONObject(json);
		JSONObject meta = main.getJSONObject("meta");
		JSONObject song = meta.getJSONObject("song");
		String temp = null;
		if (song.has("artistorg")) temp = song.getString("artistorg");
		artist = temp != null && !temp.isEmpty() ? temp : song.getString("artist");
		background = meta.has("background") && !meta.getString("background").isEmpty() ? meta.getString("background") : null;
		video = meta.has("video") && !meta.getString("video").isEmpty() ? meta.getString("video") : null;
		creator = meta.getString("creator");
		modeInt = meta.getInt("mode");
		switch (modeInt) {
			case 0:
				mode = String.valueOf(meta.getJSONObject("mode_ext").getInt("column")) + "K";
				break;
			case 7:
				mode = "Slide";
				break;
			default: mode = null;
		}
		modeExt = meta.has("mode_ext") ? meta.getJSONObject("mode_ext") : null;
		temp = null;
		if (song.has("titleorg")) temp = song.getString("titleorg");
		title = temp != null && !temp.isEmpty() ? temp : song.getString("title");
		version = meta.getString("version");
		time = main.getJSONArray("time");
		effect = main.has("effect") ? main.getJSONArray("effect") : new JSONArray();
		note = main.has("note") ? main.getJSONArray("note") : new JSONArray();
		for (int i = 0; i < note.length(); i++) {
			JSONObject obj = note.getJSONObject(i);
			if (obj.has("sound")) {
				offset = obj.has("offset") ? obj.getInt("offset") : 0;
				sound = obj.getString("sound");
				break;
			}
		}
	}
	@Override
	public JSONObject convert(ConvertionOptionsFragment cof) throws JSONException {
		int id = Random.nextInt();
		if (id < 0) id = -id;
		JSONObject result = new JSONObject();
		JSONArray bpmList = new JSONArray();
		for (int i = 0; i < time.length(); i++) bpmList.put(new JSONObject().put("startTime", time.getJSONObject(i).getJSONArray("beat")).put("bpm", time.getJSONObject(i).getDouble("bpm")));
		result.put("BPMList", bpmList).put("META", new JSONObject().put("RPEVersion", 140).put("background", background).put("charter", creator).put("composer", artist).put("id", String.valueOf(id)).put("level", version).put("name", title).put("offset", -offset).put("song", sound)).put("judgeLineGroup", new JSONArray().put("Default"));
		JSONArray judgeLineList = new JSONArray();
		JSONObject main = new JSONObject().put("Group", 0).put("Name", "main").put("Texture", "line.png").put("alphaControl", new JSONArray().put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 0d)).put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 9999999d))).put("bpmfactor", 1d);
		JSONArray mainSpeedEvents = new JSONArray();
		if (!cof.enableConst) {
			Fraction eoc = null;
			for (int i = note.length() - 1; i > 0; i--) if (!note.getJSONObject(i).has("sound")) eoc = new Fraction(note.getJSONObject(i).getJSONArray("beat"));
			Map<Fraction, Double> speeds = new HashMap<>();
			JSONObject bpm = new JSONObject().put("beat", new JSONArray().put(Integer.MIN_VALUE).put(0).put(1));
			ArrayList<Fraction> dt = new ArrayList<>();
			for (int i = 0; i < time.length(); i++) dt.add((i == time.length() - 1 ? eoc : new Fraction(time.getJSONObject(i + 1).getJSONArray("beat"))).subtract(new Fraction(time.getJSONObject(i).getJSONArray("beat"))));
			for (int i = 0; i < dt.size(); i++) if (dt.get(i).compareTo(new Fraction(bpm.getJSONArray("beat"))) == 1) bpm = time.getJSONObject(i);
			for (int i = 0; i < time.length(); i++) {
				JSONObject speed = time.getJSONObject(i);
				speeds.put(new Fraction(speed.getJSONArray("beat")), speed.getDouble("bpm") / bpm.getDouble("bpm"));
			}
			double lastSpeed = 0;
			for (int i = 0; i < effect.length(); i++) {
				JSONObject speed = effect.getJSONObject(i);
				Fraction beat = new Fraction(speed.getJSONArray("beat"));
				if (speed.has("scroll") || speed.has("sv")) speeds.put(beat, lastSpeed = speeds.containsKey(beat) ? speeds.get(beat) * (speed.has("sv") ? speed.getDouble("sv") : speed.getDouble("scroll")) : (lastSpeed = speed.has("sv") ? speed.getDouble("sv") : speed.getDouble("scroll")));
				else if (speed.has("jump")) {
					speeds.put(beat, speeds.containsKey(beat) ? speeds.get(beat) * speed.getDouble("jump") * 1000 : speed.getDouble("jump") * 1000);
					speeds.put(beat.add(new Fraction(0, 1, 256)), lastSpeed);
				}
			}
			ArrayList<Map.Entry<Fraction, Double>> list = new ArrayList<Map.Entry<Fraction, Double>>(speeds.entrySet());
			list.sort(new Comparator<Map.Entry<Fraction, Double>>() {
					@Override
					public int compare(Map.Entry<Fraction, Double> o1, Map.Entry<Fraction, Double> o2) {
						return o1.getKey().compareTo(o2.getKey());
					}
				}
			);
			for (int i = 0; i < list.size(); i++) {
				Map.Entry<Fraction, Double> entry = list.get(i);
				Fraction beat = entry.getKey();
				Fraction nextBeat = i == list.size() - 1 ? beat.add(new Fraction(1, 0, 1)) : list.get(i + 1).getKey();
				double speed = entry.getValue();
				mainSpeedEvents.put(new JSONObject().put("end", cof.defaultSpeed * speed).put("endTime", nextBeat.toJSONArray()).put("linkgroup", 0).put("start", cof.defaultSpeed * speed).put("startTime", beat.toJSONArray()));
			}
		}
		JSONArray mainEventLayers = new JSONArray().put(new JSONObject().put("alphaEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 255).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 255).put("startTime", new JSONArray().put(0).put(0).put(1))))
												   .put("moveXEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))
												   .put("moveYEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", cof.defaultY).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", cof.defaultY).put("startTime", new JSONArray().put(0).put(0).put(1))))
												   .put("rotateEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))
												   .put("speedEvents", mainSpeedEvents));
		main.put("eventLayers", mainEventLayers).put("extended", new JSONObject().put("inclineEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))).put("father", -1).put("isCover", cof.isCover ? 1 : 0);
		JSONArray notes = new JSONArray();
		if (modeInt == 0) {
			int column = modeExt.getInt("column");
			if (cof.enableLuck) {
				Fraction[] occupied = new Fraction[column];
				for (int i = 0; i < occupied.length; i++) occupied[i] = new Fraction(-1, 0, 1);
				for (int i = 0; i < note.length(); i++) {
					JSONObject n = note.getJSONObject(i);
					if (n.has("sound")) continue;
					Fraction target = new Fraction(n.has("endbeat") ? n.getJSONArray("endbeat") : n.getJSONArray("beat"));
					int track = 0;
					while (true) if (new Fraction(n.getJSONArray("beat")).compareTo(occupied[track = Random.nextInt(0, occupied.length - 1)]) == 1) {
						occupied[track] = target;
						break;
					}
					note.getJSONObject(i).put("column", track);
				}
			}
			for (int i = 0; i < note.length(); i++) {
				JSONObject jo = note.getJSONObject(i);
				if (jo.has("sound")) continue;
				notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", jo.has("endbeat") ? jo.getJSONArray("endbeat") : jo.getJSONArray("beat")).put("isFake", 0).put("positionX", 1350d / Double.valueOf(column + 1) * Double.valueOf(jo.getInt("column") + 1) - 675d).put("size", 1d).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", jo.has("endbeat") ? 2 : 1).put("visibleTime", 999999d).put("yOffset", 0d));
			}
		} else if (modeInt == 7) {
			for (int i = 0; i < note.length(); i++) {
				JSONObject jo = note.getJSONObject(i);
				if (jo.has("sound")) continue;
				double size = cof.defaultWide == 0 ? 1d : Math.max(jo.has("w") ? jo.getInt("w") : 50, 19) / (double) cof.defaultWide;
				double x = ((jo.has("x") ? jo.getInt("x") : 0) - 127.5) / 127.5 * 675d;
				if (jo.has("seg")) {
					Fraction beat = new Fraction(jo.getJSONArray("beat"));
					JSONArray seg = jo.getJSONArray("seg");
					if (cof.slideProcessMode == 0) notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", new Fraction(jo.getJSONArray("beat")).add(new Fraction(seg.getJSONObject(seg.length() - 1).getJSONArray("beat"))).toJSONArray()).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 2).put("visibleTime", 999999d).put("yOffset", 0d));
					else if (cof.slideProcessMode == 1) {
						notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", beat.toJSONArray()).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", beat.toJSONArray()).put("type", 1).put("visibleTime", 999999d).put("yOffset", 0d));
						Fraction lastBeat = new Fraction();
						int lastX = 0;
						for (int j = 0; j < seg.length(); j++) {
							JSONObject s = seg.getJSONObject(j);
							int moveX = s.has("x") ? s.getInt("x") : 0;
							Fraction target = new Fraction(s.getJSONArray("beat"));
							double v = (moveX - lastX) / target.subtract(lastBeat).toBigDecimal().doubleValue();
							Fraction current = lastBeat;
							while (current.compareTo(target) < 0) {
								current = current.add(cof.dragInterval);
								notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", beat.add(current).toJSONArray()).put("isFake", 0).put("positionX", x + (lastX + v * (current.subtract(lastBeat)).toBigDecimal().doubleValue()) / 127.5 * 675d).put("size", size).put("speed", 1d).put("startTime", beat.add(current).toJSONArray()).put("type", 4).put("visibleTime", 999999d).put("yOffset", 0d));
							}
							lastBeat = target;
							lastX = moveX;
						}
					} else if (cof.slideProcessMode == 2) {
						int temp = seg.getJSONObject(0).has("x") ? seg.getJSONObject(0).getInt("x") : 0;
						if (seg.length() == 1 && temp == 0) notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", new Fraction(jo.getJSONArray("beat")).add(new Fraction(seg.getJSONObject(0).getJSONArray("beat"))).toJSONArray()).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 2).put("visibleTime", 999999d).put("yOffset", 0d));
						else {
							JSONObject hold = new JSONObject().put("Group", 0).put("Name", "main").put("Texture", "line.png").put("alphaControl", new JSONArray().put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 0d)).put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 9999999d))).put("bpmfactor", 1d);
							JSONArray moveXEvents = new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1)));
							Fraction lastBeat = new Fraction();
							int lastX = 0;
							for (int j = 0; j < seg.length(); j++) {
								JSONObject s = seg.getJSONObject(j);
								moveXEvents.put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", s.getInt("x") / 127.5 * 675d).put("endTime", beat.add(new Fraction(s.getJSONArray("beat"))).toJSONArray()).put("linkgroup", 0).put("start", lastX / 127.5 * 675d).put("startTime", beat.add(lastBeat).toJSONArray()));
								int moveX = s.has("x") ? s.getInt("x") : 0;
								Fraction target = new Fraction(s.getJSONArray("beat"));
								double v = (moveX - lastX) / target.subtract(lastBeat).toBigDecimal().doubleValue();
								Fraction current = lastBeat;
								while (current.compareTo(target) < 0) {
									current = current.add(cof.dragInterval);
									if (cof.enableDrag) notes.put(new JSONObject().put("above", 1).put("alpha", cof.dragAlpha).put("endTime", beat.add(current).toJSONArray()).put("isFake", cof.fakeDrag ? 1 : 0).put("positionX", x + (lastX + v * (current.subtract(lastBeat)).toBigDecimal().doubleValue()) / 127.5 * 675d).put("size", size).put("speed", 1d).put("startTime", beat.add(current).toJSONArray()).put("type", 4).put("visibleTime", 999999d).put("yOffset", 0d));
								}
								lastBeat = target;
								lastX = moveX;
							}
							JSONArray holdEventLayers = new JSONArray().put(new JSONObject().put("alphaEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 255).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 255).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	   .put("moveXEvents", moveXEvents)
																	   .put("moveYEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", cof.defaultY).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", cof.defaultY).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	   .put("rotateEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	   .put("speedEvents", mainSpeedEvents));
							hold.put("eventLayers", holdEventLayers).put("extended", new JSONObject().put("inclineEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))).put("father", -1).put("isCover", cof.isCover ? 1 : 0).put("notes", new JSONArray().put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", new Fraction(jo.getJSONArray("beat")).add(new Fraction(seg.getJSONObject(seg.length() - 1).getJSONArray("beat"))).toJSONArray()).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 2).put("visibleTime", 999999d).put("yOffset", 0d))).put("numOfNotes", 1).put("posControl", new JSONArray().put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 9999999d))).put("skewControl", new JSONArray().put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 9999999d))).put("yControl", new JSONArray().put(new JSONObject().put("easing", 1).put("x", 0d).put("y", 1d)).put(new JSONObject().put("easing", 1).put("x", 9999999d).put("y", 1d))).put("zOrder", 0);
							judgeLineList.put(hold);
						}
					}
				} else if (jo.has("type") && jo.has("dir")) notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", jo.getJSONArray("beat")).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 3).put("visibleTime", 999999d).put("yOffset", 0d));
				else if (jo.has("type")) notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", jo.getJSONArray("beat")).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 4).put("visibleTime", 999999d).put("yOffset", 0d));
				else if (jo.has("dir")) {
					double px = 0;
					switch (jo.getInt("dir")) {
						case 2:
							px = 135;
							break;
						case 8:
							px = -135;
							break;
					}
					notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", jo.getJSONArray("beat")).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 1).put("visibleTime", 999999d).put("yOffset", 0d));
					notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", new Fraction(jo.getJSONArray("beat")).add(new Fraction(0, 1, 32)).toJSONArray()).put("isFake", 0).put("positionX", x + px).put("size", size).put("speed", 1d).put("startTime", new Fraction(jo.getJSONArray("beat")).add(new Fraction(0, 1, 32)).toJSONArray()).put("type", 3).put("visibleTime", 999999d).put("yOffset", 0d));
				} else notes.put(new JSONObject().put("above", 1).put("alpha", 255).put("endTime", jo.getJSONArray("beat")).put("isFake", 0).put("positionX", x).put("size", size).put("speed", 1d).put("startTime", jo.getJSONArray("beat")).put("type", 1).put("visibleTime", 999999d).put("yOffset", 0d));
			}
		}
		main.put("notes", notes).put("numOfNotes", notes.length()).put("posControl", new JSONArray().put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 9999999d))).put("skewControl", new JSONArray().put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 9999999d))).put("yControl", new JSONArray().put(new JSONObject().put("easing", 1).put("x", 0d).put("y", 1d)).put(new JSONObject().put("easing", 1).put("x", 9999999d).put("y", 1d))).put("zOrder", 0);
		judgeLineList.put(main);
		return result.put("judgeLineList", judgeLineList).put("multiLineString", "0").put("multiScale", 1d);
	}
	@Override
	public JSONObject getExtra() throws JSONException {
		if (video == null) return null;
		JSONArray bpm = new JSONArray();
		for (int i = 0; i < time.length(); i++) bpm.put(new JSONObject().put("bpm", time.getJSONObject(i).getDouble("bpm")).put("time", time.getJSONObject(i).getJSONArray("beat")));
		return new JSONObject().put("bpm", bpm).put("videos", new JSONObject().put("path", video).put("dim", 0.5));
	}
}
