package com.geeker.door.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Scroller;

/**
 * SlidingLayout 璧炰笉鏀寔padding锛宲ading鍙互鍐椤湪鍏朵粬镄刲ayout閲岄溃銆?
 * 
 * @author 1000chi
 * 
 */
public class SlidingLayout extends ViewGroup {

	private Scroller mScroller;

	private int scrollOffset;

	private int heightSpec = 0;
	private int widthSpec = 0;

	public SlidingLayout(Context context) {
		super(context);
		init();
	}

	public SlidingLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		mScroller = new Scroller(getContext());
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int count = getChildCount();
		int leftOffset = 0;
		for (int i = 0; i < count; i++) {
			View child = getChildAt(i);
			int childTop = getPaddingTop();
			child.layout(leftOffset, childTop,
					leftOffset + child.getMeasuredWidth(),
					childTop + child.getMeasuredHeight());
			leftOffset += child.getMeasuredWidth();
		}
		scrollOffset = leftOffset - getWidth();

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		// 灏濊瘯铡荤敾鍑笃iew镄勫ぇ灏忥紝铹跺悗绛夊緟瀛恦iew鐢诲嚭鏉ュ氨鐭ラ亾 wrap_content搴旇鐢诲澶т简銆?
		heightSpec = 0;
		setMeasuredDimension(getDefaultSize(400, widthMeasureSpec),
				getDefaultSize(400, heightMeasureSpec));

		final int size = getChildCount();
		for (int i = 0; i < size; ++i) {
			final View child = getChildAt(i);
			measureChildItem(child, widthMeasureSpec, heightMeasureSpec);
		}

		setMeasuredDimension(getDefaultSize(widthSpec, widthMeasureSpec),
				getDefaultSize(heightSpec, heightMeasureSpec));

	}

	private void measureChildItem(View child, int parentWidthMeasureSpec,
			int parentHeightMeasureSpec) {
		ViewGroup.LayoutParams lp = child.getLayoutParams();
		if (lp == null) {
			return;
		}
		int childWidthMeasureSpec;
		int childHeightMeasureSpec;
		childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(getMeasuredWidth(),
				lp.width == LayoutParams.FILL_PARENT ? MeasureSpec.EXACTLY
						: MeasureSpec.AT_MOST);
		childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
				getMeasuredHeight(),
				lp.height == LayoutParams.FILL_PARENT ? MeasureSpec.EXACTLY
						: MeasureSpec.AT_MOST);
		child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
		heightSpec = Math.max(heightSpec, child.getMeasuredHeight());
		widthSpec = Math.max(widthSpec, child.getMeasuredWidth());
	}

	@Override
	public void computeScroll() {
		if (!mScroller.isFinished()) {
			if (mScroller.computeScrollOffset()) {
				int oldX = getScrollX();
				int oldY = getScrollY();
				int x = mScroller.getCurrX();
				int y = mScroller.getCurrY();
				if (oldX != x || oldY != y) {
					scrollTo(x, y);
				}
				invalidate();
			}
		}
	}

	@Override
	public void scrollTo(int x, int y) {
		super.scrollTo(x, y);
		postInvalidate();
	}

	public void setDefault() {
		mScroller.abortAnimation();
		scrollTo(0, 0);
	}

	public boolean isOpened() {
		return getScrollX() > 0;
	}

	public void open() {
		if (getScrollX() <= 0) {
			smoothScrollTo(scrollOffset);
		}
	}

	public void close() {
		if (getScrollX() > 0) {
			smoothScrollTo(-scrollOffset);
		}
	}

	public boolean toggle() {
		mScroller.abortAnimation();
		int oldScrollX = getScrollX();
		if (oldScrollX > 0) {
			smoothScrollTo(-oldScrollX);
			return false;
		} else {
			smoothScrollTo(scrollOffset);
			return true;
		}
	}

	void smoothScrollTo(int dx) {
		int duration = 1000;
		int oldScrollX = getScrollX();
		mScroller.startScroll(oldScrollX, getScrollY(), dx, getScrollY(),
				duration);
		invalidate();
	}

}
