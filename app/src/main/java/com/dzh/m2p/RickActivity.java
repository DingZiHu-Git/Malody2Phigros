package com.dzh.m2p;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class RickActivity extends AppCompatActivity {
	private VideoView vv;
	private int playTimes;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_rick);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_rick_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		vv = findViewById(R.id.activity_rick_video_view);
		vv.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					mp.start();
					if (++playTimes == 2) startActivity(new Intent(RickActivity.this, MinesweeperActivity.class));
				}
			}
		);
		vv.setOnLongClickListener(new View.OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					startActivity(new Intent(RickActivity.this, MinesweeperActivity.class));
					return true;
				}
			}
		);
		vv.setVideoURI(Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.rick));
		vv.start();
	}
	@Override
	protected void onResume() {
		super.onResume();
		vv.start();
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
