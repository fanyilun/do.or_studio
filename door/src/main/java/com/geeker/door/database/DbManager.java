package com.geeker.door.database;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

/**
 * 
 */
public class DbManager {
	private DbHelper helper;
	private Context context;
	private SharedPreferences preferences;
	private SQLiteDatabase database;
	public static final int CANCELTYPE_SLIDE=0;
	public static final int CANCELTYPE_SHAKE=1;
	
	
	public DbManager(Context c){
		this.context = c;
		helper = new DbHelper(c);
		preferences = c.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		database = helper.getWritableDatabase();
	}
	
	public Cursor datas(String tableName){
		String sql = "select * from "+tableName;
		return database.rawQuery(sql, null);
	}
	
	/**
	 * 
	 * @param login ???????????????????
	 */
	public void setLogin(String uid,Boolean login){
		Editor editor = preferences.edit();
		editor.putString("uid", uid);
		editor.putBoolean("login", login);
		editor.commit();
	}
	
	/**
	 * 
	 * @return ?????????????
	 */
	public boolean getLoign(){
		return preferences.getBoolean("login", false);
	}
	
	/**
	 * ???sharePreferences??????
	 */
	public void clear(){
		if(context == null){
			return;
		}
		
		Editor editor = preferences.edit();
		editor.clear();
		editor.commit();
		
	}
	
	/**
	 * ????????
	 * @param calendar ????
	 * @param type ??????  {@link ReminderType}
	 * @param cancelType ????
	 * @param saveTime ??????????
	 * @return requestCode
	 */
	public int saveClock(Calendar calendar,ReminderType type,int cancelType,int onlineID){
		/*
		 * 参数准备
		 */
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int requestCode = nextRequestCode("clock");
		int day = type.getDay();
		String ring = type.getRing();
		String tag = type.getTag();
		if(tag.equals("")){tag="闹钟";}
		int reminder1 = type.getReminder()[0];
		int reminder2 = type.getReminder()[1];
		int reminder3 = type.getReminder()[2];
		int reminder4 = type.getReminder()[3];
		//存储添加时间，即当前时间,注意时区
		long save_time = Calendar.getInstance().getTimeInMillis();
		int volume = type.getVolume();
		String sql = "insert into clock" +
		"(hour,minute,requestCode,onlineID,day,ring,tag,reminder1,reminder2,reminder3,reminder4,cancelType,saveTime,volume) " +
		"values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
		
		Object[] objects = new Object[]{hour,minute,requestCode,onlineID,day,ring,tag,
				reminder1,reminder2,reminder3,reminder4,cancelType,save_time,volume};
		database.execSQL(sql,objects);
		return requestCode;
	}
	
	/**
	 * ???汸?????
	 * @param title ???????
	 * @param content ???????
	 * @param time ??????
	 * @param remindTime ???????
	 * @param ring ????
	 * @param vol ????
	 * @param companies С???
	 * @param tags ???
	 * @param switches 
	 * @param saveTime ??????????
	 * @return requestCode
	 */
	
	public int  saveMemo(String title,String content,Calendar time,Calendar remindTime,
			String ring,int vol,String[] companies,String[] tags, boolean[] switches,int onlineID){
		/*
		 * 参数处理
		 */
		//memo设置的发生时间
		long  occurTime = -1;
		if (time != null) {
			occurTime  = time.getTimeInMillis();
		}
		//memo 设置的提醒时间
		long reminder = -1;
		if(remindTime != null){
			reminder = remindTime.getTimeInMillis();
		}
		
		String company = "";
		//处理null值
		if (companies != null) {
			for (int i = 0; i < companies.length; i++) {
				company += companies[i] + " ";
			}
		}else {
			company = null;
		}
		//handle null
		String tag = "";
		if (tags != null) {
			for (int i = 0; i < tags.length; i++) {
				tag += tags[i] + " ";
			}
		}else{
			tag = null;
		}
		int requestCode = nextRequestCode("memo");
		//存储添加时间，即当前时间
		long save_time = Calendar.getInstance().getTimeInMillis();
		int switch1=switches[0]?1:0;
		int switch2=switches[1]?1:0;
		int switch3=switches[2]?1:0;
		int switch4=switches[3]?1:0;
		int switch5=switches[4]?1:0;
		String sql = "insert into memo(title,content,time1,time2,ring,vol,companies,tags,requestCode,onlineID,saveTime,switch1,switch2,switch3,switch4,switch5) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; 
		Object[] objects = new Object[]{title,content,occurTime,reminder,ring,vol,company,tag,requestCode,onlineID,save_time,switch1,switch2,switch3,switch4,switch5};
		database.execSQL(sql, objects);
		
		return requestCode;
		
	}
	
