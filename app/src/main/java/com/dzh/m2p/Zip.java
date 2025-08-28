package com.dzh.m2p;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zip {
	public static void unzip(String file, String dest) throws IOException {
		unzip(file, dest, "UTF-8", true);
	}
	public static void unzip(String file, String dest, String encoding) throws IOException {
		unzip(file, dest, encoding, true);
	}
	public static void unzip(String file, String dest, boolean withDirs) throws IOException {
		unzip(file, dest, "UTF-8", withDirs);
	}
	public static void unzip(String file, String dest, String encoding, boolean withDirs) throws IOException {
		unzip(new FileInputStream(file), dest, encoding, withDirs);
	}
	public static void unzip(InputStream is, String dest) throws IOException {
		unzip(is, dest, "UTF-8", true);
	}
	public static void unzip(InputStream is, String dest, String encoding) throws IOException {
		unzip(is, dest, encoding, true);
	}
	public static void unzip(InputStream is, String dest, boolean withDirs) throws IOException {
		unzip(is, dest, "UTF-8", withDirs);
	}
	public static void unzip(InputStream is, String dest, String encoding, boolean withDirs) throws IOException {
		new File(dest).mkdirs();
		ZipInputStream zis = new ZipInputStream(is, Charset.forName(encoding));
		ZipEntry ze;
		while ((ze = zis.getNextEntry()) != null) {
			String path = dest + File.separator + removeUtf8Bom(withDirs ? ze.getName() : ze.getName().substring(ze.getName().lastIndexOf("/") + 1));
			if (withDirs && ze.isDirectory()) new File(path).mkdirs();
			else {
				FileOutputStream fos = new FileOutputStream(path);
				byte[] b = new byte[1024 * 1024];
				int len;
				while ((len = zis.read(b)) >= 0) fos.write(b, 0, len);
				fos.close();
			}
			zis.closeEntry();
		}
		zis.close();
		is.close();
	}
	public static void zip(String source, String destFile) throws IOException {
		zip(source, destFile, "UTF-8", true);
	}
	public static void zip(String source, String destFile, String encoding) throws IOException {
		zip(source, destFile, encoding, true);
	}
	public static void zip(String source, String destFile, boolean withRootDir) throws IOException {
		zip(source, destFile, "UTF-8", withRootDir);
	}
	public static void zip(String source, String destFile, String encoding, boolean withRootDir) throws IOException {
		zip(source, new FileOutputStream(destFile), encoding, withRootDir);
	}
	public static void zip(String source, OutputStream os, String encoding, boolean withRootDir) throws IOException {
		ZipOutputStream zos = new ZipOutputStream(os, Charset.forName(encoding));
		File file = new File(source);
		if (file.isFile() || withRootDir) zip(file, file.getName(), zos);
		else for (File c : file.listFiles()) zip(c, c.getName(), zos);
		zos.close();
		os.close();
	}
	private static void zip(File file, String name, ZipOutputStream zos) throws IOException {
		if (file.isDirectory()) {
			zos.putNextEntry(new ZipEntry(file.getName() + (file.getName().endsWith("/") ? "" : "/")));
			zos.closeEntry();
			for (File c : file.listFiles()) zip(c, name + "/" + c.getName(), zos);
			return;
		}
		FileInputStream fis = new FileInputStream(file);
		zos.putNextEntry(new ZipEntry(name));
		byte[] b = new byte[1024 * 1024];
		int len;
		while ((len = fis.read(b)) >= 0) zos.write(b, 0, len);
		fis.close();
	}
	private static String removeUtf8Bom(String s) {
		return s.startsWith("\uFEFF") ? s.substring(1) : s;
	}
}
