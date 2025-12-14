package com.dzh.m2p;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
	private WaitDialog wait;
	private ChartListFragment clf;
	private ConvertionOptionsFragment cof;
	private int count;
	private File[] plugins;
	private ValueCallback<Uri[]> jsFilePath;
	private AndroidJSObject androidJSObject;
	private void init() {
		File settings = new File(getExternalFilesDir(null).getAbsolutePath() + File.separator + "settings.json");
		if (settings.exists()) settings.delete();
		plugins = getExternalFilesDir("plugins").listFiles();
		wait = new WaitDialog(this);
		androidJSObject = new AndroidJSObject(getApplicationContext());
		if (new SimpleDateFormat("Md").format(System.currentTimeMillis()).equals("41")) new AlertDialog.Builder(this).setTitle(R.string.april_fools_dialog_title).setMessage(R.string.april_fools_dialog_message).setPositiveButton(R.string.april_fools_dialog_ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					startActivity(new Intent(MainActivity.this, RickActivity.class));
				}
			}
		).show();
		final Intent data = getIntent();
		if (data != null && data.getAction() != null && data.getAction().equals(Intent.ACTION_VIEW) && data.getData() != null) {
			new Thread(new Runnable() {
					@Override
					public void run() {
						while (!clf.loaded) {}
						load(data.getData());
						wait.cancel();
					}
				}
			, "LoadFromOtherApp").start();
			wait.show();
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_main_toolbar));
		try {
			clf = new ChartListFragment();
			cof = new ConvertionOptionsFragment();
			final FaqFragment faq = new FaqFragment();
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
							case 2:
								return faq;
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
							case 2:
								return getString(R.string.activity_main_tab_faq);
							default: return getString(R.string.activity_main_tab_chart_list);
						}
					}
					@Override
					public int getCount() {
						return 3;
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
		} catch (Exception e) {
			AndroidJSObject.catcher(this, e);
		}
	}
	private void load(final Uri data) {
		try {
			String name = DocumentFile.fromSingleUri(MainActivity.this, data).getName();
			if (name.toLowerCase().endsWith(".mc") || name.toLowerCase().endsWith(".osu")) FileUtil.copy(getContentResolver().openInputStream(data), getFilesDir().getAbsolutePath() + File.separator + name, new byte[1024 * 1024 * 4]);
			else if (name.toLowerCase().endsWith(".mcz") || name.toLowerCase().endsWith(".osz") || name.toLowerCase().endsWith(".zip")) {
				List<String> targets = new ArrayList<>();
				ZipInputStream zis = new ZipInputStream(getContentResolver().openInputStream(data));
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
							String json = "";
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(f)));
							String line;
							while ((line = br.readLine()) != null) json += line;
							br.close();
							BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(f), "UTF-8"));
							bw.write(new JSONObject(json).toString());
							bw.close();
							MalodyChart mc = new MalodyChart(f);
							if (mc.background != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + mc.background, temp.getAbsolutePath() + File.separator + mc.background, new byte[1024 * 1024 * 4]);
							if (mc.video != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + mc.video, temp.getAbsolutePath() + File.separator + mc.video, new byte[1024 * 1024 * 4]);
							FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + mc.sound, temp.getAbsolutePath() + File.separator + mc.sound, new byte[1024 * 1024 * 4]);
						} else if (str.toLowerCase().endsWith(".osu")) {
							temp.mkdirs();
							File file = new File(getCacheDir().getAbsolutePath() + File.separator + str);
							File f = new File(temp.getAbsolutePath() + File.separator + str);
							file.renameTo(f);
							OsuChart oc = new OsuChart(f);
							if (oc.background != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + oc.background, temp.getAbsolutePath() + File.separator + oc.background, new byte[1024 * 1024 * 4]);
							if (oc.video != null) FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + oc.video, temp.getAbsolutePath() + File.separator + oc.video, new byte[1024 * 1024 * 4]);
							FileUtil.copy(getCacheDir().getAbsolutePath() + File.separator + oc.sound, temp.getAbsolutePath() + File.separator + oc.sound, new byte[1024 * 1024 * 4]);
						}
						new File(temp.getAbsolutePath() + File.separator + "pack").createNewFile();
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
			} else if (name.toLowerCase().endsWith(".js")) {
				FileUtil.copy(getContentResolver().openInputStream(data), getExternalFilesDir("plugins").getAbsolutePath() + File.separator + name, new byte[1024 * 1024 * 4]);
				runOnUiThread(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(MainActivity.this).setTitle(android.R.string.dialog_alert_title).setMessage(R.string.activity_main_plugin_modified_successfully).setPositiveButton(android.R.string.ok, null).setCancelable(false).show();
						}
					}
				);
			} else runOnUiThread(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(MainActivity.this).setTitle(R.string.activity_main_load_chart_failed_title).setMessage(R.string.activity_main_load_chart_failed_unsupported_file_type).setPositiveButton(android.R.string.ok, null).show();
						}
					}
				);
			clf.refresh();
		} catch (Exception e) {
			AndroidJSObject.catcher(this, e);
		}
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		try {
			switch (requestCode) {
				case 1:
				case 3:
					if (data != null) {
						new Thread(new Runnable() {
								@Override
								public void run() {
									load(data.getData());
									wait.cancel();
								}
							}
							, "Load").start();
						wait.show();
					}
					break;
				case 2:
					if (data != null) getContentResolver().takePersistableUriPermission(data.getData(), Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					break;
				case 4:
					if (jsFilePath == null) return;
					if (data == null) jsFilePath.onReceiveValue(null);
					else {
						Uri[] result = null;
						String dataString = data.getDataString();
						ClipData clipData = data.getClipData();
						if (clipData != null) {
							result = new Uri[clipData.getItemCount()];
							for (int i = 0; i < result.length; i++) result[i] = clipData.getItemAt(i).getUri();
						} else if (dataString != null) result = new Uri[]{ Uri.parse(dataString) };
						jsFilePath.onReceiveValue(result);
					}
					jsFilePath = null;
					break;
			}
		} catch (Exception e) {
			AndroidJSObject.catcher(this, e);
		}
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		try {
			switch (item.getItemId()) {
				case R.id.activity_main_load_chart:
					startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), 1);
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
									AndroidJSObject.catcher(MainActivity.this, e);
								}
							}
						}
						, "ClearLoadedChart").start();
					wait.show();
					return true;
				case R.id.activity_main_start_convert:
					final List<UriPermission> permissions = getContentResolver().getPersistedUriPermissions();
					for (int i = permissions.size() - 1; i > -1; i--) if (!DocumentFile.fromTreeUri(MainActivity.this, permissions.get(i).getUri()).exists()) permissions.remove(i);
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
													AndroidJSObject.catcher(MainActivity.this, e);
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
					cof.saveProperties();
					return true;
				case R.id.activity_main_plugin_manager:
					final AtomicBoolean avaliable = new AtomicBoolean(false);
					String[] names = new String[Math.max(1, plugins.length)];
					names[0] = getString(R.string.activity_main_plugin_manager_empty);
					for (int i = 0; i < plugins.length; i++) {
						names[i] = plugins[i].getName().substring(0, plugins[i].getName().lastIndexOf("."));
						avaliable.set(true);
					}
					new AlertDialog.Builder(MainActivity.this).setTitle(R.string.activity_main_plugin_manager_message).setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names), new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (avaliable.get()) {
									plugins[which].delete();
									new AlertDialog.Builder(MainActivity.this).setTitle(android.R.string.dialog_alert_title).setMessage(R.string.activity_main_plugin_modified_successfully).setPositiveButton(android.R.string.ok, null).setCancelable(false).show();
								}
							}
						}
					).setPositiveButton(android.R.string.ok, null).setNeutralButton(R.string.activity_main_plugin_manager_import_plugin, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								startActivityForResult(new Intent(Intent.ACTION_GET_CONTENT).addCategory(Intent.CATEGORY_OPENABLE).setType("*/*"), 3);
							}
						}
					).setCancelable(false).show();
					return true;
			}
		} catch (Exception e) {
			AndroidJSObject.catcher(this, e);
		} finally {
			return super.onOptionsItemSelected(item);
		}
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
		MenuItem mi = menu.getItem(menu.size() - 1);
		SubMenu sm = mi.getSubMenu();
		for (int i = 0; i < plugins.length; i++) {
			File f = plugins[i];
			if (f.getName().toLowerCase().endsWith(".js")) sm.add(f.getName().substring(0, f.getName().lastIndexOf("."))).setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
					@Override
					public boolean onMenuItemClick(MenuItem item) {
						try {
							final File plugin = plugins[item.getOrder()];
							final StringBuilder js = new StringBuilder();
							BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(plugin)));
							String line;
							while ((line = br.readLine()) != null) js.append(line).append("\n");
							br.close();
							final WebView wv = new WebView(MainActivity.this);
							new AlertDialog.Builder(MainActivity.this).setTitle(plugin.getName().substring(0, plugin.getName().lastIndexOf("."))).setView(wv).show();
							WebSettings ws = wv.getSettings();
							ws.setJavaScriptEnabled(true);
							ws.setAllowContentAccess(true);
							ws.setAllowFileAccess(true);
							ws.setDomStorageEnabled(true);
							ws.setMixedContentMode(WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE);
							wv.setWebViewClient(new WebViewClient() {
									@Override
									public void onPageFinished(WebView view, String url) {
										wv.evaluateJavascript(js.toString(), null);
									}
								}
							);
							wv.setWebChromeClient(new WebChromeClient() {
									@Override
									public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
										new AlertDialog.Builder(MainActivity.this).setTitle(plugin.getName().substring(0, plugin.getName().lastIndexOf("."))).setMessage(message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													result.confirm();
												}
											}
										).show();
										return true;
									}
									@Override
									public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
										try {
											if (jsFilePath != null) jsFilePath.onReceiveValue(null);
											jsFilePath = filePathCallback;
											startActivityForResult(fileChooserParams.createIntent(), 4);
											return true;
										} catch (ActivityNotFoundException e) {
											filePathCallback.onReceiveValue(null);
											return false;
										}
									}
								}
							);
							wv.addJavascriptInterface(androidJSObject, "Android");
							wv.loadUrl("about:blank");
							return true;
						} catch (Exception e) {
							AndroidJSObject.catcher(MainActivity.this, e);
							return false;
						}
					}
				}
			);
		}
		if (BuildConfig.DEBUG) menu.add("DEBUG").setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					String str = "";
					for (File f : getFilesDir().listFiles()) {
						str += f.getName() + "\n";
						if (f.isDirectory()) for (File c : f.listFiles()) str += "    " + c.getName() + "\n";
					}
					new AlertDialog.Builder(MainActivity.this).setMessage(str).show();
					return true;
				}
			}
		);
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
