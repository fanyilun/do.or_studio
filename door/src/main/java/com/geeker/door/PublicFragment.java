package com.geeker.door;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.utils.ItemEntity;
import com.geeker.door.utils.MultiDirectionSlidingDrawer;
import com.geeker.door.utils.PinnedHeaderListView;
import com.geeker.door.utils.PinnedHeaderListView.OnRefreshListener;
import com.geeker.door.utils.PublicCustomAdapter;

public class PublicFragment extends Fragment{
	PinnedHeaderListView listView;
	MultiDirectionSlidingDrawer mDrawer;
	ImageView[] imageGroup1=new ImageView[4];
	ImageView[] imageGroup2=new ImageView[2];
	TextView[] textGroup1=new TextView[4];
	TextView[] textGroup2=new TextView[4];
	TextView[] textGroup3=new TextView[2];
	private BoardService boardService;
	List<ItemEntity> data;
	private int listStatus;//0-7 0-3表示按添加时间排序的全部、闹钟、备忘、请求 4-7表示发生时间排序
	private DbManager dbManager;
	PublicCustomAdapter customAdapter;
	View footView;
	 int visibleItemCount;
     int visibleLastIndex;
     int lastEventID;
     
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		boardService=new BoardService(getActivity());
		dbManager=new DbManager(getActivity());
		View v = inflater.inflate(R.layout.public_board, null);
		listView = (PinnedHeaderListView) v.findViewById(R.id.listview2);
		initListView(inflater,container);
		View header=inflater.inflate(R.layout.blank_layout, null);
		listView.addHeaderView(header);
		View HeaderView = inflater.inflate(R.layout.listview_item_header2,listView, false);
		listView.setPinnedHeader(HeaderView);
		initSign(v);
		mDrawer = (MultiDirectionSlidingDrawer) v.findViewById( R.id.drawer );
		listView.setDrawer(mDrawer);
		initBottom(v);
		return v;
	}
	
	private void initSign(View v) {
		View signButton=v.findViewById(R.id.public_sign);
		signButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)<5 || Calendar.getInstance().get(Calendar.HOUR_OF_DAY)>11){
					Toast.makeText(getActivity(), "5点-11点之间才可以签到哦", Toast.LENGTH_SHORT).show();
					return;
				}
				final EditText inputServer = new EditText(getActivity());
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
				builder.setTitle("签到感言（可不填哦）").setView(inputServer);
				builder.setPositiveButton("确认",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								new SignTask().executeOnExecutor(
										AsyncTask.THREAD_POOL_EXECUTOR,
										dbManager.getKey("lastAlarm"),
										inputServer.getText().toString());
							}
						});
				builder.show();
			}
		});
	}

	public void refreshBoard(){
		if(footView!=null){
		footView.setVisibility(View.GONE);
		}
		new NewDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
	
	private void initListView(LayoutInflater inflater,ViewGroup container) {
		data = createTestData();
		customAdapter = new PublicCustomAdapter(getActivity(), data);
		listView.setAdapter(customAdapter);
		//为listview添加Drawer
		footView=inflater.inflate(R.layout.load_more, null);
		listView.setOnRefreshListener(new OnRefreshListener() {
			
			@Override
			public void onRefresh(PinnedHeaderListView listView) {
				refreshBoard();
			}
		});
		footView.setVisibility(View.GONE);
		listView.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				int itemsLastIndex = customAdapter.getCount() + 1;    //数据集最后一项的索引   
		        int lastIndex = itemsLastIndex + 1;             //加上底部的loadMoreView项   
		        if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && visibleLastIndex == lastIndex && footView.getVisibility()==View.VISIBLE) {  
		        	System.out.println("自动加载了");
		        	new AddDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		        }  
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
					PublicFragment.this.visibleItemCount = visibleItemCount;  
			        visibleLastIndex = firstVisibleItem + visibleItemCount - 1;  
			        listView.controlPinnedHeader(firstVisibleItem);
			}
		});
	}
	

	private void initBottom(View v) {
		int[] imageID=new int[]{R.id.imageView1,R.id.imageView2,R.id.imageView3,R.id.imageView4,R.id.imageView5,R.id.imageView6};
		int[] textID=new int[]{R.id.textView1,R.id.textView2,R.id.textView3,R.id.textView4,R.id.textView5,
				R.id.textView6,R.id.textView7,R.id.textView8,R.id.textView9,R.id.textView10};
		int[] linearID=new int[]{R.id.linear11,R.id.linear2,R.id.linear3,R.id.linear4,R.id.linear5,R.id.linear6};
		for (int i = 0; i < 4; i++) {
			imageGroup1[i]=(ImageView)v.findViewById(imageID[i]);
			textGroup1[i]=(TextView)v.findViewById(textID[i]);
			textGroup2[i]=(TextView)v.findViewById(textID[i+4]);
			v.findViewById(linearID[i]).setOnClickListener(new MyListener1(i));
			if(i!=0){
				imageGroup1[i].setAlpha(100);
			}
		}
		for (int i = 0; i < 2; i++) {
			imageGroup2[i]=(ImageView)v.findViewById(imageID[i+4]);
			textGroup3[i]=(TextView)v.findViewById(textID[i+8]);
			v.findViewById(linearID[i+4]).setOnClickListener(new MyListener2(i));
			if(i!=0){
				imageGroup2[i].setAlpha(100);
			}
		}
		
	}
	
	//TODO 目前测试用
	private List<ItemEntity> createTestData() {
		List<ItemEntity> data = new ArrayList<ItemEntity>();
		/*data.add(new ItemEntity("4月6日", "08:46", "开会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月6日", "08:47", "又开会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:48", "再开会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:49", "还开会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:50", "再开一次会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:51", "开个小会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:52", "开个大会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:53", "开开开会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));
		data.add(new ItemEntity("4月7日", "08:54", "最后一次会", "15:53",EventVO.TYPE_ALARM,0,0,0,0,"null",null));*/
		return data;

	}
	
	
	private class NewDataTask extends AsyncTask<Void, Void, Object[][]> {
        @Override
        protected Object[][] doInBackground(Void... params) {
        	if(footView!=null){
        		footView.setVisibility(View.GONE);
        	}
        	Object[][] result=boardService.getBoard(-1,listStatus);
			return result;
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	data.clear();
        	if(result!=null){
        	for (int i = 0; i < result.length; i++) {
        		data.add(new ItemEntity((String)result[i][0],(String)result[i][1],(String)result[i][2],
        				(String)result[i][3],(Integer)result[i][4],(Integer)result[i][5],(Integer)result[i][6],(Integer)result[i][7]
        						,(Integer)result[i][8],(String)result[i][9],(String)result[i][10],(String)result[i][11],(String[])result[i][12],(String[])result[i][13]));
			}
        	}
        	customAdapter.notifyDataSetChanged();
        	if(result!=null && result.length>0){
        		lastEventID=(Integer)result[result.length-1][5];
        		if(result.length>=10){
            		if(listView.getFooterViewsCount()==0){
            			listView.addFooterView(footView);
            		}
            		footView.setVisibility(View.VISIBLE);
            	}
        	}
        	listView.completeRefreshing();
            super.onPostExecute(result);
        }
    }
	
	private class AddDataTask extends AsyncTask<Void, Void, Object[][]> {
        @Override
        protected Object[][] doInBackground(Void... params) {
        	Object[][] result=boardService.getBoard(lastEventID,listStatus);
        	if(result==null){return null;}
        	for (int i = 0; i < result.length; i++) {
        		data.add(new ItemEntity((String)result[i][0],(String)result[i][1],(String)result[i][2],
        				(String)result[i][3],(Integer)result[i][4],(Integer)result[i][5],(Integer)result[i][6],(Integer)result[i][7]
        						,(Integer)result[i][8],(String)result[i][9],(String)result[i][10],(String)result[i][11],(String[])result[i][12],(String[])result[i][13]));
			}
			return result;
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	customAdapter.notifyDataSetChanged();
        	if(result!=null && result.length>0){
        		lastEventID=(Integer)result[result.length-1][5];
        		if(result.length>=10){
            		footView.setVisibility(View.VISIBLE);
            	}else{
            		listView.removeFooterView(footView);
            		footView.setVisibility(View.GONE);
            	}
        	}
        	listView.completeRefreshing();
            super.onPostExecute(result);
        }
    }
	private class SignTask extends AsyncTask<String, Void, String> {
		@Override
		protected String doInBackground(String... params) {
			return boardService.sign(params[0], params[1]);
		}
		
		@Override
		protected void onPostExecute(String result) {
			Toast.makeText(getActivity(), result, Toast.LENGTH_SHORT).show();
			super.onPostExecute(result);
		}
	}
	
