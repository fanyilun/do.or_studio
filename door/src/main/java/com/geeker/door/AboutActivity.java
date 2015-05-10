package com.geeker.door;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.businesslogic.SettingsService;

public class AboutActivity extends Activity{

	SettingsService settingsService;
	ProgressDialog progressDialog;
	ProgressDialog progressDialog2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.about);
		setTitle("帮助");
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(Color.WHITE);
		settingsService=new SettingsService(this);
		initButton();
		super.onCreate(savedInstanceState);
	}
	
	private void initButton() {
		View checkUpdate=findViewById(R.id.check_update);
		View feedback=findViewById(R.id.suggestion_feedback);
		View about=findViewById(R.id.linear_about);
		TextView versionText=(TextView)findViewById(R.id.version_text);
		versionText.setText("V "+getVersionName(this));
		checkUpdate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(progressDialog==null){
					progressDialog = ProgressDialog.show(AboutActivity.this, "Do.or", "正在检查最新版本", true, true);
				}else{
					progressDialog.show();
				}
				new GetLatestVersionTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
		feedback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final EditText inputServer = new EditText(AboutActivity.this);
		        AlertDialog.Builder builder = new AlertDialog.Builder(AboutActivity.this);
		        builder.setTitle("请输入反馈意见").setView(inputServer).setNegativeButton("取消", null);
		        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
		            @Override
					public void onClick(DialogInterface dialog, int which) {
		            	new FeedBackTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, inputServer.getText().toString());
		            }
		        });
		        builder.show();	
			}
		});
		about.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();        
		        intent.setAction("android.intent.action.VIEW");    
		        Uri content_url = Uri.parse("http://doorserver.sinaapp.com/index.php/help");   
		        intent.setData(content_url);  
		        startActivity(intent);
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
	
	public static int getVersionCode(Context context)//获取版本号(内部识别号)  
	{  
	    try {  
	        PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return pi.versionCode;  
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();  
	        return 0;  
	    }  
	}  
	
	public static String getVersionName(Context context)//获取版本号(内部识别号)  
	{  
	    try {  
	        PackageInfo pi=context.getPackageManager().getPackageInfo(context.getPackageName(), 0);  
	        return pi.versionName;  
	    } catch (NameNotFoundException e) {  
	        e.printStackTrace();  
	        return "0.0";  
	    }  
	}  
	
	private class GetLatestVersionTask extends AsyncTask<Void, Void, Integer> {
        @Override
        protected Integer doInBackground(Void... params) {
			return settingsService.getLatestVersion();
        }

        @Override
        protected void onPostExecute(Integer result) {
        	progressDialog.dismiss();
        	if(result>getVersionCode(getApplicationContext())){
        		new AlertDialog.Builder(AboutActivity.this).setTitle("检查更新").setMessage("发现最新版本，点击确定打开下载链接")
        		.setPositiveButton("确定", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(progressDialog2==null){
							progressDialog2 = ProgressDialog.show(AboutActivity.this, "Do.or", "正在获取下载链接", true, true);
						}else{
							progressDialog2.show();
						}
						new GetLatestURLTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					}
				})
  	          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
  	              public void onClick(DialogInterface dialog, int which) {
  	              }
  	          })
  	          .show();
        	}else{
        		new AlertDialog.Builder(AboutActivity.this).setTitle("检查更新").setMessage("您的版本已是最新版本无需更新")
  	          .setNegativeButton("OK", new DialogInterface.OnClickListener() {
  	              public void onClick(DialogInterface dialog, int which) {
  	              }
  	          })
  	          .show();
        	}
        	
        	super.onPostExecute(result);
        }
    }
	
	
	private class GetLatestURLTask extends AsyncTask<Void, Void, String>{

		@Override
		protected String doInBackground(Void... params) {
			return settingsService.getLatestURL();
		}
		
		@Override
		protected void onPostExecute(String result) {
			progressDialog2.dismiss();
			 Intent intent = new Intent();        
		        intent.setAction("android.intent.action.VIEW");    
		        Uri content_url = Uri.parse(result);   
		        intent.setData(content_url);  
		        startActivity(intent);

			super.onPostExecute(result);
		}
		
	} 
	
	private class FeedBackTask extends AsyncTask<String, Void, Boolean>{
		
		@Override
		protected Boolean doInBackground(String... params) {
			return settingsService.sendFeedback(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Toast.makeText(AboutActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
		
	} 
}
