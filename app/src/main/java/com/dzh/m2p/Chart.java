package com.dzh.m2p;

import org.json.JSONObject;

public abstract class Chart {
	public String artist;
	public String background;
	public String creator;
	public String mode;
	public int offset;
	public String sound;
	public String title;
	public String version;
	public abstract JSONObject convert(ConvertionOptionsFragment cof) throws Exception;
}
