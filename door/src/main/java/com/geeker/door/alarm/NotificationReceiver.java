package com.geeker.door.alarm;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.MemoVO;

public class NotificationReceiver extends BroadcastReceiver{
	
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		int code=arg1.getIntExtra("requestcode",-1);
		if(code==-1){return;}
		DbManager db=new DbManager(arg0);
		MemoVO memo=db.getMemo(code);
		String title;
		String subtitle;
		if(memo.getContent().equals("")){
			title="备忘提醒";
			subtitle=memo.getTitle();
		}else{
			title=memo.getTitle();
			subtitle=memo.getContent();
		}

		Intent i=new Intent(arg0,FinishAndDeleteReceiver.class);
		Intent i2=new Intent(arg0,FinishAndDeleteReceiver.class);
		i.putExtra("requestcode",code);
		i.putExtra("type", 0);
		i2.putExtra("requestcode",code);
		i2.putExtra("type", 1);
		PendingIntent mapPendingIntent =
				PendingIntent.getBroadcast(arg0, 0, i, 0);
		PendingIntent mapPendingIntent2 =
				PendingIntent.getBroadcast(arg0, 0, i2, 0);

		NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender()
				.setHintShowBackgroundOnly(true);
		Bitmap bitmap=((BitmapDrawable)arg0.getResources().getDrawable(R.drawable.board_title)).getBitmap();
		NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(arg0)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(bitmap)
				.setContentTitle(title)
				.setContentText(subtitle)
				.setDefaults(Notification.DEFAULT_ALL)
				.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000, 1000})
				.addAction(R.drawable.ic_finish,
						"完成", mapPendingIntent)
				.addAction(R.drawable.ic_delete,
						"删除", mapPendingIntent2)
				.setContentIntent(PendingIntent.getActivity(arg0, 0, new Intent(), 0))
				.setAutoCancel(true)
				.extend(wearableExtender);

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(arg0);
		notificationManager.notify(29, notificationBuilder.build());
	}

}
