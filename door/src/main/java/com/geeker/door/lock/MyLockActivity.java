package com.geeker.door.lock;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import qq.qq757225051.nongli.NongLi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.MemoVO;
import com.geeker.door.database.RequestVO;
import com.geeker.door.lock.HandlerView.OnViewSelectListener;
import com.geeker.door.utils.WaterWaveView;

/**
 * 自定义锁屏喽
 * @author Administrator
 *
 */
public class MyLockActivity extends Activity implements OnViewSelectListener,Runnable{

	public static boolean isShowing;
	long timeOffset;
	Handler viewHandler;
	TextView TimetextView;
	TextView DatetextView;
	TextView SMStextView;
	TextView weathertextView;
	TextView lunarText;
	HandlerView handler;
	ImageView node1;
	ImageView node2;
	View[] linears;
	ImageView[] lines;
	ImageView[] icons;
	TextView[] timeTexts;
	TextView[] labelTexts;
	DbManager dbManager;
	int itemNum;
	int offset;
	List<LockItem> list;
	private final BroadcastReceiver SMSreceiver = new BroadcastReceiver() {  
		@Override
		public void onReceive(final Context context, final Intent intent) {
			viewHandler.post(new Runnable() {
				@Override
				public void run() {
					SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
					String time=timeformat.format(new Date());
					SMStextView.setText(time);
				}
			});
			context.unregisterReceiver(this);
		}
	};
	private BroadcastReceiver closeReceiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			isShowing=false;
			finish();
		}
	};
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		System.out.println("oncreat");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().setType(2004);
		requestWindowFeature(Window.FEATURE_NO_TITLE);  
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lock);
		dbManager=new DbManager(this);
		viewHandler=new Handler();
		//initBg();
		new Thread(this).start();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.provider.Telephony.SMS_RECEIVED");
		filter.setPriority(1000);
		registerReceiver(SMSreceiver, filter);
		registerReceiver(closeReceiver, new IntentFilter("com.geeker.door.CLOSE_LOCK"));
	}

	

	private void initBg() {
		LinearLayout bgLinear=(LinearLayout)findViewById(R.id.linear_bg);
		WaterWaveView waterWaveView = new WaterWaveView(this);
		bgLinear.addView(waterWaveView);
	}



	private void initText() {
		TimetextView=(TextView)findViewById(R.id.time);
		DatetextView=(TextView)findViewById(R.id.date);
		lunarText=(TextView)findViewById(R.id.lunar_text);
		SMStextView=(TextView)findViewById(R.id.lock_time2);
		weathertextView=(TextView)findViewById(R.id.weather_text);
		int[] iconID=new int[]{R.id.lock_icon1,R.id.lock_icon2,R.id.lock_icon3,R.id.lock_icon4,R.id.lock_icon5,R.id.lock_icon6};
		int[] timeID=new int[]{R.id.lock_time1,R.id.lock_time2,R.id.lock_time3,R.id.lock_time4,R.id.lock_time5,R.id.lock_time6};
		int[] labelID=new int[]{R.id.lock_text1,R.id.lock_text2,R.id.lock_text3,R.id.lock_text4,R.id.lock_text5,R.id.lock_text6};
		icons=new ImageView[iconID.length];
		timeTexts=new TextView[timeID.length];
		labelTexts=new TextView[labelID.length];
		for (int i = 0; i < labelID.length; i++) {
			icons[i]=(ImageView)findViewById(iconID[i]);
			timeTexts[i]=(TextView)findViewById(timeID[i]);
			labelTexts[i]=(TextView)findViewById(labelID[i]);
		}
		//前两个用于短信和电话
		list=new ArrayList<LockItem>();
		//请求添加一个
		List<EventVO> requests=dbManager.getRequestByOccurTime();
		if(requests.size()!=0){
			Collections.shuffle(requests);
			RequestVO request=dbManager.getRequest(requests.get(0).getRequestCode());
			list.add(new LockItem(EventVO.TYPE_REQUEST, request.getTimeStringWithoutYear(), request.getTitle()));
		}
		//闹钟一个
		int clockNum=0;
		
		System.out.println(dbManager.getNextClock());
		if(dbManager.getNextClock()!=0){clockNum=1;}
		//剩下的都是备忘
		List<EventVO> schedules=dbManager.getMemoByOccurTime();
		if(schedules.size()!=0){
			Collections.shuffle(schedules);
			int num=Math.min(schedules.size(), 4-list.size()-clockNum);
			for (int i = 0; i < num; i++) {
				MemoVO memo=dbManager.getMemo(schedules.get(i).getRequestCode());
				if(memo.getOccurTime().after(Calendar.getInstance())){
					list.add(new LockItem(EventVO.TYPE_SCHEDULE, memo.getTimeStringWithoutYear(), memo.getTitle()));
				}else{
					list.add(new LockItem(EventVO.TYPE_SCHEDULE, memo.getTimeStringWithoutYear(), memo.getTitle(),true));
				}
			}
		}
		//闹钟在最后添加
		if(clockNum!=0){
			ClockVO clock=dbManager.getClock(dbManager.getNextClock());
			list.add(new LockItem(EventVO.TYPE_ALARM, clock.getTimeString(), clock.getType().getTag()));
		}
		dbManager.closeDB();
		viewHandler.post(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < list.size(); i++) {
					LockItem item = list.get(i);
					switch (item.type) {
					case EventVO.TYPE_ALARM:
						icons[i + 2].setImageDrawable(getResources().getDrawable(
								R.drawable.lock_icon_alarm));
						break;
					case EventVO.TYPE_SCHEDULE:
						icons[i + 2].setImageDrawable(getResources().getDrawable(
								R.drawable.lock_icon_schedule));
						break;
					case EventVO.TYPE_REQUEST:
						icons[i + 2].setImageDrawable(getResources().getDrawable(
								R.drawable.lock_icon_request));
						break;
					}
					timeTexts[i+2].setText(item.time);
					labelTexts[i+2].setText(item.label);
					if(item.red){
						timeTexts[i+2].setTextColor(getResources().getColor(R.color.red));
						labelTexts[i+2].setTextColor(getResources().getColor(R.color.red));
					}
				}
			}
		});
		
		itemNum=2+list.size();
	}


	private void initHandler() {
		int[] ids=new int[]{R.id.linear1,R.id.linear2,R.id.linear3,R.id.linear4,R.id.linear5,R.id.linear6};
		final int[] lineIDs=new int[]{R.id.imageView1,R.id.imageView2,R.id.imageView3};
		linears=new View[ids.length];
		lines=new ImageView[lineIDs.length];
		handler=(HandlerView)findViewById(R.id.handler);
		node1=(ImageView)findViewById(R.id.node1);
		node2=(ImageView)findViewById(R.id.node2);
		handler.addView(node1);
		handler.addView(node2);
		for (int i = 0; i < itemNum; i++) {
			linears[i]=findViewById(ids[i]);
			handler.addView(linears[i]);
		}
		viewHandler.post(new Runnable() {
			@Override
			public void run() {
				for (int i = 0; i < lineIDs.length; i++) {
					lines[i]=(ImageView)findViewById(lineIDs[i]);
					lines[i].setAlpha(0);
				}		
			}
		});
		handler.setViewSelectListener(this);
	}



	/**
	 * 使用重写onKeydown来屏蔽后退键。
	 * 此方法在4.0之后的系统里不能用于屏蔽Home键。屏蔽Home键需要覆盖系统Launcher
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}
	
	
	
	private void unlockSuccess() {
		finish();
	}

	@Override
	protected void onRestart() {
		System.out.println("onRestart");
		super.onRestart();
	}
	
	@Override
	protected void onStart() {
		isShowing = true;
		refreshTime();
		System.out.println("onstart");
		super.onStart();
	}


	@Override
	protected void onStop() {
		System.out.println("onstop");
		isShowing=false;
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
		if(pm.isScreenOn()){System.out.println("finish");isShowing=false;finish();}
		super.onStop();
	}
	
	@Override
	protected void onDestroy() {
		System.out.println("ondestroy");
		isShowing=false;
		try {
			unregisterReceiver(SMSreceiver);
			unregisterReceiver(closeReceiver);
		} catch (Exception e) {
		}
		super.onDestroy();
	}
	
	public void refreshTime() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
				final String time=timeformat.format(new Date());
				final String date=c.get(Calendar.YEAR)+"年"+(c.get(Calendar.MONTH)+1)+"月"+c.get(Calendar.DAY_OF_MONTH)+"日";
				SimpleDateFormat timeformat2=new SimpleDateFormat("yyyyMMdd");
				//String[] lunars=NongLi.getDate(timeformat2.format(new Date())).split("年");
				final String lunar = getDayOfWeek(c.get(Calendar.DAY_OF_WEEK));
				DbManager db=new DbManager(getApplicationContext());
				final String weather=db.getWeather();
				db.closeDB();
				viewHandler.post(new Runnable() {
					@Override
					public void run() {
						TimetextView.setText(time);
						DatetextView.setText(date);
						lunarText.setText(lunar);
						weathertextView.setText(weather);
						
					}
				});
			}
		}).start();
	}



	@Override
	public void viewSelect(int viewID) {
		if(viewID==2){
			//通话记录
			try {
				Intent intent=new Intent();
				intent.setAction(Intent.ACTION_CALL_BUTTON);
				startActivity(intent);  
			} catch (Exception e) {
			}
		}else if(viewID==3){
			try {
				PackageManager packageManager =this.getPackageManager();
				Intent intent=new Intent();
				intent =packageManager.getLaunchIntentForPackage("com.android.mms");
				startActivity(intent);
			} catch (Exception e) {
			}
		}else if(viewID>3){
			Intent i =new Intent(MyLockActivity.this,MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i); 
			//TODO 直接跳转到系统锁屏
			/*Intent intent = new Intent();
			ComponentName cm = new ComponentName("com.android.settings","com.android.settings.ChooseLockGeneric");
			intent.setComponent(cm);
			intent.setAction("android.intent.action.VIEW");
			startActivityForResult(intent,0);*/
		}
		isShowing=false;
		finish();
	}

	private static String getDayOfWeek(int i){
		switch (i) {
		case 1:
			return "周日";
		case 2:
			return "周一";
		case 3:
			return "周二";
		case 4:
			return "周三";
		case 5:
			return "周四";
		case 6:
			return "周五";
		case 7:
			return "周六";
		}
		return "";
	}


	@Override
	public void handleStart() {
		node1.setImageDrawable(getResources().getDrawable(R.drawable.lock_unlock));
		node2.setImageDrawable(getResources().getDrawable(R.drawable.lock_unlock));
		for (int i = 0; i < lines.length; i++) {
			lines[i].setAlpha(255);
		}
	}



	@Override
	public void handleEnd() {
		node1.setImageDrawable(null);
		node2.setImageDrawable(null);
		for (int i = 0; i < lines.length; i++) {
			lines[i].setAlpha(0);
		}
	}
	
	class LockItem{
		int type;
		String time;
		String label;
		boolean red;
		public LockItem(int type,String time,String label){
			this.type=type;
			this.time=time;
			this.label=label;
		}
		
		public LockItem(int type,String time,String label,boolean overdue){
			this.type=type;
			this.time=time;
			this.label=label;
			this.red=true;
		}
	}

	@Override
	public void run() {
		initText();
		initHandler();
	}
	
}
