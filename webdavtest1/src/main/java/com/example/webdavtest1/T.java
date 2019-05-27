package com.example.webdavtest1;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class T {

	public static Drawable getFileDrawable(Context context, String imageFile) {
		return getFileDrawable(context, imageFile, 1);
	}

	public static boolean tmpGetFileDrawableOutOfMemory;
	public static Drawable getFileDrawable(Context context, String imageFile,
										   int startSampleSize) {
		try {
			if (!isFile(imageFile))
				return null;

			ParcelFileDescriptor pfd;
			try {
				pfd = context.getContentResolver().openFileDescriptor(Uri.fromFile(new File(imageFile)), "r");
			} catch (Exception e) {
				A.error(e);
				return null;
			}

			java.io.FileDescriptor fd = pfd.getFileDescriptor();
			Drawable d = null;
			try {
				d = getFileDrawable_proc(context, imageFile, fd, startSampleSize);
			} catch (OutOfMemoryError e1) {
				tmpGetFileDrawableOutOfMemory = true;
				System.gc();
				startSampleSize++;
				A.log("1) OutOfMemory, startSampleSize:"+startSampleSize);
				try {
					d = getFileDrawable_proc(context, imageFile, fd, startSampleSize);
				} catch (OutOfMemoryError e2) {
					System.gc();
					startSampleSize += 2;
					A.log("2) OutOfMemory, startSampleSize:"+startSampleSize);
					try {
						d = getFileDrawable_proc(context, imageFile, fd, startSampleSize);
					} catch (OutOfMemoryError e3) {
						System.gc();
						startSampleSize += 2;
						A.log("3) OutOfMemory, startSampleSize:"+startSampleSize);
						try {
							d = getFileDrawable_proc(context, imageFile, fd, startSampleSize);
						} catch (OutOfMemoryError e4) {
							A.log("4) still OutOfMemory, startSampleSize:"+startSampleSize);
							System.gc();
							return null;
						}
					}
					System.gc();
				}
			}
			return d;
		} catch (OutOfMemoryError e) {
			A.log("(*) getFileDrawable OutOfMemoryError");
			System.gc();
			return null;
		} catch (Exception e) {
			A.error(e);
			return null;
		}
	}

	private static Drawable getFileDrawable_proc(Context context,
												 String imageFile, java.io.FileDescriptor fd, int inSampleSize) {
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFileDescriptor(fd, null, opts);
		opts.inSampleSize = inSampleSize;
		opts.inJustDecodeBounds = false;
		return new BitmapDrawable(context.getResources(),
				BitmapFactory.decodeFile(imageFile, opts));
	}

	/**
	 * 这个函数对一些字符编码有问题, 用String2InputSource好一点
	 *
	 * @param str
	 * @return
	 */
	public static InputStream String2InputStream(String str) {
		try {
			return new ByteArrayInputStream(str.getBytes());
		} catch (OutOfMemoryError e) {
			System.gc();
		} catch (Exception e) {
			A.error(e);
		}
		return null;
	}

	public static void mySleep(long i) {
		try {
			Thread.sleep(i);
		} catch (Exception e) {
			A.error(e);
		}
	}
	public static InputSource String2InputSource(String str) {
		return new InputSource(new StringReader(str));
	}

	public static String inputStream2String(InputStream is) {
		return inputStream2String(is, "UTF-8", false);
	}
	public static String inputStream2String(InputStream is, String encoding){
		return inputStream2String(is, encoding, false);
	}
	public static String inputStream2String(InputStream is, String encoding, boolean firstLineOnly){
		if (is == null)
			return "";

		BufferedReader in;
		StringBuilder sb = null;
		try {
			int available = is.available();
			try {
				sb = !firstLineOnly && available > 0 ? new StringBuilder(available + 16)
						: new StringBuilder();
			} catch (OutOfMemoryError e) {
				A.error(e);
				sb = new StringBuilder();
			}

			if (encoding.equals(""))
				in = new BufferedReader(new InputStreamReader(is));
			else
				in = new BufferedReader(new InputStreamReader(is, encoding));

			if (firstLineOnly){
				sb.append(in.readLine());
//				String line;
//				while ((line = in.readLine()) != null) {
//					sb.append(line + "\n");
//					if (firstLineOnly)
//						break;
//				}
			}else{
				char[] buffer = new char[1024 * 8];
				while (true) {
					int size = in.read(buffer);
					if (size != -1) {
						sb.append(String.valueOf(buffer, 0, size));
					} else {
						in.close();
						break;
					}
				}
			}

			return sb.toString();
		} catch (OutOfMemoryError e) {
			System.gc();
		} catch (Exception e) {
			A.error(e);
		}

		try {
			return sb == null ? "" : sb.toString();
		} catch (OutOfMemoryError e) {
			System.gc();
			return "";
		}
	}

	public static String string2Encode(String s, String old_encode, String new_encode){
		try {
			byte[] ptext = s.getBytes(old_encode);
			return new String(ptext, new_encode);
		} catch (Exception e) {
			A.error(e);
			return s;
		}
	}

	public static boolean isOutOfMemoryError;

//	public static String ArrayList2String(ArrayList<String> list) {
//		StringBuilder sb = new StringBuilder();
//		for (String s : list)
//			sb.append(s + "\n");
//		return sb.toString();
//	}

	public static String stringList2Text(ArrayList<String> categories){
		StringBuilder sb = new StringBuilder();
		for (String s : categories)
			sb.append(s+"\n");
		return sb.toString();
	}

	public static ArrayList<String> text2StringList(String text){
		return text2StringList(text, false);
	}

	public static ArrayList<String> text2StringList(String text, boolean anti_order){
		ArrayList<String> al = new ArrayList<String>();
		int j = 0;
		if (!T.isNull(text))
			while (true){
				int i = text.indexOf("\n", j);
				if (i!=-1){
					if (anti_order)
						al.add(0, text.substring(j, i));
					else
						al.add(text.substring(j, i));
					j = i+1;
				}else{
					if (j<text.length()-1)
						if (anti_order)
							al.add(0, text.substring(j));
						else
							al.add(text.substring(j));
					break;
				}
			}
		return al;
	}

	public static ArrayList<String> inputStream2StringList(InputStream is) {
		try {
			return inputStream2StringList(is, "UTF-8");
		} catch (Exception e) {
			A.error(e);
		}
		return null;
	}

	public static ArrayList<String> inputStream2StringList(InputStream is,
														   String encoding) throws Exception {
		if (is == null)
			return null;

		BufferedReader in;
		ArrayList<String> al = new ArrayList<String>();
		try {
			if (encoding.equals(""))
				in = new BufferedReader(new InputStreamReader(is));
			else
				in = new BufferedReader(new InputStreamReader(is, encoding));
			String line;
			while ((line = in.readLine()) != null)
				al.add(line);
		} catch (Exception e) {
			A.error(e);
		}
		return al;
	}

	public static byte[] InputStream2Byte(InputStream is) {
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int nRead;
		byte[] data = new byte[16384];
		try {
			while ((nRead = is.read(data, 0, data.length)) != -1) {
				buffer.write(data, 0, nRead);
			}
			buffer.flush();
			return buffer.toByteArray();
		} catch (OutOfMemoryError e) {
			return null;
		} catch (Exception e) {
			A.error(e);
			return null;
		}
	}

	public static boolean createFolder(String path){
		if (createNormalFolder(path))
			return true;

		return false;
	}

	public static boolean createNormalFolder(String path){
		if (T.isNull(path))
			return false;
		File folder = new File(path);
		if (folder.isFile())
			return false;
		if (!folder.exists())
			if (!folder.mkdirs())
				return false;
		return true;
	}

	public static InputStream Byte2InputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}

	public static boolean inputStream2File(InputStream is, String filename) {
		try {
			if (!createFolder(T.getFilePath(filename)))
				return false;

			byte[] bs = new byte[1024 * 8];
			int len;
			OutputStream os = new FileOutputStream(filename);
			while ((len = is.read(bs)) != -1)
				os.write(bs, 0, len);
			os.close();
		} catch (Exception e) {
			A.error(e);
			return false;
		}
		return true;
	}

	public static boolean appendInputStream2File(InputStream is, String filename) {
		try {
			if (!createFolder(T.getFilePath(filename)))
				return false;

			byte[] bs = new byte[1024 * 8];
			int len;
			OutputStream os = new FileOutputStream(filename, true);
			while ((len = is.read(bs)) != -1)
				os.write(bs, 0, len);
			os.close();
		} catch (Exception e) {
			A.error(e);
			return false;
		}
		return true;
	}

	public static InputStream file2InputStream(String filename) {
		try {
			return new DataInputStream(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			A.error(e);
			return null;
		}
	}

	/**
	 * "/sdcard/1.TXT" return ".txt" (lowsercase)
	 *
	 * @return
	 */
	public static String getFileExt(String filename) {
		if (filename == null)
			return "";
		int dot = filename.lastIndexOf(".");
		if (dot == -1)
			return "";
		return filename.substring(dot, filename.length()).toLowerCase();
	}

	/**
	 * "/sdcard/1.txt" -> "1.txt", "/sdcard/1.zip?a.txt" -> "a.txt"
	 *
	 * @return
	 */
	public static String getFilename(String filename) {
		if (filename == null)
			return "";
		int dot = filename.indexOf("?");
		if (dot != -1)
			filename = filename.substring(dot+1);
		dot = filename.lastIndexOf("/");
		if (dot == -1)
			return filename;
		else
			return filename.substring(dot + 1, filename.length());
	}

	/**
	 * "/sdcard/1.txt" return "1"
	 *
	 * @param filename
	 * @return
	 */
	public static String getOnlyFilename(String filename) {
		if (filename == null)
			return "";
		String name = getFilename(filename);
		int dot = name.lastIndexOf(".");
		if (dot == -1)
			return name;
		else
			return name.substring(0, dot);
	}

	/**
	 * "/sdcard/txt" return "/sdcard"
	 *
	 * @return
	 */
	public static String getFilePath(String filename) {
		if (T.isNull(filename))
			return "";
		int dot = filename.lastIndexOf("/");
		if (dot == -1)
			return "";
		return filename.substring(0, dot);
	}

	public static boolean isFile(String filename) {
		if (isNull(filename))
			return false;
		File afile = new File(filename);
		return afile.isFile();
	}

	public static boolean isEmptyFile(String filename) {
		if (filename == null)
			return true;
		if (filename.equals(""))
			return true;
		File afile = new File(filename);
		return !afile.isFile() || afile.length() == 0;
	}

	public static boolean isFolder(String folder) {
		if (folder == null)
			return false;
		if (folder.equals(""))
			return false;
		File afile = new File(folder);
		return afile.isDirectory();
	}

	public static ArrayList<String> getFolderFileList(String path,
													  boolean includeSubFolder) {
		return getFolderFileList(path, includeSubFolder, true, true);
	}

	public static boolean scanCanceled;

	public static ArrayList<String> getFolderFileList(String path,
													  boolean includeSubFolder, boolean listFilesOnly,
													  boolean includeHiddenFiles) {
		return getFolderFileList(path, includeSubFolder, listFilesOnly, includeHiddenFiles, null, 0);
	}

	public static ArrayList<String> getFolderFileList(String path,
													  boolean includeSubFolder, boolean listFilesOnly,
													  boolean includeHiddenFiles, Handler handler, int msgWhat) {
		ArrayList<String> al = new ArrayList<String>();

		try {
			if (handler!=null){
				handler.removeMessages(msgWhat);
				handler.sendMessage(handler.obtainMessage(msgWhat, path));
			}

			File folder = new File(path);
			File[] files = folder.listFiles();

			if (files != null) {
				if (includeSubFolder) {
					for (File file : files) {
						if (scanCanceled) // 2012.4.1 allow cancel
							return al;

						if (file.isFile()) {
							if (includeHiddenFiles
									|| !file.getName().startsWith("."))
								al.add(file.getAbsolutePath());
						}
						if (file.isDirectory()) {
							if (!listFilesOnly)
								if (includeHiddenFiles
										|| !file.getName().startsWith("."))
									al.add(file.getAbsolutePath());
							if (includeHiddenFiles
									|| !file.getName().startsWith(".")) {
								ArrayList<String> al_sub = getFolderFileList(
										file.getAbsolutePath(), true,
										listFilesOnly, includeHiddenFiles, handler, msgWhat);
								al.addAll(al_sub);
							}
						}
					}
				} else {
					for (File file : files) {
						if (file.isFile()) {
							if (includeHiddenFiles
									|| !file.getName().startsWith("."))
								al.add(file.getAbsolutePath());
						} else if (!listFilesOnly)
							if (includeHiddenFiles
									|| !file.getName().startsWith("."))
								al.add(file.getAbsolutePath());
					}
					return al;
				}
			}
		} catch (OutOfMemoryError e) {
			A.error(e);
			System.gc();
		} catch (Exception e) {
			A.error(e);
		} catch (Throwable e) {//v1.9.4 Throwable includes all errors
			A.error(e);
		}

		return al;
	}

	public static boolean deleteFolder(String fullname) {
		if (deleteNormalFolder(fullname))
			return true;

		return false;
	}

	public static boolean deleteNormalFolder(String fullname) {
		if (!isFolder(fullname))
			return false;

		try {
			ArrayList<String> list = getFolderFileList(fullname, true, false, true);
			for (int i = list.size() - 1; i >= 0; i--) {
				if (!(new File(list.get(i)).delete()))
					return false;
			}
			new File(fullname).delete();
		} catch (Exception e) {
			A.error(e);
		}

		return true;
	}

	// ------------------------------------------

	/**
	 * default encoding: UTF-8
	 *
	 * @return
	 */
	public static String getFileText(String filename) {
		return getFileText(filename, "UTF-8");
	}
	public static String getFileText(String filename, String encoding) {
		try {
			return getFileText(filename, encoding, true);
		} catch (Exception e) {
			A.error(e);
			return null;
		}
	}

	public static String getFileText(String filename, String encoding,
									 boolean nullIfOutOfMemory) throws Exception {
		if (filename == null || encoding == null)
			return null;
		File file = new File(filename);
		if (!file.isFile())
			return null;

		StringBuilder sb = null;
		try {
			InputStream is = new DataInputStream(new FileInputStream(filename));
			BufferedReader in;
			if (encoding.equals(""))
				in = new BufferedReader(new InputStreamReader(is));
			else
				in = new BufferedReader(new InputStreamReader(is, encoding));

			try {
				int capacity = (int) file.length();
				sb = new StringBuilder(capacity + 16); // important!
			} catch (OutOfMemoryError e) {
				A.error(e);
				sb = new StringBuilder();
			}

			char[] buffer = new char[1024 * 8];
			while (true) {
				int size = in.read(buffer);
				if (size != -1) {
					sb.append(String.valueOf(buffer, 0, size));
				} else {
					in.close();
					break;
				}
			}

			return sb.toString();
		} catch (OutOfMemoryError e) {
			A.error(e);
			System.gc();
			if (!nullIfOutOfMemory && sb!=null && sb.length()>0)
				try {
					return sb.toString();
				} catch (OutOfMemoryError e2) {
					throw new Exception(e2);
				}
			throw new Exception(e);
		} catch (Exception e) {
			A.error(e);
			throw new Exception(e);
		}
	}

	public static boolean saveFileText(String filename, String text,
									   String encoding) {
		try {
			if (encoding == null)
				return saveFileText(filename, text);
			ByteArrayInputStream is = new ByteArrayInputStream(
					text.getBytes(encoding));
			return inputStream2File(is, filename);
		} catch (Exception e) {
			A.error(e);
			return false;
		}
	}

	public static boolean saveFileText(String filename, String text) {
		BufferedReader bufferedReader = null;
		BufferedWriter bufferedWriter = null;
		try {
			if (!createFolder(T.getFilePath(filename)))
				return false;
			File distFile = new File(filename);
			bufferedReader = new BufferedReader(new StringReader(text));
			bufferedWriter = new BufferedWriter(new FileWriter(distFile));
			char buf[] = new char[1024];
			int len;
			while ((len = bufferedReader.read(buf)) != -1) {
				bufferedWriter.write(buf, 0, len);
			}
			bufferedWriter.flush();
			bufferedReader.close();
			bufferedWriter.close();
		} catch (OutOfMemoryError e) {
			A.error(e);
			return false;
		} catch (Exception e) {
			A.error(e);
			return false;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (Exception e) {
					A.error(e);
				}
			}
		}
		return true;
	}

	public static boolean appendFileText(String filename, String text) {
		if (!isFile(filename))
			return saveFileText(filename, text);
		return saveFileText(filename, getFileText(filename) + "\n" + text);
	}

	// ------------------------------------------

	// -------------------------------------------------------------

	/**
	 * 模拟类似delphi random(100)类型的随机数
	 *
	 * @param range
	 * @return
	 */
	public static int myRandom(int range) {
		double a = Math.random() * range;
		a = Math.ceil(a);
		int randomNum = new Double(a).intValue();
		return randomNum - 1;
	}

	public static String deleteSpecialChar(String str) {
		// 只允许字母和数字和".": String regEx = "[^a-zA-Z0-9]";
		try {
			String regEx = "[`~!@#$%^&*()+=|{}':;',\\[\\]<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(str);
			return m.replaceAll("").trim().replace("\n", "");
		} catch (Exception e) {
			A.error(e);
			return str;
		}

	}

	public static void showToastText(Context context, CharSequence text) {
		showToastText(context, text, 0);
	}

	/**
	 * duration: 0 short time, 1 long time
	 * @param context
	 * @param text
	 * @param duration
	 */
	public static void showToastText(Context context, CharSequence text, int duration) {
		showToastText(context, text, duration, Gravity.BOTTOM);
	}

	static boolean smartToastInited;
	public static void showToastText(Context context, CharSequence text, int duration, int gravity) {
		try {
			if (mToast != null) {
				if (Build.VERSION.SDK_INT >= 26){// && mToast.getView().isShown()){
					mToast.cancel();
					mToast = Toast.makeText(context, text, duration);
				}else
				{
					mToast.setText(text);
					mToast.setDuration(duration);
				}
			} else
				mToast = Toast.makeText(context, text, duration);
//			mToast.setGravity(gravity, 0, gravity == Gravity.BOTTOM? A.getScreenHeight()/10 : 0);
			setToastColors();
			mToast.show();
			A.log("Toast: " + text.toString());

			/*if (!smartToastInited) {
				smartToastInited = true;
				SmartShow.init(A.application);
			}
				SmartToast.setting().backgroundColor(0xee33333).textColor(0xffffffff);
//			if (SmartToast.isShowing())
//				SmartToast.dismiss();
			SmartToast.info(text);
			float yOff = 0;
			if (gravity == Gravity.BOTTOM)
				yOff = context.getResources().getDisplayMetrics().heightPixels/context.getResources().getDisplayMetrics().density/10;
			if (duration == 0)
				SmartToast.showAtLocation(text, gravity, 0, yOff);
			else
				SmartToast.showLongAtLocation(text, gravity, 0, yOff);*/
		} catch (Throwable e) {
			A.error(e);
			hideToast();
		}
	}

	public static void showToastText(Context context, String title, String text, int duration) {
		showToastText(context, title, text, duration, Gravity.BOTTOM);
	}

	public static void showToastText(Context context, String title, String text, int duration, int gravity) {
		try {
			CharSequence html = Build.VERSION.SDK_INT >= 26?
					Html.fromHtml(""+title+"<br>" +text) :
					Html.fromHtml("<b><font color=\"#FFFF00\">"+title+"</font></b><br>" +text);
			showToastText(context, html, duration, gravity);
		} catch (Throwable e) {
			A.error(e);
			hideToast();
		}
	}

	private static Toast mToast;

	public static void hideToast() {
		if (mToast != null)
			mToast.cancel();
	}

	private static void setToastColors() {
		/*if (Build.VERSION.SDK_INT >= 26) {
			View view = mToast.getView();
			if (view != null){
				view.setBackgroundResource(R.drawable.round_grey);
				TextView tv = view.findViewById(android.R.id.message);
				if (tv != null)
					tv.setTextColor(0xffffffff);
			}
		}*/
	}
	public static CharSequence forceWhite(CharSequence text) {
		return Html.fromHtml("<font color=\"#FFFFFF\">"+text+"</font>");
	}


	public static String time() {
		return time(true, false, Locale.US);
	}

	public static String time(boolean showSecond, boolean use12Hour,
							  Locale locale) {
		return time(showSecond, use12Hour ,locale, System.currentTimeMillis());
	}

	public static String time(boolean showSecond, boolean use12Hour,
							  Locale locale, long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("", locale);
		boolean isAsiaLocale = false;
		if (use12Hour) {
			isAsiaLocale = locale.getLanguage().equals("zh")
					|| locale.getLanguage().equals("jp")
					|| locale.getLanguage().equals("ko");
			if (showSecond) {
				if (isAsiaLocale)
					sdf.applyPattern("a hh:mm:ss");
				else
					sdf.applyPattern("hh:mm:ss a");
			} else {
				if (isAsiaLocale)
					sdf.applyPattern("a hh:mm");
				else
					sdf.applyPattern("hh:mm a");
			}
		} else {
			if (showSecond)
				sdf.applyPattern("HH:mm:ss");
			else
				sdf.applyPattern("HH:mm");
		}

		String str = sdf.format(time);
		if (isAsiaLocale && (str.startsWith("AM ") || str.startsWith("PM ")))
			str = str.substring(3) + " " + str.substring(0, 2);
		return str;
	}

	public static String dateTimeToStr(Long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("", Locale.US);
		sdf.applyPattern("yyyy-MM-dd HH:mm:ss");
		return sdf.format(time);
	}

	public static String dateToStr(Long time, Locale locale) {
		return DateFormat.getDateInstance(DateFormat.MEDIUM, locale).format(new Date(time));
	}

	public static String chinaTime() {
		return chinaTime(System.currentTimeMillis());
	}

	public static String chinaTime(Long time) {
		SimpleDateFormat sdf = new SimpleDateFormat("",
				Locale.SIMPLIFIED_CHINESE);
		sdf.applyPattern("yyyy年MM月dd日 HH时mm分ss秒");
		return sdf.format(time);
	}

	/**
	 * 按正则表达式返回字符串
	 *
	 * @param regEx
	 *            正则表达式
	 * @param text
	 *            准备从中提取字符串的文本
	 * @return 返回根据正则表达式从文本中提取到的第一个匹配字符串
	 *         <p>
	 *         Example: getMatcherText("a.c", "123abcd") = "abc"
	 */
	public static String getMatcherText(String regEx, String text) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(text);
		if (matcher.find())
			return matcher.group();
		else
			return "";
	}

	/**
	 * 按正则表达式返回字符串数组（正则表达式有好几种，java的实现方法有待学习）
	 *
	 * @param regEx
	 *            正则表达式
	 * @param text
	 *            准备从中提取字符串的文本
	 * @return 返回根据正则表达式从文本中提取到的字符串数组
	 */
	public static ArrayList<String> getMatcherTexts(String regEx, String text) {
		Pattern pattern = Pattern.compile(regEx);
		Matcher matcher = pattern.matcher(text);
		ArrayList<String> sl = new ArrayList<String>();
		while (matcher.find())
			sl.add(matcher.group());
		return sl;
	}

	/**
	 * filenameMatch("*.txt", "file.txt")=true<br>
	 * ("", "file.txt")=true<br>
	 * ("le", "file.txt")=true<br>
	 * ("?le*", "file.txt")=true<br>
	 */
	public static boolean filenameMatch(String pattern, String filename) {
		return filenameMatch(pattern, filename, true);
	}

	public static boolean filenameMatch(String pattern, String filename,
										boolean checkEmptyAndPart) {
		if (checkEmptyAndPart) {
			if (pattern.equals(""))
				return true;
			if (filename.indexOf(pattern) != -1)
				return true;
		}

		int patternLength = pattern.length();
		int strLength = filename.length();
		int strIndex = 0;
		char ch;
		for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
			ch = pattern.charAt(patternIndex);
			if (ch == '*') {
				// 通配符星号*表示可以匹配任意多个字符
				while (strIndex < strLength) {
					if (filenameMatch(pattern.substring(patternIndex + 1),
							filename.substring(strIndex), false)) {
						return true;
					}
					strIndex++;
				}
			} else if (ch == '?') {
				// 通配符问号?表示匹配任意一个字符
				strIndex++;
				if (strIndex > strLength) {
					// 表示str中已经没有字符匹配?了。
					return false;
				}
			} else {
				if ((strIndex >= strLength)
						|| (ch != filename.charAt(strIndex))) {
					return false;
				}
				strIndex++;
			}
		}
		return (strIndex == strLength);
	}

	public static int getScreenWidth(Context context) {
		return context.getResources().getDisplayMetrics().widthPixels;
	}

	public static int getScreenHeight(Context context) {
		return context.getResources().getDisplayMetrics().heightPixels;
	}

	public static String getScreenDescription(Context context) {
		int w = context.getResources().getDisplayMetrics().widthPixels;
		int h = context.getResources().getDisplayMetrics().heightPixels;
		return w < h ? w + "x" + h : h + "x" + w;
	}

	public static Bitmap zoomImage(Bitmap bm, int newWidth, int newHeight) {
		try {
			if (newWidth <= 0)
				newWidth = 1;
			if (newHeight <= 0)
				newHeight = 1;
			int width = bm.getWidth();
			int height = bm.getHeight();
			Matrix matrix = new Matrix();
			float scaleWidth = ((float) newWidth) / width;
			float scaleHeight = ((float) newHeight) / height;
			matrix.postScale(scaleWidth, scaleHeight);
			bm = Bitmap.createBitmap(bm, 0, 0, width, height,
					matrix, true);
		} catch (OutOfMemoryError e) {
			System.gc();
		} catch (Exception e) {
			A.error(e);
		}
		return bm;
	}

	public static Drawable zoomDrawable(Resources res, Drawable drawable,
										int w, int h) {
		try {
			return new BitmapDrawable(res, drawableToBitmap(drawable, w, h));
		} catch (Exception e) {
			A.error(e);
			return drawable;
		}
	}

	public static Bitmap drawableToBitmap(Drawable d) {
		if (d==null)
			return null;
		if (d instanceof BitmapDrawable)
			return ((BitmapDrawable) d).getBitmap();
		return drawableToBitmap(d, d.getIntrinsicWidth(),
				d.getIntrinsicHeight());
	}

	public static Bitmap drawableToBitmap(Drawable d, int width, int height) {
		try {
			if (d instanceof BitmapDrawable)
				if (d.getIntrinsicWidth()==width  && d.getIntrinsicHeight()==height)
					return ((BitmapDrawable) d).getBitmap();

			Bitmap.Config config = d.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
					: Bitmap.Config.RGB_565;// 取drawable的颜色格式
			if (width <= 0)
				width = 1;
			if (height <= 0)
				height = 1;
			Bitmap bitmap = Bitmap.createBitmap(width, height, config);
			Canvas canvas = new Canvas(bitmap);
			d.setBounds(0, 0, width, height);
			d.draw(canvas);
			return bitmap;
		} catch (OutOfMemoryError e) {
			System.gc();
			A.error(e);
			return null;
		} catch (Exception e) {
			A.error(e);
			return null;
		}
	}

	public static boolean drawableToFile(Drawable d, String filename) {
		try {
			Bitmap bm = drawableToBitmap(d, d.getIntrinsicWidth(),
					d.getIntrinsicHeight());
			return bitmapToFile(bm, filename);
		} catch (OutOfMemoryError e) {
			A.error(e);
		} catch (Exception e) {
			A.error(e);
		}
		return false;
	}

	public static boolean bitmapToFile(Bitmap bm, String filename) {
		try {
			if (!createFolder(T.getFilePath(filename)))
				return false;

			OutputStream fOut = new FileOutputStream(filename);
			if (filename.endsWith(".jpg"))
				bm.compress(Bitmap.CompressFormat.JPEG, 90, fOut);
			else
				bm.compress(Bitmap.CompressFormat.PNG, 100, fOut);
			fOut.flush();
			fOut.close();
			return true;
		} catch (OutOfMemoryError e) {
			System.gc();
		} catch (Exception e) {
			A.error(e);
		}
		return false;
	}

	public static String getPercentStr(long pos, long total) {
		if (pos < 0)
			pos = 0;
		if (pos > total)
			pos = total;
		String s = total == 0 ? "0.0%" : (double) ((pos * 1000 / total) * 100)
				/ 1000 + "%";
		if (s.equals("100.0%"))
			s = "100%";
		return s;
	}

	public static boolean moveFile(String oldFilename, String destFilename,  boolean overwrite) {
		if (moveNormalFile(oldFilename, destFilename, overwrite))
			return true;
		return false;
	}

	public static boolean moveNormalFile(String oldFilename, String destFilename,  boolean overwrite) {
		File srcFile = new File(oldFilename);
		if (!srcFile.isFile())
			return false;

		File destFile = new File(destFilename);
		if (destFile.isFile()) {
			if (overwrite) {
				if (!destFile.delete())
					return false;
			} else
				return false;
		}

		if (destFile.isDirectory()) {
			if (overwrite) {
				if (!deleteFolder(destFilename))
					return false;
			} else
				return false;
			}

		if (!createFolder(T.getFilePath(destFilename)))
			return false;

		if (srcFile.renameTo(destFile))
			return true;
		else
			return false;
	}

	private static boolean moveDocumentFile(String oldFilename, String destFilename, boolean overwrite) {
		File srcFile = new File(oldFilename);
		if (!srcFile.isFile())
			return false;
		File destFile = new File(destFilename);
		if (destFile.isFile()) {
			if (overwrite) {
				if (!deleteFile(destFilename))
					return false;
			} else
				return false;
		}
		if (!copyFile(oldFilename, destFilename, true))
			return false;
		deleteFile(oldFilename);
		return true;
	}

	public static boolean renameFile(String oldFilename, String destFilename, boolean overwrite) {
		File srcFile = new File(oldFilename);
		if (!srcFile.isFile() && !srcFile.isDirectory())
			return false;
		File destFile = new File(destFilename);
		if (destFile.isFile() || destFile.isDirectory()) {
			if (overwrite) {
				if (!deleteFile(destFilename))
					return false;
			} else
				return false;
		}

		if (srcFile.renameTo(destFile))
			return true;
		return false;
	}

	public static boolean deleteFile(String filename) {
		File file = new File(filename);
		if (file.isFile() || file.isDirectory()){
			if (file.delete())
				return true;

		}
		return false;
	}

	public static boolean copyFile(String oldFilename, String destFilename, boolean overwrite) {
		if (copyNormalFile(oldFilename, destFilename, overwrite))
			return true;
		return false;
	}

	public static boolean copyNormalFile(String oldFilename, String destFilename, boolean overwrite) {
		if (!T.isFile(oldFilename))
			return false;
		File destFile = new File(destFilename);
		if (destFile.isFile()) {
			if (overwrite) {
				if (!destFile.delete())
					return false;
			} else
				return false;
		}

		if (destFile.isDirectory()) {
			if (overwrite) {
				if (!deleteFolder(destFilename))
					return false;
			} else
				return false;
		}

		if (!createFolder(T.getFilePath(destFilename)))
			return false;

		try {
			int byteread = 0;
			InputStream inStream = new FileInputStream(oldFilename);
			FileOutputStream fs = new FileOutputStream(destFilename);
			byte[] buffer = new byte[8 * 1024];
			while ((byteread = inStream.read(buffer)) != -1) {
				fs.write(buffer, 0, byteread);
			}
			inStream.close();
			return true;
		} catch (Exception e) {
			A.error(e);
			return false;
		}
	}


	public static void openUrl(Activity activity, String url) {
		try {
			Uri uri = Uri.parse(url);
			Intent intent = new Intent(Intent.ACTION_VIEW, uri);
			activity.startActivity(intent);
		} catch (Exception e) {
			A.error(e);
		}
	}

	public static String getHtmlBody(String html) {
		if (html==null)
			return null;
		try {
			String html2 = html.toLowerCase();
			int i1 = html2.indexOf("<body>");
			int i2 = html2.indexOf("</body>");
			if ((i1 != -1) && (i2 != -1))
				return html.substring(i1 + 6, i2);
			else if (i2 != -1) {
				i1 = html2.indexOf("<body");
				if (i1 != -1) {
					i1 = html2.indexOf(">", i1);
					if (i1 > 0 && i1 < i2)
						return html.substring(i1 + 1, i2);
				}
			} else {
				i1 = html2.indexOf("<html>");
				i2 = html2.indexOf("</html");
				if ((i1 != -1) && (i2 != -1))
					return html.substring(i1 + 6, i2);
				else if (i2 != -1) {
					i1 = html2.indexOf("<html");
					if (i1 != -1) {
						i1 = html2.indexOf(">", i1);
						if (i1 > 0 && i1 < i2)
							return html.substring(i1 + 1, i2);
					}
				} else {
					i1 = html2.indexOf("<html");
					if (i1 != -1)// bad file, has start tag "<html" but no end tag "</html>", Html.from() will be dead!
						return ""; // Html.fromHtml(text).toString();
				}
			}
		} catch (OutOfMemoryError e) {
			System.gc();
		} catch (Exception e) {
			A.error(e);
		}
		return html;
	}

	public static String deleteHtmlStyle(String html) {
		// html = html.replaceAll("(?i)<style.*</style>", ""); //not work in
		// mult-lines?
		try {
			String html2 = html.toLowerCase();
			while (true) {
				int i1 = html2.indexOf("<style");
				if (i1 == -1)
					break;
				int i2 = html2.indexOf("</style>");
				if (i2 != -1 && i2 > i1) {
					html = html.substring(0, i1) + html.substring(i2 + 8);
					html2 = html.toLowerCase();
				} else
					break;
			}
			while (true) {
				int i1 = html2.indexOf("<script");
				if (i1 == -1)
					break;
				int i2 = html2.indexOf("</script>");
				if (i2 != -1 && i2 > i1) {
					html = html.substring(0, i1) + html.substring(i2 + 9);
					html2 = html.toLowerCase();
				} else
					break;
			}
		} catch (OutOfMemoryError e) {
		}
		return html;
	}

	private static final String[][] MIME_MapTable = { { ".3gp", "video/3gpp" },
			{ ".apk", "application/vnd.android.package-archive" },
			{ ".asf", "video/x-ms-asf" }, { ".avi", "video/x-msvideo" },
			{ ".bin", "application/octet-stream" }, { ".bmp", "image/bmp" },
			{ ".class", "application/octet-stream" },
			{ ".doc", "application/msword" },
			{ ".exe", "application/octet-stream" }, { ".gif", "image/gif" },
			{ ".gtar", "application/x-gtar" }, { ".gz", "application/x-gzip" },
			{ ".jar", "application/java-archive" }, { ".jpeg", "image/jpeg" },
			{ ".jpg", "image/jpeg" }, { ".m3u", "audio/x-mpegurl" },
			{ ".m4a", "audio/mp4a-latm" }, { ".m4b", "audio/mp4a-latm" },
			{ ".m4p", "audio/mp4a-latm" }, { ".m4u", "video/vnd.mpegurl" },
			{ ".m4v", "video/x-m4v" }, { ".mov", "video/quicktime" },
			{ ".mp2", "audio/x-mpeg" }, { ".mp3", "audio/x-mpeg" },
			{ ".mp4", "video/mp4" },
			{ ".mpc", "application/vnd.mpohun.certificate" },
			{ ".mpe", "video/mpeg" }, { ".mpeg", "video/mpeg" },
			{ ".mpg", "video/mpeg" }, { ".mpg4", "video/mp4" },
			{ ".mpga", "audio/mpeg" },
			{ ".msg", "application/vnd.ms-outlook" }, { ".ogg", "audio/ogg" },
			{ ".pdf", "application/pdf" }, { ".doc", "application/doc" },
			{ ".doc", "application/mkv" }, { ".png", "image/png" },
			{ ".pps", "application/vnd.ms-powerpoint" },
			{ ".ppt", "application/vnd.ms-powerpoint" },
			{ ".rar", "application/x-rar-compressed" },
			{ ".rmvb", "video/x-pn-realaudio" },
			{ ".rm", "video/x-pn-realaudio" }, { ".rtf", "application/rtf" },
			{ ".tar", "application/x-tar" },
			{ ".tgz", "application/x-compressed" }, { ".wav", "audio/x-wav" },
			{ ".wma", "audio/x-ms-wma" }, { ".wmv", "audio/x-ms-wmv" },
			{ ".wps", "application/vnd.ms-works" },
			{ ".z", "application/x-compress" }, { ".zip", "application/zip" },
			{ "", "*/*" } };

	public static String getMIMEType(String filename) {
		String type1 = "*/*";
		if (filename==null)
			return type1;
		int dotIndex = filename.lastIndexOf(".");
		if (dotIndex < 0)
			return type1;
		String ext = filename.substring(dotIndex, filename.length())
				.toLowerCase();
		if (ext == "")
			return type1;

		String type2 = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
				filename.substring(dotIndex + 1, filename.length()));
		if (type2 != null)
			return type2;

		for (int i = 0; i < MIME_MapTable.length; i++)
			if (ext.equals(MIME_MapTable[i][0]))
				return MIME_MapTable[i][1];

		return type1;
	}

	public static void openFileWithDefaultApp(Activity activity, String filename) {
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://" + filename),
					T.getMIMEType(filename));
			activity.startActivity(intent);
		} catch (Exception e) {
			A.error(e);
			try {
				Intent intent = new Intent(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.parse("file://" + filename), "*/*");
				activity.startActivity(intent);
			} catch (Exception e2) {
				A.error(e);
			}
		}
	}

	public static int string2Int(String s) {
		int i = 0;
		while (i < s.length() && !(s.charAt(i) >= '0' && s.charAt(i) <= '9'))
			i++;
		int start = i;
		while (i < s.length() && (s.charAt(i) >= '0' && s.charAt(i) <= '9'))
			i++;
		try {
			return Integer.valueOf(s.substring(start, i));
		} catch (Exception e) {
			A.error(e);
		}
		return 0;
	}

	public static float string2Float(String s) {
		int i = 0;
		while (i < s.length() && !(s.charAt(i) >= '0' && s.charAt(i) <= '9'))
			i++;
		int start = i;
		while (i < s.length() && (s.charAt(i)=='.' || (s.charAt(i) >= '0' && s.charAt(i) <= '9')))
			i++;
		try {
			return Float.valueOf(s.substring(start, i));
		} catch (Exception e) {
			A.error(e);
		}
		return 0;
	}

	public static boolean charIsNumber(char c){
		return c >= '0' && c <= '9';
	}

	public static String buildString(String... strings) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < strings.length; i++)
			if (strings[i].length() > 0)
				sb.append(strings[i]);
		return sb.toString();
	}

	public static String getEmailFileName(ContentResolver resolver, Uri uri) {
		String result = null;
		try {
			Cursor cursor = resolver.query(uri,
					new String[] { "_display_name" }, null, null, null);
			cursor.moveToFirst();
			int nameIndex = cursor.getColumnIndex("_display_name");
			if (nameIndex >= 0)
				result = cursor.getString(nameIndex);
		} catch (Exception e) {
			A.error(e);
			result = null;
		}
		if (result == null)
			try {
				Cursor cursor = resolver.query(uri, null, null, null, null);
				cursor.moveToFirst();
				int nameIndex = cursor.getColumnIndex("_display_name");
				if (nameIndex >= 0)
					result = cursor.getString(nameIndex);
			} catch (Exception e) {
				A.error(e);
				result = null;
			}
		return result;
	}

	public static String getFileTypeFromBinary(String binary) {
		if (binary.length()>100)
			binary = binary.substring(0, 100);
		if (binary.indexOf("PNG") == 1)
			return ".png";
		else if (binary.indexOf("PDF")>=0 && binary.indexOf("PDF") < 5)
			return ".pdf";
		else if (binary.indexOf("GIF") == 0)
			return ".gif";
		else if (binary.indexOf("JFIF") == 6)
			return ".jpg";
		else if (binary.indexOf("application/epub+zip") == 38)
			return ".epub";
		else if (binary.indexOf("PK") == 0)
			return ".zip";
		else if (binary.indexOf("Rar!") == 0)
			return ".rar";
		return "";
	}

	public static int getWordsCount(String text, boolean isCJK) {
		if (isCJK) {
			int count = 0;
			for (int i = 0; i < text.length(); i++) {
				char c = text.charAt(i);
				if (Character.getType(c) == Character.OTHER_LETTER
						|| Character.isLetter(c))
					count++;
			}
			return count;
		} else
			return new StringTokenizer(text, "\n,. :;/").countTokens();
	}

	public static String html2Text(String html){ // for count about chars only
		try {
			StringBuilder sb = new StringBuilder();
			int start = html.indexOf("<body");
			if (start==-1)
				start = html.indexOf("<BODY");
			if (start==-1)
				start = 0;

			int i1=-1, i2=-1;
			for (int i = start; i<html.length(); i++){
				int c = html.charAt(i);
				if (c=='>'){
					i1=i;
					i2=-1;
				}
				if (c=='<'){
					i2=i;
				}
				if (i1>0 && i2>i1+1){
					String s = html.substring(i1+1, i2).trim();
					if (s.length()>0)
						sb.append(s+" ");
					i1=i2=-1;
				}
			}
			return sb.toString().replace("\r", "").replace("\n", "");
		} catch (OutOfMemoryError e) {
			A.error(e);
			return html;
		}
	}

	public static String deleteHtmlTag(String html){
		try {
			StringBuilder sb = new StringBuilder();
			char c;
			boolean start = false;
			for (int i = 0; i<html.length(); i++){
				c = html.charAt(i);
				if (c=='<'){
					start = true;
				}
				if (!start)
					sb.append(c);
				if (c=='>'){
					start = false;
				}
			}
			return sb.toString();
		} catch (OutOfMemoryError e) {
			A.error(e);
			return html;
		}
	}

	public static int getDrawableAboutColor(Drawable d) {
		Bitmap bm = drawableToBitmap(d);
		return getBitmapAboutColor(bm);
	}

	public static Drawable bitmapToDrawble(Context context, Bitmap bm) {
		return context != null? new BitmapDrawable(context.getResources(), bm)
				: new BitmapDrawable(bm);
	}

	public static int getBitmapAboutColor(Bitmap bm) {
		return getBitmapAboutColor(bm, 1, false, false);
	}

	public static int getBitmapAboutColor(Bitmap bm, float grayRate,
										  boolean centerOnly, boolean includeAlpha) {
		if (bm==null)
			return 0;
		int w = bm.getWidth();
		int h = bm.getHeight();
		if (w > 0 && h > 0) {
			int x = 10;
			int[][] colors = new int[x][4];
			for (int i = 0; i < x; i++) {
				int c = centerOnly ? bm.getPixel(w / 3 + (w / 3) * (i / 10), h
						/ 3 + (h / 3) * (i / 10)) : bm.getPixel(w * (i + 1)
						/ (x + 2), h * (i + 1) / (x + 2));
				colors[i][0] = Color.red(c);
				colors[i][1] = Color.green(c);
				colors[i][2] = Color.blue(c);
				colors[i][3] = Color.alpha(c);
			}
			int r = getAverageColorValue(colors, 0, grayRate);
			int g = getAverageColorValue(colors, 1, grayRate);
			int b = getAverageColorValue(colors, 2, grayRate);
			int a = includeAlpha ? getAverageColorValue(colors, 3, 1) : 255;
			return Color.argb(a, r, g, b);
		}
		return 0;
	}

	private static int getAverageColorValue(int[][] colors, int column,
											float grayRate) {
		int count = 0;
		for (int i = 0; i < colors.length; i++)
			count += colors[i][column];
		return (int) (grayRate * count / colors.length);
	}

	public static int getColorValue(int c) {
		int r = Color.red(c);
		int g = Color.green(c);
		int b = Color.blue(c);
		return (r + g + b) / 3;
	}

	public static boolean isNull(String s) {
		return s == null || s.length() == 0;
	}

	public static boolean isNull(CharSequence s) {
		return s == null || s.length() == 0;
	}

	public static boolean isNull(Activity act) {
		return act == null || act.isFinishing();
	}

	public static boolean extractFileFromAsset(AssetManager am, String asset_filename,
											   String dest_filename) {
		try {
			InputStream is = am.open(asset_filename);
			byte[] bs = new byte[1024];
			int len;
			OutputStream os = new FileOutputStream(dest_filename);
			while ((len = is.read(bs)) != -1)
				os.write(bs, 0, len);
			os.close();
			is.close();
			return true;
		} catch (Exception e) {
			A.error(e);
		}
		return false;
	}

	public static String mySimpleEncript(String s){
		StringBuilder sb = new StringBuilder(s.length());
		for (int i=s.length()-1; i>=0; i--)
			sb.append((char)(90+i+s.charAt(i)));
		return sb.toString();
	}

	public static String mySimpleDecript(String s){
		StringBuilder sb = new StringBuilder(s.length());
		for (int i=s.length()-1; i>=0; i--)
			sb.append((char)(s.charAt(i)-90-(s.length()-1-i)));
		return sb.toString();
	}

	public static boolean isNetworkConnecting(Activity act) {
		try {
			ConnectivityManager cm =(ConnectivityManager)act.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			return info==null? false : info.isConnected();
		} catch (Exception e) {
			A.error(e);
			return false;
		}
	}
	public static boolean isWiFiConnecting(Activity act) {
		try {
			ConnectivityManager cm =(ConnectivityManager)act.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = cm.getActiveNetworkInfo();
			if (info==null || !info.isConnected())
				return false;
			return info.getType() == ConnectivityManager.TYPE_WIFI;
		} catch (Exception e) {
			A.error(e);
		}
		return false;
	}

	/**
	 * 2:20 -> 2
	 */
	public static int getHours(long eclipsedTime){
		return (int) (eclipsedTime/60/60/1000);
	}
	/**
	 * 2:20 -> 2*60+20=140
	 */
	public static int getMinitues(long eclipsedTime){
		return (int) (eclipsedTime/60/1000);
	}
	/**
	 * 2:20 -> 20
	 */
	public static int getMinituesAfterHours(long eclipsedTime){
		return (int) ((eclipsedTime%(60*60*1000))/60/1000);
	}

	public static boolean recycle(Bitmap bm){
		if (bm!=null){
			bm.recycle();
			return true;
		}else
			return false;
	}

	public static boolean isRecycled(Bitmap bm){
		return bm==null || bm.isRecycled();
	}

	public static String deleteQuotes(String s) {
		s = s.trim();
		if (s.startsWith("'") || s.startsWith("\"") || s.startsWith("["))
			s = s.substring(1);
		if (s.endsWith("'") || s.endsWith("\"") || s.endsWith("]") || s.endsWith(":"))
			s = s.substring(0, s.length()-1);
		return s;
	}

	public static String getMinuteTag(int minute) {
		return (minute<10?"0":"")+minute;
	}

	public static String combineString(String s1, String s2) {
		if (s1.length()>0 && Character.getType(s1.charAt(s1.length()-1)) != Character.OTHER_LETTER)
			s1 += " ";
		return s1+s2;
	}

	public static String cleanString(String s){
		s = s.trim();
		if (s.endsWith(":"))
			s = s.substring(0, s.length()-1);
		return s;
	}

	public static String UrlEncode(String href) {
		String ALLOWED_URI_CHARS = "@#&=*+-_.,:!?()/~'%";
		href = Uri.encode(href, ALLOWED_URI_CHARS);
		return href;
	}

	public static String deleteHtmlComment(String html) {
		int start = 0;
		while (true){
			int i = html.indexOf("<!--", start);
			if (i != -1) {
				int j = html.indexOf("-->", i);
				if (j != -1) {
					html = html.substring(0, i) + html.substring(j+3, html.length());
					start = i;
				}else
					break;
			}else
				break;
		}
		return html;
	}

	public static Object getLast(Spanned text, Class kind) {
		Object[] objs = text.getSpans(0, text.length(), kind);
		if (objs.length == 0) {
			return null;
		} else {
			return objs[objs.length - 1];
		}
	}

	/**
	 * mergeHref("http://a.com/", "b.htm") = http://a.com/b.html
	 * mergeHref("http://a.com/abc/", "/b.htm") = http://a.com/b.html
	 * mergeHref("http://a.com", "/b.htm") = http://a.com/b.html
	 * mergeHref("http://a.com/aa/bb.asp", "/b.htm") = http://a.com/b.html
	 */
	public static String mergeHref(String baseUrl, String url) {
		try {
			if (url.startsWith("//")) //gutenberg error url "//www.gutenberg.org/ebooks/16328.epub.noimages"
				return "http://"+url.substring(2);
			if (!baseUrl.startsWith("http")) // a.com --> http://a.com
				baseUrl = "http://" + baseUrl;
			if (baseUrl.lastIndexOf("/") < 12) // http://a.com --> http://a.com/
				baseUrl = baseUrl + "/";

			if (url.startsWith("http"))
				return url;
			if (!url.startsWith("/")) {
				if (baseUrl.endsWith("/"))
					return baseUrl + url;
				else {
					int j = baseUrl.lastIndexOf("/");
					return baseUrl.substring(0, j + 1) + url;
				}
			} else {
				int i = baseUrl.indexOf(".");
				i = baseUrl.indexOf("/", i);
				return baseUrl.substring(0, i) + url;
			}
		} catch (Exception e) {
			A.error(e);
			return url;
		}
	}

	private static final String HTML_A_TAG_PATTERN = "(?i)<a([^>]+)>(.+?)</a>";
	private static final String HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";
	public static ArrayList<HtmlLink> getHtmlLinks(final String html) {
		ArrayList<HtmlLink> result = new ArrayList<>();
		Pattern patternTag = Pattern.compile(HTML_A_TAG_PATTERN);
		Pattern patternLink = Pattern.compile(HTML_A_HREF_TAG_PATTERN);
		Matcher matcherTag = patternTag.matcher(html);
		while (matcherTag.find()) {
			String href = matcherTag.group(1);
			String linkText = matcherTag.group(2);
			Matcher matcherLink = patternLink.matcher(href);
			while (matcherLink.find()) {
				String url = matcherLink.group(1);
				url = deleteQuotes(url);
				if (!isNull(url)) {
					HtmlLink link = new HtmlLink();
					link.url = url;
					link.title = linkText;
					result.add(link);
				}
			}
		}
		return result;
	}

    public static ArrayList<String> getFolderFileList(String book_cache, boolean b, boolean b1, boolean b2, boolean b3) {
		return  new ArrayList<String>();
    }

    public static OutputStream getFileOutputStream(String name) throws FileNotFoundException {
		return new FileOutputStream(name);
    }

    public static class HtmlLink {
		String url;
		String title;
	}

	private static boolean disabledUriExpose;
	public static void disableUriExpose(){
		if(Build.VERSION.SDK_INT>=24 && !disabledUriExpose){
			try{
				disabledUriExpose = true;
				Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
				m.invoke(null);
			}catch(Exception e){
				A.error(e);
			}
		}
	}

    //bug: 当前日期要先加TimeZone.getDefault().getRawOffset()再减去，否则，在北京时间8:00前计算就会得到前一天而不是当日。
	public static long getTodayNumber_error() {
		return System.currentTimeMillis() / day(1);
	}

	public static long getTodayNumber() {
		return (getTodayStartMills() + TimeZone.getDefault().getRawOffset()) / day(1);
	}

	public static long getTodayStartMills() {
		long day = day(1);
		int rawOffset = TimeZone.getDefault().getRawOffset();
		return ((System.currentTimeMillis() + rawOffset) / day * day) - rawOffset;
	}

	public static long minute(long minute) {
		return minute * 60 * 1000;
	}

	public static long hour(long hour) {
		return hour * 60 * 60 * 1000;
	}

	public static long day(long day) { //todo: day必须是long类型, 如果是int类型, day(30)就会整型溢出小于day(1)
		return day * 24 * 60 * 60 * 1000;
	}

	public static boolean appInstalled(Context context, String packageName) {
		try{
			ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, 0);
			return true;
		} catch( PackageManager.NameNotFoundException e ){
			return false;
		}
	}

}
