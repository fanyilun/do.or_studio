package com.geeker.door.alarm;


import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.MemoVO;
import com.geeker.door.wear.WearDataListener;

public class FinishAndDeleteReceiver extends BroadcastReceiver{
	
	AlarmService alarmService;
	@Override
	public void onReceive(Context arg0, Intent arg1) {
		int code=arg1.getIntExtra("requestcode",-1);
		int type=arg1.getIntExtra("type",-1);

		if(code==-1||type==-1){return;}

		alarmService=new AlarmService(arg0);
		DbManager db=new DbManager(arg0);
		MemoVO memo=db.getMemo(code);
		if(type==0){
			//finish
			new FinishEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					db.getEventID(EventVO.TYPE_SCHEDULE, code));

			db.deleteItem(EventVO.TYPE_SCHEDULE, code);

			new WearDataListener(arg0).sendMemoData();
		}else{
			new DeleteEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
					db.getEventID(EventVO.TYPE_SCHEDULE, code));
			db.deleteItem(EventVO.TYPE_SCHEDULE, code);
		}

		NotificationManagerCompat notificationManager = NotificationManagerCompat.from(arg0);
		notificationManager.cancel(29);
	}
	class FinishEventTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			return alarmService.finishAlarm(params[0].toString());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}

	class DeleteEventTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			return alarmService.deleteAlarm(params[0].toString());
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
}