	/**
	 * ????????
	 * @param title ????
	 * @param content ????
	 * @param calendar ???
	 * @param companies С???
	 * @param tags ???
	 * @param saveTime ??????????
	 * @return requestCode
	 */
	public int saveRequest(String title,String content,Calendar calendar,String[] companies,String[] tags,boolean[] switches,int onlineID){
		long time = -1;
		if(calendar != null){
			time = calendar.getTimeInMillis();
		}
		String company = "";
		if (companies != null) {
			for (int i = 0; i < companies.length; i++) {
				company += companies[i] + " ";
			}
		}else {
			company = null;
		}
		String tag = "";
		if (tags != null) {
			for (int i = 0; i < tags.length; i++) {
				tag += tags[i] + " ";
			}
		}else {
			tag = null;
		}
		int requestCode = nextRequestCode("request");
		//存储添加时间，即当前时间
		long save_time = Calendar.getInstance().getTimeInMillis();
		int switch1=switches[0]?1:0;
		int switch2=switches[1]?1:0;
		int switch3=switches[2]?1:0;
		int switch4=switches[3]?1:0;
		String sql = "insert into request(title,content,time,companies,tags,requestCode,onlineID,saveTime,switch1,switch2,switch3,switch4) values(?,?,?,?,?,?,?,?,?,?,?,?)";
		Object[] objects = new Object[]{title,content,time,company,tag,requestCode,onlineID,save_time,switch1,switch2,switch3,switch4};
		database.execSQL(sql, objects);
		return requestCode; 
	}
	
	
	/**
	 * 
	 * @param requestCode 
	 * @return 删除是否成功
	 */
	public boolean deleteClock(int requestCode){
		String[] args = {String.valueOf(requestCode)}; 
		int  i = database.delete("clock", "requestCode=?", args);
		
		if (i == 1) {
			return true;
		}
		return false;
	}
	
	public boolean deleteMemo(int requestCode){
		String[] args = {String.valueOf(requestCode)};
		int i = database.delete("memo", "requestCode=?", args);
		if (i == 1) {
			return true;
		}
		return false;
	}
	
	public boolean deleteRequest(int requestCode){
		String[] args = {String.valueOf(requestCode)};
		int i = database.delete("request", "requestCode=?", args);
		
		if(i == 1){
			return true;
		}
		return false;
	}
	
