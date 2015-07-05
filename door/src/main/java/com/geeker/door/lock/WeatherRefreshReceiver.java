package com.geeker.door.lock;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.geeker.door.database.DbManager;
import com.geeker.door.network.NetworkHelper;
import com.geeker.door.wear.WearDataListener;

public class WeatherRefreshReceiver extends BroadcastReceiver{
	
	Context c;
	DbManager db;
	
	@Override
	public void onReceive(Context arg0, Intent arg1) {

		this.c=arg0;
		db=new DbManager(c);
		if(db.getKey("weatherCode").equals("")){return;}
		new GetWeather().execute("");
	}

	
	
	class GetWeather extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			String[] result=NetworkHelper.HTTPGetWeather(db.getKey("weatherCode"));
			int counter=10;
			while(result[0]==null && counter>0){
				try {
					Thread.sleep(20000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				result=NetworkHelper.HTTPGetWeather(db.getKey("weatherCode"));
				counter--;
			}
			if(result[0]==null){return null;}
			//同步手表
			new WearDataListener(c).sendWeatherData(result);
			return result[4]+"，"+result[3];
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if(result==null){return ;}
			//保存到数据库
			db.saveWeather(result);
			db.closeDB();
			Intent stopIntent = new Intent("com.geeker.door.newweather");
        	stopIntent.putExtra("weather", result);
    		c.sendBroadcast(stopIntent);
    		Toast.makeText(c, "获取天气："+result, Toast.LENGTH_SHORT).show();
		}
		
	}
}
