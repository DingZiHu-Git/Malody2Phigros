package com.dzh.m2p;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.io.IOException;
import org.json.JSONException;

public class MainActivity extends AppCompatActivity {
	private WaitDialog wait;
	private ChartListFragment clf;
	private ConvertionOptionsFragment cof;
	private int count;
	private void init() {
		wait = new WaitDialog(this);
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) new AlertDialog.Builder(this).setTitle(R.string.activity_main_permissions_request_title).setMessage(R.string.activity_main_permissions_request_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					ActivityCompat.requestPermissions(MainActivity.this, new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
				}
			}
		).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					finish();
				}
			}
		).setCancelable(false).show();
		else if (new SimpleDateFormat("Md").format(System.currentTimeMillis()).equals("41")) new AlertDialog.Builder(this).setTitle(R.string.april_fools_dialog_title).setMessage(R.string.april_fools_dialog_message).setPositiveButton(R.string.april_fools_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(MainActivity.this, RickActivity.class));
				}
			}
		).show();
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));
		clf = new ChartListFragment();
		cof = new ConvertionOptionsFragment();
		count = 0;
		final ViewPager viewPager = findViewById(R.id.activity_main_view_pager);
		viewPager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {
				@Override
				public Fragment getItem(int p) {
					switch (p) {
						case 0:
							return clf;
						case 1:
							return cof;
						default: return clf;
					}
				}
				@Override
				public CharSequence getPageTitle(int position) {
					switch (position) {
						case 0:
							return getString(R.string.activity_main_tab_chart_list);
						case 1:
							return getString(R.string.activity_main_tab_convertion_options);
						default: return getString(R.string.activity_main_tab_chart_list);
					}
				}
				@Override
				public int getCount() {
					return 2;
				}
			}
		);
		TabLayout tabLayout = findViewById(R.id.activity_main_tab_layout);
		tabLayout.setupWithViewPager(viewPager);
		tabLayout.setOnTabSelectedListener(new TabLayout.BaseOnTabSelectedListener() {
				@Override
				public void onTabReselected(TabLayout.Tab t) {
					if (++count == 6) startActivity(new Intent(MainActivity.this, RickActivity.class));
				}
				@Override
				public void onTabSelected(TabLayout.Tab t) {
					count = 0;
				}
				@Override
				public void onTabUnselected(TabLayout.Tab t) {
					count = 0;
				}
			}
		);
		init();
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			switch (requestCode) {
				case 1:
					new Thread(new Runnable() {
							@Override
							public void run() {
								try {
								String name = DocumentFile.fromSingleUri(MainActivity.this, data.getData()).getName();
									if (name.toLowerCase().endsWith(".mc") || name.toLowerCase().endsWith(".osu")) FileUtil.copy(getContentResolver().openInputStream(data.getData()), getFilesDir().getAbsolutePath() + File.separator + name, new byte[1024 * 1024 * 4]);
									else if (name.toLowerCase().endsWith(".mcz") || name.toLowerCase().endsWith(".osz") || name.toLowerCase().endsWith(".zip")) {
										List<String> targets = new ArrayList<>();
										ZipInputStream zis = new ZipInputStream(getContentResolver().openInputStream(data.getData()));
										ZipEntry ze;
										while ((ze = zis.getNextEntry()) != null) {
											FileOutputStream fos = new FileOutputStream(getCacheDir().getAbsolutePath() + File.separator + ze.getName().substring(ze.getName().lastIndexOf("/") + 1));
											byte[] b = new byte[1024 * 1024 * 4];
											int len;
											while ((len = zis.read(b)) > -1) fos.write(b, 0, len);
											fos.close();
											if (ze.getName().toLowerCase().endsWith(".mc") || ze.getName().toLowerCase().endsWith(".osu")) targets.add(ze.getName().substring(ze.getName().lastIndexOf("/") + 1));
											zis.closeEntry();
										}
										zis.close();
										if (targets.size() > 0) {
											for (String str : targets) {
												File temp = new File(getFilesDir().getAbsolutePath() + File.separator + str);
												if (str.toLowerCase().endsWith(".mc")) {
													temp.mkdirs();
													File file = new File(getCacheDir().getAbsolutePath() + File.separator + str);
													File f = new File(temp.getAbsolutePath() + File.separator + str);
													file.renameTo(f);
													MalodyChart mc = new MalodyChart(f);
													if (mc.background != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + mc.background, temp.getAbsolutePath() + File.separator + mc.background, new byte[1024 * 1024 * 4]);
													FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + mc.sound, temp.getAbsolutePath() + File.separator + mc.sound, new byte[1024 * 1024 * 4]);
												} else if (str.toLowerCase().endsWith(".osu")) {
													temp.mkdirs();
													File file = new File(getCacheDir().getAbsolutePath() + File.separator + str);
													File f = new File(temp.getAbsolutePath() + File.separator + str);
													file.renameTo(f);
													OsuChart oc = new OsuChart(f);
													if (oc.background != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + oc.background, temp.getAbsolutePath() + File.separator + oc.background, new byte[1024 * 1024 * 4]);
													FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + oc.sound, temp.getAbsolutePath() + File.separator + oc.sound, new byte[1024 * 1024 * 4]);
												}
											}
										} else {
											runOnUiThread(new Runnable() {
													@Override
													public void run() {
														new AlertDialog.Builder(MainActivity.this).setTitle(R.string.activity_main_load_chart_failed_title).setMessage(R.string.activity_main_load_chart_failed_couldnt_find_chart_file).setPositiveButton(android.R.string.ok, null).show();
													}
												}
											);
										}
										for (File f : getCacheDir().listFiles()) f.delete();
									} else runOnUiThread(new Runnable() {
											@Override
											public void run() {
												new AlertDialog.Builder(MainActivity.this).setTitle(R.string.activity_main_load_chart_failed_title).setMessage(R.string.activity_main_load_chart_failed_unsupported_file_type).setPositiveButton(android.R.string.ok, null).show();
											}
										}
									);
									clf.refresh();
									wait.cancel();
								} catch (Exception e) {
									catcher(e);
								}
							}
						}
					, "LoadCharts").start();
					wait.show();
					break;
				case 2:
					try {
						getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					} catch (Exception e) {
						catcher(e);
					}
					break;
			}
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.activity_main_load_chart:
				startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), 1);
				return true;
			case R.id.activity_main_clear_loaded_chart:
				new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								for (File f : getFilesDir().listFiles()) {
									if (f.isDirectory()) for (File c : f.listFiles()) c.delete();
									f.delete();
								}
								clf.refresh();
								wait.cancel();
							} catch (Exception e) {
								catcher(e);
							}
						}
					}
				, "ClearLoadedChart").start();
				wait.show();
				return true;
			case R.id.activity_main_start_convert:
				final List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
				final String[] accessibleDirs = new String[permissions.size() + 1];
				for (int i = 0; i < permissions.size(); i++) accessibleDirs[i] = DocumentFile.fromTreeUri(MainActivity.this, permissions.get(i).getUri()).getName();
				accessibleDirs[accessibleDirs.length - 1] = getString(R.string.activity_main_select_folder);
				new AlertDialog.Builder(MainActivity.this).setTitle(R.string.activity_main_select_save_location).setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, accessibleDirs), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, final int which) {
							if (which == accessibleDirs.length - 1) startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE), 2);
							else {
								final ExecutorService es = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
								clf.convert(es, cof, permissions.get(which).getUri());
								new Thread(new Runnable() {
										@Override
										public void run() {
											try {
												es.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
												wait.cancel();
											} catch (Exception e) {
												catcher(e);
											}
										}
									}
								, "Convert").start();
								wait.show();
							}
						}
					}
				).show();
				return true;
			case R.id.activity_main_save_properties:
				try {
					cof.saveProperties();
				} catch (Exception e) {
					catcher(e);
				}
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.main, menu);
		for (int i = 0; i < menu.size(); i++) {
			Drawable icon = menu.getItem(i).getIcon();
			if (icon != null) {
				icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				menu.getItem(i).setIcon(icon);
			}
		}
		if (BuildConfig.DEBUG) {
			menu.add("DEBUG");
			menu.getItem(menu.size() - 1).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						String result = "";
						for (File f : getFilesDir().listFiles()) {
							result += f.getName() + "\n";
							if (f.isDirectory()) for (File c : f.listFiles()) result += "    " + c.getName() + "\n";
						}
						new AlertDialog.Builder(MainActivity.this).setMessage(result).show();
						return true;
					}
				}
			);
		}
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permission, int[] grantResult) {
		super.onRequestPermissionsResult(requestCode, permission, grantResult);
		init();
	}
	private void catcher(Exception e) {
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		runOnUiThread(new Runnable() {
				@Override
				public void run() {
					AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								((ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Malody2Phigros", sw.toString()));
								finish();
							}
						}
					).setNegativeButton(R.string.crash_cancel, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								finish();
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