	/**
	 * ???????????
	 * @param requestCode ???????μ?????
	 * @param calendar ???????
	 * @param type	{@link ReminderType}
	 * @param cancelType
	 * @return ?????3??
	 */
	public boolean updateClock(int requestCode,Calendar calendar,ReminderType type,int cancelType){
		
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		int day = type.getDay();
		String ring = type.getRing();
		String tag = type.getTag();
		int reminder1 = type.getReminder()[0];
		int reminder2 = type.getReminder()[1];
		int reminder3 = type.getReminder()[2];
		int reminder4 = type.getReminder()[3];
		int vol = type.getVolume();
		ContentValues cv = new ContentValues();
		cv.put("hour", hour);
		cv.put("minute", minute);
		cv.put("day", day);
		cv.put("ring", ring);
		cv.put("tag", tag);
		cv.put("reminder1", reminder1);
		cv.put("reminder2", reminder2);
		cv.put("reminder3", reminder3);
		cv.put("reminder4", reminder4);
		cv.put("volume", vol);
		cv.put("cancelType", cancelType);
		
		String[] args = {String.valueOf(requestCode)};
		int i = database.update("clock", cv, "requestCode=?",args );
		
		if (i == 1) {
			return true;
		}
		return false;
	}
	
	
	public boolean updateMemo(int requestCode,String title,String content,Calendar time,Calendar remindTime,
			String ring,int vol,String[] companies,String[] tags, boolean[] switches){

			ContentValues cv = new ContentValues();
			//memo设置的发生时间
			long  occurTime = -1;
			if (time != null) {
				occurTime  = time.getTimeInMillis();
			}
			//memo 设置的提醒时间
			long reminder = -1;
			if(remindTime != null){
				reminder = remindTime.getTimeInMillis();
			}

			String company = "";
			//处理null值
			if (companies != null) {
				for (int i = 0; i < companies.length; i++) {
					company += companies[i] + " ";
				}
			}else {
				company = null;
			}
			//handle null
			String tag = "";
			if (tags != null) {
				for (int i = 0; i < tags.length; i++) {
					tag += tags[i] + " ";
				}
			}else{
				tag = null;
			}
			//存储添加时间，即当前时间
			long save_time = Calendar.getInstance().getTimeInMillis();
			int switch1=switches[0]?1:0;
			int switch2=switches[1]?1:0;
			int switch3=switches[2]?1:0;
			int switch4=switches[3]?1:0;
			int switch5=switches[4]?1:0;
			
			cv.put("title", title);
			cv.put("content", content);
			cv.put("time1", occurTime);
			cv.put("time2", reminder);
			cv.put("ring", ring);
			cv.put("vol", vol);
			cv.put("companies", company);
			cv.put("tags", tag);
			cv.put("saveTime", Calendar.getInstance().getTimeInMillis());
			cv.put("switch1", switch1);
			cv.put("switch2", switch2);
			cv.put("switch3", switch3);
			cv.put("switch4", switch4);
			cv.put("switch5", switch5);

			String[] args = {String.valueOf(requestCode)};
			int i = database.update("memo", cv, "requestCode=?",args );
			
			if(i == 1){
				return true;
			}
					
			return false;
		}
		
		public boolean updateRequest(int requestCode,String title,String content,Calendar calendar,
				String[] companies,String[] tags,boolean[] switches){
			ContentValues cv = new ContentValues();

			long time = -1;
			if(calendar != null){
				time = calendar.getTimeInMillis();
			}
			String company = "";
			if (companies != null) {
				for (int i = 0; i < companies.length; i++) {
					company += companies[i] + " ";
				}
			}else {
				company = null;
			}
			String tag = "";
			if (tags != null) {
				for (int i = 0; i < tags.length; i++) {
					tag += tags[i] + " ";
				}
			}else {
				tag = null;
			}
			int switch1=switches[0]?1:0;
			int switch2=switches[1]?1:0;
			int switch3=switches[2]?1:0;
			int switch4=switches[3]?1:0;
			
			cv.put("title", title);
			cv.put("content", content);
			cv.put("companies", company);
			cv.put("time", time);
			cv.put("tags", tag);
			cv.put("saveTime", Calendar.getInstance().getTimeInMillis());
			cv.put("switch1", switch1);
			cv.put("switch2", switch2);
			cv.put("switch3", switch3);
			cv.put("switch4", switch4);
			
			String[] args = {String.valueOf(requestCode)};
			int i = database.update("request", cv, "requestCode=?",args);
			
			if(i == 1){
				return true;
			}
			return false;
		}
	
