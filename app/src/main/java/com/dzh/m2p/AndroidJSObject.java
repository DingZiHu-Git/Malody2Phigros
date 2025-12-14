package com.dzh.m2p;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.webkit.JavascriptInterface;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;

public class AndroidJSObject {
	private Context c;
	public AndroidJSObject(Context c) {
		this.c = c;
	}
	@JavascriptInterface
	public String getBoard() {
		return Build.BOARD;
	}
	@JavascriptInterface
	public String getBootloader() {
		return Build.BOOTLOADER;
	}
	@JavascriptInterface
	public String getBrand() {
		return Build.BRAND;
	}
	@JavascriptInterface
	public String getDevice() {
		return Build.DEVICE;
	}
	@JavascriptInterface
	public String getDisplay() {
		return Build.DISPLAY;
	}
	@JavascriptInterface
	public String getFingerprint() {
		return Build.FINGERPRINT;
	}
	@JavascriptInterface
	public String getHardware() {
		return Build.HARDWARE;
	}
	@JavascriptInterface
	public String getHost() {
		return Build.HOST;
	}
	@JavascriptInterface
	public String getId() {
		return Build.ID;
	}
	@JavascriptInterface
	public String getManufacturer() {
		return Build.MANUFACTURER;
	}
	@JavascriptInterface
	public String getModel() {
		return Build.MODEL;
	}
	@JavascriptInterface
	public String getProduct() {
		return Build.PRODUCT;
	}
	@JavascriptInterface
	public int getSdkInt() {
		return Build.VERSION.SDK_INT;
	}
	@JavascriptInterface
	public String getUser() {
		return Build.USER;
	}
	@JavascriptInterface
	public void showToast(String text, int length) {
		Toast.makeText(c, text, length).show();
	}
	public static void catcher(final Activity a, Exception e) {
		for (File f : a.getCacheDir().listFiles()) f.delete();
		final StringWriter sw = new StringWriter().append("VERSION: " + BuildConfig.VERSION_NAME + " (" + BuildConfig.VERSION_CODE + ")\nSDK_INT: " + Build.VERSION.SDK_INT + "\nBRAND: " + Build.BRAND + "\n");
		e.printStackTrace(new PrintWriter(sw, true));
		a.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					new AlertDialog.Builder(a).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								((ClipboardManager) a.getSystemService(a.CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Malody2Phigros", sw.toString()));
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
