package com.geeker.door.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.businesslogic.LoginService;
import com.geeker.door.database.DbManager;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;

public class LoginActivity extends Activity{
	ImageButton renrenButton;
	View loginButton;
	EditText username;
	EditText password;
	private RennClient rennClient;
	LoginService loginService;
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		// RennClient初始化
		rennClient = RennClient.getInstance(this);
		rennClient.init(Constants.APP_ID, Constants.API_KEY,Constants.Secret_Key);
		rennClient.setScope(Constants.SCOPE);
		rennClient.setTokenType("bearer");
		loginService=new LoginService(this);
		initButton();
	}

	private void initButton() {
		renrenButton=(ImageButton)findViewById(R.id.login_imageButton_rrlogin);
		loginButton=findViewById(R.id.login_imageButton_startingUse);
		username=(EditText)findViewById(R.id.login_editText_userNameedit);
		password=(EditText)findViewById(R.id.login_password_edit);
		progressBar=(ProgressBar)findViewById(R.id.progressbar);
		renrenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rennClient.setLoginListener(new LoginListener() {
					
					@Override
					public void onLoginSuccess() {
						String uidString = String.valueOf(rennClient.getUid());
						Toast.makeText(LoginActivity.this, "loginSuccess:"+uidString, Toast.LENGTH_SHORT).show();
						//将当前用户id显示
						//TODO 登录成功处理
						new LoginByRenren().execute(uidString);
					}
					@Override
					public void onLoginCanceled() {
						// TODO Auto-generated method stub
						Toast.makeText(LoginActivity.this, "人人连接失败", Toast.LENGTH_SHORT).show();
					}
				});
				rennClient.login(LoginActivity.this);
			}
		});
		loginButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				LoginNormally loginNormally=new LoginNormally();
				loginNormally.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				progressBar.setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			this.finish(); // finish当前activity
			overridePendingTransition(R.anim.back_left_in,
					R.anim.back_right_out);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	class LoginNormally extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			return loginService.loginNormally(username.getText().toString(), password.getText().toString(),"username");
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			progressBar.setVisibility(View.GONE);
			if(!result){
				Toast.makeText(LoginActivity.this, "用户名密码错误", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent i=new Intent(getApplicationContext(), MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			super.onPostExecute(result);
		}
		
	}
	
	class LoginByRenren extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			return loginService.loginByRenren(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(!result){
				Toast.makeText(LoginActivity.this, "用户名不存在", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent i=new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			super.onPostExecute(result);
		}
		
	}
	
}
