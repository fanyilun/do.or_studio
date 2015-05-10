package com.geeker.door.welcome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.businesslogic.LoginService;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;

public class CreatAccountActivity extends Activity{

	private RennClient rennClient;
	EditText userName;
	EditText nickName;
	EditText password;
	EditText password2;
	LoginService loginService;
	String uidString;
	String name;
	String headURL;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creating_account);
		// RennClient初始化
		rennClient = RennClient.getInstance(this);
		rennClient.init(Constants.APP_ID, Constants.API_KEY,Constants.Secret_Key);
		rennClient.setScope(Constants.SCOPE);
		rennClient.setTokenType("bearer");
		initButton();
		loginService=new LoginService(this);
	}
	
	private void initButton() {
		Button startButton=(Button)findViewById(R.id.createaccount_imageButton_startingUse);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//注册账户
				if(!password.getText().toString().equals(password2.getText().toString())){
					Toast.makeText(CreatAccountActivity.this, "两次密码输入不一致哦", Toast.LENGTH_SHORT).show();
					return;
				}else if(userName.getText().toString().equals("") || password.getText().toString().equals("")){
					Toast.makeText(CreatAccountActivity.this, "用户名密码不能为空哦", Toast.LENGTH_SHORT).show();
					return;
				}
				new  AlertDialog.Builder(CreatAccountActivity.this)   
				.setTitle("提示" )  
				.setMessage("如果您有人人账号，强烈建议您使用人人注册，确定继续？" )  
				.setPositiveButton("是" , new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						RegistNormally task=new RegistNormally();
						task.execute("");
					}
				})  
				.setNegativeButton("否" , null)  
				.show();  
			}
		});
		ImageButton renrenButton=(ImageButton)findViewById(R.id.createaccount_imageButton_rrlogin);
		renrenButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				rennClient.setLoginListener(new LoginListener() {
					
					@Override
					public void onLoginSuccess() {
						uidString = String.valueOf(rennClient.getUid());
						System.out.println("token:"+rennClient.getAccessToken());
						new GetRenrenUserInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, uidString,rennClient.getAccessToken().accessToken);
						/*Intent i=new Intent(getApplicationContext(), BindingPhoneActivity.class);
						startActivity(i);
						overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  */
					}
					
					@Override
					public void onLoginCanceled() {
						Toast.makeText(CreatAccountActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
					}
				});
				rennClient.login(CreatAccountActivity.this);
			}
		});
		userName=(EditText)findViewById(R.id.createaccount_editText_userNameedit);
		nickName=(EditText)findViewById(R.id.createAccount_editText_nickName);
		password=(EditText)findViewById(R.id.createaccount_password_edit);
		password2=(EditText)findViewById(R.id.createAccount_editText_password2);
		TextView textview=(TextView)findViewById(R.id.createAccount_textview_creatingLater);
		textview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new  AlertDialog.Builder(CreatAccountActivity.this)   
				.setTitle("提示" )  
				.setMessage("如果您有人人账号，强烈建议您使用人人注册，确定继续？" )  
				.setPositiveButton("是" , new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent i=new Intent(getApplicationContext(), MainActivity.class);
						i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
						startActivity(i);
						overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
					}
				})  
				.setNegativeButton("否" , null)  
				.show();  
				
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
	
	
	class RegistNormally extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			loginService.registNormally(userName.getText().toString(), nickName.getText().toString(),password.getText().toString());
			return loginService.loginNormally(userName.getText().toString(), password.getText().toString(),"username");
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(!result){
				Toast.makeText(CreatAccountActivity.this, "注册失败", Toast.LENGTH_SHORT).show();
				return;
			}
			Intent i=new Intent(getApplicationContext(), BindingPhoneActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			Toast.makeText(CreatAccountActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}
	
	class GetRenrenUserInfo extends AsyncTask<String, Void, Object[]>{

		@Override
		protected Object[] doInBackground(String... params) {
			return loginService.getRenrenUserInfo(params[0], params[1]);
		}
		
		@Override
		protected void onPostExecute(Object[] result) {
			RegistByRenren task=new RegistByRenren();
			
			task.executeOnExecutor(THREAD_POOL_EXECUTOR, uidString,(String)result[0],(String)result[1]);
			super.onPostExecute(result);
		}
	}
	
	class RegistByRenren extends AsyncTask<String, Integer, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			boolean re=loginService.registByRenren(params[0],params[1],params[2]);
			if(re){
				loginService.loginByRenren(params[0]);
			}
			return re;
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(!result){
				Toast.makeText(CreatAccountActivity.this, "注册失败，人人账号可能已经注册", Toast.LENGTH_SHORT).show();
				return;
			}
			System.out.println("OnpostExecute  "+result);
			Intent i=new Intent(getApplicationContext(), BindingPhoneActivity.class);
			startActivity(i);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			Toast.makeText(CreatAccountActivity.this, "注册成功", Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}
}
