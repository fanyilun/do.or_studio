package com.geeker.door.welcome;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.MainActivity;
import com.geeker.door.R;
import com.geeker.door.businesslogic.LoginService;

public class BindingPhoneActivity extends Activity{

	//private Button getAuthCode;
	private View checkButton;
	private EditText phoneNumber;
	//private EditText authCode;
	private LoginService loginService;
	private ProgressBar progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.binding_phone);
		loginService=new LoginService(this);
		initButton();
	}
	
	
	
	 private void initButton() {
		//getAuthCode=(Button)findViewById(R.id.bindingphone_imageButton1);
		checkButton=findViewById(R.id.bindingphone_imageButton2);
		phoneNumber=(EditText)findViewById(R.id.bindingphone_edit2);
		//authCode=(EditText)findViewById(R.id.bindingphone_edit1);
		TextView nexttext=(TextView)findViewById(R.id.bindingphone_bottom_text);
		progress=(ProgressBar)findViewById(R.id.bindingphone_progressbar);
		/*getAuthCode.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(BindingPhoneActivity.this, "尚未开发。点击验证可以直接完成手机注册。", Toast.LENGTH_SHORT).show();
			}
		});*/
		checkButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progress.setVisibility(View.VISIBLE);
				new InitUserInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
		nexttext.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new InitUserInfo().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
	}

	@Override  
	    public boolean onKeyDown(int keyCode, KeyEvent event) {  
	        if(keyCode==KeyEvent.KEYCODE_BACK){  
	            this.finish();  //finish当前activity  
	            overridePendingTransition(R.anim.back_left_in,  
	                    R.anim.back_right_out);  
	            return true;  
	        }  
	        return super.onKeyDown(keyCode, event);  
	    }
	
	class InitUserInfo extends AsyncTask<Void, Boolean, Boolean>{

		@Override
		protected void onPostExecute(Boolean result) {
			progress.setVisibility(View.GONE);
			Intent i=new Intent(getApplicationContext(), MainActivity.class);
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			startActivity(i);
			overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			super.onPostExecute(result);
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			loginService.bindPhone(phoneNumber.getText().toString());
			loginService.initUserInfo();
			return null;
		}
	}

}