	/**
	 * 
	 * @param requestCode
	 * @return {@link ClockVO}
	 */
	public ClockVO getClock(int requestCode){
		String sql = "select * from clock where requestCode="+requestCode;
		Cursor cursor =  database.rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			/*
			 * 闹钟时间
			 */
			int hour = cursor.getInt(cursor.getColumnIndex("hour"));
			int minute = cursor.getInt(cursor.getColumnIndex("minute"));
			Calendar time = Calendar.getInstance();
			time.set(Calendar.HOUR_OF_DAY, hour);time.set(Calendar.MINUTE, minute);
			/*
			 * reminderType封装
			 */
			int day = cursor.getInt(cursor.getColumnIndex("day"));
			String ring = cursor.getString(cursor.getColumnIndex("ring"));
			String tag = cursor.getString(cursor.getColumnIndex("tag"));
			int vol = cursor.getInt(cursor.getColumnIndex("volume"));
			int reminder1 = cursor.getInt(cursor.getColumnIndex("reminder1"));
			int reminder2 = cursor.getInt(cursor.getColumnIndex("reminder2"));
			int reminder3 = cursor.getInt(cursor.getColumnIndex("reminder3"));
			int reminder4 = cursor.getInt(cursor.getColumnIndex("reminder4"));
			int[] reminder = {reminder1,reminder2,reminder3,reminder4};
			ReminderType type = new ReminderType(day, ring, tag, vol, reminder);
			
			int cancleType = cursor.getInt(cursor.getColumnIndex("cancelType"));
			/*
			 * 添加时间
			 */
			long millions = cursor.getLong(cursor.getColumnIndex("saveTime"));
			Calendar saveTime = Calendar.getInstance();
			saveTime.setTimeInMillis(millions);
			
			ClockVO vo = new ClockVO(time, type, cancleType, requestCode, saveTime);
			return vo;
		}
		return null;
		
	}
	
	/**
	 * 
	 * @param requestCode
	 * @return {@link MemoVO}
	 */
	public MemoVO getMemo(int requestCode){
		String sql = "select * from memo where requestCode="+requestCode;
		Cursor cursor = database.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String content = cursor.getString(cursor.getColumnIndex("content"));
			/*
			 * 备忘发生时间
			 */
			long occurmillion = cursor.getLong(cursor.getColumnIndex("time1"));
			Calendar occurTime = Calendar.getInstance();
			 if(occurmillion != -1){
				 occurTime.setTimeInMillis(occurmillion);
			 }else {
				occurTime = null;
			}
			
			/*
			 * 备忘提醒时间
			 */
			long reminderMillis = cursor.getLong(cursor.getColumnIndex("time2"));
			Calendar reminderTime = Calendar.getInstance();
			if(reminderMillis != -1){
				reminderTime.setTimeInMillis(reminderMillis);
			}else {
				reminderTime = null;
			}
			
			/*
			 * 备忘添加时间
			 */
			long saveMillis = cursor.getLong(cursor.getColumnIndex("saveTime"));
			Calendar saveTime = Calendar.getInstance();
			saveTime.setTimeInMillis(saveMillis);
			//铃声
			String ring = cursor.getString(cursor.getColumnIndex("ring"));
			//音量
			int vol = cursor.getInt(cursor.getColumnIndex("vol"));
			/*
			 * 小伙伴
			 */
			String company = cursor.getString(cursor.getColumnIndex("companies"));
			String[] companies;
			if(company != null){
				companies = company.split(" ");
			}else {
				companies = null;
			}
			/*
			 * 标签
			 */
			String tag = cursor.getString(cursor.getColumnIndex("tags"));
			String[] tags ;
			if (tag != null) {
				tags = tag.split(" ");
			}else {
				tags = null;
			}
			String[] switchStrings={"switch1","switch2","switch3","switch4","switch5"};
			boolean[] switches=new boolean[5];
			for (int i = 0; i < switches.length; i++) {
				switches[i]=cursor.getInt(cursor.getColumnIndex(switchStrings[i]))==1?true:false;
			}
			MemoVO vo = new MemoVO(title, content, occurTime, reminderTime, 
					saveTime, ring, vol, companies, tags, requestCode,switches); 
			return vo;
		}
		return null;
	}
	
	/**
	 * 
	 * @param requestCode
	 * @return {@link RequestCode}
	 */
	public RequestVO getRequest(int requestCode){
		String sql = "select * from request where requestCode="+requestCode;
		Cursor cursor = database.rawQuery(sql, null);
		
		if (cursor.moveToFirst()) {
			String title = cursor.getString(cursor.getColumnIndex("title"));
			String content = cursor.getString(cursor.getColumnIndex("content"));
			/*
			 * 请求时间
			 */
			long occurMillis = cursor.getLong(cursor.getColumnIndex("time"));
			Calendar occurTime = Calendar.getInstance();
			if(occurMillis != -1){
				occurTime.setTimeInMillis(occurMillis);
			}else {
				occurTime = null;
			}
			
			/*
			 * 请求添加时间
			 */
			long saveMillis = cursor.getLong(cursor.getColumnIndex("saveTime"));
			Calendar  saveTime = Calendar.getInstance();
			saveTime.setTimeInMillis(saveMillis);
			
			/*
			 * 小伙伴
			 */
			String company = cursor.getString(cursor.getColumnIndex("companies"));
			String[] companies;
			if (company != null) {
				companies = company.split(" ");
			}else {
				companies = null;
			}
			/*
			 * 标签
			 */
			String tag = cursor.getString(cursor.getColumnIndex("tags"));
			String[] tags ;
			
			if(tag != null){
				tags = tag.split(" ");
			}else {
				tags = null;
			}
			String[] switchStrings={"switch1","switch2","switch3","switch4"};
			boolean[] switches=new boolean[4];
			for (int i = 0; i < switches.length; i++) {
				switches[i]=cursor.getInt(cursor.getColumnIndex(switchStrings[i]))==1?true:false;
			}
			RequestVO vo = new RequestVO(title, content, occurTime, 
					saveTime, companies, tags, requestCode,switches);
			return vo;
		}
		
		return null;
	}
	/**
	 * 
	 * @return ???????????????????????????????????null??empty Arraylist
	 * 
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<EventVO> getEventCodeByAddTime(){
		ArrayList<EventVO> clockVos = getClockBySaveTime();
		ArrayList<EventVO> memoVos  = getMemoBySaveTime();
		ArrayList<EventVO> requestVos = getRequestBySaveTime();
		ArrayList<EventVO> result = new ArrayList<EventVO>();
		result.addAll(clockVos);
		result.addAll(memoVos);
		result.addAll(requestVos);
		Collections.sort(result);//????
		Collections.reverse(result);//????
		return result;
	}
	
	
	
	/**
	 * 
	 * @return ??? ?????????????? ?????????????????RequestCode
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<EventVO> getEventCodeByOccurTime(){
		ArrayList<EventVO> clockVos = getClockByOccurTime();
		ArrayList<EventVO> memoVos = getMemoByOccurTime();
		ArrayList<EventVO> requestVos = getRequestByOccurTime();
		ArrayList<EventVO> result = new ArrayList<EventVO>();
		result.addAll(clockVos);
		result.addAll(memoVos);
		result.addAll(requestVos);
		Collections.sort(result);
		return result;
	}
	
	
	public void close(){
		database.close();
	}
	
	/**
	 * 
	 * @return ??????????????RequestCode
	 */
	public int getNextClock(){
		ArrayList<EventVO> list=getClockByOccurTime();
		System.out.println("getsize="+list.size());
		if(list.size()==0){return 0;}
		return list.get(0).getRequestCode();
	}
	
	/**
	 * 
	 * @return ???й??????
	 */
	public ArrayList<EventVO> getAllOverdueEvent(){
		ArrayList<EventVO> memoVos = getClockByOccurTime();
		ArrayList<EventVO> requestVos = getRequestByOccurTime();
		ArrayList<EventVO> result = new ArrayList<EventVO>();
		result.addAll(memoVos);result.addAll(requestVos);
		
		long millis = Calendar.getInstance().getTimeInMillis();
		for(int i = 0;i < result.size();i++){
			//??????????????
			if (result.get(i).getMillis() > millis) {
				result.remove(i);
				i--;
			}
		}
		return result;
	}
	
	/**
	 * ???requestCode
	 * @param tableName ????
	 * @return requestCode
	 */
	private int nextRequestCode(String tableName){
		String sql="select * from "+tableName;
		ArrayList<Integer> list=new ArrayList<Integer>();
		Cursor cursor=database.rawQuery(sql, null);
		while(cursor.moveToNext()){
			list.add(cursor.getInt(cursor.getColumnIndex("requestCode")));
		}
		cursor.close();
		for (int i = 10; i <= list.size()+10; i++) {
			boolean repeat=false;
			for (Integer integer : list) {
				if(integer==i){repeat=true;break;}
			}
			if(!repeat){return i;}
		}
		
		return 0;
	}
	/**
	 * 
	 * @return ??????????????null?????????empty arraylist
	 */
	public ArrayList<EventVO> getClockBySaveTime(){
		String sql = "select * from clock";
		Cursor cursor = database.rawQuery(sql, null);
		
		ArrayList<EventVO> vos = new ArrayList<EventVO>();
		while(cursor.moveToNext()){
			int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			long millis = cursor.getLong(cursor.getColumnIndex("saveTime"));
			EventVO vo = new EventVO(1, requestCode, millis);
			vos.add(vo);
		}
		Collections.sort(vos);
		Collections.reverse(vos);
		return vos;
	}
	
	/**
	 * 
	 * @return ??????????????null?????????empty arraylist
	 */
	public ArrayList<EventVO> getMemoBySaveTime(){
		String sql = "select * from memo";
		Cursor cursor = database.rawQuery(sql, null);
		
		ArrayList<EventVO> vos = new ArrayList<EventVO>();
		while(cursor.moveToNext()){
			int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			long millis = cursor.getLong(cursor.getColumnIndex("saveTime"));
			
			EventVO vo = new EventVO(2, requestCode, millis);
			vos.add(vo);
		}
		Collections.sort(vos);
		Collections.reverse(vos);
		return vos;
	}
	
	/**
	 * 
	 * @return ??????????????null?????????empty arraylist
	 */
	public ArrayList<EventVO> getRequestBySaveTime(){
		String sql = "select * from request";
		Cursor cursor = database.rawQuery(sql, null);
		
		ArrayList<EventVO> vos = new ArrayList<EventVO>();
		while(cursor.moveToNext()){
			int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			long millis = cursor.getLong(cursor.getColumnIndex("saveTime"));
			
			EventVO vo = new EventVO(3, requestCode, millis);
			vos.add(vo);
		}
		Collections.sort(vos);
		Collections.reverse(vos);
		return vos;
	}
	
	
	public ArrayList<EventVO> getClockByOccurTime(){
		String sql = "select * from clock";
		Cursor cursor = database.rawQuery(sql, null);
		//?????????
		Calendar calendar = Calendar.getInstance();
		//???????
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		int date = calendar.get(Calendar.DATE);
		
		ArrayList<EventVO> result = new ArrayList<EventVO>();
		while (cursor.moveToNext()) {
			System.out.println("one alarm");
			int hour = cursor.getInt(cursor.getColumnIndex("hour"));
			int minute  = cursor.getInt(cursor.getColumnIndex("minute"));
			
			calendar.set(Calendar.HOUR_OF_DAY, hour);
			calendar.set(Calendar.MINUTE, minute);
			calendar.set(Calendar.DATE, Calendar.getInstance().get(Calendar.DATE));
			
			int dayOfClock = cursor.getInt(cursor.getColumnIndex("day"));
			//calendar ??????????????????
			boolean nextWeek=false;
			if(dayOfClock==day){
				if(calendar.getTimeInMillis()<=Calendar.getInstance().getTimeInMillis()){
					nextWeek=true;
				}
			}
			 if(dayOfClock >= day && !nextWeek){
				 calendar.set(Calendar.DATE, date+dayOfClock-day);
			 }else{
				 calendar.set(Calendar.DATE, date + 7 - day + dayOfClock);
			 }
			 int requestCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			 EventVO vo = new EventVO(1, requestCode, calendar.getTimeInMillis());
			 System.out.println("add success");
			 result.add(vo);
		}
		System.out.println("size="+result.size());
		Collections.sort(result);
		return result;
	}
	
	public ArrayList<EventVO> getMemoByOccurTime(){
		String sql = "select * from memo";
		Cursor cursor = database.rawQuery(sql, null);
		
		ArrayList<EventVO> vos = new ArrayList<EventVO>();
		while (cursor.moveToNext()) {
			int requstCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			long millis = cursor.getLong(cursor.getColumnIndex("time1"));
			
			if (millis != 0) {
				EventVO vo = new EventVO(2, requstCode, millis);
				vos.add(vo);
			}
			
		}
		Collections.sort(vos);
		return vos;
	}
	
	public ArrayList<EventVO> getRequestByOccurTime(){
		String sql = "select * from request";
		Cursor cursor = database.rawQuery(sql, null);
		
		ArrayList<EventVO> vos = new ArrayList<EventVO>();
		while (cursor.moveToNext()) {
			int requstCode = cursor.getInt(cursor.getColumnIndex("requestCode"));
			long millis = cursor.getLong(cursor.getColumnIndex("time"));
			if (millis != 0) {
				EventVO vo = new EventVO(3, requstCode, millis);
				vos.add(vo);
			}
		}
		Collections.sort(vos);
		return vos;
	}
	
	public void saveWeather(final String weather){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putString("weather", weather);
		editor.commit();
	}
 
	public String getWeather(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final String s=preferences.getString("weather", "暂无天气信息");
		return s;
	}

	public void savePushID(String pushID){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putString("pushid", pushID);
		editor.commit();
	}
	
	public String getPushID(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final String s=preferences.getString("pushid", null);
		return s;
	}
	
	public String getUserName(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final String s=preferences.getString("username", "");
		return s;
	}
	
	public void saveUserName(String UserName){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putString("username", UserName);
		editor.commit();
	}
	
	public String getKey(String key){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final String s=preferences.getString(key, "");
		return s;
	}
	
	public void saveKey(String key,String info){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putString(key, info);
		editor.commit();
	}
	
	public void closeDB(){
		helper.close();
	}
	
	public void setEventID(int type, int requestCode,int eventID){
		System.out.println("setEventID"+eventID+" "+requestCode);
		ContentValues cv = new ContentValues();
		cv.put("onlineID", eventID);
		
		String[] args = {String.valueOf(requestCode)};
		switch (type) {
		case EventVO.TYPE_ALARM:
			database.update("clock", cv, "requestCode=?", args);
			break;
		case EventVO.TYPE_SCHEDULE:
			database.update("memo", cv, "requestCode=?", args);
			break;
		case EventVO.TYPE_REQUEST:
			database.update("request", cv, "requestCode=?", args);
		default:
			break;
		}
	}
	
	public int getEventID(int type,int requestCode){
		String sql =  "";
		switch (type) {
		case EventVO.TYPE_ALARM:
			sql = "select onlineID from clock where requestCode="+requestCode;
			break;
		case EventVO.TYPE_SCHEDULE:
			sql = "select onlineID from memo where requestCode="+requestCode;
			break;
		case EventVO.TYPE_REQUEST:
			sql = "select onlineID from request where requestCode="+requestCode;
		default:
			break;
		}
		
		Cursor cursor = database.rawQuery(sql, null);
		
		if(cursor.moveToFirst()){
			int eventID = cursor.getInt(cursor.getColumnIndex("onlineID"));
			return eventID;
		}
		return 0;
		
	}
	
	public void deleteItem(int type,int requestCode){
		switch (type) {
		case EventVO.TYPE_ALARM:
			deleteClock(requestCode);
			break;
		case EventVO.TYPE_SCHEDULE:
			deleteMemo(requestCode);
			break;
		case EventVO.TYPE_REQUEST:
			deleteRequest(requestCode);
			break;
		}
	}
	
	public String getCityCode(String cityName){
//		String fileName = "cityCode.txt";
		try {
//			InputStream is = context.getAssets().open(fileName);
//			BufferedReader br = new BufferedReader(new InputStreamReader(is));
//			String s = "";
//			while((s=br.readLine()) != null){
//				s=s.trim();
//				String[] ss = s.split("=");
//				String cityCode = ss[0];
//				String Name = ss[1];
//				if(cityName.contains(Name)){
//					return cityCode;
//				}
//			}
//		br.close();
//		is.close();
			if(cityName.endsWith("市")){
				return cityName.substring(0,cityName.length()-1);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return "";
		
	}
	
	public void setPush(boolean push){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putBoolean("push", push);
		editor.commit();
	}
	
	public boolean isPush(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final boolean s=preferences.getBoolean("push", true);
		return s;
	}
	
	public void setForceAwake(boolean ForceAwake){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putBoolean("ForceAwake", ForceAwake);
		editor.commit();
	}
	
	public boolean isForceAwake(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final boolean s=preferences.getBoolean("ForceAwake", true);
		return s;
	}
	
	public void setDoorLock(boolean DoorLock){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		Editor editor = preferences.edit();
		editor.putBoolean("DoorLock", DoorLock);
		editor.commit();
	}
	
	public boolean isDoorLock(){
		preferences = context.getSharedPreferences("sp", Context.MODE_MULTI_PROCESS);
		final boolean s=preferences.getBoolean("DoorLock", true);
		return s;
	}
}
