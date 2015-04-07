package com.tblin.market.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AngleUitl {
	
public static Drawable toRoundCorner(Drawable b, int pixels) {  
		BitmapDrawable bd=(BitmapDrawable)b;
		Bitmap bitmap=bd.getBitmap();
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
         
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
        final float roundPx = pixels;  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        BitmapDrawable dra = new BitmapDrawable(output);
        return dra;  
    }

}
