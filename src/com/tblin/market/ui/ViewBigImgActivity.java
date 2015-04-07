package com.tblin.market.ui;

import java.util.List;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import com.tblin.embedmarket.R;
import com.tblin.embedmarket.SessionManager;

public class ViewBigImgActivity extends Activity {
	private ImageView im;
	private SessionManager session;
	private List<Drawable> list;
	private GestureDetector mygesture;
	private int k;
	private int imgsize;

	@SuppressWarnings("unchecked")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_big_img);
		im = (ImageView) findViewById(R.id.viewbigimg);
		session = SessionManager.getInstance(this);
		mygesture = new GestureDetector(this, new myGesture());
		list = (List<Drawable>) session.get("imgs");
		imgsize = list.size();
		k = getIntent().getIntExtra("viewNum", -1);
		im.setImageDrawable(list.get(k));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mygesture.onTouchEvent(event);
	}

	public void goinfo(View view) {
		finish();
	}

	public void toLeft() {
		if (k == 0) {
		} else {
			im.setImageDrawable(list.get(k - 1));
			k--;
		}
	}

	public void toRight() {
		if (k < (imgsize - 1)) {
			im.setImageDrawable(list.get(k + 1));
			k++;
		}
	}

	class myGesture extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (e1.getX() - e2.getX() > 20) {
				toRight();
			} else if (e2.getX() - e1.getX() > 20) {
				toLeft();
			}
			return super.onFling(e1, e2, velocityX, velocityY);
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			finish();
			return super.onSingleTapConfirmed(e);
		}
	}

}
