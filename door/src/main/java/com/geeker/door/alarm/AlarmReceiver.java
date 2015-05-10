package com.geeker.door.alarm;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceiver extends BroadcastReceiver{
	
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		//关闭背景音乐
		Intent stopIntent = new Intent("com.example.clockdemo.STOP_PRE_ALARM");
		arg0.sendBroadcast(stopIntent);
		   Intent i=new Intent(arg0,AlarmActivity.class);
		   i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		   i.putExtras(arg1);
		   arg0.startActivity(i);
	}

}
