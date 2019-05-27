package com.example.webdavtest1; //!

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class A {

	public static String book_cache = "/sdcard/cbz/cache";
    public static boolean crcError;


    public static Rect getStreamBitmapBounds(InputStream is, boolean fitScreen, boolean keepStream) {
		try {
			if (!keepStream || is.markSupported()) {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;
				if (keepStream)
					is.mark(is.available());
				BitmapFactory.decodeStream(is, null, opts);
				if (keepStream)
					is.reset();
				int scale = !fitScreen? 1 : computeSampleSize(opts, A.NORMAL_SIZE, 0);
				return new Rect(0, 0, opts.outWidth/scale, opts.outHeight/scale);
			} else {
				byte[] bytes = T.InputStream2Byte(is);
				if (bytes != null)
					return getBytesBitmapBounds(bytes);
			}
		} catch (OutOfMemoryError e) {
		} catch (Exception e) {
			A.error(e);
		}
		return null;
	}

	public static Rect getBytesBitmapBounds(byte[] bytes) {
		try {
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeByteArray(bytes, 0, bytes.length, opts);
			int scale = computeSampleSize(opts, A.NORMAL_SIZE, 0);
			return new Rect(0, 0, opts.outWidth/scale, opts.outHeight/scale);
		} catch (OutOfMemoryError e) {
		} catch (Exception e) {
			A.error(e);
		}
		return null;
	}

	public static Rect getFileBitmapBounds(String imageFile, boolean fitScreen) {
		try {
			ParcelFileDescriptor pfd = getContext().getContentResolver()
					.openFileDescriptor(Uri.fromFile(new File(imageFile)), "r");
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(pfd.getFileDescriptor(), null, opts);
			int scale = !fitScreen? 1 : computeSampleSize(opts, A.NORMAL_SIZE, 0);
			return new Rect(0, 0, opts.outWidth/scale, opts.outHeight/scale);
		} catch (OutOfMemoryError e) {
		} catch (Exception e) {
			A.error(e);
		}
		return null;
	}

	public static boolean isLandscape() {
		return getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
	}

	public static String xml_files_folder;
	public static long logTime, priorLogTime;
	public static boolean mainNightTheme=false;
	public static Application application;
	public static boolean newVersionRun;

	public static void setContext(Context context) {
		mContext = context;
		xml_files_folder = context.getApplicationInfo().dataDir + "/shared_prefs";
	}

	public static void log(Object... o) {
		String info = getLogInfo(o);
		if (logTime > 0) {
			if (priorLogTime < logTime)
				priorLogTime = logTime;
			long now = SystemClock.elapsedRealtime();
			info = "[" + (now - logTime) + "," + (now - priorLogTime) + "]" + info;
			priorLogTime = now;
		}
		Log.i("MR2", info);
	}

	private static String getLogInfo(Object[] items) {
		if (items.length == 1 && (items[0] instanceof String))
			return (String) items[0];

		StringBuilder sb = new StringBuilder();
		for (Object it : items) {
			if (it == null)
				sb.append("@null");
			else
				sb.append(it);
			if (items.length > 0)
				sb.append(" | ");
		}
		return sb.toString();
	}


	public static boolean tmpOutOfMemoryTag;
	public static String lastErrorInfo="";
	public static String savedUriStr = "content://com.android.externalstorage.documents/tree/external_SD%3A";

	public static boolean cbz_smooth=true;
	public static void error(Throwable e) {
		try {
			if (e instanceof OutOfMemoryError) {
				tmpOutOfMemoryTag = true;
				A.log("####OutOfMemoryError####-----------------------------------");
				saveMemoryLog("");
			} else {
				A.log("####ERROR####-----------------------------------");
				lastErrorInfo = errorMsg(e);
				A.log(lastErrorInfo + "##");
				e.printStackTrace();
			}
		} catch (Throwable e2) {
			e2.printStackTrace();
		}
	}
	public static String errorMsg(Throwable e) {
		String err = e.getMessage();
		if (err == null)
			err = e.toString();
		return err;
	}
	public static boolean isImageFileExt(String ext) {
		return ext.equals(".png") || ext.equals(".jpg") ||  ext.equals(".jpeg") || ext.equals(".gif")
				|| ext.equals(".webp") || ext.equals(".svg");
	}

	public static boolean isLollipopPermissionError(){
		return lastErrorInfo!=null && lastErrorInfo.indexOf("Permission denied")!=-1;
	}
	public static boolean isLollipopExtSdcardFile(String filename){
		if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
		try {
			return Environment.isExternalStorageRemovable(new File(filename));
		} catch (Exception e) {
			A.error(e);
			return false;
		}
		return false;
	}
	
	public static String saveMemoryLog(String info) {
		return saveMemoryLog(info, true);
	}

	public static String saveMemoryLog(String info, boolean logIt) {
		long maxMemory = Runtime.getRuntime().maxMemory();
		long usedMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		String s = info + formateSize(maxMemory) + ", " + formateSize(usedMemory) + ", "
				+ formateSize(freeMemory) + " - " + (formateSize(usedMemory - freeMemory));
		if (logIt)
			log(s);
		return s;
	}

	public static Context mContext;

	public static String formateSize(long size) {
		if (mContext != null)
			return Formatter.formatFileSize(mContext, size);
		else
			return null;
	}
	public static int d(int i) {
		return (int) (i * A.getDensity());
	}
	public static float df(float i) {
		return (i * A.getDensity());
	}
	public static float getDensity() { //i5700:1 i9100:1.5 Galaxy10.1:1.0 GalaxyNote:2.0 Nexus 7:1.33
		return mContext.getResources().getDisplayMetrics().density;
	}
	
	public static Context getContext() {
		return mContext;
	}


	public static Bitmap getFileBitmap(Context context, File file, int destSize, int quality) {
		if (file.isFile()) {
			try {
				BitmapFactory.Options opts = new BitmapFactory.Options();
				ParcelFileDescriptor pfd;
				try {
					pfd = context.getContentResolver().openFileDescriptor(Uri.fromFile(file), "r");
				} catch (Exception ex) {
					return null;
				}
				java.io.FileDescriptor fd = pfd.getFileDescriptor();
				opts.inJustDecodeBounds = true;
				BitmapFactory.decodeFileDescriptor(fd, null, opts);
				opts.inSampleSize = computeSampleSize(opts, destSize, quality);
				opts.inJustDecodeBounds = false;
				return BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
			} catch (OutOfMemoryError e) {
				System.gc();
			} catch (Exception e) {
				A.error(e);
			}
		}
		return null;
	}
	public static final int BIG_SIZE = -1;
	public static final int NORMAL_SIZE = 0;
	public static final int SMALL_SIZE = 1;
	public static final int VERY_SMALL_SIZE = 2;

	public static final int NORMAL_QUALITY = 0;
	public static final int LOW_QUALITY = 1;

	public static int computeSampleSize(BitmapFactory.Options opts, int destSize, int quality) {
		int size;
		if (destSize > 0) { //small or very small
			int target = (destSize == VERY_SMALL_SIZE)? d(60) : d(A.d(80));
			if (quality > 0)
				target = target * 2 / (2 + quality);
			size = opts.outWidth / target;
		} else { //normal or high
			int target = getContext().getResources().getDisplayMetrics().widthPixels;
			if (quality > 0)
				target = target * 2 / (2 + quality);
			size = opts.outWidth / target;
			if (destSize == BIG_SIZE)
				size -= 1;
		}
		return size > 0 ? size : 1;
	}


	public static void setSystemUiVisibility(View view, boolean hide) {
		if (view == null)
			return;
		try {
			if (!hide)
				view.setSystemUiVisibility(0);
			else {
				int flags = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
						| View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
						| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
						| View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
						| View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
						| View.SYSTEM_UI_FLAG_IMMERSIVE;
				if (Build.VERSION.SDK_INT >= 23)
//					flags = (flags | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
					flags = (flags & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
				view.setSystemUiVisibility(flags);
			}
		} catch (Throwable e) {
			A.error(e);
		}
	}

	public static boolean isCutout(){
		return false;
	}

	public static void fillCutout(Activity act) {
		if (Build.VERSION.SDK_INT >= 28) {
			WindowManager.LayoutParams lp = act.getWindow().getAttributes();
//			lp.layoutInDisplayCutoutMode =  WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
			try {
				Field field = WindowManager.LayoutParams.class.getDeclaredField("layoutInDisplayCutoutMode");
				field.setAccessible(true);
				field.set(lp, 1);
				act.getWindow().setAttributes(lp);
			} catch (Exception e) {
				A.error(e);
			}
		}
	}

	public static boolean isCutoutScreen(Activity act) {
		if (Build.VERSION.SDK_INT >= 28)
			try {
				WindowInsets wi = act.getWindow().getDecorView().getRootWindowInsets();
				Method m = WindowInsets.class.getMethod("getDisplayCutout");
				Object o = m.invoke(wi);
				A.log("cutout screen: " + o);
				return o != null;
			} catch (Exception e) {
				A.error(e);
			}
		return false;
	}


}
