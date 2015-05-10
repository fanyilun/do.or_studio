package com.geeker.door.businesslogic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.network.NetworkHelper;

public class AlarmService {
	
	DbManager dbHelper;
	Context c;
	public AlarmService(Context c){
		dbHelper=new DbManager(c);
		this.c=c;
		
	}
	
	public int addAlarm(Calendar aimTime,boolean isVisible,String ring,String tag,int volumn,String pattern,String method,int eventID) {

		String userName=dbHelper.getUserName();
		JSONObject message = new JSONObject();
		//打包成json格式
		try {
			message.put("ring",ring);
			message.put("tag", tag);
			message.put("volumn",volumn);
			message.put("pattern",pattern);
			message.put("method", method);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String url="set_clock";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", userName));
		params.add(new BasicNameValuePair("aimTime", String.valueOf(aimTime.getTimeInMillis()/1000)));
		params.add(new BasicNameValuePair("setTime", String.valueOf(Calendar.getInstance().getTimeInMillis()/1000)));
		params.add(new BasicNameValuePair("isVisible", isVisible==true?"1":"0"));
		params.add(new BasicNameValuePair("message", message.toString()));
		if(eventID>0){
			params.add(new BasicNameValuePair("eventID", String.valueOf(eventID)));
			url="update_clock";
		}
		Object[] result = NetworkHelper.HTTPpost("SetEvent/"+url, params,new String[][]{{"eventID","int"},{"succ","boolean"}});
		if(result.length<1){return 0;}
		return (Integer)result[0];
	}
	
	
	public Boolean finishAlarm(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", eventID));
		params.add(new BasicNameValuePair("setTime", String.valueOf(Calendar.getInstance().getTimeInMillis()/1000)));
		params.add(new BasicNameValuePair("isVisible", "1"));
		Object[] objResults = NetworkHelper.HTTPpost("SetEvent/accomplish_event", params, 
				new String[][]{{"succ","boolean"},{"eventID","string"}});
		if(objResults.length<1){return false;}
		dbHelper.saveKey("lastAlarm", objResults[1].toString());
		return (Boolean)objResults[0];
	}
	
	public Boolean deleteAlarm(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[] objResults = NetworkHelper.HTTPpost("SetEvent/delete_event", params, 
				new String[][]{{"succ","boolean"},{"reason","string"}});
		if(objResults.length<1){return false;}
		return (Boolean)objResults[0];
	}
}
