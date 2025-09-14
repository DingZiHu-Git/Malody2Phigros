package com.dzh.m2p;

import com.dzh.m2p.Fraction;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class OsuChart extends Chart {
	private int circleSize;
	private int modeInt;
	private List<TimingPoint> time;
	private List<TimingPoint> effect;
	private List<HitObject> note;
	public OsuChart(File file) throws IOException {
		time = new ArrayList<>();
		note = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		boolean timingPoints = false;
		boolean hitObjects = false;
		while ((line = br.readLine()) != null) {
			if (line.isEmpty()) timingPoints = hitObjects = false;
			else if (timingPoints) {
				TimingPoint tp = new TimingPoint(line);
				if (tp.isEffect) effect.add(tp);
				else time.add(tp);
			} else if (hitObjects) note.add(new HitObject(line));
			else if (line.toLowerCase().startsWith("audiofilename")) sound = line.substring(line.indexOf(":") + 1).trim();
			else if (line.toLowerCase().startsWith("mode")) modeInt = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
			else if (line.toLowerCase().startsWith("title")) title = (title == null ? line.substring(line.indexOf(":") + 1).trim() : title);
			else if (line.toLowerCase().startsWith("titleunicode") && !line.substring(line.indexOf(":") + 1).trim().isEmpty()) title = line.substring(line.indexOf(":") + 1).trim();
			else if (line.toLowerCase().startsWith("artist")) artist = (artist == null ? line.substring(line.indexOf(":") + 1).trim() : artist);
			else if (line.toLowerCase().startsWith("artistunicode") && !line.substring(line.indexOf(":") + 1).trim().isEmpty()) artist = line.substring(line.indexOf(":") + 1).trim();
			else if (line.toLowerCase().startsWith("creator")) creator = line.substring(line.indexOf(":") + 1).trim();
			else if (line.toLowerCase().startsWith("version")) version = line.substring(line.indexOf(":") + 1).trim();
			else if (line.toLowerCase().startsWith("circlesize")) circleSize = Integer.parseInt(line.substring(line.indexOf(":") + 1).trim());
			else if (line.toLowerCase().startsWith("0,0,")) background = line.split(",")[2].substring(1, line.split(",")[2].length() - 1);
			else if (line.equalsIgnoreCase("[timingpoints]")) timingPoints = true;
			else if (line.equalsIgnoreCase("[hitobjects]")) hitObjects = true;
		}
		br.close();
		switch (modeInt) {
			case 3:
				mode = String.valueOf(circleSize) + "K";
				break;
			default: mode = null;
		}
	}
	public static class TimingPoint {
		public int time;
		public double beatLength;
		public int meter;
		public boolean isEffect;
		public TimingPoint(String str) {
			String[] params = str.split(",");
			time = Integer.parseInt(params[0].trim());
			beatLength = Double.parseDouble(params[1].trim());
			meter = Integer.parseInt(params[2].trim());
			isEffect = Integer.parseInt(params[6].trim()) == 0;
		}
	}
	public static class HitObject {
		public boolean isLN;
		public int time;
		public int endTime;
		public int x;
		public HitObject(String str) {
			String[] params = str.split(",");
			x = Integer.parseInt(params[0].trim());
			time = Integer.parseInt(params[2].trim());
			String[] param = params[5].split(":");
			isLN = param.length == 6;
			endTime = Integer.parseInt(param[0].trim());
		}
	}
	@Override
	public JSONObject convert(ConvertionOptionsFragment cof) throws JSONException {
		/*int id = Random.nextInt();
		if (id < 0) id = -id;
		JSONObject result = new JSONObject();
		TimingPoint lastTimingPoint = null;
		JSONArray bpmList = new JSONArray();
		for (int i = 0; i < time.size(); i++) if (!time.get(i).isEffect) {
			bpmList.put(new JSONObject().put("startTime", time.getJSONObject(i).getJSONArray("beat")).put("bpm", time.getJSONObject(i).getDouble("bpm")));
		}
		result.put("BPMList", bpmList).put("META", new JSONObject().put("RPEVersion", 140).put("background", background).put("charter", creator).put("composer", artist).put("id", String.valueOf(id)).put("level", version).put("name", title).put("offset", -offset).put("song", sound)).put("judgeLineGroup", new JSONArray().put("Default"));
		JSONArray judgeLineList = new JSONArray();
		JSONObject main = new JSONObject().put("Group", 0).put("Name", "main").put("Texture", "line.png").put("alphaControl", new JSONArray().put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 0d)).put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 9999999d))).put("bpmfactor", 1d);
		JSONArray mainSpeedEvents = new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", cof.defaultSpeed).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", cof.defaultSpeed).put("startTime", new JSONArray().put(0).put(0).put(1)));
		if (!cof.enableConst) {
			Map<Fraction, Double> speeds = new HashMap<>();
			double bpm = time.get(0).beatLength;
			lastTimingPoint = null;
			for (int i = 1; i < time.size(); i++) {
				TimingPoint lastSpeed = time.get(i - 1);
				TimingPoint speed = time.get(i);
				speeds.put(lastBeat.add(new Fraction(speed.time - lastSpeed.time)), speed.beatLength / (speed.isEffect ? -100d : bpm));
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
		
		main.put("notes", notes).put("numOfNotes", notes.length()).put("posControl", new JSONArray().put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("pos", 1d).put("x", 9999999d))).put("skewControl", new JSONArray().put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 0d)).put(new JSONObject().put("easing", 1).put("skew", 0d).put("x", 9999999d))).put("yControl", new JSONArray().put(new JSONObject().put("easing", 1).put("x", 0d).put("y", 1d)).put(new JSONObject().put("easing", 1).put("x", 9999999d).put("y", 1d))).put("zOrder", 0);
		judgeLineList.put(main);
		return result.put("judgeLineList", judgeLineList).put("multiLineString", "0").put("multiScale", 1d);*/
		return null;
	}
	@Override
	public JSONObject getExtra() throws JSONException {
		return null;
	}
}
