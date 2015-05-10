package com.geeker.door;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.imgcache.ImageDownloader;

public class MessageCenterActivity extends MyActionBarActivity{

	DbManager dbManager;
	ListView listView;
	List<Map<String, String>> data;
	ProgressDialog progressDialog;
	BoardService boardService;
	MyAdapter myAdapter;
	Button clearButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		setContentView(R.layout.message_layout);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		progressDialog = ProgressDialog.show(this, "Loading...", "Please wait...", true, true);  
		initText();
		new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		setTitle("消息中心");
	}

	private void initText() {
		View titleView=findViewById(R.id.relativeLayout1);
		listView=(ListView)findViewById(R.id.listView1);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2,	long arg3) {
				if(data.get(arg2).get("eventID").equals("0")){
					new AlertDialog.Builder(MessageCenterActivity.this)   
					.setTitle("Do.or")  
					.setMessage("是否接受好友请求？")  
					.setPositiveButton("是", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new AddFriendTask(arg2).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(arg2).get("senderName"));
						}
					})  
					.setNegativeButton("否", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new ReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(arg2).get("noticeID"));
						}
					})  
					.show(); 
				}else if(data.get(arg2).get("eventID").equals("-1")){
					new ReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(arg2).get("noticeID"));
				}else{
					new ReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(arg2).get("noticeID"));
					Intent i=new Intent(MessageCenterActivity.this,EventDetailsActivity.class);
					i.putExtra("eventID", Integer.parseInt(data.get(arg2).get("eventID")));
					startActivity(i);
				}
			}
		});
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		getActionBar().setDisplayUseLogoEnabled(true);
		super.onWindowFocusChanged(hasFocus);
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
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main2, menu);
		clearButton = (Button) menu.findItem(R.id.menu_clear).getActionView();
		clearButton.setText("清除");
		clearButton.setBackgroundColor(getResources().getColor(R.color.main_color));
		clearButton.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					clearButton.setBackgroundColor(getResources().getColor(R.color.press_color));
					break;
				case MotionEvent.ACTION_UP:
					clearButton.setBackgroundColor(getResources().getColor(R.color.main_color));
					break;
				}
				return false;
			}
		});
		clearButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(MessageCenterActivity.this, "Loading...", "Please wait...", true, false); 
				new AllReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
		return true;
	}
	
	
	private class InitTask extends AsyncTask<Void, Void, Object[][]> {
        @Override
        protected Object[][] doInBackground(Void... params) {
			return boardService.getUnreadNotifiction();
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.length; i++) {
				Map<String, String> map=new HashMap<String, String>();
				map.put("nickName", result[i][0].toString());
				map.put("headURL", result[i][1].toString());
				map.put("setTime", result[i][2].toString());
				map.put("message1", result[i][3].toString());
				map.put("message2", result[i][4].toString());
				//此处的eventID可能是null哦
				map.put("eventID", result[i][5].toString());
				map.put("noticeID", result[i][6].toString());
				map.put("senderName", result[i][7].toString());
				data.add(map);
			}
			myAdapter=new MyAdapter(MessageCenterActivity.this);
			listView.setAdapter(myAdapter);
			progressDialog.dismiss();
        	super.onPostExecute(result);
        }
    }
	
	private class ReadTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			boardService.readNotifiction(params[0]);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		}
	}
	
	private class AllReadTask extends AsyncTask<String, Void, Void> {
		@Override
		protected Void doInBackground(String... params) {
			boardService.readAllNotifiction();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			progressDialog.dismiss();
			super.onPostExecute(result);
		}
	}
	
	private class AddFriendTask extends AsyncTask<String, Void, Boolean> {
		int index;
		public AddFriendTask(int index) {
			this.index=index;
		}
		@Override
		protected Boolean doInBackground(String... params) {
			return boardService.addFriend(params[0]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result){
				Toast.makeText(MessageCenterActivity.this, "添加好友成功", Toast.LENGTH_SHORT).show();
				new ReadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(index).get("noticeID"));
			}else{
				Toast.makeText(MessageCenterActivity.this, "添加好友失败", Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}
	}
	
	
	class MyAdapter extends BaseAdapter  
    {      
        private LayoutInflater mInflater = null;  
        private  ImageDownloader mImageDownloader ;
        private MyAdapter(Context context)  
        {  
        	 this.mImageDownloader = new ImageDownloader(context);
            this.mInflater = LayoutInflater.from(context);  
        }  
  
        @Override  
        public int getCount() {  
            return data.size();  
        }  
  
        @Override  
        public Object getItem(int position) {  
            return position;  
        }  
  
        @Override  
        public long getItemId(int position) {  
            return position;  
        }  
          
        @Override  
        public View getView(int position, View convertView, ViewGroup parent) {  
            ViewHolder holder = null;  
            if(convertView == null)  
            {  
                holder = new ViewHolder();  
                convertView = mInflater.inflate(R.layout.message_list_inner, null);  
                holder.img = (ImageView)convertView.findViewById(R.id.imageView1);  
                holder.username = (TextView)convertView.findViewById(R.id.textView1);  
                holder.content = (TextView)convertView.findViewById(R.id.textView2);  
                holder.info = (TextView)convertView.findViewById(R.id.textView3);  
                holder.time = (TextView)convertView.findViewById(R.id.textView4);  
                convertView.setTag(holder);  
            }else  
            {  
                holder = (ViewHolder)convertView.getTag();  
            }  
            if(data.get(position).get("headURL")!=null && !data.get(position).get("headURL").equals("null")){
            	mImageDownloader.download(data.get(position).get("headURL"), holder.img,ScaleType.FIT_XY);
            }else{
            	holder.img.setImageDrawable(getResources().getDrawable(R.drawable.ic_launcher));
	        }
			mImageDownloader.download(data.get(position).get("headURL"), holder.img,ScaleType.FIT_XY);
            holder.username.setText((String)data.get(position).get("nickName")+"：");  
            holder.content.setText((String)data.get(position).get("message1"));  
            holder.info.setText((String)data.get(position).get("message2"));  
            holder.time.setText((String)data.get(position).get("setTime"));  
            return convertView;  
        }  
          
    }  
	
	  class ViewHolder  
	    {  
	        public ImageView img;  
	        public TextView username;  
	        public TextView info;  
	        public TextView time;  
	        public TextView content;  
	    }  
	
	
}
