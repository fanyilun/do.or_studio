package com.geeker.door.utils;

public class ItemEntity {
	private String mTitle;
	private String mContent;
	private String msubContent;
	private String addTime;
	private int type;
	private int requestCode;
	private int likeNum;
	private int dislikeNum;
	private int commentNum;
	private String relatedID;
	private String headURL;
	private String content2;
	private String[] tags;
	private String[] friends;
	

	public String getContent2() {
		return content2;
	}

	public ItemEntity(String pTitle, String pContent,String psubContent,String addTime,int type,int requestCode,int likeNum
			,int dislikeNum,int commentNum,String relatedID,String headURL,String content2,String[] tags,String[] friends) {
		mTitle = pTitle;
		mContent = pContent;
		this.addTime=addTime;
		msubContent=psubContent;
		this.type=type;
		this.requestCode=requestCode;
		this.likeNum=likeNum;
		this.dislikeNum=dislikeNum;
		this.commentNum=commentNum;
		this.relatedID=relatedID;
		this.headURL=headURL;
		this.content2=content2;
		this.tags=tags;
		this.friends=friends;
	}

	public String[] getTags() {
		return tags;
	}

	public String[] getFriends() {
		return friends;
	}

	public String getHeadURL() {
		return headURL;
	}

	public int getLikeNum() {
		return likeNum;
	}

	public int getDislikeNum() {
		return dislikeNum;
	}

	public String getRelatedID() {
		return relatedID;
	}

	public void setLikeNum(int likeNum) {
		this.likeNum = likeNum;
	}

	public void setDislikeNum(int dislikeNum) {
		this.dislikeNum = dislikeNum;
	}

	public int getCommentNum() {
		return commentNum;
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
