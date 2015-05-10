package com.geeker.door;

import android.app.Activity;
import android.content.Intent;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.geeker.door.database.DbManager;
import com.geeker.door.push.Utils;
import com.geeker.door.welcome.WelcomeActivity;

public class LaunchReceiver extends Activity {

	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		dbManager = new DbManager(this);
		if (!dbManager.getUserName().equals("")) {
			startActivity(new Intent(this, MainActivity.class));
		} else {
			startActivity(new Intent(this, WelcomeActivity.class));
		}
		//PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(this, "api_key"));
	};

	DbManager dbManager;

}
