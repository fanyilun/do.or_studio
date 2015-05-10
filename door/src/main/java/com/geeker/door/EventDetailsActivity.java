package com.geeker.door;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.imgcache.ImageDownloader;

public class EventDetailsActivity extends MyActionBarActivity{

	DbManager dbManager;
	ProgressDialog progressDialog;
	BoardService boardService;
	ListView listView;
	String eventID;
	List<Map<String, String>> data;
	MyAdapter myAdapter;
	Button commentButton;
	EditText commentText;
	String aimUserName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_review_layout);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		if(progressDialog==null){
		progressDialog = ProgressDialog.show(this, "Do.or", "正在读取事件", true, true);
		}
		initButton();
		initListview();
		eventID=String.valueOf(getIntent().getIntExtra("eventID", 0));
		new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, getIntent().getIntExtra("eventID", 0));
		new LikeHeadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new DislikeHeadTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new InitCommentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		setTitle("事件详情");
	}

	private void initListview() {
		//谁说我写代码不写注释
		listView=(ListView)findViewById(R.id.listView1);
		
		
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
	
	private void initButton() {
		commentText=(EditText)findViewById(R.id.comment_text);
		commentButton=(Button)findViewById(R.id.button1);
		commentButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				commentButton.setClickable(false);
				new AddCommentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,aimUserName,commentText.getText().toString());
			}
		});
	}
	
	private class InitTask extends AsyncTask<Integer, Void, Object[]> {
        @Override
        protected Object[] doInBackground(Integer... params) {
			return boardService.getDetails(params[0]);
        }

        @Override
        protected void onPostExecute(final Object[] result) {
        	if(result.length>0){
        	TextView dateText=(TextView)findViewById(R.id.textView1);
    		TextView timeText=(TextView)findViewById(R.id.textView2);
    		TextView title=(TextView)findViewById(R.id.textView3);
    		TextView content=(TextView)findViewById(R.id.textView4);
    		TextView subcontent=(TextView)findViewById(R.id.textView5);
    		TextView likeText=(TextView)findViewById(R.id.textView9);
    		TextView dislikeText=(TextView)findViewById(R.id.textView6);
    		ImageView headImg=(ImageView)findViewById(R.id.imageView2);
    		LinearLayout tagLinear=(LinearLayout)findViewById(R.id.tag_linear);
    		LinearLayout friendLinear=(LinearLayout)findViewById(R.id.friend_linear);
    		View wakeup=findViewById(R.id.wake_up);
    		View divideLine=findViewById(R.id.divide_line);
    		dateText.setText((String)result[0]);
    		timeText.setText((String)result[3]);
    		title.setText((String)result[2]);
    		content.setText((String)result[1]);
    		if(!result[11].equals("")){
    			subcontent.setVisibility(View.VISIBLE);
    			subcontent.setText((String)result[11]);
    		}
    		likeText.setText(result[6].toString());
    		dislikeText.setText(result[7].toString());
    		String url=result[10].toString();
    		if(url!=null && !url.equals("null")){
    			ImageDownloader mImageDownloader = new ImageDownloader(EventDetailsActivity.this);
    			mImageDownloader.download(url, headImg,ScaleType.FIT_XY);
    		}
    		String[] tags=(String[])result[12];
    		String[] friends=(String[])result[13];
    		for (int i = 0; i < tags.length; i++) {
    			if(tags[i].equals("")){continue;}
    			TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
            	label.setText(tags[i]);
    			tagLinear.addView(label);
			}
    		for (int i = 0; i < friends.length; i++) {
    			TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
    			label.setText(friends[i]);
    			friendLinear.addView(label);
    		}
    		int type=(Integer)result[4];
    		if(type==EventVO.TYPE_ALARM){
    			wakeup.setVisibility(View.VISIBLE);
    			divideLine.setVisibility(View.GONE);
    			wakeup.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						new AlertDialog.Builder(EventDetailsActivity.this).setTitle("强制叫醒").setMessage("这将会即时的在对方的手机上设置闹钟，继续？")
						.setPositiveButton("确认", new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								new AwakeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, result[5].toString());
							}
						})
				          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
				              public void onClick(DialogInterface dialog, int which) {
				              }
				          })
				          .show();
					}
				});
    		}
        	}
    		progressDialog.dismiss();
            super.onPostExecute(result);
        }
    }
	
	private class LikeHeadTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
			return boardService.getLikeURLs(eventID);
        }

        @Override
        protected void onPostExecute(String[] result) {
        	LinearLayout linear=(LinearLayout)findViewById(R.id.linear_like);
        	ImageDownloader mImageDownloader = new ImageDownloader(EventDetailsActivity.this);
        	for (int i = 0; i < result.length; i++) {
        		ImageView img = new ImageView(EventDetailsActivity.this);
        		img.setLayoutParams(new LayoutParams(Utils.dip2px(getApplicationContext(), 25), Utils.dip2px(getApplicationContext(), 25)));
        		mImageDownloader.download(result[i], img,ScaleType.FIT_XY);
        		linear.addView(img);
			}
            super.onPostExecute(result);
        }
    }
	
	private class DislikeHeadTask extends AsyncTask<Void, Void, String[]> {
        @Override
        protected String[] doInBackground(Void... params) {
			return boardService.getDislikeURLs(eventID);
        }

        @Override
        protected void onPostExecute(String[] result) {
        	LinearLayout linear=(LinearLayout)findViewById(R.id.linear_dislike);
        	ImageDownloader mImageDownloader = new ImageDownloader(EventDetailsActivity.this);
        	for (int i = 0; i < result.length; i++) {
        		ImageView img = new ImageView(EventDetailsActivity.this);
        		img.setLayoutParams(new LayoutParams(Utils.dip2px(getApplicationContext(), 25), Utils.dip2px(getApplicationContext(), 25)));
        		mImageDownloader.download(result[i], img,ScaleType.FIT_XY);
        		linear.addView(img);
			}
            super.onPostExecute(result);
        }
    }
	
	private class InitCommentTask extends AsyncTask<Void, Void, Object[][]> {
		@Override
		protected Object[][] doInBackground(Void... params) {
			return boardService.getComment(eventID);
		}
		
		@Override
		protected void onPostExecute(Object[][] result) {
			data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.length; i++) {
				Map<String, String> map=new HashMap<String, String>();
				map.put("username", (String)result[i][0]);
				map.put("nickName", (String)result[i][1]);
				map.put("headURL", (String)result[i][2]);
				map.put("aimUserName", (String)result[i][3]);
				map.put("text", (String)result[i][4]);
				map.put("setTime", (String)result[i][5]);
				map.put("aimUserNickname", (String)result[i][6]);
				data.add(map);
			}
			myAdapter=new MyAdapter(EventDetailsActivity.this);
			listView.setAdapter(myAdapter);
			listView.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,int arg2, long arg3) {
					aimUserName=data.get(arg2).get("username");
					commentText.setHint("回复 "+data.get(arg2).get("nickName")+"：");
				}
			});
			super.onPostExecute(result);
		}
	}
	
	private class AddCommentTask extends AsyncTask<String, Void, Boolean> {
		@Override
		protected Boolean doInBackground(String... params) {
			if(commentText.getText().toString().equals("")){return null;}
			aimUserName=null;
			return boardService.addComment(eventID,params[0],params[1]);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			if(result==null){
				
			}else if(result){
				Toast.makeText(EventDetailsActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
				new InitCommentTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}else if(result.equals(false)){
				Toast.makeText(EventDetailsActivity.this, "评论失败", Toast.LENGTH_SHORT).show();
			}
			commentButton.setClickable(true);
			commentText.setText("");
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
                convertView = mInflater.inflate(R.layout.review_detail_listcontent, null);  
                holder.img = (ImageView)convertView.findViewById(R.id.imageView1);  
                holder.username = (TextView)convertView.findViewById(R.id.textView1);  
                holder.info = (TextView)convertView.findViewById(R.id.textView2);  
                holder.time = (TextView)convertView.findViewById(R.id.textView3);  
                convertView.setTag(holder);  
            }else  
            {  
                holder = (ViewHolder)convertView.getTag();  
            }  
			mImageDownloader.download(data.get(position).get("headURL"), holder.img,ScaleType.FIT_XY);
			System.out.println(data.get(position).get("aimUserName"));
			if(data.get(position).get("aimUserName").equals("null") || data.get(position).get("aimUserName").equals(" ")){
				holder.username.setText(data.get(position).get("nickName")+"：");  
			}else{
				holder.username.setText(data.get(position).get("nickName")+" 回复 "+data.get(position).get("aimUserNickname")+"：");  
			}
            holder.info.setText(data.get(position).get("text"));  
            holder.time.setText(data.get(position).get("setTime"));  
            return convertView;  
        }  
          
    }  
	
	  class ViewHolder  
	    {  
	        public ImageView img;  
	        public TextView username;  
	        public TextView info;  
	        public TextView time;  
	    }  
	  
	  private class AwakeTask extends AsyncTask<String, Void, Integer> {
			
	        @Override
	        protected Integer doInBackground(String... params) {
				return boardService.forceAlarm(params[0]);
	        }

	        @Override
	        protected void onPostExecute(Integer result) {
	        	String s="网络连接错误";
	        	switch (result) {
				case -1:
					s="成功发送唤醒";
					break;
				case 1:
					s="亲密度不够哦";
					break;
				case 2:
					s="您的操作过于频繁";
					break;
				case 3:
				case 4:
					s="只能在闹钟前后半小时内唤醒哦";
					break;
				}
	        	Toast.makeText(EventDetailsActivity.this, s, Toast.LENGTH_SHORT).show();
	        	super.onPostExecute(result);
	        }
	    }
	
}
