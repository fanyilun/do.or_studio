package com.geeker.door;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.MessageCenterActivity.MyAdapter;
import com.geeker.door.businesslogic.LoginService;
import com.geeker.door.database.DbManager;
import com.geeker.door.imgcache.ImageDownloader;

public class AccountModifyActivity extends MyActionBarActivity{

	DbManager dbManager;
	EditText nickNameText;
	EditText phoneNumText;
	EditText originPswText;
	EditText newPswText1;
	EditText newPswText2;
	LoginService loginService;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_modify);
		dbManager=new DbManager(this);
		loginService=new LoginService(this);
		initText();
		setTitle("修改信息");
	}

	private void initText() {
		TextView nickName=(TextView)findViewById(R.id.infomodify_rrname_text);
		TextView rennID=(TextView)findViewById(R.id.infomodify_rrid_text);
		ImageView img1=(ImageView)findViewById(R.id.info_modify_rrheader_imageview);
		nickNameText=(EditText)findViewById(R.id.info_modify_nickname_edittext);
		phoneNumText=(EditText)findViewById(R.id.info_modify_phonenumber_edittext);
		originPswText=(EditText)findViewById(R.id.info_modify_nowpassword_edittext);
		newPswText1=(EditText)findViewById(R.id.info_modify_newpassword_edittext);
		newPswText2=(EditText)findViewById(R.id.info_modify_newpassword_edittext2);
		ImageButton cancelButton=(ImageButton)findViewById(R.id.cancel_button);
		ImageButton okButton=(ImageButton)findViewById(R.id.ok_button);
		nickName.setText(dbManager.getKey("nickName"));
		nickNameText.setText(dbManager.getKey("nickName"));
		phoneNumText.setText(dbManager.getKey("phoneNum"));
		rennID.setText(dbManager.getKey("renrenID"));
		String url=dbManager.getKey("headURL");
		if(url!=null && !url.equals("null")){
			ImageDownloader mImageDownloader = new ImageDownloader(this);
			mImageDownloader.download(url, img1,ScaleType.FIT_XY);
		}
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!nickNameText.getText().toString().equals(dbManager.getKey("nickName"))){
					new ModifyNameTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				if(!phoneNumText.getText().toString().equals(dbManager.getKey("phoneNum"))){
					new ModifyTelTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				}
				if(!originPswText.getText().toString().equals("") && !newPswText1.getText().toString().equals("")
						&& !newPswText2.getText().toString().equals("")){
					if(newPswText1.getText().toString().equals(newPswText2.getText().toString())){
						new ModifyPasswordTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}else{
						Toast.makeText(AccountModifyActivity.this, "两次密码不一致", Toast.LENGTH_SHORT).show();
					}
				}
				finish();
			}
		});
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish(); // finish当前activity
			overridePendingTransition(R.anim.back_left_in,
					R.anim.back_right_out);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private class ModifyNameTask extends AsyncTask<Void, Void, Boolean> {
		String nickName;
		
        @Override
        protected Boolean doInBackground(Void... params) {
        	nickName=nickNameText.getText().toString();
			return loginService.modifyInfo(nickNameText.getText().toString());
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if(result){
        		Toast.makeText(AccountModifyActivity.this, "昵称修改成功", Toast.LENGTH_SHORT).show();
        		dbManager.saveKey("nickName", nickName);
        	}else{
        		Toast.makeText(AccountModifyActivity.this, "昵称修改失败", Toast.LENGTH_SHORT).show();
        	}
        	super.onPostExecute(result);
        }
    }
	
	private class ModifyTelTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return loginService.bindPhone(phoneNumText.getText().toString());
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Toast.makeText(AccountModifyActivity.this, "手机号修改成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(AccountModifyActivity.this, "手机号修改失败", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
	private class ModifyPasswordTask extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... params) {
			return loginService.modifyPass(originPswText.getText().toString(),newPswText1.getText().toString());
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Toast.makeText(AccountModifyActivity.this, "密码修改成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(AccountModifyActivity.this, "密码修改失败", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
	
}
