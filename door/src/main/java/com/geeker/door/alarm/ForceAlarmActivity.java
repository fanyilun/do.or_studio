package com.geeker.door.alarm;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.geeker.door.R;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.ReminderType;


public class ForceAlarmActivity extends Activity{

	private MediaPlayer mp;
	private TextView time;
	private TextView tag;
	private DbManager dbManager;
	int originVolume=1;
	WakeLock mWakelock;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		setContentView(R.layout.alarm);
		// 点亮屏幕：
				PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
				mWakelock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP
						| PowerManager.SCREEN_DIM_WAKE_LOCK, "SimpleTimer");
				mWakelock.acquire();
		TextView hintText=(TextView)findViewById(R.id.hint_text);
		hintText.setVisibility(View.VISIBLE);
		startMusic();
		dbManager=new DbManager(this);
		initButton();
	}

	private void startMusic() {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//具体音量数待定
		originVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,am.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
		try {
			String ringURI=RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM).toString();
			mp=MediaPlayer.create(this,Uri.parse(ringURI));
		} catch (Exception e) {
			mp=MediaPlayer.create(this,R.raw.fallbackring);
		}
		 mp.setLooping(true);
		 mp.start(); 
	}

	private void initButton() {
		View bottom=findViewById(R.id.glow_pad_view);
		bottom.setVisibility(View.GONE);
		View background=findViewById(R.id.alarm_bg);
		background.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		time=(TextView)findViewById(R.id.time_title);
		tag=(TextView)findViewById(R.id.alarm_label);
		tag.setText(getIntent().getStringExtra("waker")+"正在叫你");
	}
	
	@Override
	protected void onStart() {
		SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
		String tim=timeformat.format(new Date());
		time.setText(tim);
		super.onStart();
	}
	
	@Override
	protected void onDestroy() {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		am.setStreamVolume(AudioManager.STREAM_MUSIC,originVolume, 0);
		super.onDestroy();
		mp.stop();
	}

}
