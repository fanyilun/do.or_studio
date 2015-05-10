package com.geeker.door.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.database.DbManager;
import com.geeker.door.push.Utils;

public class WelcomeActivity extends Activity{
	
	DbManager dbManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//已经登录直接进主界面
		dbManager=new DbManager(this);
		if(!dbManager.getUserName().equals("")){
			finish();
			startActivity(new Intent(WelcomeActivity.this,MainActivity.class));
		}
		
		setContentView(R.layout.welcome);
		Button startButton=(Button)findViewById(R.id.welcom_button);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(getApplicationContext(), CreatAccountActivity.class);
				startActivity(i);
				 overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			}
		});
		
		TextView textview=(TextView)findViewById(R.id.welcome_textview);
		textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(getApplicationContext(), LoginActivity.class);
				startActivity(i);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			}
		});
	}

}