class MyListener1 implements OnClickListener{
		
		int index;
		
		public MyListener1(int index){
			this.index=index;
		}

		@Override
		public void onClick(View v) {
			System.out.println(index);
			for (int i = 0; i < 4; i++) {
				if(i==index){
					imageGroup1[i].setAlpha(255);
					textGroup1[i].setTextColor(getResources().getColorStateList(R.color.white));
					textGroup2[i].setTextColor(getResources().getColorStateList(R.color.white));
				}else{
					imageGroup1[i].setAlpha(100);
					textGroup1[i].setTextColor(getResources().getColorStateList(R.color.gray));
					textGroup2[i].setTextColor(getResources().getColorStateList(R.color.gray));
				}
			}
			if(listStatus>3){
				listStatus=index+4;
			}else{
				listStatus=index;
			}
			new NewDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
		
	}
	
	class MyListener2 implements OnClickListener{
		int index;
		public MyListener2(int index){
			this.index=index;
		}

		@Override
		public void onClick(View v) {
			for (int i = 0; i < 2; i++) {
				if(i==index){
					imageGroup2[i].setAlpha(255);
					textGroup3[i].setTextColor(getResources().getColorStateList(R.color.white));
				}else{
					imageGroup2[i].setAlpha(100);
					textGroup3[i].setTextColor(getResources().getColorStateList(R.color.gray));
				}
			}
			listStatus=listStatus%4+4*index;
			new NewDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		}
	}
	
}
