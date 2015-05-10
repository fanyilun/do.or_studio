package com.geeker.door.friends;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.R;
import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.imgcache.ImageDownloader;
import com.geeker.door.renren.Constants;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;

public class NewfriendFragment extends Fragment{
	
	private RennClient rennClient;
	String accessToken;
	DbManager dbManager;
	ListView listView;
	List<Map<String, String>> data;
	ProgressDialog progressDialog;
	BoardService boardService;
	MyAdapter myAdapter;
	String renrenID;
	Button buttons[]; 
	boolean sent[];
	HashMap<String , String> phoneMap;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.new_friend,null);
		listView=(ListView)v.findViewById(R.id.listview1);
		initButton(v);
		dbManager=new DbManager(getActivity());
		boardService=new BoardService(getActivity());
		return v;
	}

	private void initButton(View v) {
		View renren=v.findViewById(R.id.linear_renren);
		renren.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				initToken();
			}
		});
		View book=v.findViewById(R.id.linear_book);
		book.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				phoneMap=getPhoneContracts(getActivity());
				new getContactBookFriendsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, phoneMap);
			}
		});
		final EditText edittext=(EditText)v.findViewById(R.id.edittext_serch);
		Button search=(Button)v.findViewById(R.id.search);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new SearchFriendTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, edittext.getText().toString());
				
			}
		});
		
	}

	protected void initToken() {
		rennClient = RennClient.getInstance(getActivity());
		rennClient.init(Constants.APP_ID, Constants.API_KEY, Constants.Secret_Key);
		rennClient.setScope(Constants.SCOPE);
		rennClient.setTokenType("bearer");
		rennClient.setLoginListener(new LoginListener() {
			@Override
			public void onLoginSuccess() {
				accessToken=rennClient.getAccessToken().accessToken;
				renrenID=rennClient.getUid().toString();
				progressDialog = ProgressDialog.show(getActivity(), "Loading...", "正在调用人人网接口查找请稍候...", true, false);
				progressDialog.setIndeterminate(true);
				progressDialog.setCancelable(true);
				new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
			
			@Override
			public void onLoginCanceled() {
			}
		});
		rennClient.login(getActivity());
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
			sent=new boolean[result.length];
			myAdapter=new MyAdapter(getActivity());
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
            if(data.get(position).get("renenID")==null){
            	holder.content.setText("用户名："+data.get(position).get("userName"));
            }else if(data.get(position).get("renenID").equals("-1")){
            	holder.content.setText("手机联系人："+getContactName(data.get(position).get("phoneNum")));
            }else{
            	holder.content.setText("人人ID："+data.get(position).get("renenID"));
            }
            holder.button.setOnClickListener(new MyButtonListener(holder.button,position)) ;
            if(sent[position]){
            	holder.button.setClickable(false);
            	holder.button.setText("已发送请求");
            	holder.button.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray_label));
            }else{
            	holder.button.setClickable(true);
            	holder.button.setText("添加");
            	holder.button.setBackgroundDrawable(getResources().getDrawable(R.drawable.radiobutton_bg_selector));
            }
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
			 final EditText inputServer = new EditText(getActivity());
		        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		        builder.setTitle("请输入验证信息").setView(inputServer).setNegativeButton("取消", null);
		        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
		            @Override
					public void onClick(DialogInterface dialog, int which) {
		            	new AddFriendTask(button,position).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,data.get(position).get("userName"),
		            			inputServer.getText().toString());
		            	
		             }
		        });
		        builder.show();
		}
		
	}
	
	private class AddFriendTask extends AsyncTask<String, Void, Boolean> {
		private Button button;
		int positon;
		public AddFriendTask(Button button, int position) {
			this.button=button;
			this.positon=position;
		}
        @Override
        protected Boolean doInBackground(String... params) {
			return boardService.addFriendRequest(params[0], params[1]);
        }

        @Override
        protected void onPostExecute(Boolean result) {
        	if(result){
        		sent[positon]=true;
        		button.setClickable(false);
            	button.setText("已发送请求");
            	button.setBackgroundDrawable(getResources().getDrawable(R.drawable.gray_label));
        	}else{
        		Toast.makeText(getActivity(), "添加失败", Toast.LENGTH_SHORT).show();
        	}
        	super.onPostExecute(result);
        }
    }
	
	/**
	 * 写代码不喜欢写注释呀
	 * @author Fanyl
	 *
	 */
	private class SearchFriendTask extends AsyncTask<String, Void, String[][]> {

		@Override
        protected String[][] doInBackground(String... params) {
			return boardService.searchFriends(params[0]);
        }

        @Override
        protected void onPostExecute(String[][] result) {
        	data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.length; i++) {
				Map<String, String> map=new HashMap<String, String>();
				map.put("userName", result[i][0].toString());
				map.put("name", result[i][1].toString());
				map.put("renenID", null);
				map.put("headURL", result[i][2].toString());
				data.add(map);
			}
			buttons=new Button[result.length];
			sent=new boolean[result.length];
			myAdapter=new MyAdapter(getActivity());
			listView.setAdapter(myAdapter);
        	super.onPostExecute(result);
        }
    }
	
	
	private class getContactBookFriendsTask extends AsyncTask<HashMap<String , String>, Void, Object[][]> {

		@Override
        protected Object[][] doInBackground(HashMap<String , String>... params) {
			return boardService.getContactBookUser(params[0]);
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	data = new ArrayList<Map<String, String>>();
			for (int i = 0; i < result.length; i++) {
				Map<String, String> map=new HashMap<String, String>();
				map.put("userName", result[i][0].toString());
				map.put("name", result[i][1].toString());
				map.put("renenID", "-1");
				map.put("phoneNum", result[i][3].toString());
				map.put("headURL", result[i][2].toString());
				data.add(map);
			}
			buttons=new Button[result.length];
			sent=new boolean[result.length];
			myAdapter=new MyAdapter(getActivity());
			listView.setAdapter(myAdapter);
        	
        	super.onPostExecute(result);
        }
    }
	
	
	
	 public static HashMap<String, String> getPhoneContracts(Context mContext){
		  HashMap<String, String> map = new HashMap<String, String>();
		  ContentResolver resolver = mContext.getContentResolver();
		 // 获取手机联系人 
		 Cursor phoneCursor = resolver.query(Phone.CONTENT_URI,null, null, null, null); //传入正确的uri
		 if(phoneCursor!=null){
		  while(phoneCursor.moveToNext()){
		  int nameIndex = phoneCursor.getColumnIndex(Phone.DISPLAY_NAME); //获取联系人name
		 String name = phoneCursor.getString(nameIndex);
		 String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(Phone.NUMBER)); //获取联系人number
		 if(TextUtils.isEmpty(phoneNumber)){
		 continue;
		 }
		 phoneNumber=phoneNumber.replaceAll(" ", "");
		 phoneNumber=phoneNumber.replaceAll("-", "");
		 //不能直接写+86哦。他会误认为你要用正则表达式。
		 phoneNumber=phoneNumber.replaceFirst("\\+86", "");
		 map.put(name, phoneNumber);
		}
		 phoneCursor.close();
		 }
		 return map;
	 }

	public String getContactName(String string) {
		Iterator iter = phoneMap.entrySet().iterator();
		while (iter.hasNext()) {
		Map.Entry entry = (Map.Entry) iter.next();
		if(entry.getValue().equals(string)){
			return entry.getKey().toString();
		}
		}
		return string;
	}
}
