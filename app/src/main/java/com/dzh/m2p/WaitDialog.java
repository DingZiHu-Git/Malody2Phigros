package com.dzh.m2p;

import android.annotation.NonNull;
import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WaitDialog extends Dialog {
	public WaitDialog(@NonNull Context c) {
		super(c, false, null);
		LinearLayout ll = new LinearLayout(c);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ll.setGravity(Gravity.CENTER);
		ProgressBar pb = new ProgressBar(c);
		pb.setPadding(64, 64, 64, 64);
		ll.addView(pb);
		TextView tv = new TextView(c);
		tv.setPadding(0, 0, 256, 0);
		tv.setText(R.string.please_wait);
		ll.addView(tv);
		setContentView(ll);
		setCancelable(false);
	}
}
