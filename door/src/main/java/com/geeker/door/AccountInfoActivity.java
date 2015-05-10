package com.geeker.door;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.imgcache.ImageDownloader;

public class AccountInfoActivity extends MyActionBarActivity{

	DbManager dbManager;
	TextView levelText;
	TextView signText;
	ProgressBar levelBar;
	BoardService boardService;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.info_normal);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		initText();
		new GetExpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new GetSignNumTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		setTitle("账户设置");
	}

	private void initText() {
		TextView userName=(TextView)findViewById(R.id.info_normal_username_text);
		TextView sign=(TextView)findViewById(R.id.info_normal_signing_text);
		TextView nickName=(TextView)findViewById(R.id.infonormal_rrname_text);
		levelText=(TextView)findViewById(R.id.info_normal_level_text);
		signText=(TextView)findViewById(R.id.info_normal_signing_text);
		levelBar=(ProgressBar)findViewById(R.id.infonormal_progressbar);
		TextView rennID=(TextView)findViewById(R.id.infonormal_rrid_text);
		ImageView img1=(ImageView)findViewById(R.id.info_normal_header_imageview);
		ImageView img2=(ImageView)findViewById(R.id.info_normal_rrheader_imageview);
		ImageButton modify=(ImageButton)findViewById(R.id.info_normal_changebutton);
		modify.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(AccountInfoActivity.this, AccountModifyActivity.class);
				startActivity(i);
			}
		});
		userName.setText(dbManager.getUserName());
		nickName.setText(dbManager.getKey("nickName"));
		rennID.setText(dbManager.getKey("renrenID"));
		String url=dbManager.getKey("headURL");
		if(url!=null && !url.equals("null")){
			ImageDownloader mImageDownloader = new ImageDownloader(this);
			mImageDownloader.download(url, img1,ScaleType.FIT_XY);
			mImageDownloader.download(url, img2,ScaleType.FIT_XY);
		}
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish(); // finish当前activity
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private class GetExpTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
        	String[] result=new String[2];
        	result[0]=boardService.getExp();
        	result[1]=boardService.getLev();
			return result;
        }

        @Override
        protected void onPostExecute(String[] result) {
        	int[] maxExp=new int[]{1,5,15,30,50,100,200,500,1000,2000,3000,6000,10000,18000,30000,60000};
        	levelBar.setMax(maxExp[Integer.valueOf(result[1])]);
        	levelBar.setProgress(Integer.valueOf(result[0]));
        	levelText.setText("Lv "+result[1]+"("+result[0]+"/"+maxExp[Integer.valueOf(result[1])]+")");
            super.onPostExecute(result);
        }
    }
	private class GetSignNumTask extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... params) {
			return boardService.getSignNum();
		}
		
		@Override
		protected void onPostExecute(String result) {
			signText.setText("连续签到"+result+"天");
			super.onPostExecute(result);
		}
	}
	
	
}
