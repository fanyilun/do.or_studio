package com.geeker.door.businesslogic;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.network.NetworkHelper;

public class BoardService {

	DbManager dbHelper;
	public BoardService(Context c){
		dbHelper=new DbManager(c);
	}
	
	public Object[][] getBoard(int lastEventID,int eventType){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventType", String.valueOf(eventType%4)));
		if(lastEventID>=0){
			params.add(new BasicNameValuePair("lastEventID", String.valueOf(lastEventID)));
		}
		String url="CommonBoard/get_event_order_by_setTime";
		if(eventType>3){
			url="CommonBoard/get_event_order_by_aimTime";
		}
		Object[][] networkResult = NetworkHelper.HTTPpostForArray(url, params,
							new String[][]{{"eventID","int"},{"operationType","int"},{"eventType","int"}
							,{"userName","string"},{"nickname","string"},{"setTime","long"},{"aimTime","long"}
							,{"relationEventID","string"},{"message","string"},{"numOfZan","int"}
							,{"numOfBs","int"},{"numOfComment","int"},{"headURL","string"}});
		//if(networkResult==null){return new Object[0][0];}
		Object[][] result=new Object[networkResult.length][14];
		for (int i = 0; i < result.length; i++) {
			//data.add(new ItemEntity("4月6日", "08:46", "开会", "15:53",EventVO.TYPE_ALARM,0));
			result[i]=test(networkResult[i]);
			/*Calendar setTime=Calendar.getInstance();
			setTime.setTimeInMillis((Long)networkResult[i][5]*1000);
			Calendar aimTime=Calendar.getInstance();
			aimTime.setTimeInMillis((Long)networkResult[i][6]*1000);
			String date=setTime.get(Calendar.MONTH)+1+"月"+setTime.get(Calendar.DAY_OF_MONTH)+"日";
			SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm");
			String time=timeformat.format(new Date(setTime.getTimeInMillis()));
			String title="";
			String content="";
			JSONObject msg=null;
			String name=networkResult[i][4].equals("null")?(String)networkResult[i][3]:(String)networkResult[i][4];
			System.out.println("Name="+name);
			try {
				msg=new JSONObject((String)networkResult[i][8]);
				switch ((Integer)networkResult[i][2]) {
				case EventVO.TYPE_ALARM:
					title=name+operationType2String((Integer)networkResult[i][1])+"了一个闹钟："+msg.getString("tag");
					content=timeformat.format(new Date(aimTime.getTimeInMillis()));
					break;
				case EventVO.TYPE_SCHEDULE:
					title=name+operationType2String((Integer)networkResult[i][1])+"了一个备忘：";
					content=msg.getString("title");
					break;
				case EventVO.TYPE_REQUEST:
					title=name+operationType2String((Integer)networkResult[i][1])+"了一个请求：";
					content=msg.getString("title");
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			result[i][0]=date;
			result[i][1]=content;
			result[i][2]=title;
			result[i][3]=time;
			result[i][4]=networkResult[i][2];
			result[i][5]=networkResult[i][0];
			result[i][6]=networkResult[i][9];
			result[i][7]=networkResult[i][10];
			result[i][8]=networkResult[i][11];
			result[i][9]=networkResult[i][7];
			result[i][10]=networkResult[i][12];*/
		}
		return result;
	}

	public Object[][] getRelated(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[][] networkResult = NetworkHelper.HTTPpostForArray("CommonBoard/get_event_past_state", params,
							new String[][]{{"eventID","int"},{"operationType","int"},{"eventType","int"}
							,{"userName","string"},{"nickname","string"},{"setTime","long"},{"aimTime","long"}
							,{"relationEventID","string"},{"message","string"},{"numOfZan","int"}
							,{"numOfBs","int"},{"numOfComment","int"}});
		//if(networkResult==null){return new Object[0][0];}
		Object[][] result=new Object[networkResult.length][14];
		for (int i = 0; i < result.length; i++) {
			result[i]=test(networkResult[i]);
		}
		return result;
	}
	
	public int like(int eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", String.valueOf(eventID)));
		Object[] result=NetworkHelper.HTTPpost("CommonBoard/set_zan", params,
				new String[][]{{"setZan","boolean"},{"zanNum","int"}});
		if(result.length<1){return 0;}
		return (Integer)result[1];
	}
	
	public int dislike(int eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", String.valueOf(eventID)));
		Object[] result=NetworkHelper.HTTPpost("CommonBoard/set_bs", params,
				new String[][]{{"setBs","boolean"},{"bsNum","int"}});
		if(result.length<1){return 0;}
		return (Integer)result[1];
	}
	
