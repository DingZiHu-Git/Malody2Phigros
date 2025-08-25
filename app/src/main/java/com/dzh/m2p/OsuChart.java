package com.dzh.m2p;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OsuChart {
	public String artist;
	public String background;
	private int circleSize;
	public String creator;
	private int modeInt;
	public String mode;
	public int offset;
	public String sound;
	public String title;
	public String version;
	public List<Note> note;
	public OsuChart(File file) throws IOException {
		note = new ArrayList<>();
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
		String line;
		boolean hitObjects = false;
		while ((line = br.readLine()) != null) {
			if (hitObjects) note.add(new Note(line));
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
			else if (line.equalsIgnoreCase("[hitobjects]")) hitObjects = true;
			else if (line.isEmpty()) hitObjects = false;
		}
		br.close();
		switch (modeInt) {
			case 3:
				mode = String.valueOf(circleSize) + "K";
				break;
			default: mode = null;
		}
	}
	public static class Note {
		public boolean isLN;
		public int time;
		public int endTime;
		public int x;
		public Note(String str) {
			String[] params = str.split(",");
			x = Integer.parseInt(params[0].trim());
			time = Integer.parseInt(params[2].trim());
			String[] param = params[5].split(":");
			isLN = param.length == 6;
			endTime = Integer.parseInt(param[0].trim());
		}
	}
	public String convert(boolean enableConst, double defaultSpeed, boolean enableLuck) {
		return null;
	}
}
