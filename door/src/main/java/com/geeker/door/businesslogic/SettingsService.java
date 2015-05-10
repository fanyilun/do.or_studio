package com.geeker.door.businesslogic;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;

import com.geeker.door.database.DbManager;
import com.geeker.door.network.NetworkHelper;

public class SettingsService {

	DbManager dbHelper;
	public SettingsService(Context c){
		dbHelper=new DbManager(c);
	}
	
	public int getLatestVersion(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Object[] objResults = NetworkHelper.HTTPpost("Update/get_latest_verion_no", params, 
				new String[][]{{"versionNo","int"}});
		if(objResults.length<1){return 0;}
		return (Integer)objResults[0];
	}
	
	public String getLatestURL(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		Object[] objResults = NetworkHelper.HTTPpost("Update/get_latest_apk", params, 
				new String[][]{{"address","string"}});
		if(objResults.length<1){return "";}
		return (String)objResults[0];
	}

	public Boolean sendFeedback(String string) {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("content", string));
		Object[] objResults = NetworkHelper.HTTPpost("Feedback/set_feedback", params, 
				new String[][]{{}});
		return true;
	}
	
	
}
