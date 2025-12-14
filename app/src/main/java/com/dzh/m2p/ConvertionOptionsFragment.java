package com.dzh.m2p;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextWatcher;
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
import android.text.Editable;

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
	public int dragInterval = 12;
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
		EditText defaultSpeedEditText = root.findViewById(R.id.fragment_convertion_options_default_speed);
		defaultSpeedEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					String str = s.toString();
					if (!(str.isEmpty() || str.equals("-") || str.equals("+"))) defaultSpeed = Double.parseDouble(s.toString());
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
		EditText defaultYEditText = root.findViewById(R.id.fragment_convertion_options_default_y);
		defaultYEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					String str = s.toString();
					if (!(str.isEmpty() || str.equals("-") || str.equals("+"))) defaultY = Double.parseDouble(s.toString());
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
					defaultWideValue.setText(String.valueOf(defaultWide = progress));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
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
								String s = et.getText().toString();
								if (!s.isEmpty()) defaultWideSeekBar.setProgress(Integer.parseInt(s));
								defaultWide = defaultWideSeekBar.getProgress();
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
					dragAlphaValue.setText(String.valueOf(dragAlpha = progress));
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
								String s = et.getText().toString();
								if (!s.isEmpty()) dragAlphaSeekBar.setProgress(Integer.parseInt(s));
								dragAlpha = dragAlphaSeekBar.getProgress();
							}
						}
					).setNegativeButton(android.R.string.cancel, null).setCancelable(false).show();
				}
			}
		);
		final EditText dragIntervalEditText = root.findViewById(R.id.fragment_convertion_options_drag_interval);
		dragIntervalEditText.addTextChangedListener(new TextWatcher() {
				@Override
				public void afterTextChanged(Editable s) {
					String str = s.toString();
					if (!str.isEmpty()) dragInterval = Math.max(1, Integer.parseInt(str));
				}
				@Override
				public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
				@Override
				public void onTextChanged(CharSequence s, int start, int before, int count) {}
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
			dragInterval = prop.has("ver") && prop.getLong("ver") > 3 ? prop.getInt("interval") : prop.getJSONArray("interval").getInt(2);
			dragIntervalEditText.setText(String.valueOf(dragInterval));
			optimizeChart = prop.getBoolean("optimize");
		} catch (Exception e) {
			AndroidJSObject.catcher(activity, e);
		}
		return root;
	}
	public void saveProperties() throws IOException, JSONException {
		properties = new File(activity.getExternalFilesDir(null).getAbsolutePath() + File.separator + "properties.json");
		properties.createNewFile();
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(properties), "UTF-8"));
		bw.write(new JSONObject().put("ver", BuildConfig.VERSION_CODE).put("const", enableConst).put("speed", defaultSpeed).put("cover", isCover).put("y", defaultY).put("luck", enableLuck).put("wide", defaultWide).put("slide", slideProcessMode).put("drag", enableDrag).put("fake", fakeDrag).put("alpha", dragAlpha).put("interval", dragInterval).put("optimize", optimizeChart).toString());
		bw.close();
	}
}
