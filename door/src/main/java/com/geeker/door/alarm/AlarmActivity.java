package com.geeker.door.alarm;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.os.Vibrator;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.fima.glowpadview.GlowPadView;
import com.fima.glowpadview.GlowPadView.OnTriggerListener;
import com.geeker.door.R;
import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.ReminderType;

public class AlarmActivity extends Activity implements OnTriggerListener, SensorEventListener{
	public static boolean isRunning;
	private GlowPadView mGlowPadView;
	MediaPlayer mediaPlayer;
	WakeLock mWakelock;
	Vibrator mVibrator;
	SensorManager mSensorManager;  
	TextView timeText;
	TextView labelText;
	TextView restTimesText;
	Handler handler;
	int shakeNum;
	DbManager dbManager;
	int volume;
	String ringURI;
	int requestcode;
	boolean isShake;
	int[] settings;
	String tagString;
	int originVolume=1;
	AlarmService alarmService;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 把闹钟的View设置为WindowManager置顶
		// 三星 htc主流机型测试通过
		// 有效的越过了手势锁屏
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		dbManager=new DbManager(getApplicationContext());
		initSettings();
		if(isShake){
			setContentView(R.layout.alarm_shake);
			restTimesText=(TextView)findViewById(R.id.rest_times_text);
			initSensor();
		}else{
			setContentView(R.layout.alarm);
			mGlowPadView = (GlowPadView) findViewById(R.id.glow_pad_view);
		}
		timeText=(TextView)findViewById(R.id.time_title);
		labelText=(TextView)findViewById(R.id.alarm_label);
		handler=new Handler();
		alarmService=new AlarmService(this);
		dbManager=new DbManager(this);
		isRunning=true;
		initText();
		// 播放声音啊
		setSystemVolumeMax(this);
		mediaPlayer = new MediaPlayer();
		// 设置音量。据说范围在0.0-1.0之间
		try {
			mediaPlayer.reset();// 媒体对象重置
			mediaPlayer.setDataSource(this,Uri.parse(ringURI));// 设置媒体资源
			mediaPlayer.prepare();
		} catch (Exception e) {
			e.printStackTrace();
		}
		mediaPlayer.setLooping(true);
		mediaPlayer.setVolume(1.0f, 1.0f);
		mediaPlayer.start();
		// 点亮屏幕：
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
				| PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");
		mWakelock.acquire();
		// 点亮结束
		//震动拉
		mVibrator = (Vibrator)getApplication().getSystemService(Context.VIBRATOR_SERVICE);//获取振动器
		if(settings!=null && settings[2]!=0){
			mVibrator.vibrate(new long[] { 100, 100, 100, 1000 },0);
		}
		if(mGlowPadView!=null){
			mGlowPadView.setOnTriggerListener(this);
			mGlowPadView.setShowTargetsOnIdle(true);
		}
		if(settings!=null && settings[0]==0){
			new FinishAlarmTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,String.valueOf(dbManager.getEventID(EventVO.TYPE_ALARM, requestcode)));
			dbManager.deleteClock(requestcode);
		}
	}

	private void initSettings() {
		requestcode=getIntent().getIntExtra("requestcode", -1);
		if(requestcode<10){return;}
		ClockVO clockVO=dbManager.getClock(requestcode);
		if(clockVO.getCancleType()==DbManager.CANCELTYPE_SHAKE){
			isShake=true;
		}
		ReminderType type=clockVO.getType();
		ringURI=type.getRing();
		volume=type.getVolume();
		tagString=type.getTag();
		settings=type.getReminder();
		
	}

	private void initText() {
		Calendar c=Calendar.getInstance();
		timeText.setText(c.get(Calendar.HOUR_OF_DAY)+" : "+c.get(Calendar.MINUTE));
		labelText.setText(tagString);
	}
	
	private void initSensor() {
		//加速度传感器  
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	      mSensorManager.registerListener(this,   
	      mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   
	      //还有SENSOR_DELAY_UI、SENSOR_DELAY_FASTEST、SENSOR_DELAY_GAME等，  
	      //根据不同应用，需要的反应速率不同，具体根据实际情况设定  
	      SensorManager.SENSOR_DELAY_NORMAL); 
	}



	/**
	 * 此方法可以将系统的媒体音量调到最大
	 * 
	 * @param context
	 */
	public void setSystemVolumeMax(Context context) {
		AudioManager am = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
		// 最后一个参数传0，为了不让系统音量进度条显示，如果要显示系统音量进度条则传AudioManager.STREAM_MUSIC
		int vol=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		if(volume!=0){vol=volume;}
		originVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,vol, 0);
		
	}

	@Override
	protected void onDestroy() {
		// 释放并关闭，经证实，这里不能写成进程，在生命周期里写进程会产生若干未知的问题
		mVibrator.cancel();
		if (mediaPlayer.isPlaying()) {
			mediaPlayer.stop();
			mediaPlayer.release();
		}
		if (mWakelock.isHeld()) {
			mWakelock.release();
		}
		//强力唤醒：
		if(settings!=null && settings[0]!=0){
			Intent intent=new Intent(this,ForceAwakeService.class);
			intent.putExtra("requestcode", requestcode);
			startService(intent);
		}
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,originVolume, 0);
		isRunning=false;
		System.out.println("destroy");
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		System.out.println("onrestart");
		super.onRestart();
	}

	@Override
	protected void onPause() {
		System.out.println("onpause");
		super.onPause();
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return true;
	}

	@Override
	public void onGrabbed(View v, int handle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onReleased(View v, int handle) {
		mGlowPadView.ping();
		
	}

	@Override
	public void onTrigger(View v, int target) {
		final int resId = mGlowPadView.getResourceIdForTarget(target);
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(),	AlarmReceiver.class);
		intent.putExtra("requestcode", 2);
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), 2, intent,PendingIntent.FLAG_CANCEL_CURRENT);
		switch (resId) {
		case R.drawable.ic_item_5min:
			am.set(AlarmManager.RTC_WAKEUP,Calendar.getInstance().getTimeInMillis()+5*60*1000, sender);
			Toast.makeText(this, "5分钟后再次响铃", Toast.LENGTH_SHORT).show();
			break;
		case R.drawable.ic_item_10min:
			am.set(AlarmManager.RTC_WAKEUP,Calendar.getInstance().getTimeInMillis()+10*60*1000, sender);
			Toast.makeText(this, "10分钟后再次响铃", Toast.LENGTH_SHORT).show();
			break;
		case R.drawable.ic_item_15min:
			am.set(AlarmManager.RTC_WAKEUP,Calendar.getInstance().getTimeInMillis()+15*60*1000, sender);
			Toast.makeText(this, "15分钟后再次响铃", Toast.LENGTH_SHORT).show();
			break;
		case R.drawable.ic_item_unlock:
			break;
		}
		finish();
	}

	@Override
	public void onGrabbedStateChange(View v, int handle) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onFinishFinalAnimation() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		final float[] values = event.values;
		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			if ((Math.abs(values[0]) >= 15 || Math.abs(values[1]) >= 15 || Math
					.abs(values[2]) >= 15)) {
				shakeNum++;
				if (shakeNum >= 10) {
					finish();
				}
				handler.post(new Runnable() {
					@Override
					public void run() {
						restTimesText.setText(String.valueOf(10 - shakeNum));
					}
				});
			}
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	
	class FinishAlarmTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			return alarmService.finishAlarm(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}

}
