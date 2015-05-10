package com.geeker.door.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 
 * @author liushao
 *@since 2014-3-15
 *@version 1.0
 */
public class ClockVO {
	
	private Calendar time;
	private ReminderType type;
	private int cancleType;
	private int requestCode;
	private Calendar saveTime;
	
	public ClockVO(Calendar time,ReminderType type,int cancleType,int requestCode,Calendar saveTime){
		this.time = time;
		this.type = type;
		this.cancleType = cancleType;
		this.requestCode = requestCode;
		this.saveTime = saveTime;
	}
	
	public ClockVO(){}

	public Calendar getTime() {
		return time;
	}

	public void setTime(Calendar time) {
		this.time = time;
	}

	public ReminderType getType() {
		return type;
	}

	public void setType(ReminderType type) {
		this.type = type;
	}

	public int getCancleType() {
		return cancleType;
	}

	public void setCancleType(int cancleType) {
		this.cancleType = cancleType;
	}

	public int getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}

	public Calendar getSaveTime() {
		return saveTime;
	}

	public void setSaveTime(Calendar saveTime) {
		this.saveTime = saveTime;
	}
	
	public String getTimeString(){
		SimpleDateFormat timeformat=new SimpleDateFormat("HH : mm");
		return timeformat.format(time.getTime()); 
	}

	public String getDayString(){
		Calendar clock=Calendar.getInstance();
		clock.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		clock.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
		//如果当天就有闹钟
		if(clock.get(Calendar.DAY_OF_WEEK) == type.getDay() && clock.getTimeInMillis()<=Calendar.getInstance().getTimeInMillis()){
			clock.add(Calendar.DAY_OF_MONTH, 7);
		}
		while (clock.get(Calendar.DAY_OF_WEEK) != type.getDay()) {
			// 应该可以跨月加的吧。。。
			clock.add(Calendar.DAY_OF_MONTH, 1);
		}
		String date=(clock.get(Calendar.MONTH)+1)+"月"+clock.get(Calendar.DAY_OF_MONTH)+"日";
		return date;
	}
}
