package com.geeker.door.alarm;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.widget.Toast;

import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;

public class ForceAwakeService extends Service implements Runnable,SensorEventListener{
	
	SensorManager mSensorManager;  
	Thread myThread;
	Intent intent;
	boolean finished;
	
	@Override
	public void run() {
		//允许15s空闲
		try {
			Thread.sleep(15000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		initSensor();
		//等待2min
		try {
			Thread.sleep(120000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		if(finished){return;}
		//运行到这里说明时间段内没有碰手机
		Intent i=new Intent(this,AlarmActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		System.out.println(intent);
		i.putExtras(intent);
		startActivity(i);
		stopSelf();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		this.intent=intent;
		return super.onStartCommand(intent, flags, startId);
	}
	
	@Override
	public void onCreate() {
		myThread=new Thread(this);
		myThread.start();
		Toast.makeText(this, "开启强力唤醒", Toast.LENGTH_SHORT).show();
		super.onCreate();
	}

	private void initSensor() {
		//加速度传感器  
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
	      mSensorManager.registerListener(this,   
	      mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),   
	      SensorManager.SENSOR_DELAY_NORMAL); 
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		int sensorType = event.sensor.getType();
		final float[] values = event.values;
		if (sensorType == Sensor.TYPE_ACCELEROMETER) {
			if (Math.abs(values[0]) > 3 || Math.abs(values[1]) > 3 ) {
				Toast.makeText(ForceAwakeService.this, Math.abs(values[0])+" "+Math.abs(values[1])+" "+Math.abs(values[2]), Toast.LENGTH_LONG).show();
				System.out.println(Math.abs(values[0]) + " " + Math.abs(values[1]) + " " + Math.abs(values[2]));
				finished=true;
				mSensorManager.unregisterListener(this);
				try {
					new FinishAlarmTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(new DbManager(ForceAwakeService.this).getEventID(EventVO.TYPE_ALARM, intent.getIntExtra("requestcode", -1))));
					new DbManager(ForceAwakeService.this).deleteClock(intent.getIntExtra("requestcode", -1));
				}catch (Exception e){}
				stopSelf();
			}
		}
	}
	
	class FinishAlarmTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			return new AlarmService(ForceAwakeService.this).finishAlarm(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				
			}
			super.onPostExecute(result);
		}
	}
}
