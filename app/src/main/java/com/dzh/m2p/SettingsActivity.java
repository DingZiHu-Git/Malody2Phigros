package com.dzh.m2p;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.json.JSONObject;

public class SettingsActivity extends AppCompatActivity {
	public Uri initialUri;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_settings_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(getExternalFilesDir(null).getAbsolutePath() + File.separator + "settings.json"), "UTF-8"));
			initialUri = Uri.parse(new JSONObject(br.readLine()).getString("initialUri"));
			br.close();
			
		} catch (Exception e) {
			catcher(e);
		}
	}
	@Override
	public void onBackPressed() {
		setResult(RESULT_OK, new Intent().setData(initialUri));
		super.onBackPressed();
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void catcher(Exception e) {
		for (File f : getCacheDir().listFiles()) f.delete();
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(SettingsActivity.this).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Malody2Phigros", sw.toString()));
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
