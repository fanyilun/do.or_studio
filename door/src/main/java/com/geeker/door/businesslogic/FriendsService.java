package com.geeker.door.businesslogic;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import com.geeker.door.database.DbManager;
import com.geeker.door.network.NetworkHelper;

import android.content.Context;

public class FriendsService {
	
	DbManager dbManager;
	public FriendsService(Context c) {
		dbManager=new DbManager(c);
	}
	
	/**
	 *	代码写的越多越寂寞 
	 */
	public Object[][] getFriendsList(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbManager.getUserName()));
		Object[][] networkResult = NetworkHelper.HTTPpostForArray("User/select_friend", params,
				new String[][]{{"userName","string"},{"nickname","string"},{"headURL","string"},{"score","string"}});
		return networkResult;
	}

}
