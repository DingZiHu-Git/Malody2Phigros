package com.dzh.m2p;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ConvertionOptionsFragment extends Fragment {
	public boolean enableConst = false;
	public double defaultSpeed = 10;
	public boolean isCover = false;
	public double defaultY = -278.15;
	public boolean enableLuck = false;
	public int defaultWide = 50;
	public int slideProcessMode = 2;
	public boolean enableDrag = true;
	public boolean fakeDrag = true;
	public int dragAlpha = 128;
	public Fraction dragInterval = new Fraction(0, 1, 12);
	public boolean optimizeChart = false;
	private Activity activity;
	private File properties;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_convertion_options, container, false);
		activity = getActivity();
		Switch enableConstSwitch = root.findViewById(R.id.fragment_convertion_options_enable_const);
		enableConstSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					enableConst = isChecked;
				}
			}
		);
		final EditText defaultSpeedEditText = root.findViewById(R.id.fragment_convertion_options_default_speed);
		((ImageButton) root.findViewById(R.id.fragment_convertion_options_default_speed_confirm)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					defaultSpeed = Double.parseDouble(defaultSpeedEditText.getText().toString());
					if (defaultSpeed < 0) new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_dialog_alert).setTitle(android.R.string.dialog_alert_title).setMessage(R.string.fragment_convertion_options_negative_speed_message).setPositiveButton(R.string.fragment_convertion_options_negative_speed_yes, null).setNegativeButton(R.string.fragment_convertion_options_negative_speed_no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								defaultSpeed = -defaultSpeed;
								defaultSpeedEditText.setText(String.valueOf(defaultSpeed));
							}
						}
					).setCancelable(false).show();
				}
			}
		);
		Switch isCoverSwitch = root.findViewById(R.id.fragment_convertion_options_is_cover);
		isCoverSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					isCover = isChecked;
				}
			}
		);
		final EditText defaultYEditText = root.findViewById(R.id.fragment_convertion_options_default_y);
		((ImageButton) root.findViewById(R.id.fragment_convertion_options_default_y_confirm)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					defaultY = Double.parseDouble(defaultYEditText.getText().toString());
					if (defaultY < -450 || defaultY > 450) new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_dialog_alert).setTitle(android.R.string.dialog_alert_title).setMessage(R.string.fragment_convertion_options_invisible_line_message).setPositiveButton(R.string.fragment_convertion_options_invisible_line_yes, null).setNegativeButton(R.string.fragment_convertion_options_invisible_line_no, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								defaultY = -278.15;
								defaultYEditText.setText(String.valueOf(defaultY));
							}
						}
					).setCancelable(false).show();
				}
			}
		);
		Switch enableLuckSwitch = root.findViewById(R.id.fragment_convertion_options_enable_luck);
		enableLuckSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					enableLuck = isChecked;
				}
			}
		);
		final SeekBar defaultWideSeekBar = root.findViewById(R.id.fragment_convertion_options_default_wide);
		final TextView defaultWideValue = root.findViewById(R.id.fragment_convertion_options_default_wide_value);
		defaultWideSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					defaultWide = progress;
					defaultWideValue.setText(String.valueOf(defaultWide));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
		defaultWideSeekBar.setMin(0);
		defaultWideSeekBar.setMax(255);
		defaultWideValue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final EditText et = new EditText(activity);
					et.setText(String.valueOf(defaultWide));
					et.setInputType(InputType.TYPE_CLASS_NUMBER);
					new AlertDialog.Builder(activity).setTitle(R.string.fragment_convertion_options_default_wide).setView(et).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								defaultWideSeekBar.setProgress(Integer.parseInt(et.getText().toString()));
							}
						}
					).setNegativeButton(android.R.string.cancel, null).setCancelable(false).show();
				}
			}
		);
		Spinner slideProcessModeSpinner = root.findViewById(R.id.fragment_convertion_options_slide_process_mode);
		slideProcessModeSpinner.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, new String[]{ getString(R.string.fragment_convertion_options_slide_mode_straight_hold), getString(R.string.fragment_convertion_options_slide_mode_tap_with_drags), getString(R.string.fragment_convertion_options_slide_mode_moving_hold) }));
		slideProcessModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
				@Override
				public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
					slideProcessMode = position;
				}
				@Override
				public void onNothingSelected(AdapterView<?> parent) {
					slideProcessMode = 0;
				}
			}
		);
		slideProcessModeSpinner.setSelection(2);
		Switch enableDragSwitch = root.findViewById(R.id.fragment_convertion_options_enable_drag);
		enableDragSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					enableDrag = isChecked;
				}
			}
		);
		Switch fakeDragSwitch = root.findViewById(R.id.fragment_convertion_options_fake_drag);
		fakeDragSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					fakeDrag = isChecked;
				}
			}
		);
		final SeekBar dragAlphaSeekBar = root.findViewById(R.id.fragment_convertion_options_drag_alpha);
		final TextView dragAlphaValue = root.findViewById(R.id.fragment_convertion_options_drag_alpha_value);
		dragAlphaSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					dragAlpha = progress;
					dragAlphaValue.setText(String.valueOf(dragAlpha));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
		dragAlphaSeekBar.setMax(255);
		dragAlphaValue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final EditText et = new EditText(activity);
					et.setText(String.valueOf(dragAlpha));
					et.setInputType(InputType.TYPE_CLASS_NUMBER);
					new AlertDialog.Builder(activity).setTitle(R.string.fragment_convertion_options_drag_alpha).setView(et).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dragAlpha = Integer.parseInt(et.getText().toString());
								dragAlphaSeekBar.setProgress(dragAlpha);
							}
						}
					).setNegativeButton(android.R.string.cancel, null).setCancelable(false).show();
				}
			}
		);
		final EditText dragIntervalNum = root.findViewById(R.id.fragment_convertion_options_drag_interval_num);
		final EditText dragIntervalUp = root.findViewById(R.id.fragment_convertion_options_drag_interval_up);
		final EditText dragIntervalDown = root.findViewById(R.id.fragment_convertion_options_drag_interval_down);
		((ImageButton) root.findViewById(R.id.fragment_convertion_options_drag_interval_confirm)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dragInterval = new Fraction(Integer.parseInt(dragIntervalNum.getText().toString()), Integer.parseInt(dragIntervalUp.getText().toString()), Integer.parseInt(dragIntervalDown.getText().toString()));
				}
			}
		);
		try {
			properties = new File(activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "properties.json");
			if (properties.createNewFile()) {
				BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(properties), "UTF-8"));
				bw.write(new JSONObject().put("const", false).put("speed", 10d).put("cover", false).put("y", -278.15).put("luck", false).put("wide", 50).put("slide", 2).put("drag", true).put("fake", true).put("alpha", 128).put("interval", new JSONArray().put(0).put(1).put(12)).put("optimize", false).toString());
				bw.close();
			}
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(properties), "UTF-8"));
			JSONObject prop = new JSONObject(br.readLine());
			br.close();
			enableConstSwitch.setChecked(enableConst = prop.getBoolean("const"));
			defaultSpeedEditText.setText(String.valueOf(defaultSpeed = prop.getDouble("speed")));
			isCoverSwitch.setChecked(isCover = prop.getBoolean("cover"));
			defaultYEditText.setText(String.valueOf(defaultY = prop.getDouble("y")));
			enableLuckSwitch.setChecked(enableLuck = prop.getBoolean("luck"));
			defaultWideSeekBar.setProgress(defaultWide = prop.getInt("wide"));
			slideProcessModeSpinner.setSelection(slideProcessMode = prop.getInt("slide"));
			enableDragSwitch.setChecked(enableDrag = prop.getBoolean("drag"));
			fakeDragSwitch.setChecked(fakeDrag = prop.getBoolean("fake"));
			dragAlphaSeekBar.setProgress(dragAlpha = prop.getInt("alpha"));
			dragInterval = new Fraction(prop.getJSONArray("interval"));
			dragIntervalNum.setText(String.valueOf(dragInterval.num));
			dragIntervalUp.setText(String.valueOf(dragInterval.up));
			dragIntervalDown.setText(String.valueOf(dragInterval.down));
			optimizeChart = prop.getBoolean("optimize");
		} catch (Exception e) {
			catcher(e);
		}
		return root;
	}
	public void saveProperties() throws IOException, JSONException {
		properties = new File(activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "properties.json");
		properties.createNewFile();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(properties), "UTF-8"));
		bw.write(new JSONObject().put("const", enableConst).put("speed", defaultSpeed).put("cover", isCover).put("y", defaultY).put("luck", enableLuck).put("wide", defaultWide).put("slide", slideProcessMode).put("drag", enableDrag).put("fake", fakeDrag).put("alpha", dragAlpha).put("interval", dragInterval.toJSONArray()).put("optimize", optimizeChart).toString());
		bw.close();
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
