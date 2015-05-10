package com.geeker.door.utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class MyViewPager extends ViewPager{


	//镙囱瘑
	private int abc = 1;
	private float mLastMotionX;
	private float mLastMotionY;
	private float xDistance,yDistance;

	public MyViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		final float x = ev.getX();
		final float y = ev.getY();
		/*switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			xDistance = 0;
			yDistance = 0;
			//down浜嬩欢娉ㄥ唽涓嶆嫤鎴?
			abc = 1;
			//down涓嬫潵镄刹 y鍧愭爣
			mLastMotionX = x;
			mLastMotionY = y;
			break;
		case MotionEvent.ACTION_MOVE:
			xDistance += Math.abs(x - mLastMotionX);
			yDistance += Math.abs(y - mLastMotionY);
			if(abc==2){return true;}
			if(abc==3){return false;}
			if(xDistance>50){
				System.out.println("return ttrruuee");
				abc=2;
				return true;
			}
			if(yDistance>50){
				System.out.println("return false");
				abc=3;
				return false;
			}

			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_CANCEL:
			xDistance = 0;
			yDistance = 0;
			abc=1;
			break;
		}*/
		return super.onInterceptTouchEvent(ev);
	}


}
