package com.tblin.market.ui;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.tblin.embedmarket.R;

/**
 * 继承弹窗，构造我们需要的弹窗
 */
public class QuickActions extends PopupWindow {

	private final View root;
	private final ImageView mArrowUp;
	private final ImageView mArrowDown;
	private final Animation mTrackAnim;
	private final LayoutInflater inflater;
	private final Context context;
	protected final View anchor;
	protected final PopupWindow window;
	private Drawable background = null;
	protected final WindowManager windowManager;
	protected static final int ANIM_GROW_FROM_LEFT = 1;
	protected static final int ANIM_GROW_FROM_RIGHT = 2;
	protected static final int ANIM_GROW_FROM_CENTER = 3;
	protected static final int ANIM_AUTO = 4;

	private int animStyle;
	private ViewGroup mTrack;
	private ArrayList<ActionItem> actionList;

	/**
	 * 构造器，在这里初始化一些内容
	 * 
	 * @param anchor
	 *            像我之前博客所说的理解成一个基准 弹窗以此为基准弹出
	 */
	public QuickActions(View anchor) {
		super(anchor);

		this.anchor = anchor;
		this.window = new PopupWindow(anchor.getContext());
		// 在popwindow外点击即关闭该window
		window.setTouchInterceptor(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_OUTSIDE) {
					QuickActions.this.window.dismiss();
					return true;
				}
				return false;
			}
		});

		// 得到一个windowManager对象，用来得到窗口的一些属性
		windowManager = (WindowManager) anchor.getContext().getSystemService(
				Context.WINDOW_SERVICE);

		actionList = new ArrayList<ActionItem>();
		context = anchor.getContext();
		inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		// 装载布局，root即为弹出窗口的布局
		root = (ViewGroup) inflater.inflate(R.layout.quickaction, null);

		// 得到上下两个箭头
		mArrowDown = (ImageView) root.findViewById(R.id.arrow_down);
		mArrowUp = (ImageView) root.findViewById(R.id.arrow_up);
		
		setContentView(root);

		mTrackAnim = AnimationUtils.loadAnimation(anchor.getContext(),
				R.anim.rail);

		// TODO
		// 设置动画的加速效果
		mTrackAnim.setInterpolator(new Interpolator() {

			public float getInterpolation(float t) {

				final float inner = (t * 1.55f) - 1.1f;

				return 1.2f - inner * inner;
			}
		});

		// 这个是弹出窗口内的水平布局
		mTrack = (ViewGroup) root.findViewById(R.id.tracks);
		animStyle = ANIM_GROW_FROM_CENTER;// 设置动画风格
	}

	/**
	 * 设置动画风格
	 */
	public void setAnimStyle(int animStyle) {
		this.animStyle = animStyle;
	}

	/**
	 * 增加一个action
	 */
	public void addActionItem(ActionItem action) {
		actionList.add(action);
	}

	/**
	 * 弹出弹窗
	 */
	public void show() {
		// 预处理，设置window
		preShow();

		int[] location = new int[2];
		// 得到anchor的位置
		anchor.getLocationOnScreen(location);

		// 以anchor的位置构造一个矩形
		Rect anchorRect = new Rect(location[0], location[1], location[0]
				+ anchor.getWidth(), location[1] + anchor.getHeight());

		root.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		root.measure(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

		int rootWidth = root.getMeasuredWidth();
		int rootHeight = root.getMeasuredHeight();

		// 得到屏幕的宽
		int screenWidth = windowManager.getDefaultDisplay().getWidth();
		int screenHeight = windowManager.getDefaultDisplay().getHeight();
		// 设置弹窗弹出的位置的x/y
		int xPos = (screenWidth - rootWidth) / 2;
		int yPos = anchorRect.bottom;

		boolean onTop = false;

		// 在底部弹出
		if (anchorRect.bottom + rootHeight > screenHeight) {
			yPos = anchorRect.top - rootHeight;
			onTop = true;
		}

		// 根据弹出位置，设置不同方向箭头图片
//		showArrow(((onTop) ? R.id.arrow_down : R.id.arrow_up),
//				anchorRect.centerX());

		// 设置弹出动画风格
		setAnimationStyle(screenWidth, anchorRect.centerX(), onTop);

		// 创建action list
		createActionList();

		// 在指定位置弹出弹窗
		window.showAtLocation(this.anchor, Gravity.NO_GRAVITY, xPos, yPos);

		/*// 设置弹窗内部的水平布局的动画
		if (animateTrack)
			mTrack.startAnimation(mTrackAnim);*/
	}

	@Override
	public void dismiss() {
		window.dismiss();
	}

	/**
	 * 预处理窗口
	 */
	protected void preShow() {
		if (root == null) {
			throw new IllegalStateException("需要为弹窗设置布局");
		}

		// 背景是唯一能确定popupwindow宽高的元素，这里使用root的背景，但是需要给popupwindow设置一个空的BitmapDrawable
		if (background == null) {
			window.setBackgroundDrawable(new BitmapDrawable());
		} else {
			window.setBackgroundDrawable(background);
		}

		window.setWidth(WindowManager.LayoutParams.FILL_PARENT);
		window.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
		window.setTouchable(true);
		window.setFocusable(true);
		window.setOutsideTouchable(true);
		// 指定布局
		window.setContentView(root);
	}

	/**
	 * 设置动画风格
	 * 
	 * @param screenWidth
	 *            屏幕宽底
	 * @param requestedX
	 *            距离屏幕左边的距离
	 * @param onTop
	 *            一个flag用来标识窗口的显示位置，如果为true则显示在anchor的顶部
	 */
	private void setAnimationStyle(int screenWidth, int requestedX,
			boolean onTop) {
		// 取得屏幕左边到箭头中心的位置
		int arrowPos = requestedX - mArrowUp.getMeasuredWidth() / 2;
		// 根据animStyle设置相应动画风格
		switch (animStyle) {
		case ANIM_GROW_FROM_LEFT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
					: R.style.Animations_PopDownMenu_Left);
			break;

		case ANIM_GROW_FROM_RIGHT:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Right
					: R.style.Animations_PopDownMenu_Right);
			break;

		case ANIM_GROW_FROM_CENTER:
			window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
					: R.style.Animations_PopDownMenu_Center);
			break;

		case ANIM_AUTO:
			if (arrowPos <= screenWidth / 4) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Left
						: R.style.Animations_PopDownMenu_Left);
			} else if (arrowPos > screenWidth / 4
					&& arrowPos < 3 * (screenWidth / 4)) {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopUpMenu_Center
						: R.style.Animations_PopDownMenu_Center);
			} else {
				window.setAnimationStyle((onTop) ? R.style.Animations_PopDownMenu_Right
						: R.style.Animations_PopDownMenu_Right);
			}

			break;
		}
	}

	/**
	 * 创建action list
	 */
	private void createActionList() {
		View view;
		String title;
		Drawable icon;
		OnClickListener listener;
		int index = 0;

		for (int i = 0; i < actionList.size(); i++) {
			title = actionList.get(i).getTitle();
			icon = actionList.get(i).getIcon();
			listener = actionList.get(i).getListener();
			// 得到action item
			view = getActionItem(title, icon, listener);

			view.setFocusable(true);
			view.setClickable(true);

			// 将其加入布局
			mTrack.addView(view, index);

			index++;
		}
	}

	/**
	 * 获得 action item
	 * 
	 * @param title
	 *            action的标题
	 * @param icon
	 *            action的图标
	 * @param listener
	 *            action的点击事件监听器
	 * @return action的item
	 */
	private View getActionItem(String title, Drawable icon,
			OnClickListener listener) {
		// 装载action布局
		LinearLayout container = (LinearLayout) inflater.inflate(
				R.layout.action_item, null);
		ImageView img = (ImageView) container.findViewById(R.id.icon);
		TextView text = (TextView) container.findViewById(R.id.title);

		if (icon != null) {
			img.setImageDrawable(icon);
		} else {
			img.setVisibility(View.GONE);
		}

		if (title != null) {
			text.setText(title);
		} else {
			text.setVisibility(View.GONE);
		}

		if (listener != null) {
			container.setOnClickListener(listener);
		}

		return container;
	}

	/**
	 * 显示箭头
	 * 
	 * @param 箭头资源id
	 * @param 距离屏幕左边的距离
	 */
	private void showArrow(int whichArrow, int requestedX) {
		final View showArrow = (whichArrow == R.id.arrow_up) ? mArrowUp
				: mArrowDown;
		final View hideArrow = (whichArrow == R.id.arrow_up) ? mArrowDown
				: mArrowUp;

		final int arrowWidth = mArrowUp.getMeasuredWidth();

		showArrow.setVisibility(View.VISIBLE);

		ViewGroup.MarginLayoutParams param = (ViewGroup.MarginLayoutParams) showArrow
				.getLayoutParams();

		// 以此设置距离左边的距离
		param.leftMargin = requestedX - arrowWidth / 2;

		hideArrow.setVisibility(View.INVISIBLE);
	}

}