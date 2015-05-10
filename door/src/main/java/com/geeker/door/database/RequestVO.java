package com.geeker.door.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * 
 * @author liushao
 * @version 1.0
 * @since 2014-3-15
 *
 */
public class RequestVO {
	private String title;
	private String content;
	private Calendar  occurTime;
	private Calendar saveTime;
	private String[] companies;
	private String[] tags;
	private int requestCode;
	private boolean[] switches;
	
	public RequestVO(String title,String content,Calendar occurTime,Calendar saveTime,String[] companies,
			String[] tags,int requestCode,boolean[] switches){
		this.title = title;
		this.content = content;
		this.occurTime = occurTime;
		this.saveTime = saveTime;
		this.companies = companies;
		this.tags = tags;
		this.requestCode = requestCode;
		this.switches=switches;
	}

	public String getTitle() {
		return title;
	}

	public boolean[] getSwitches() {
		return switches;
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
		return occurTime;
	}

	public void setOccurTime(Calendar occurTime) {
		this.occurTime = occurTime;
	}

	public Calendar getSaveTime() {
		return saveTime;
	}

	public void setSaveTime(Calendar saveTime) {
		this.saveTime = saveTime;
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

	public int getRequestCode() {
		return requestCode;
	}

	public void setRequestCode(int requestCode) {
		this.requestCode = requestCode;
	}
	
	public String getTimeString(){
		SimpleDateFormat time=new SimpleDateFormat("HH : mm");
		String s= time.format(occurTime.getTime());
		return occurTime.get(Calendar.YEAR)+"年"+(occurTime.get(Calendar.MONTH)+1)+"月"+occurTime.get(Calendar.DAY_OF_MONTH)+"日   "+s;
	}
	
	public String getTimeStringWithoutYear(){
		return (occurTime.get(Calendar.MONTH)+1)+"月"+occurTime.get(Calendar.DAY_OF_MONTH)+"日";
	}
	
}
