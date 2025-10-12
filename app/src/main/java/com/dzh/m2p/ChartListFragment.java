package com.dzh.m2p;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ChartListFragment extends Fragment {
	public boolean loaded;
	private Activity activity;
	private List<Map<String, Object>> listData = new ArrayList<>();
	private SimpleAdapter listAdapter;
	private ListView list;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.fragment_chart_list, container, false);
		activity = getActivity();
		listAdapter = new SimpleAdapter(activity, listData, R.layout.chart_list_item, new String[]{ "title", "description", "check" }, new int[]{ R.id.chart_list_item_title, R.id.chart_list_item_description, R.id.chart_list_item_check });
		list = root.findViewById(R.id.fragment_chart_list_list);
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					try {
						Map<String, Object> map = listData.get(position);
						File f = (File) map.get("file");
						f.renameTo(new File(f.getParent() + File.separator + ((boolean) map.get("check") ? f.getName() + ".disabled" : f.getName().substring(0, f.getName().lastIndexOf(".")))));
						refresh();
					} catch (Exception e) {
						catcher(e);
					}
				}
			}
		);
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
					final Map<String, Object> map = listData.get(position);
					String title = (String) map.get("title");
					ViewGroup.LayoutParams vglp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					LinearLayout ll = new LinearLayout(activity);
					ll.setOrientation(LinearLayout.VERTICAL);
					LinearLayout ll1 = new LinearLayout(activity);
					ll1.setLayoutParams(vglp);
					ll1.setOrientation(LinearLayout.HORIZONTAL);
					TextView tv1 = new TextView(activity);
					tv1.setTextAppearance(android.R.style.TextAppearance_Large);
					tv1.setTextSize(16);
					tv1.setText(R.string.fragment_chart_list_modify_title);
					final EditText et1 = new EditText(activity);
					et1.setLayoutParams(vglp);
					et1.setText(title);
					ll1.addView(tv1);
					ll1.addView(et1);
					final Switch s1 = new Switch(activity);
					s1.setLayoutParams(vglp);
					s1.setTextSize(16);
					s1.setText(R.string.fragment_chart_list_pack_converted_chart);
					s1.setChecked(map.get("pack"));
					s1.setEnabled(map.get("packable"));
					ll.addView(ll1);
					ll.addView(s1);
					new AlertDialog.Builder(activity).setTitle(title).setView(ll).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									File f = (File) map.get("file");
									String extension = (String) map.get("extension");
									if (f.isDirectory()) {
										for (File c : f.listFiles()) {
											if (c.getName().equals(f.getName())) c.renameTo(new File(f.getAbsolutePath() + File.separator + et1.getText().toString() + extension));
											else if (!s1.isChecked() && c.getName().equals("pack")) c.delete();
										}
										if (s1.isChecked()) new File(f.getAbsolutePath() + File.separator + "pack").createNewFile();
									}
									f.renameTo(new File(f.getParent() + File.separator + et1.getText().toString() + extension));
									refresh();
								} catch (Exception e) {
									catcher(e);
								}
							}
						}
					).setNegativeButton(android.R.string.cancel, null).setNeutralButton(R.string.fragment_chart_list_delete_chart, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								try {
									File f = (File) map.get("file");
									if (f.isDirectory()) for (File c : f.listFiles()) c.delete();
									f.delete();
									refresh();
								} catch (Exception e) {
									catcher(e);
								}
							}
						}
					).setCancelable(false).show();
					return true;
				}
			}
		);
		try {
			refresh();
		} catch (Exception e) {
			catcher(e);
		}
		loaded = true;
		return root;
	}
	public void convert(ExecutorService es, final ConvertionOptionsFragment cof, final Uri uri) {
		for (final Map<String, Object> map : listData) {
			if ((boolean) map.get("check")) es.submit(new Runnable() {
					@Override
					public void run() {
						try {
							File temp = new File(activity.getCacheDir() + File.separator + map.get("title"));
							temp.mkdirs();
							Chart chart = (Chart) map.get("chart");
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp.getAbsolutePath() + File.separator + map.get("title") + ".json"), "UTF-8"));
							bw.write(chart.convert(cof).toString());
							bw.close();
							if ((boolean) map.get("pack")) {
								File f = (File) map.get("file");
								if (chart.background == null) FileUtil.copy(activity.getResources().openRawResource(R.raw.icon), temp.getAbsolutePath() + File.separator + "icon.png", new byte[128671]);
								else FileUtil.copy(f.getAbsolutePath() + File.separator + chart.background, temp.getAbsolutePath() + File.separator + chart.background, new byte[1024 * 1024 * 4]);
								if (chart.video != null) {
									FileUtil.copy(f.getAbsolutePath() + File.separator + chart.video, temp.getAbsolutePath() + File.separator + chart.video, new byte[1024 * 1024 * 4]);
									bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp.getAbsolutePath() + File.separator + "extra.json"), "UTF-8"));
									bw.write(chart.getExtra().toString());
									bw.close();
								}
								FileUtil.copy(f.getAbsolutePath() + File.separator + chart.sound, temp.getAbsolutePath() + File.separator + chart.sound, new byte[1024 * 1024 * 4]);
								bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(temp.getAbsolutePath() + File.separator + "info.txt"), "UTF-8"));
								bw.write("#\nName: " + chart.title + "\nPath: " + chart.title + "\nSong: " + chart.sound + "\nPicture: " + chart.background + "\nChart: " + map.get("title") + ".json\nLevel: " + chart.version + "\nComposer: " + chart.artist + "\nCharter: " + chart.creator + "\n");
								bw.close();
								Zip.zip(temp.getAbsolutePath(), activity.getContentResolver().openOutputStream(DocumentFile.fromTreeUri(activity, uri).createFile("application/octet-stream", map.get("title") + ".pez").getUri()), "UTF-8", false);
							} else FileUtil.copy(temp.getAbsolutePath() + File.separator + map.get("title") + ".json", activity.getContentResolver().openOutputStream(DocumentFile.fromTreeUri(activity, uri).createFile("application/octet-stream", map.get("title") + ".json").getUri()), new byte[1024 * 1024 * 4]);
							for (File f : temp.listFiles()) f.delete();
							temp.delete();
						} catch (Exception e) {
							catcher(e);
						}
					}
				}
			);
		}
		es.shutdown();
	}
	public void refresh() throws IOException, JSONException {
		listData.clear();
		for (File f : activity.getFilesDir().listFiles()) {
			boolean add = false;
			Map<String, Object> map = new HashMap<>();
			String name = f.getName();
			if (!name.contains(".")) return;
			String processedName = name.endsWith(".disabled") ? name.substring(0, name.lastIndexOf(".")) : name;
			map.put("title", processedName.substring(0, processedName.lastIndexOf(".")));
			map.put("extension", processedName.substring(processedName.lastIndexOf(".")));
			boolean pack = false;
			if (f.isDirectory()) {
				map.put("packable", true);
				for (File c : f.listFiles()) {
					if (c.getName().toLowerCase().endsWith(".mc")) {
						add = true;
						parseMalody(map, c);
					} else if (c.getName().toLowerCase().endsWith(".osu")) {
						add = true;
						parseOsu(map, c);
					}
					if (c.getName().equals("pack")) pack = true;
				}
			} else {
				if (f.getName().toLowerCase().endsWith(".mc")) {
					add = true;
					parseMalody(map, f);
				} else if (f.getName().toLowerCase().endsWith(".osu")) {
					add = true;
					parseOsu(map, f);
				}
				map.put("packable", false);
			}
			map.put("check", !name.endsWith(".disabled"));
			map.put("pack", pack);
			map.put("file", f);
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
		map.put("sound", mc.sound);
		map.put("offset", -mc.offset);
		map.put("description", getString(R.string.fragment_chart_list_description_title) + mc.title + "\n" + getString(R.string.fragment_chart_list_description_artist) + mc.artist + "\n" + getString(R.string.fragment_chart_list_description_creator) + mc.creator + "\n" + getString(R.string.fragment_chart_list_description_mode) + (mc.mode == null ? getString(R.string.unknown) : mc.mode) + "\n" + getString(R.string.fragment_chart_list_description_version) + mc.version);
		map.put("chart", mc);
	}
	private void parseOsu(Map<String, Object> map, File f) throws IOException {
		map.put("offset", 0);
		OsuChart oc = new OsuChart(f);
		map.put("background", oc.background);
		map.put("sound", oc.sound);
		map.put("description", getString(R.string.fragment_chart_list_description_title) + oc.title + "\n" + getString(R.string.fragment_chart_list_description_artist) + oc.artist + "\n" + getString(R.string.fragment_chart_list_description_creator) + oc.creator + "\n" + getString(R.string.fragment_chart_list_description_mode) + (oc.mode == null ? getString(R.string.unknown) : oc.mode) + "\n" + getString(R.string.fragment_chart_list_description_version) + oc.version);
		map.put("chart", oc);
	}
	private void catcher(Exception e) {
		for (File f : activity.getCacheDir().listFiles()) f.delete();
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		activity.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								((ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Malody2Phigros", sw.toString()));
							}
						}
					).setNegativeButton(R.string.crash_cancel, null).setCancelable(false).show();
				}
			}
		);
		try {
			sw.close();
		} catch (Exception ignore) {}
	}
}
