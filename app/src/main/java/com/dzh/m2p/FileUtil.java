package com.dzh.m2p;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FileUtil {
	public static void copy(String file, String dest, byte[] b) throws IOException {
		copy(new FileInputStream(file), new FileOutputStream(dest), b);
	}
	public static void copy(InputStream is, String dest, byte[] b) throws IOException {
		copy(is, new FileOutputStream(dest), b);
	}
	public static void copy(InputStream is, OutputStream os, byte[] b) throws IOException {
		int len;
		while ((len = is.read(b)) > -1) os.write(b, 0, len);
		is.close();
		os.close();
	}
}
