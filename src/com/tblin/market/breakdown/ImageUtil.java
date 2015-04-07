package com.tblin.market.breakdown;

import java.io.ByteArrayOutputStream;

import com.tblin.embedmarket.MyLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * 这个类主要负责图存入数据库和取出时的数据转换工作 主要使用byteToDrawable和drawableToByte这两个方法
 */

public class ImageUtil {

	private static final String TAG = ImageUtil.class.toString();

	/**
	 * 从数据库中取出二进制数据，利用此方法转换为图片
	 */
	public static Drawable byteToDrawable(byte[] b) {
		if (b == null || b.length == 0) {
			return null;
		}
		// MyLog.i(TAG, "byteToDrawable?" + (true));
		return bitmapToDrawable(Bytes2Bimap(b));
	}

	/**
	 * 在将图片存入数据库中之前，先用此方法将图片转换为二进制数据
	 */
	public static byte[] drawableToByte(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		// MyLog.i(TAG, "drawableToByte?" + (true));
		return Bitmap2Bytes(drawableToBitmap(drawable));
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		if (drawable == null) {
			return null;
		}
		Bitmap bitmap = Bitmap
				.createBitmap(
						drawable.getIntrinsicWidth(),
						drawable.getIntrinsicHeight(),
						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
								: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		// MyLog.i(TAG, "drawableToBitmap?" + (bitmap != null));
		return bitmap;
	}

	public static Drawable bitmapToDrawable(Bitmap bmp) {
		if (bmp == null) {
			return null;
		}
		// MyLog.i(TAG, "bitmapToDrawable?" + (bmp != null));
		return new BitmapDrawable(bmp);
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		if (bm == null) {
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		// MyLog.i(TAG, "Bitmap2Bytes?" + (baos != null));
		return baos.toByteArray();
	}

	public static Bitmap Bytes2Bimap(byte[] b) {
		if (b != null && b.length != 0) {
			MyLog.i(TAG, "bytes.length:" + b.length);
			return BitmapFactory.decodeByteArray(b, 0, b.length);
		} else {
			return null;
		}
	}

}
