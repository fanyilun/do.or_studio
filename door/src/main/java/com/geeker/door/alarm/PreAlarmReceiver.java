package com.geeker.door.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PreAlarmReceiver extends BroadcastReceiver{

	@Override
	public void onReceive(Context context, Intent intent) {
		 Intent i=new Intent(context,PreAlarmActivity.class);
		 i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		 i.putExtras(intent);
		 context.startActivity(i);
	}

	

}
