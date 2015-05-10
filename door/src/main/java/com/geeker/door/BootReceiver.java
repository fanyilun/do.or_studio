package com.geeker.door;

import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.geeker.door.alarm.AlarmReceiver;
import com.geeker.door.alarm.PreAlarmReceiver;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.lock.MyLockService;

public class BootReceiver extends BroadcastReceiver{

	DbManager dbManager;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		
		dbManager=new DbManager(arg0);
		//启动自定义锁屏服务
		if(dbManager.isDoorLock()){
		Intent i=new Intent(arg0,MyLockService.class);
		arg0.startService(i);
		}
		//初始化闹钟
		List<EventVO> vos=dbManager.getClockBySaveTime();
		AlarmManager am = (AlarmManager) arg0.getSystemService(Context.ALARM_SERVICE);
		for (EventVO eventVO : vos) {
			ClockVO clockvo=dbManager.getClock(eventVO.getRequestCode());
			int reqCode=clockvo.getRequestCode();
			Intent intent = new Intent(arg0,AlarmReceiver.class);
			Intent preintent = new Intent(arg0,PreAlarmReceiver.class);
			intent.putExtra("requestcode", reqCode);
			PendingIntent sender = PendingIntent.getBroadcast(arg0 ,reqCode,intent,PendingIntent.FLAG_CANCEL_CURRENT);
			PendingIntent presender = PendingIntent.getBroadcast(arg0 ,reqCode,preintent,PendingIntent.FLAG_CANCEL_CURRENT);
			am.set(AlarmManager.RTC_WAKEUP,clockvo.getTime().getTimeInMillis(),sender);
			//提前唤醒
			am.set(AlarmManager.RTC_WAKEUP,clockvo.getTime().getTimeInMillis()-(AddAlarmActivity.PRE_ALARM_MINUTE*60*1000),presender);
			
		}
	}

}
