package com.geeker.door.database;

public class ReminderType {
	
	private int day;
	private String ring;
	private String tag;
	private int volume;
	private int[] reminder;
	
	/**
	 * 
	 * @param day ????X
	 * @param ring ????????
	 * @param tag ???
	 * @param vol ????
	 * @param reminder ??????
	 */
	public ReminderType(int day,String ring,String tag,int vol,boolean[] reminder){
		this.day = day;
		this.ring = ring;
		this.tag = tag;
		this.volume = vol;
		this.reminder = new int[reminder.length];
		for (int i = 0; i < reminder.length; i++) {
			if(reminder[i]){
				this.reminder[i]=1;
			}else{
				this.reminder[i]=0;
			}
		}
	}
	
	public ReminderType(int day,String ring,String tag,int vol,int[] reminder){
		this.day = day;
		this.ring = ring;
		this.tag = tag;
		this.volume = vol;
		this.reminder = reminder;
		
	}
	
	public ReminderType(){}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public String getRing() {
		return ring;
	}

	public void setRing(String ring) {
		this.ring = ring;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int[] getReminder() {
		return reminder;
	}

	public void setReminder(int[] reminder) {
		this.reminder = reminder;
	}
	
	

}
