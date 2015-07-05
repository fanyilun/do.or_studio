package com.geeker.door.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;


/**
 * 
 * @author liushao
 *	@version 1.0
 * @since 2014-3-15
 */
public class MemoVO {
	private String title;
	private String content;
	private Calendar occurTime;
	private Calendar reminderTime;
	private Calendar saveTime;
	private String ring;
	private int vol;
	private String[] companies;
	private String[] tags;
	private int requestCode;
	private boolean[] switches;
	
	
	public MemoVO(String title,String content,Calendar occurTime,Calendar reminderTime,Calendar saveTime,
			String ring,int vol,String[] companies,String[] tags,int requestCode,boolean[] switches){
		this.title = title;
		this.content = content;
		this.occurTime = occurTime;
		this.reminderTime = reminderTime;
		this.saveTime = saveTime;
		this.ring = ring;
		this.vol = vol;
		this.companies = companies;
		this.tags = tags;
		this.requestCode = requestCode;
		this.switches=switches;
	}


	public int getRequestCode() {
		return requestCode;
	}


	public boolean[] getSwitches() {
		return switches;
	}


	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}


	public String getTitle() {
		return title;
	}


	public void setTitle(String title) {
		this.title = title;
	}


	public String getContent() {
		return content;
	}


	public void setContent(String content) {
		this.content = content;
	}


	public Calendar getOccurTime() {
		if(occurTime==null){return Calendar.getInstance();}
		return occurTime;
	}


	public void setOccurTime(Calendar occurTime) {
		this.occurTime = occurTime;
	}


	public Calendar getReminderTime() {
		return reminderTime;
	}


	public void setReminderTime(Calendar reminderTime) {
		this.reminderTime = reminderTime;
	}


	public Calendar getSaveTime() {
		return saveTime;
	}


	public void setSaveTime(Calendar saveTime) {
		this.saveTime = saveTime;
	}


	public String getRing() {
		return ring;
	}


	public void setRing(String ring) {
		this.ring = ring;
	}


	public int getVol() {
		return vol;
	}


	public void setVol(int vol) {
		this.vol = vol;
	}


	public String[] getCompanies() {
		if(companies==null){return new String[0];}
		return companies;
	}


	public void setCompanies(String[] companies) {
		this.companies = companies;
	}


	public String[] getTags() {
		if(tags==null){return new String[0];}
		return tags;
	}


	public void setTags(String[] tags) {
		this.tags = tags;
	}
	
	public String getTimeString(){
		SimpleDateFormat time=new SimpleDateFormat("HH : mm");
		if(occurTime==null){return "未设置时间";}
		String s= time.format(occurTime.getTime());
		return occurTime.get(Calendar.YEAR)+"年"+(occurTime.get(Calendar.MONTH)+1)+"月"+occurTime.get(Calendar.DAY_OF_MONTH)+"日   "+s;
	}
	
	public String getTimeStringWithoutYear(){
		if(occurTime==null){
			return "";
		}
		return (occurTime.get(Calendar.MONTH)+1)+"月"+occurTime.get(Calendar.DAY_OF_MONTH)+"日";
	}
}
