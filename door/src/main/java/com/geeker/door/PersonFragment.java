package com.geeker.door;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import qq.qq757225051.nongli.NongLi;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.MemoVO;
import com.geeker.door.database.RequestVO;
import com.geeker.door.imgcache.ImageDownloader;
import com.geeker.door.lock.WeatherRefreshReceiver;
import com.geeker.door.network.NetworkHelper;
import com.geeker.door.swipe.SwipeMenu;
import com.geeker.door.swipe.SwipeMenuCreator;
import com.geeker.door.swipe.SwipeMenuItem;
import com.geeker.door.swipe.SwipeMenuListView;
import com.geeker.door.utils.CustomAdapter;
import com.geeker.door.utils.LocationUtils;
import com.geeker.door.utils.MultiDirectionSlidingDrawer;
import com.geeker.door.utils.PinnedHeaderListView;
import com.geeker.door.utils.PinnedHeaderListView.OnRefreshListener;
import com.geeker.door.wear.WearDataListener;

public class PersonFragment extends Fragment {
	
	MultiDirectionSlidingDrawer mDrawer;
	ImageView[] imageGroup1=new ImageView[4];
	ImageView[] imageGroup2=new ImageView[2];
	TextView[] textGroup1=new TextView[4];
	TextView[] textGroup2=new TextView[4];
	TextView[] textGroup3=new TextView[2];
	View[] linears1=new View[4];
	View[] linears2=new View[2];
	private DbManager dbManager;
	Vibrator vibrator;
	TextView signing;
	TextView levelText;
	ProgressBar levelBar;
	TextView timeText;
	List<EventVO> events;
	SwipeMenuListView listView;
	private int listStatus;//0-7 0-3表示按添加时间排序的全部、闹钟、备忘、请求 4-7表示发生时间排序
	TextView weatherText;
	ImageView weatherIcon;
	private BoardService boardService;
	private BroadcastReceiver receiver1=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			weatherText.setText(intent.getStringExtra("weather"));
			System.out.println("1:"+intent.getStringExtra("weather"));
		}
	};
	Handler handler;
	TextView hintText;

	//定位专用
	private String loc = null; // 保存定位信息
	public LocationClient mLocationClient = null;
	public BDLocationListener myListener;
	boolean locateSucc;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		dbManager=new DbManager(getActivity());
		boardService=new BoardService(getActivity());
		View v = inflater.inflate(R.layout.person_board, null);
		listView = (SwipeMenuListView) v.findViewById(R.id.listview);
		hintText=(TextView)v.findViewById(R.id.person_hint);
		View header=inflater.inflate(R.layout.board_title, null);
		listView.addHeaderView(header);
		handler=new Handler();
		initHeader(header);
		// * 创建新的HeaderView，即置顶的HeaderView
		View HeaderView = inflater.inflate(R.layout.listview_item_header,
				listView, false);
		listView.setPinnedHeader(HeaderView);
		
		initListView();
		mDrawer = (MultiDirectionSlidingDrawer) v.findViewById( R.id.drawer );
		listView.setDrawer(mDrawer);
		initBottom(v);
		//如果是第一次打开则初始化天气
		if(dbManager.getKey("weatherCode").equals("")){
			initWeatherCode();
		}
		new GetExpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		new GetSignNumTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		getActivity().registerReceiver(receiver1, new IntentFilter("com.geeker.door.newweather"));
		return v;
	}

	private void initHeader(View header) {
		timeText=(TextView)header.findViewById(R.id.time_text);
		TextView dateText=(TextView)header.findViewById(R.id.date_text);
		weatherText=(TextView)header.findViewById(R.id.weather_text);
		weatherIcon=(ImageView)header.findViewById(R.id.weather_icon);
		TextView nameText=(TextView)header.findViewById(R.id.info_normal_username_text);
		signing=(TextView)header.findViewById(R.id.info_normal_signing_text);
		levelText=(TextView)header.findViewById(R.id.info_normal_level_text);
		levelBar=(ProgressBar)header.findViewById(R.id.infonormal_progressbar);
		signing=(TextView)header.findViewById(R.id.info_normal_signing_text);
		View weatherLinear=header.findViewById(R.id.weather_linear);
		ImageView head=(ImageView)header.findViewById(R.id.img_head);
		Calendar c = Calendar.getInstance();
		SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
		final String time=timeformat.format(new Date());
		SimpleDateFormat timeformat2=new SimpleDateFormat("yyyyMMdd");
		//String[] lunars=NongLi.getDate(timeformat2.format(new Date())).split("年");
		//String[] lunars=NongLi.getDate(timeformat2.format(new Date())).split("年");
		final String lunar = getDayOfWeek(c.get(Calendar.DAY_OF_WEEK));
		DbManager db=new DbManager(getActivity());
		final String weather=db.getWeather();
		timeText.setText(time);
		dateText.setText(lunar);
		weatherText.setText(weather);
		initWeatherIcon(weather);
		String url=dbManager.getKey("headURL");
		if(url!=null && !url.equals("null")){
			ImageDownloader mImageDownloader = new ImageDownloader(getActivity());
			mImageDownloader.download(url, head,ScaleType.FIT_XY);
		}
		String name=dbManager.getKey("nickName");
		if(name!=null && !name.equals("null")){
			nameText.setText(name);
		}
		View editButton=header.findViewById(R.id.edit_button);
		editButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				initWeatherCode();
			}
		});
		timeText.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
				final String time=timeformat.format(new Date());
				timeText.setText(time);
			}
		});
		weatherLinear.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new GetWeather().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,"");
			}
		});
		
		
	}
	
	private void initWeatherIcon(String weather) {
		if(weather.contains("雷阵雨")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_leizhenyu));
		}else if(weather.contains("雨夹雪")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_yujiaxue));
		}else if(weather.contains("小雨")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_xiaoyu));
		}else if(weather.contains("大雨")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_dayu));
		}else if(weather.contains("雨")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_zhongyu));
		}else if(weather.contains("阴")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_yin));
		}else if(weather.contains("多云")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_duoyun));
		}else if(weather.contains("晴")){
			weatherIcon.setImageDrawable(getResources().getDrawable(R.drawable.day_qing));
		}
	}

	private void initWeatherCode() {
		myListener= new MyLocationListener();
		mLocationClient = new LocationClient(getActivity()); // 声明LocationClient类
		mLocationClient.registerLocationListener(myListener); // 注册监听函数
		LocationClientOption option = new LocationClientOption();
		option.setOpenGps(true);// 打开GPS
		option.setAddrType("all");// 返回的定位结果包含地址信息
		option.setCoorType("bd09ll");// 返回的定位结果是百度经纬度,默认值gcj02
		option.setScanSpan(5000);// 设置发起定位请求的间隔时间为3000ms
		option.disableCache(false);// 禁止启用缓存定位
		option.setPriority(LocationClientOption.NetWorkFirst);// 网络定位优先
		mLocationClient.setLocOption(option);// 使用设置
		mLocationClient.start();// 开启定位SDK
		mLocationClient.requestLocation();// 开始请求位置
	}
	
	class MyLocationListener implements BDLocationListener
	{
		@Override
		public void onReceiveLocation(BDLocation location)
		{
			if(locateSucc){
				stopListener();
				return;
			}
			if (location != null)
			{
				StringBuffer sb = new StringBuffer(128);// 接受服务返回的缓冲区
				sb.append(location.getCity());// 获得城市
				loc = sb.toString().trim();
				new WeatherCodeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, loc);
				Toast.makeText(getActivity(), "您现在位于："+loc, Toast.LENGTH_SHORT).show();
				locateSucc=true;
			} else
			{
				Toast.makeText(getActivity(), "定位失败", Toast.LENGTH_SHORT).show();
				return;
			}
		}

		@Override
		public void onReceivePoi(BDLocation arg0)
		{
			// TODO Auto-generated method stub

		}

	}

	/**
	 * 停止，减少资源消耗
	 */
	public void stopListener()
	{
		if (mLocationClient != null && mLocationClient.isStarted())
		{
			mLocationClient.stop();// 关闭定位SDK
			mLocationClient = null;
		}
	}
	
	@Override
	public void onDestroy()
	{
		stopListener();//停止监听
		getActivity().unregisterReceiver(receiver1);
		super.onDestroy();
	}

	private void initListView() {
		List<ItemEntity> data = createData();
		final CustomAdapter customAdapter = new CustomAdapter(getActivity().getApplication(), data);
		listView.setAdapter(customAdapter);
		listView.setOnScrollListener(customAdapter);
		//为listview添加Drawer
		vibrator = (Vibrator) getActivity().getSystemService(getActivity().VIBRATOR_SERVICE);   

		listView.setOnRefreshListener(new OnRefreshListener() {

			@Override
			public void onRefresh(PinnedHeaderListView listView) {
				new NewDataTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				new GetExpTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				new GetSignNumTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			}
		});
		SwipeMenuCreator creator = new SwipeMenuCreator() {

			@Override
			public void create(SwipeMenu menu) {
				// create "delete" item
				SwipeMenuItem deleteItem = new SwipeMenuItem(
						getActivity());
				// set item background
				deleteItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				deleteItem.setWidth(dip2px(getActivity(), 90));
				// set a icon
				deleteItem.setIcon(R.drawable.ic_delete);
				// add to menu
				menu.addMenuItem(deleteItem);
				// create "delete" item
				SwipeMenuItem finishItem = new SwipeMenuItem(
						getActivity());
				// set item background
				finishItem.setBackground(new ColorDrawable(Color.rgb(0xF9,
						0x3F, 0x25)));
				// set item width
				finishItem.setWidth(dip2px(getActivity(), 90));
				// set a icon
				finishItem.setIcon(R.drawable.ic_finish);
				// add to menu
				menu.addMenuItem(finishItem);
			}
		};
		// set creator
		listView.setMenuCreator(creator);
		listView.setOnMenuItemClickListener(new SwipeMenuListView.OnMenuItemClickListener() {
			@Override
			public boolean onMenuItemClick(final int position, SwipeMenu menu, int index) {

				switch (index) {
					case 0:
						customAdapter.deleteButton(position);
						refreshList();
						break;
					case 1:
						customAdapter.finishButton(position);
						refreshList();
						break;
				}
				return false;
			}
		});

		// set SwipeListener
		listView.setOnSwipeListener(new SwipeMenuListView.OnSwipeListener() {

			@Override
			public void onSwipeStart(int position) {
				// swipe start
			}

			@Override
			public void onSwipeEnd(int position) {
				// swipe end
			}
		});


	}
	public static int dip2px(Context context, float dpValue) {

		final float scale = context.getResources().getDisplayMetrics().density;

		return (int) (dpValue * scale + 0.5f);

	}
	private void refreshList(){
		List<ItemEntity> data = createData();
		CustomAdapter customAdapter = new CustomAdapter(getActivity().getApplication(), data);
		listView.setAdapter(customAdapter);
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
	
	@Override
	public void onStart() {
		refreshList();
		SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
		final String time=timeformat.format(new Date());
		timeText.setText(time);
		super.onStart();
	}

	private List<ItemEntity> createData() {
		List<ItemEntity> data = new ArrayList<ItemEntity>();
		switch (listStatus) {
		case 1:
			events=dbManager.getClockBySaveTime();
			break;
		case 2:
			events=dbManager.getMemoBySaveTime();
			break;
		case 3:
			events=dbManager.getRequestBySaveTime();
			break;
		case 4:
			events=dbManager.getEventCodeByOccurTime();
			break;
		case 5:
			events=dbManager.getClockByOccurTime();
			break;
		case 6:
			events=dbManager.getMemoByOccurTime();
			break;
		case 7:
			events=dbManager.getRequestByOccurTime();
			break;
		default:
			events=dbManager.getEventCodeByAddTime();
			break;
		}
		for (EventVO eventVO : events) {
			String content="";
			String subContent="";
			switch (eventVO.getType()) {
			case EventVO.TYPE_ALARM:
				ClockVO clockVo=dbManager.getClock(eventVO.getRequestCode());
				content=clockVo.getTimeString();
				subContent=clockVo.getType().getTag();
				break;
			case EventVO.TYPE_SCHEDULE:
				MemoVO memoVo=dbManager.getMemo(eventVO.getRequestCode());
				content=memoVo.getTitle();
				subContent=memoVo.getTimeString();
				break;
			case EventVO.TYPE_REQUEST:
				RequestVO requestVo=dbManager.getRequest(eventVO.getRequestCode());
				content=requestVo.getTitle();
				subContent=requestVo.getTimeString();
				break;
			}
			data.add(new ItemEntity(eventVO.getDate(), content, subContent,eventVO.getTime(),eventVO.getType(),eventVO.getRequestCode()));
		}
		if(data.size()==0){
			hintText.setVisibility(View.VISIBLE);
		}else {
			hintText.setVisibility(View.GONE);
		}
		return data;

	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

	public class ItemEntity {
		private String mTitle;
		private String mContent;
		private String msubContent;
		private String addTime;
		private int type;
		private int requestCode;

		public ItemEntity(String pTitle, String pContent,String psubContent,String addTime,int type,int requestCode) {
			mTitle = pTitle;
			mContent = pContent;
			this.addTime=addTime;
			msubContent=psubContent;
			this.type=type;
			this.requestCode=requestCode;
		}

		public String getTitle() {
			return mTitle;
		}

		public String getContent() {
			return mContent;
		}
		
		public String getSubContent(){
			return msubContent;
		}
		
		public int getType(){
			return type;
		}
		
		public String getTime(){
			return addTime;
		}
		
		public int getRequestCode(){
			return requestCode;
		}
	}
	
	
	class MyListener1 implements OnClickListener{
		
		int index;
		
		public MyListener1(int index){
			this.index=index;
		}

		@Override
		public void onClick(View v) {
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
			refreshList();
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
				listStatus=listStatus%4+4*index;
				refreshList();
			}
		}
	}
	
	
	private class NewDataTask extends AsyncTask<Void, Void, String> {
		CustomAdapter customAdapter;
        @Override
        protected String doInBackground(Void... params) {
        	try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        	List<ItemEntity> data = createData();
    		customAdapter = new CustomAdapter(getActivity().getApplication(), data);
    		//listView.setAdapter(customAdapter);
			return null;
        }

        @Override
        protected void onPostExecute(String result) {
        	int current=listView.getAdapter().getCount();
        	//TODO refresh
        	listView.setAdapter(customAdapter);
            listView.completeRefreshing();

            super.onPostExecute(result);
        }
    }
	
	private class WeatherCodeTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
        	if(loc==null|| loc.equals("")){return "";}
			return dbManager.getCityCode(loc);
        }

        @Override
        protected void onPostExecute(String result) {
        	dbManager.saveKey("weatherCode", result);
        	Intent intent = new Intent(getActivity(),	WeatherRefreshReceiver.class);
        	getActivity().sendBroadcast(intent);
        	final String weather=dbManager.getWeather();
    		weatherText.setText(weather);
    		initWeatherIcon(weather);
            super.onPostExecute(result);
        }
    }
	
	
	
	private static String getDayOfWeek(int i){
		switch (i) {
		case 1:
			return "周日";
		case 2:
			return "周一";
		case 3:
			return "周二";
		case 4:
			return "周三";
		case 5:
			return "周四";
		case 6:
			return "周五";
		case 7:
			return "周六";
		}
		return "";
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
        	levelText.setText("Lv "+result[1]);
        	int[] maxExp=new int[]{1,5,15,30,50,100,200,500,1000,2000,3000,6000,10000,18000,30000,60000};
        	levelBar.setMax(maxExp[Integer.valueOf(result[1])]);
        	levelBar.setProgress(Integer.valueOf(result[0]));
        	Intent stopIntent = new Intent("com.geeker.door.EXPLEV");
        	stopIntent.putExtra("exp", levelBar.getProgress());
        	stopIntent.putExtra("max", levelBar.getMax());
        	stopIntent.putExtra("lev", result[1]);
    		getActivity().sendBroadcast(stopIntent);
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
			signing.setText("连续签到"+result+"天");
			Intent stopIntent = new Intent("com.geeker.door.SIGNDAYS");
			stopIntent.putExtra("sign", result);
    		getActivity().sendBroadcast(stopIntent);
			super.onPostExecute(result);
		}
	}
	
	class GetWeather extends AsyncTask<String, Integer, String>{

		@Override
		protected String doInBackground(String... params) {
			String[] result=NetworkHelper.HTTPGetWeather(dbManager.getKey("weatherCode"));
			if(result[0]==null){return "暂无天气信息";}
			new WearDataListener(getActivity()).sendWeatherData(result);
			return result[4]+"，"+result[3];
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			//保存到数据库
			dbManager.saveWeather(result);
			dbManager.closeDB();
			System.out.println("2:"+result);
			weatherText.setText(result);
    		Toast.makeText(getActivity(), "获取天气："+result, Toast.LENGTH_SHORT).show();
		}
		
	}
	
	
}
