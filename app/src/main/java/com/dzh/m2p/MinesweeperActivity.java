package com.dzh.m2p;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class MinesweeperActivity extends AppCompatActivity {
	private LinearLayout root;
	private MineCell[][] cells;
	private int[][] surroundingMines;
	private int mines;
	private boolean isFirstClick;
	private long seed;
	private int minesLeft;
	private final AtomicInteger timeUsed = new AtomicInteger(0);
	private TextView status;
	private final AtomicInteger pause = new AtomicInteger(1);
	private Thread lifecycle;
	private volatile boolean runLifecycle;
	private void setupGame(final int row, final int col, final int mines, long seed) {
		root.removeAllViews();
		final Random rand = new Random(seed);
		isFirstClick = true;
		cells = new MineCell[row][col];
		surroundingMines = new int[row][col];
		this.mines = mines;
		this.seed = seed;
		minesLeft = mines;
		timeUsed.set(0);
		status = findViewById(R.id.activity_minesweeper_status);
		status.setText(getString(R.string.activity_minesweeper_mines_left) + minesLeft + "    " + getString(R.string.activity_minesweeper_time_used) + timeUsed.get());
		runLifecycle = true;
		for (int i = 0; i < row; i++) {
			LinearLayout ll = new LinearLayout(this);
			ll.setOrientation(LinearLayout.HORIZONTAL);
			for (int j = 0; j < col; j++) {
				final MineCell mc = new MineCell(this, i, j);
				cells[i][j] = mc;
				mc.setLayoutParams(new ViewGroup.LayoutParams(128, 128));
				mc.setTextSize(16);
				mc.setBackgroundResource(R.drawable.cell_bg);
				mc.setAllCaps(false);
				mc.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							if (isFirstClick) {
								int minesPlaced = 0;
								boolean[][] exclude = new boolean[row][col];
								for (int r = Math.max(0, mc.row - 1); r <= Math.min(row - 1, mc.row + 1); r++) for (int c = Math.max(0, mc.col - 1); c <= Math.min(col - 1, mc.col + 1); c++) exclude[r][c] = true;
								while (minesPlaced < mines) {
									int r = rand.nextInt(row);
									int c = rand.nextInt(col);
									if (!exclude[r][c] && !cells[r][c].isMine) {
										cells[r][c].isMine = true;
										minesPlaced++;
									}
								}
								for (int r = 0; r < row; r++) {
									for (int c = 0; c < col; c++) {
										if (!cells[r][c].isMine) {
											int count = 0;
											for (int dr = -1; dr <= 1; dr++) {
												for (int dc = -1; dc <= 1; dc++) {
													if (dr == 0 && dc == 0) continue;
													int nr = r + dr, nc = c + dc;
													if (nr >= 0 && nr < row && nc >= 0 && nc < col && cells[nr][nc].isMine) count++;
												}
											}
											surroundingMines[r][c] = count;
										}
									}
								}
								isFirstClick = false;
								pause.set(0);
							}
							revealCell((MineCell) v);
						}
					}
				);
				mc.setOnLongClickListener(new View.OnLongClickListener() {
						@Override
						public boolean onLongClick(View v) {
							MineCell cell = (MineCell) v;
							if (!cell.isRevealed) {
								cell.isFlagged = !cell.isFlagged;
								if (cell.isFlagged) {
									cell.setText("🚩");
									minesLeft--;
								} else {
									cell.setText("");
									minesLeft++;
								}
								status.setText(getString(R.string.activity_minesweeper_mines_left) + minesLeft + "    " + getString(R.string.activity_minesweeper_time_used) + timeUsed.get());
							}
							checkWin();
							return true;
						}
					}
				);
				ll.addView(mc);
			}
			root.addView(ll);
		}
		lifecycle = new Thread("MinesweeperLifecycle") {
			@Override
			public void run() {
				try {
					int temp = 0;
					while (runLifecycle) {
						if (pause.get() > 0) continue;
						sleep(1);
						temp++;
						if (temp > 999) {
							timeUsed.incrementAndGet();
							temp = 0;
							runOnUiThread(new Runnable() {
									@Override
									public void run() {
										status.setText(getString(R.string.activity_minesweeper_mines_left) + minesLeft + "    " + getString(R.string.activity_minesweeper_time_used) + timeUsed.get());
									}
								}
							);
						}
					}
				} catch (Exception e) {
					catcher(e);
				}
			}
		};
		lifecycle.start();
	}
	private void revealCell(MineCell cell) {
		if (cell.isRevealed || cell.isFlagged) return;
		cell.isRevealed = true;
		cell.setBackgroundResource(R.drawable.revealed_cell_bg);
		if (cell.isMine) {
			pause.set(1);
			cell.setText("💣");
			cell.setBackgroundResource(R.drawable.wrong_cell_bg);
			for (MineCell[] mcs : cells) for (MineCell mc : mcs) {
				if (mc.isMine) mc.setText(mc.isFlagged ? "🚩" : "💣");
				else if (mc.isFlagged) mc.setBackgroundResource(R.drawable.wrong_cell_bg);
				mc.setEnabled(false);
			}
		} else if (surroundingMines[cell.row][cell.col] > 0) cell.setText(String.valueOf(surroundingMines[cell.row][cell.col]));
		else expandBlankArea(cell.row, cell.col);
		checkWin();
	}
	private void expandBlankArea(int r, int c) {
		for (int dr = -1; dr <= 1; dr++) {
			for (int dc = -1; dc <= 1; dc++) {
				int nr = r + dr, nc = c + dc;
				if (nr >= 0 && nr < cells.length && nc >= 0 && nc < cells[0].length) {
					MineCell neighbor = cells[nr][nc];
					if (neighbor.isFlagged) {
						neighbor.isFlagged = false;
						neighbor.setText("");
						minesLeft++;
						status.setText(getString(R.string.activity_minesweeper_mines_left) + minesLeft + "    " + getString(R.string.activity_minesweeper_time_used) + timeUsed.get());
					}
					if (!neighbor.isRevealed) revealCell(neighbor);
				}
			}
		}
	}
	private void checkWin() {
		int unrevealedSafeCells = 0;
		for (int r = 0; r < cells.length; r++) {
			for (int c = 0; c < cells[0].length; c++) {
				MineCell cell = cells[r][c];
				if (!cell.isRevealed && !cell.isMine) unrevealedSafeCells++;
			}
		}
		if (unrevealedSafeCells == 0) {
			pause.set(1);
			status.setText(getString(R.string.activity_minesweeper_mines_left) + 0 + "    " + getString(R.string.activity_minesweeper_time_used) + timeUsed.get());
			for (int r = 0; r < cells.length; r++) {
				for (int c = 0; c < cells[0].length; c++) {
					MineCell cell = cells[r][c];
					cell.setEnabled(false);
					if (cell.isMine && !cell.isFlagged) cell.setText("💣");
				}
			}
		}
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_minesweeper);
		setSupportActionBar((Toolbar) findViewById(R.id.activity_minesweeper_toolbar));
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		root = findViewById(R.id.activity_minesweeper_root);
		setupGame(8, 8, 10, new Random().nextLong());
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		new MenuInflater(this).inflate(R.menu.minesweeper, menu);
		for (int i = 0; i < menu.size(); i++) {
			Drawable icon = menu.getItem(i).getIcon();
			if (icon != null) {
				icon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);
				menu.getItem(i).setIcon(icon);
			}
		}
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				onBackPressed();
				return true;
			case R.id.activity_minesweeper_settings:
				pause.set(pause.get() + 1);
				LinearLayout ll = new LinearLayout(this);
				ll.setOrientation(LinearLayout.VERTICAL);
				LinearLayout ll1 = new LinearLayout(this);
				ll1.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout ll2 = new LinearLayout(this);
				ll2.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout ll3 = new LinearLayout(this);
				ll3.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout ll4 = new LinearLayout(this);
				ll4.setOrientation(LinearLayout.HORIZONTAL);
				TextView tv1 = new TextView(this);
				tv1.setText(R.string.activity_minesweeper_settings_rows);
				tv1.setTextAppearance(android.R.style.TextAppearance_Large);
				tv1.setTextSize(16);
				TextView tv2 = new TextView(this);
				tv2.setText(R.string.activity_minesweeper_settings_cols);
				tv2.setTextAppearance(android.R.style.TextAppearance_Large);
				tv2.setTextSize(16);
				TextView tv3 = new TextView(this);
				tv3.setText(R.string.activity_minesweeper_settings_mines);
				tv3.setTextAppearance(android.R.style.TextAppearance_Large);
				tv3.setTextSize(16);
				TextView tv4 = new TextView(this);
				tv4.setText(R.string.activity_minesweeper_settings_seed);
				tv4.setTextAppearance(android.R.style.TextAppearance_Large);
				tv4.setTextSize(16);
				final EditText et1 = new EditText(this);
				et1.setText(String.valueOf(cells.length));
				et1.setInputType(InputType.TYPE_CLASS_NUMBER);
				et1.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				final EditText et2 = new EditText(this);
				et2.setText(String.valueOf(cells[0].length));
				et2.setInputType(InputType.TYPE_CLASS_NUMBER);
				et2.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				final EditText et3 = new EditText(this);
				et3.setText(String.valueOf(mines));
				et3.setInputType(InputType.TYPE_CLASS_NUMBER);
				et3.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				final EditText et4 = new EditText(this);
				et4.setText(String.valueOf(seed));
				et4.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);
				et4.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
				ll1.addView(tv1);
				ll1.addView(et1);
				ll2.addView(tv2);
				ll2.addView(et2);
				ll3.addView(tv3);
				ll3.addView(et3);
				ll4.addView(tv4);
				ll4.addView(et4);
				ll.addView(ll1);
				ll.addView(ll2);
				ll.addView(ll3);
				ll.addView(ll4);
				new AlertDialog.Builder(this).setTitle(R.string.activity_minesweeper_settings).setView(ll).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							int rows;
							int cols;
							int mines;
							long seed;
							try {
								rows = Math.max(8, Integer.parseInt(et1.getText().toString()));
							} catch (NumberFormatException e) {
								rows = 8;
								e.printStackTrace();
							}
							try {
								cols = Math.max(8, Integer.parseInt(et2.getText().toString()));
							} catch (NumberFormatException e) {
								cols = 8;
								e.printStackTrace();
							}
							try {
								mines = Math.min(rows * cols - 9, Integer.parseInt(et3.getText().toString()));
								mines = Math.max(10, mines);
							} catch (NumberFormatException e) {
								mines = 10;
								e.printStackTrace();
							}
							try {
								seed = Long.parseLong(et4.getText().toString());
							} catch (NumberFormatException e) {
								seed = new Random().nextLong();
								e.printStackTrace();
							}
							runLifecycle = false;
							setupGame(rows, cols, mines, seed);
						}
					}
				).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							pause.set(pause.get() - 1);
						}
					}
				).setCancelable(false).show();
				return true;
		}
		return super.onOptionsItemSelected(item);
	}
	private void catcher(Exception e) {
		final StringWriter sw = new StringWriter();
		e.printStackTrace(new PrintWriter(sw, true));
		AlertDialog.Builder adb = new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_delete).setTitle(R.string.crash_title).setMessage(sw.toString()).setPositiveButton(R.string.crash_ok, new DialogInterface.OnClickListener() {
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
		try {
			sw.close();
		} catch (Exception ignore) {}
	}
}
