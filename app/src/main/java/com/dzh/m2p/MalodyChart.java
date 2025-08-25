package com.dzh.m2p;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MalodyChart {
	public String artist;
	public String background;
	private JSONArray time;
	public String creator;
	private JSONArray effect;
	public String mode;
	private int modeInt;
	public int offset;
	public String sound;
	public String title;
	public String version;
	public JSONArray note;
	public MalodyChart(File file) throws IOException, JSONException {
		String json = "";
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		while ((line = br.readLine()) != null) json += line;
		JSONObject main = new JSONObject(json);
		JSONObject meta = main.getJSONObject("meta");
		JSONObject song = meta.getJSONObject("song");
		String temp = null;
		if (song.has("artistorg")) temp = song.getString("artistorg");
		artist = temp != null && !temp.isEmpty() ? temp : song.getString("artist");
		background = meta.getString("background");
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
		temp = null;
		if (song.has("titleorg")) temp = song.getString("titleorg");
		title = temp != null && !temp.isEmpty() ? temp : song.getString("title");
		version = meta.getString("version");
		time = main.getJSONArray("time");
		effect = main.getJSONArray("effect");
		note = main.getJSONArray("note");
		for (int i = 0; i < note.length(); i++) {
			JSONObject obj = note.getJSONObject(i);
			if (obj.has("sound")) {
				offset = obj.getInt("offset");
				sound = obj.getString("sound");
				break;
			}
		}
	}
	public String convert(boolean enableConst, double defaultSpeed, double defaultY, boolean enableLuck, int defaultSize, int slideProcessMode, boolean enableDrag, boolean fakeDrag, int dragAlpha, Fraction dragInterval, boolean optimizeSlide) throws JSONException {
		JSONObject result = new JSONObject();
		JSONArray bpmList = new JSONArray();
		for (int i = 0; i < time.length(); i++) bpmList.put(new JSONObject().put("startTime", time.getJSONObject(i).getJSONArray("beat")).put("bpm", time.getJSONObject(i).getDouble("bpm")));
		result.put("BPMList", bpmList).put("META", new JSONObject().put("RPEVersion", 140).put("background", background).put("charter", creator).put("composer", artist).put("id", Random.nextLong(0, Long.MAX_VALUE)).put("level", version).put("name", title).put("offset", offset).put("song", sound)).put("judgeLineGroup", new JSONArray().put("Default"));
		JSONArray judgeLineList = new JSONArray();
		JSONObject main = new JSONObject().put("Group", 0).put("Name", "main").put("Texture", "line.png").put("alphaControl", new JSONArray().put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 0d)).put(new JSONObject().put("alpha", 1d).put("easing", 1).put("x", 9999999d))).put("bpmfactor", 1d);
		JSONArray mainEventLayers = new JSONArray().put(new JSONObject().put("alphaEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 255).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 255).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	    .put("moveXEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	    .put("moveYEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", defaultY).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", defaultY).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	    .put("rotateEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", 0d).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", 0d).put("startTime", new JSONArray().put(0).put(0).put(1))))
																	    .put("speedEvents", new JSONArray().put(new JSONObject().put("bezier", 0).put("bezierPoints", new JSONArray().put(0d).put(0d).put(0d).put(0d)).put("easingLeft", 0d).put("easingRight", 1d).put("easingType", 1).put("end", defaultSpeed).put("endTime", new JSONArray().put(1).put(0).put(1)).put("linkgroup", 0).put("start", defaultSpeed).put("startTime", new JSONArray().put(0).put(0).put(1)))));
		if (!enableConst) {
			
		}
		return result.toString();
	}
}