	public Object[] getDetails(int eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", String.valueOf(eventID)));
		Object[] networkResult=NetworkHelper.HTTPpost("CommonBoard/get_event", params,
				new String[][]{{"eventID","int"},{"operationType","int"},{"eventType","int"}
				,{"userName","string"},{"nickname","string"},{"setTime","long"},{"aimTime","long"}
				,{"relationEventID","string"},{"message","string"},{"zanNum","int"}
				,{"bsNum","int"},{"commentNum","int"},{"headURL","string"}});
		Object[] result=test(networkResult);
		return result;
	}
	
	private Object[] test(Object[] networkResult){
		if(networkResult.length==0){
			return new Object[0];
		}
		Object[] result=new Object[14];
		try {
		Calendar setTime=Calendar.getInstance();
		setTime.setTimeInMillis((Long)networkResult[5]*1000);
		Calendar aimTime=Calendar.getInstance();
		aimTime.setTimeInMillis((Long)networkResult[6]*1000);
		String date=setTime.get(Calendar.MONTH)+1+"月"+setTime.get(Calendar.DAY_OF_MONTH)+"日";
		SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm");
		String time=timeformat.format(new Date(setTime.getTimeInMillis()));
		String title="";
		String content="";
		String subcontent="";
		String[] tags=new String[0];
		String[] friends=new String[0];
		JSONObject msg=null;
		String name=networkResult[4].equals("null")?(String)networkResult[3]:(String)networkResult[4];
			msg=new JSONObject((String)networkResult[8]);
			switch ((Integer)networkResult[2]) {
			case EventVO.TYPE_ALARM:
				title=name+operationType2String((Integer)networkResult[1])+"了一个闹钟：";
				content=timeformat.format(new Date(aimTime.getTimeInMillis()));
				subcontent=aimTime.get(Calendar.MONTH)+1+"月"+aimTime.get(Calendar.DAY_OF_MONTH)+"日";
				tags=new String[]{msg.getString("tag")};
				break;
			case EventVO.TYPE_SCHEDULE:
				title=name+operationType2String((Integer)networkResult[1])+"了一个备忘：";
				content=msg.getString("title");
				subcontent=msg.getString("content");
				JSONArray arr=new JSONArray(msg.getString("tag"));
				tags=new String[arr.length()];
				for (int i = 0; i < arr.length(); i++) {
					tags[i]=arr.get(i).toString();
				}
				break;
			case EventVO.TYPE_REQUEST:
				title=name+operationType2String((Integer)networkResult[1])+"了一个请求：";
				content=msg.getString("title");
				subcontent=msg.getString("content");
				JSONArray arr2=new JSONArray(msg.getString("companies"));
				friends=new String[arr2.length()];
				for (int i = 0; i < arr2.length(); i++) {
					friends[i]=arr2.get(i).toString();
				}
				break;
			}
			result[0]=date;
			result[1]=content;
			result[2]=title;
			result[3]=time;
			result[4]=networkResult[2];
			result[5]=networkResult[0];
			result[6]=networkResult[9];
			result[7]=networkResult[10];
			result[8]=networkResult[11];
			result[9]=networkResult[7];
			result[11]=subcontent;
			result[12]=tags;
			result[13]=friends;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		if(networkResult.length>12){
			result[10]=networkResult[12];
		}
		return result;
	}
	
	
	public String[] getLikeURLs(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[][] networkResult=NetworkHelper.HTTPpostForArray("EventBoard/get_zan_head", params,
				new String[][]{{"userName","string"},{"headURL","string"}});
		String[] result=new String[networkResult.length];
		for (int i = 0; i < result.length; i++) {
			result[i]=networkResult[i][1].toString();
		}
		return result;
	}
	

	public String[] getDislikeURLs(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[][] networkResult=NetworkHelper.HTTPpostForArray("EventBoard/get_bs_head", params,
				new String[][]{{"userName","string"},{"headURL","string"}});
		String[] result=new String[networkResult.length];
		for (int i = 0; i < result.length; i++) {
			result[i]=networkResult[i][1].toString();
		}
		return result;
	}
	

	public Object[][] getComment(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[][] networkResult=NetworkHelper.HTTPpostForArray("EventBoard/get_comment", params,
				new String[][]{{"userName","string"},{"nickname","string"},{"headURL","string"},{"aimUserName","string"}
				,{"text","string"},{"setTime","string"},{"aimUserNickname","string"}});
		for (int i = 0; i < networkResult.length; i++) {
			networkResult[i][5]=time2String(Long.parseLong((String)networkResult[i][5]));
		}
		return networkResult;
	}
	
	
	public boolean addComment(String eventID,String aimUserName,String text){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("eventID", eventID));
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("aimUserName", aimUserName));
		params.add(new BasicNameValuePair("text", text));
		Object[] networkResult=NetworkHelper.HTTPpost("EventBoard/set_comment", params,
				new String[][]{{"succ","boolean"}});
		if(networkResult.length<1){return false;}
		return (Boolean)networkResult[0];
	}
	
	/**
	 * @return
	 * 0 昵称
	 * 1 头像URL
	 * 2 时间
	 * 3 消息内容1
	 * 4 消息内容2
	 */
	public Object[][] getUnreadNotifiction(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[][] networkResult=NetworkHelper.HTTPpostForArray("Notice/get_unread_notice", params,
				new String[][]{{"nickname","string"},{"headURL","string"},{"setTime","long"},{"message","string"}
				,{"extraMessage","string"},{"type","int"},{"noticeID","string"},{"senderName","string"}});
		if(networkResult.length<1){return new Object[0][0];}
		for (int i = 0; i < networkResult.length; i++) {
			networkResult[i][2]=time2String((Long)networkResult[i][2]);
			//处理消息内容
			String content1="";
			String content2="";
			int eventType=0;
			int eventID=0;
			String title="";
			try {
				JSONObject json=new JSONObject(networkResult[i][4].toString());
				title=json.getString("title");
				eventID=json.getInt("eventID");
				eventType=json.getInt("eventType");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			switch ((Integer)networkResult[i][5]) {
			case 1://系统通知
				String msg2="";
				msg2=networkResult[i][3].toString();
				content1=""+msg2;
				content2="来自可爱的系统管理员";
				break;
			case 2://添加好友
				content1="请求添加你为好友";
				content2="验证信息： "+title;
				break;
			case 3://请求
				content1="邀请了你";
				content2="在他的 "+ type2String(eventType)+" "+title;
				break;
			case 4://评论
				String msg="";
				if(networkResult[i][3].toString().indexOf(":")>0){
					msg=networkResult[i][3].toString().substring(networkResult[i][3].toString().indexOf(":")+1);
				}
				content1="评论了你："+msg;
				if(eventType==1 && isNumeric(title) && title.length()>9){
					//时间 秒数转换格式
					title=time2String(Long.parseLong(title));
				}
				content2="在他的 "+ type2String(eventType) +" "+title;
				break;
			case 5:
				content1="我通过了你的好友请求";
				content2="";
				eventID=-1;
				break;
			}
			networkResult[i][3]=content1;
			networkResult[i][4]=content2;
			networkResult[i][5]=eventID;
		}
		return networkResult;
	}
	
	public int getNotifictionNum(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[] networkResult=NetworkHelper.HTTPpost("Notice/get_unread_notice_num", params,
				new String[][]{{"count","int"}});
		if(networkResult.length<1 || networkResult[0]==null){return 0;}
		return (Integer)networkResult[0];
	}
	
	public void readNotifiction(String noticeID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("noticeID", noticeID));
		NetworkHelper.HTTPpost("Notice/set_notice_read", params,new String[][]{{}});
	}
	
	public void readAllNotifiction(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		NetworkHelper.HTTPpost("Notice/set_all_notice_read", params,new String[][]{{}});
	}
	
	public Object[][] getRenrenFriend(String accessToken, String renrenID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("renrenID",renrenID));
		params.add(new BasicNameValuePair("accessToken", accessToken));
		Object[][] objResults = NetworkHelper.HTTPpostForArray("User/get_renren_friend", params, 
				new String[][]{{"userName","String"},{"name","String"},{"renrenID","int"},{"headURL","String"}});
		return objResults;
	}
	
	public Boolean addFriendRequest(String friendName,String mesage){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("friendName", friendName));
		params.add(new BasicNameValuePair("message", mesage));
		Object[] objResults = NetworkHelper.HTTPpost("User/send_friend_request", params, 
				new String[][]{{"succ","boolean"}});
		if(objResults.length<1){return false;}
		return (Boolean)objResults[0];
	}
	
	
	public Boolean addFriend(String friendName){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("friendName", friendName));
		Object[] objResults = NetworkHelper.HTTPpost("User/add_friend", params, 
				new String[][]{{"succ","boolean"}});
		if(objResults.length<1){return false;}
		return (Boolean)objResults[0];
	}
	
	public String sign(String eventID,String message){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", eventID));
		params.add(new BasicNameValuePair("message", message));
		Object[] objResults = NetworkHelper.HTTPpost("Sign/sign", params, 
				new String[][]{{"succ","boolean"},{"reason","int"},{"days","int"},{"score","int"},{"allDays","int"}});
		if(objResults.length<1){return "网络连接失败";}
		switch ((Integer)objResults[1]) {
		case 1:
			return "闹钟还没有完成哦";
		case 2:
			return "闹钟目标时间不在5点-11点哦";
		case 3:
			return "不是在起床后5-15分钟内签到哦";
		case 4:
			return "今天已经签过到了哦";
		}
		return "签到失败";
	}
	
	
	public Object[][] getMyFriend(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[][] objResults = NetworkHelper.HTTPpostForArray("User/select_friend", params, 
				new String[][]{{"userName","String"},{"nickname","String"}});
		return objResults;
	}
	
	
	private String operationType2String(int operationType){
		String result="";
		switch (operationType) {
		case 1:
			result="添加";
			break;
		case 2:
			result="修改";
			break;
		case 3:
			result="延时";
			break;
		case 4:
			result="完成";
			break;
		case 5:
			result="错过";
			break;
		}
		return result;
	}
	
	private String type2String(int type){
		String result="";
		switch (type) {
		case 1:
			result="闹钟";
			break;
		case 2:
			result="备忘";
			break;
		case 3:
			result="请求";
			break;
		}
		return result;
	}

	public static boolean isNumeric(String str) {
		for (int i = str.length(); --i >= 0;) {
			if (!Character.isDigit(str.charAt(i))) {
				return false;
			}
		}
		return true;
	}
	
	private String time2String(long time){
		Calendar c=Calendar.getInstance();
		c.setTimeInMillis(time*1000);
		SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm");
		String time1=timeformat.format(new Date(c.getTimeInMillis()));
		return c.get(Calendar.MONTH)+1+"月"+c.get(Calendar.DAY_OF_MONTH)+"日  "+time1;
	}
	
	public String[][] searchFriends(String userInfo){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("userInfo", userInfo));
		Object[] objResults = NetworkHelper.HTTPpost("User/search_user", params, 
				new String[][]{{"succ","boolean"},{"user","string"}});
		boolean succ=(Boolean)objResults[0];
		if(!succ){return null;}
		JSONArray json;
		String[][] result=null;
		try {
			json = new JSONArray(objResults[1].toString());
			result=new String[json.length()][3];
			for (int i = 0; i < result.length; i++) {
				JSONObject jsonobj=new JSONObject(json.get(i).toString());
				result[i][0]=jsonobj.getString("userName");
				result[i][1]=jsonobj.getString("nickname");
				result[i][2]=jsonobj.getString("headURL");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	
	public Object[][] getContactBookUser(HashMap<String , String> map){
		ArrayList<String> s = new ArrayList<String>();
		for(String key : map.keySet()){
			s.add(map.get(key));
		}
		JSONArray arr=new JSONArray(s);
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("phoneUser", arr.toString()));
		Object[][] objResults = NetworkHelper.HTTPpostForArray("User/get_contacts_book_user", params, 
				new String[][]{{"userName","String"},{"nickname","String"},{"headURL","String"},{"phoneNum","String"}});
		return objResults;
	}
	
	public int forceAlarm(String eventID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("eventID", eventID));
		Object[] objResults = NetworkHelper.HTTPpost("CommonBoard/force_call", params, 
				new String[][]{{"succ","boolean"},{"reasson","int"}});
		if(objResults.length<1){return 5;}
		return (Integer)objResults[1];
	}

	public String getExp() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[] objResults = NetworkHelper.HTTPpost("User/get_user_score", params, 
				new String[][]{{"score","int"}});
		if(objResults.length<1){return "0";}
		return objResults[0].toString();
	}

	public String getSignNum() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[] objResults = NetworkHelper.HTTPpost("Sign/get_continuity_days", params, 
				new String[][]{{"myDays","int"}});
		if(objResults.length<1){return "0";}
		return objResults[0].toString();
	}

	public String getLev() {
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		Object[] objResults = NetworkHelper.HTTPpost("User/get_user_level", params, 
				new String[][]{{"level","int"}});
		if(objResults.length<1){return "0";}
		return objResults[0].toString();
	}
	
	
}
