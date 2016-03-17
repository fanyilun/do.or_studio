package com.geeker.door;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

public class SelectAddPopupWindow extends PopupWindow {

	private View mMenuView;

	public SelectAddPopupWindow(final Activity context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.actionbar_add, null);
		
		int h = context.getWindowManager().getDefaultDisplay().getHeight();
		int w = context.getWindowManager().getDefaultDisplay().getWidth();
		//设置按钮监听
		//设置SelectPicPopupWindow的View
		this.setContentView(mMenuView);
		//设置SelectPicPopupWindow弹出窗体的宽
		this.setWidth(w/2+50);
		//设置SelectPicPopupWindow弹出窗体的高
		this.setHeight(LayoutParams.WRAP_CONTENT);
		//设置SelectPicPopupWindow弹出窗体可点击
		this.setFocusable(true);
		//设置SelectPicPopupWindow弹出窗体动画效果
		this.setAnimationStyle(R.style.mystyle);
		//实例化一个ColorDrawable颜色为半透明
		ColorDrawable dw = new ColorDrawable(0000000000);
		//设置SelectPicPopupWindow弹出窗体的背景
		this.setBackgroundDrawable(dw);
		//mMenuView添加OnTouchListener监听判断获取触屏位置如果在选择框外面则销毁弹出框
		mMenuView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				
				int height = mMenuView.findViewById(R.id.popview_add).getTop();
				int y=(int) event.getY();
				if(event.getAction()==MotionEvent.ACTION_UP){
					if(y<height){
						dismiss();
					}
				}				
				return true;
			}
		});
		LinearLayout clock=(LinearLayout)mMenuView.findViewById(R.id.clock);
		LinearLayout schedule=(LinearLayout)mMenuView.findViewById(R.id.schedule);
//		LinearLayout request=(LinearLayout)mMenuView.findViewById(R.id.request);
		clock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(context,AddAlarmActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(i);
				context.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				dismiss();
			}
		});
		schedule.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(context,AddScheduleActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(i);
				context.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				dismiss();
			}
		});
//		request.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent i=new Intent(context,AddRequestActivity.class);
//				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
//				context.startActivity(i);
//				context.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
//				dismiss();
//			}
//		});
	}
}
