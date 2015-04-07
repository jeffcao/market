package com.tblin.market.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

import com.tblin.embedmarket.R;

public class TextProgressBar extends ProgressBar {

	private Context mContext;

	public TextProgressBar(Context context) {
		super(context);
		init(context);
	}

	public TextProgressBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public TextProgressBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		mContext = context;
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Paint p = new Paint();
		p.setAntiAlias(true);
		String text = (getMax() != 0 ? getProgress() * 100 / getMax(): 0) + "%";
		p.setColor(mContext.getResources().getColor(R.color.black));
		float textSize = (getBottom() - getTop()) * 0.8f;
		p.setTextSize(textSize);
		float y = (getBottom() - getTop()) * 0.8f;
		float x = (getRight() - textSize * (text.length() + 1) / 2) / 2;
		canvas.drawText(text, x, y, p);
	}

}
