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
import java.io.PrintWriter;
import java.io.StringWriter;

public class ConvertionOptionsFragment extends Fragment {
	public boolean enableConst;
	public double defaultSpeed;
	public double defaultY;
	public boolean enableLuck;
	public int defaultSize;
	public int slideProcessMode;
	public boolean enableDrag;
	public boolean fakeDrag;
	public int dragAlpha;
	public Fraction dragInterval;
	public boolean optimizeSlide;
	private Activity activity;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View root = inflater.inflate(R.layout.fragment_convertion_options, container, false);
		activity = getActivity();
		((Switch) root.findViewById(R.id.fragment_convertion_options_enable_const)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
					if (defaultSpeed < 0) new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.fragment_convertion_options_warning).setMessage(R.string.fragment_convertion_options_negative_speed_message).setPositiveButton(R.string.fragment_convertion_options_negative_speed_yes, null).setNegativeButton(R.string.fragment_convertion_options_negative_speed_no, new DialogInterface.OnClickListener() {
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
		final EditText defaultYEditText = root.findViewById(R.id.fragment_convertion_options_default_y);
		((ImageButton) root.findViewById(R.id.fragment_convertion_options_default_y_confirm)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					defaultY = Double.parseDouble(defaultYEditText.getText().toString());
					if (defaultY < -450 || defaultY > 450) new AlertDialog.Builder(activity).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.fragment_convertion_options_warning).setMessage(R.string.fragment_convertion_options_invisible_line_message).setPositiveButton(R.string.fragment_convertion_options_invisible_line_yes, null).setNegativeButton(R.string.fragment_convertion_options_invisible_line_no, new DialogInterface.OnClickListener() {
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
		((Switch) root.findViewById(R.id.fragment_convertion_options_enable_luck)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					enableLuck = isChecked;
				}
			}
		);
		final SeekBar defaultSizeSeekBar = root.findViewById(R.id.fragment_convertion_options_default_size);
		final TextView defaultSizeValue = root.findViewById(R.id.fragment_convertion_options_default_size_value);
		defaultSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
				@Override
				public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
					defaultSize = progress;
					defaultSizeValue.setText(String.valueOf(defaultSize));
				}
				@Override
				public void onStartTrackingTouch(SeekBar seekBar) {}
				@Override
				public void onStopTrackingTouch(SeekBar seekBar) {}
			}
		);
		defaultSizeSeekBar.setMin(19);
		defaultSizeSeekBar.setMax(255);
		defaultSizeSeekBar.setProgress(53);
		defaultSizeValue.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final EditText et = new EditText(activity);
					et.setText(String.valueOf(defaultSize));
					et.setInputType(InputType.TYPE_CLASS_NUMBER);
					new AlertDialog.Builder(activity).setTitle(R.string.fragment_convertion_options_default_size).setView(et).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								defaultSize = Integer.parseInt(et.getText().toString());
								defaultSizeSeekBar.setProgress(defaultSize);
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
		((Switch) root.findViewById(R.id.fragment_convertion_options_enable_drag)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					enableDrag = isChecked;
				}
			}
		);
		((Switch) root.findViewById(R.id.fragment_convertion_options_fake_drag)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
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
		dragAlphaSeekBar.setProgress(255);
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
		((ImageButton) root.findViewById(R.id.fragment_convertion_options_drag_interval_confirm)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					dragInterval = new Fraction(Integer.parseInt(((EditText) root.findViewById(R.id.fragment_convertion_options_drag_interval_num)).getText().toString()), Integer.parseInt(((EditText) root.findViewById(R.id.fragment_convertion_options_drag_interval_up)).getText().toString()), Integer.parseInt(((EditText) root.findViewById(R.id.fragment_convertion_options_drag_interval_down)).getText().toString()));
				}
			}
		);
		return root;
	}
}
