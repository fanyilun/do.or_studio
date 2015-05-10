package com.geeker.door.businesslogic;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.geeker.door.database.DbManager;
import com.geeker.door.network.NetworkHelper;

public class ScheduleService {
	
	DbManager dbHelper;
	public ScheduleService(Context c){
		dbHelper=new DbManager(c);
	}

	public int addSchedule(Calendar aimTime,boolean isVisible,String title,String content,List<String> tag,List<String> company,int eventID) {
		String userName=dbHelper.getUserName();
		JSONObject message = new JSONObject();
		JSONArray tags=null;
		JSONArray companies=null;
		if(tag!=null){
			tags=new JSONArray(tag);
		}else{
			tags=new JSONArray();
		}
		if(company!=null){
			companies=new JSONArray(company);
		}else{
			companies=new JSONArray();
		}
		//打包成json格式
		try {
			message.put("title",title);
			message.put("content", content);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String url="set_thing";
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", userName));
		if(aimTime==null){
			params.add(new BasicNameValuePair("aimTime", String.valueOf(Calendar.getInstance().getTimeInMillis()/1000)));
		}else{
			params.add(new BasicNameValuePair("aimTime", String.valueOf(aimTime.getTimeInMillis()/1000)));
		}
		params.add(new BasicNameValuePair("setTime", String.valueOf(Calendar.getInstance().getTimeInMillis()/1000)));
		params.add(new BasicNameValuePair("isVisible", isVisible==true?"1":"0"));
		params.add(new BasicNameValuePair("message", message.toString()));
		params.add(new BasicNameValuePair("tag", tags.toString()));
		params.add(new BasicNameValuePair("companies", companies.toString()));
		if(eventID>0){
			params.add(new BasicNameValuePair("eventID", String.valueOf(eventID)));
			url="update_thing";
		}
		Object[] result = NetworkHelper.HTTPpost("SetEvent/"+url, params,
							new String[][]{{"succ","boolean"},{"eventID","int"}});
		if(result.length<1){return 0;}
		return (Integer)result[1];
	}
	

}
