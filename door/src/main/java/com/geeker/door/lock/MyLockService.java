package com.geeker.door.lock;

import java.util.Calendar;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.PowerManager.WakeLock;
import android.telephony.TelephonyManager;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.geeker.door.DesktopActivity;
import com.geeker.door.R;
import com.geeker.door.database.DbManager;
import com.geeker.door.push.Utils;

public class MyLockService extends Service implements Runnable{
	Intent startIntent=null;
	TelephonyManager tm;
	WakeLock wakeLock;
	boolean isAbleToClose;
	KeyguardManager mKeyguardManager;
 	KeyguardLock mKeyguardLock;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {  
		// 困扰了一天的问题、点亮屏幕后接收广播总要有延迟
		@Override
		public void onReceive(final Context context, final Intent intent) {
			//接电话状态不锁屏
			System.out.println("receliveeeeeee");
			if(tm.getCallState()!=TelephonyManager.CALL_STATE_IDLE){Toast.makeText(context, "电话状态", Toast.LENGTH_SHORT).show();return;}
			Intent i = new Intent(context, MyLockActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
			context.startActivity(i);
			System.out.println("activity started");
		}
	};
	
	
	private BroadcastReceiver closeReceiver=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			System.out.println("close");
			isAbleToClose=true;
			EnableSystemKeyguard(true);
			stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	 @Override 
     public void onCreate() {  
		 EnableSystemKeyguard(false);
		 tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		 //前台运行
		startForeground(1, new Notification());
		 //电源管理.百度锁屏就是这么实现的。但是有费电的缺点
        /*PowerManager pm = (PowerManager) MyLockService.this.getSystemService(Context.POWER_SERVICE); 
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,"com.geeker.door.lock.MyLockService"); 
        wakeLock.acquire(); */
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.SCREEN_OFF");
		// filter.addAction(Intent.ACTION_USER_PRESENT);
		//filter.addAction("android.intent.action.SCREEN_ON");
		filter.setPriority(1000);
		registerReceiver(receiver, filter);
		registerReceiver(closeReceiver, new IntentFilter("com.geeker.door.CLOSE_DOOR_LOCK"));
		Toast.makeText(this, "启动Do.or锁屏服务，强烈建议您关闭系统锁屏", Toast.LENGTH_SHORT).show();
		//new Thread(this).start();
		//showNotification(getApplicationContext());
     }

	

	@Override
	public void onStart(Intent intent, int startId) {
		 startIntent=intent;  
		super.onStart(intent, startId);
	}
	 //防止被销毁哦也
     @Override 
     public void onDestroy() {  
    	 unregisterReceiver(receiver);
    	 unregisterReceiver(closeReceiver);
    	 
    	 if(isAbleToClose){
    		 stopForeground(false);
    		 return;
    	 }
    	 stopForeground(true);
           
         //EnableSystemKeyguard(true);
       if(startIntent!=null){  
             startService(startIntent);  
         } 
       if (wakeLock != null) { 
           wakeLock.release(); 
           wakeLock = null; 
       }
     }
     

     
     /**
      * 关闭系统锁屏。我在网上找了半天居然没有这段代码！！！！！
      */
     public void EnableSystemKeyguard(boolean bEnable){
    	 if(mKeyguardManager==null){
     	mKeyguardManager = (KeyguardManager)getSystemService(Context.KEYGUARD_SERVICE);
    	 }
    	 if(mKeyguardLock==null){
     	mKeyguardLock = mKeyguardManager.newKeyguardLock("com.geeker.door.lock");
    	 }
     	if(bEnable)
     		mKeyguardLock.reenableKeyguard();
     	else
     		mKeyguardLock.disableKeyguard();
     }

	@Override
	public void run() {
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println(getRunningActivityName());
		}
	}
	
	private String getRunningActivityName(){        
        ActivityManager activityManager=(ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        String runningActivity=activityManager.getRunningTasks(1).get(0).topActivity.getClassName();
        return runningActivity;               
    }
	
	public static void showNotification(Context context) {
		NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);  
        int icon = android.R.drawable.stat_notify_voicemail;  
        CharSequence tickerText = "新闹钟";  
        long when = System.currentTimeMillis();  
        Notification notification = new Notification(icon, tickerText, when);  
  
        notification.flags |= Notification.FLAG_ONGOING_EVENT;  
  
        RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_layout); 
        contentView.setTextViewText(R.id.textview1, "Do.or：点击添加新事项");  
        // 指定个性化视图  
        notification.contentView = contentView;  
        Intent intent = new Intent(context.getApplicationContext(), DesktopActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent contentIntent = PendingIntent.getActivity(context.getApplicationContext(), 0, intent, 0);
        // 指定内容意图  
        notification.contentIntent = contentIntent;  
        mNotificationManager.notify(1, notification);  
	}
}