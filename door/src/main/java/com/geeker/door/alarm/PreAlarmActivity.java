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
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.geeker.door.R;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.ReminderType;


public class PreAlarmActivity extends Activity{

	//使用广播实现关闭
	private MediaPlayer mp;
	private TextView time;
	private TextView tag;
	private DbManager dbManager;
	private BroadcastReceiver receiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			finish();
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.alarm);
		TextView hintText=(TextView)findViewById(R.id.hint_text);
		hintText.setVisibility(View.VISIBLE);
		startMusic();
		dbManager=new DbManager(this);
		initButton();
		//注册关闭receiver
		registerReceiver(receiver, new IntentFilter("com.example.clockdemo.STOP_PRE_ALARM"));
	}

	private void startMusic() {
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		//具体音量数待定
		am.setStreamVolume(AudioManager.STREAM_MUSIC,3, 0);
		 mp=MediaPlayer.create(this,R.raw.summer);
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
		int requestcode=getIntent().getIntExtra("requestcode", -1);
		if(requestcode<10){return;}
		ClockVO clockVO=dbManager.getClock(requestcode);
		ReminderType type=clockVO.getType();
		tag.setText("提前唤醒中");
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
		super.onDestroy();
		mp.stop();
		unregisterReceiver(receiver);
	}

}
