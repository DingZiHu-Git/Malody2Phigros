package com.dzh.m2p;

import android.content.Context;
import android.widget.Button;

public class MineCell extends Button {
	public int row, col;
	public boolean isMine, isRevealed, isFlagged;
	public MineCell(Context c) {
		super(c);
	}
	public MineCell(Context c, int row, int col) {
		super(c);
		this.row = row;
		this.col = col;
	}
}
