package com.dzh.m2p;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.widget.Adapter;

public class ChartListFragment extends Fragment {
	private Activity activity;
	private List<Map<String, Object>> listData;
	private SimpleAdapter listAdapter;
	private ListView list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_chart_list, container, false);
		activity = getActivity();
		listData = new ArrayList<>();
		listAdapter = new SimpleAdapter(activity, listData, R.layout.chart_list_item, new String[]{ "title", "description", "check" }, new int[]{ R.id.chart_list_item_title, R.id.chart_list_item_description, R.id.chart_list_item_check });
		list = root.findViewById(R.id.fragment_chart_list_list);
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Map<String, Object> map = listData.get(position);
					map.put("check", !((boolean) map.get("check")));
					listData.set(position, map);
					listAdapter.notifyDataSetChanged();
				}
			}
		);
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					new AlertDialog.Builder(activity).setMessage("Not finished!").show();
					return true;
				}
			}
		);
		new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						refresh();
					} catch (Exception e) {
						catcher(e);
					}
				}
			}
		, "init").start();
		return root;
	}
	public void refresh() throws IOException, JSONException {
		listData.clear();
		for (File f : activity.getFilesDir().listFiles()) {
			boolean add = false;
			Map<String, Object> map = new HashMap<>();
			map.put("title", f.getName());
			if (f.isDirectory()) {
				for (File c : f.listFiles()) {
					if (c.getName().toLowerCase().endsWith(".mc")) {
						add = true;
						parseMalody(map, c);
					} else if (c.getName().toLowerCase().endsWith(".osu")) {
						add = true;
						parseOsu(map, c);
					}
				}
			} else {
				if (f.getName().toLowerCase().endsWith(".mc")) {
					add = true;
					parseMalody(map, f);
				} else if (f.getName().toLowerCase().endsWith(".osu")) {
					add = true;
					parseOsu(map, f);
				}
			}
			map.put("check", false);
			if (add) listData.add(map);
		}
		activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listAdapter.notifyDataSetChanged();
					list.setVisibility(listData.isEmpty() ? View.GONE : View.VISIBLE);
				}
			}
		);
	}
	private void parseMalody(Map<String, Object> map, File f) throws IOException, JSONException {
		MalodyChart mc = new MalodyChart(f);
		map.put("background", mc.background);
		map.put("audio", mc.sound);
		map.put("offset", -mc.offset);
		map.put("description", getString(R.string.fragment_chart_list_description_title) + mc.title + "\n" + getString(R.string.fragment_chart_list_description_artist) + mc.artist + "\n" + getString(R.string.fragment_chart_list_description_creator) + mc.creator + "\n" + getString(R.string.fragment_chart_list_description_mode) + (mc.mode == null ? getString(R.string.unknown) : mc.mode) + "\n" + getString(R.string.fragment_chart_list_description_version) + mc.version);
	}
	private void parseOsu(Map<String, Object> map, File f) throws IOException {
		map.put("offset", 0);
		OsuChart oc = new OsuChart(f);
		map.put("description", getString(R.string.fragment_chart_list_description_title) + oc.title + "\n" + getString(R.string.fragment_chart_list_description_artist) + oc.artist + "\n" + getString(R.string.fragment_chart_list_description_creator) + oc.creator + "\n" + getString(R.string.fragment_chart_list_description_mode) + (oc.mode == null ? getString(R.string.unknown) : oc.mode) + "\n" + getString(R.string.fragment_chart_list_description_version) + oc.version);
	}
	private void catcher(Exception e) {
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder adb = new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								((ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Malody2Phigros", sw.toString()));
								activity.finish();
							}
						}
					).setNegativeButton(R.string.crash_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								activity.finish();
							}
						}
					).setCancelable(false);
					if (BuildConfig.DEBUG) adb.setNeutralButton("DEBUG", null);
					adb.show();
				}
			}
		);
		try {
			sw.close();
		} catch (Exception ignore) {}
	}
}
