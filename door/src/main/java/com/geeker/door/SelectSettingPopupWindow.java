package com.geeker.door;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.geeker.door.database.DbManager;

public class SelectSettingPopupWindow extends PopupWindow {

	private View mMenuView;

	public SelectSettingPopupWindow(final Activity context) {
		super(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mMenuView = inflater.inflate(R.layout.actionbar_setting, null);
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
		LinearLayout setting=(LinearLayout)mMenuView.findViewById(R.id.setting);
		LinearLayout help=(LinearLayout)mMenuView.findViewById(R.id.help);
		LinearLayout exit=(LinearLayout)mMenuView.findViewById(R.id.exit);
		setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(context,SettingsActivity.class);
				context.startActivity(i);
				context.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				dismiss();
				/*Toast.makeText(context, "由于设计师罢工 =。= 尚未完成", Toast.LENGTH_SHORT).show();
				dismiss();*/
			}
		});
		help.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(context, AboutActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				context.startActivity(i);
				dismiss();
				/*Intent i=new Intent(context,ErrorActivity.class);
				context.startActivity(i);
				context.overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);
				dismiss();*/
			}
		});
		exit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(context)   
				.setTitle("Do.or")  
				.setMessage("确定退出吗？（退出后闹钟和锁屏会失效）")  
				.setPositiveButton("是", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent stopIntent = new Intent("com.geeker.door.CLOSE_DOOR_LOCK");
						context.sendBroadcast(stopIntent);
						new Thread(new Runnable() {
							
							@Override
							public void run() {
								try {
									Thread.sleep(400);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
								android.os.Process.killProcess(android.os.Process.myPid()) ;   
								System.exit(0);   
							}
						}).start();
					}
				})  
				.setNegativeButton("否", null)  
				.show();  
				
				dismiss();
			}
		});
	}
	
	
}
