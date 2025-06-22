package com.dzh.m2p;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileSelectorActivity extends AppCompatActivity {
	public List<Map<String, Object>> listData;
	public File dir;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_selector);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_file_selector_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		listData = new ArrayList<>();
		dir = new File(getIntent().getStringExtra("path"));
		final SimpleAdapter listAdapter = new SimpleAdapter(this, listData, R.layout.file_selector_item, new String[]{ "icon", "name" }, new int[]{ R.id.file_selector_item_icon, R.id.file_selector_item_name });
		final TextView path = findViewById(R.id.activity_file_selector_path);
		path.setText(dir.getAbsolutePath());
		path.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final EditText et = new EditText(FileSelectorActivity.this);
					et.setText(dir.getAbsolutePath());
					et.selectAll();
					new AlertDialog.Builder(FileSelectorActivity.this).setIcon(R.drawable.ic_form_textbox).setTitle(R.string.activity_file_selector_jump_to).setView(et).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								File temp;
								if (et.getText().toString().startsWith("/storage/emulated/0") && (temp = new File(et.getText().toString())).exists()) {
									dir = temp;
									refreshList();
									listAdapter.notifyDataSetChanged();
									path.setText(dir.getAbsolutePath());
								} else Toast.makeText(FileSelectorActivity.this, R.string.activity_file_selector_the_directory_does_not_exist_or_is_inaccessible, Toast.LENGTH_SHORT).show();
							}
						}
					).setNegativeButton(android.R.string.cancel, null).show();
				}
			}
		);
		ListView list = findViewById(R.id.activity_file_selector_list);
		list.setAdapter(listAdapter);
		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
					Map<String, Object> m = listData.get(position);
					if (((int) m.get("type")) == 0) {
						dir = new File(dir.getAbsolutePath() + File.separator + m.get("name"));
						refreshList();
						listAdapter.notifyDataSetChanged();
						path.setText(dir.getAbsolutePath());
					} else {
						setResult(RESULT_OK, new Intent().setData(DocumentFile.fromFile(new File(dir.getAbsolutePath() + File.separator + listData.get(position).get("name"))).getUri()));
						finish();
					}
				}
			}
		);
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
					return true;
				}
			}
		);
		((ImageButton) findViewById(R.id.activity_file_selector_go_up)).setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!dir.getAbsolutePath().equals("/storage/emulated/0")) dir = dir.getParentFile();
					refreshList();
					listAdapter.notifyDataSetChanged();
					path.setText(dir.getAbsolutePath());
				}
			}
		);
		refreshList();
		listAdapter.notifyDataSetChanged();
	}
	private void refreshList() {
		listData.clear();
		ArrayList<File> list = new ArrayList<File>(Arrays.asList(dir.listFiles()));
		list.sort(new Comparator<File>() {
				@Override
				public int compare(File o1, File o2) {
					if (o1.isDirectory() && o2.isFile()) return -1;
					if (o2.isDirectory() && o1.isFile()) return 1;
					return o1.getName().compareToIgnoreCase(o2.getName());
				}
			}
		);
		for (File f : list) {
			Map<String, Object> m = new HashMap<>();
			m.put("check", false);
			m.put("name", f.getName());
			if (f.isDirectory()) {
				m.put("type", 0);
				m.put("icon", f.list().length == 0 ? R.drawable.ic_folder_open : R.drawable.ic_folder);
			} else {
				m.put("type", 1);
				m.put("icon", R.drawable.ic_file);
			}
			listData.add(m);
		}
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
}
