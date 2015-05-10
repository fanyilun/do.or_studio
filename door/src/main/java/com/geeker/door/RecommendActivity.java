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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.imgcache.ImageDownloader;
import com.geeker.door.renren.Constants;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;

public class RecommendActivity extends Activity{

	DbManager dbManager;
	ListView listView;
	List<Map<String, String>> data;
	ProgressDialog progressDialog;
	BoardService boardService;
	MyAdapter myAdapter;
	String accessToken;
	String renrenID;
	Button buttons[]; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		setContentView(R.layout.recommend_layout);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		accessToken=getIntent().getStringExtra("token");
		renrenID=getIntent().getStringExtra("renrenID");
		initText();
		progressDialog = ProgressDialog.show(this, "Loading...", "正在调用人人网接口查找请稍候...", true, false);
		progressDialog.setIndeterminate(true);
		progressDialog.setCancelable(true);
		setTitle("好友推荐");
		new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}

	

	private void initText() {
		listView=(ListView)findViewById(R.id.listView1);
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
	
	
	private class InitTask extends AsyncTask<Void, Void, Object[][]> {
        @Override
        protected Object[][] doInBackground(Void... params) {
			return boardService.getRenrenFriend(accessToken,renrenID);
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.length; i++) {
				Map<String, String> map=new HashMap<String, String>();
				map.put("userName", result[i][0].toString());
				map.put("name", result[i][1].toString());
				map.put("renenID", result[i][2].toString());
				map.put("headURL", result[i][3].toString());
				data.add(map);
			}
			buttons=new Button[result.length];
			myAdapter=new MyAdapter(RecommendActivity.this);
			listView.setAdapter(myAdapter);
			progressDialog.dismiss();
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
                convertView = mInflater.inflate(R.layout.recommend_list_inner, null);  
                holder.img = (ImageView)convertView.findViewById(R.id.imageView1);  
                holder.username = (TextView)convertView.findViewById(R.id.textView1);  
                holder.content = (TextView)convertView.findViewById(R.id.textView2);  
                holder.button = (Button)convertView.findViewById(R.id.button_add);
                buttons[position]=holder.button;
                convertView.setTag(holder);  
            }else  
            {  
                holder = (ViewHolder)convertView.getTag();  
            }  
			mImageDownloader.download(data.get(position).get("headURL"), holder.img,ScaleType.FIT_XY);
            holder.username.setText(data.get(position).get("name"));  
            holder.content.setText("人人ID："+data.get(position).get("renenID"));  
            holder.button.setOnClickListener(new MyButtonListener(holder.button,position)) ;
            
            return convertView;  
        }  
          
    }  
	
	  class ViewHolder  
	    {  
	        public ImageView img;  
	        public TextView username;  
	        public Button button;  
	        public TextView content;  
	    }  
	
	class MyButtonListener implements OnClickListener{

		private Button button;
		private int position;
		public MyButtonListener(Button button,int position) {
			this.button=button;
			this.position=position;
		}
		@Override
		public void onClick(View v) {
			 final EditText inputServer = new EditText(RecommendActivity.this);
		        AlertDialog.Builder builder = new AlertDialog.Builder(RecommendActivity.this);
		        builder.setTitle("请输入验证信息").setView(inputServer).setNegativeButton("取消", null);
		        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
		            @Override
					public void onClick(DialogInterface dialog, int which) {
		            	new AddFriendTask(button).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(position).get("userName"),
		            			inputServer.getText().toString());
		            	
		             }
		        });
		        builder.show();
		}
		
	}
	
	private class AddFriendTask extends AsyncTask<String, Void, Boolean> {
		private Button button;
		public AddFriendTask(Button button) {
			this.button=button;
		}
        @Override
        protected Boolean doInBackground(String... params) {
			return boardService.addFriendRequest(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if(result){
        		button.setClickable(false);
            	button.setText("已发送请求");
            	button.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray_label));
        	}else{
        		Toast.makeText(RecommendActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
        	}
        	super.onPostExecute(result);
        }
    }
}
