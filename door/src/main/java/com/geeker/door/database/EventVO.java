package com.geeker.door.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class EventVO implements Comparable{
	public static final int TYPE_ALARM=1;
	public static final int TYPE_SCHEDULE=2;
	public static final int TYPE_REQUEST=3;
	public static final int OPERATE_ADD=1;
	public static final int OPERATE_MOTIFY=2;
	public static final int OPERATE_DELAY=3;
	public static final int OPERATE_FINISH=4;
	public static final int OPERATE_MISS=5;
	private int type;//1.闹钟 2.备忘 3.请求
	private int requestCode;
	private long millis;
	
	
	public EventVO(int type,int requestCode,long millis){
		this.type = type;
		this.requestCode = requestCode;
		this.millis = millis;
	}

	public long getMillis() {
		return millis;
	}

	public void setMillis(long millis) {
		this.millis = millis;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}

	@Override
	public int compareTo(Object another) {
		EventVO vo2 = (EventVO)another;
		if (this.millis < vo2.getMillis()) {
			return -1;
			
		}else if (this.millis > vo2.getMillis()) {
			return 1;
		}
		
		return 0;
	}

	public String getDate(){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(millis);
		Calendar instance=Calendar.getInstance();
		if(instance.get(Calendar.YEAR)==c.get(Calendar.YEAR) && instance.get(Calendar.DAY_OF_YEAR)==c.get(Calendar.DAY_OF_YEAR)){
			return "今天";
		}
		String s=(c.get(Calendar.MONTH)+1)+"月"+c.get(Calendar.DAY_OF_MONTH)+"日";	
		return s;
	}
	
	public String getTime(){
		SimpleDateFormat time=new SimpleDateFormat("HH : mm");
		return time.format(new Date(millis));
	}
	
}
