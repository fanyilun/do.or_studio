package com.geeker.door.businesslogic;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.geeker.door.database.DbManager;
import com.geeker.door.network.NetworkHelper;

public class LoginService {
	
	DbManager dbHelper;
	public LoginService(Context c){
		dbHelper=new DbManager(c);
	}
	
	public boolean registNormally(String userName,String nickName,String password){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", userName));
		params.add(new BasicNameValuePair("nickname", nickName));
		params.add(new BasicNameValuePair("password", password));
		Object[] result=NetworkHelper.HTTPpost("Register/register_without_renren", params,
				new String[][]{{"succ","boolean"}});
		if(result.length<1){return false;}
		if((Boolean)result[0].equals(new Boolean(true))){
			dbHelper.saveUserName(userName);
		}
		return (Boolean)result[0];
	}
	
	public boolean registByRenren(String renrenID,String nickName,String headURL){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("renrenID", renrenID));
		params.add(new BasicNameValuePair("nickname", nickName));
		params.add(new BasicNameValuePair("headURL", headURL));
		Object[] result=NetworkHelper.HTTPpost("Register/register_only_renren", params,
				new String[][]{{"succ","boolean"}});
		if(result.length<1){return false;}
		if((Boolean)result[0].equals(new Boolean(true))){
			dbHelper.saveUserName(renrenID);
		}
		return (Boolean)result[0];
	}
	
	
	public boolean loginNormally(String userName,String password,String type){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", userName));
		params.add(new BasicNameValuePair("password", password));
		params.add(new BasicNameValuePair("pushID", dbHelper.getPushID()));
		/*Object[] result=NetworkHelper.HTTPpost("Login/login", params,
				new String[][]{{"isLogin","boolean"},{"reason","int"},
				{"name","string"},{"renrenID","int"},{"phoneNum","long"},
				{"touxiangURL","string"}});*/
		Object[] result=NetworkHelper.HTTPpost("Login/login", params,
				new String[][]{{"succ","boolean"},{"nickname","string"},{"headURL","string"}});
		if(result.length<1){return false;}
		if((Boolean)result[0].equals(new Boolean(true))){
			dbHelper.saveUserName(userName);
			dbHelper.saveKey("nickName", result[1].toString());
			dbHelper.saveKey("headURL", result[2].toString());
		}
		return (Boolean)result[0];
	}

	public boolean loginByRenren(String renrenID){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("renrenID", renrenID));
		params.add(new BasicNameValuePair("pushID", dbHelper.getPushID()));
		Object[] result=NetworkHelper.HTTPpost("Login/login_only_renren", params,
				new String[][]{{"succ","boolean"},{"nickname","string"},{"headURL","string"},{"userName","string"}});
		if(result.length<1){return false;}
		if((Boolean)result[0].equals(new Boolean(true))){
			dbHelper.saveUserName(result[3].toString());
			dbHelper.saveKey("nickName", result[1].toString());
			dbHelper.saveKey("headURL", result[2].toString());
			dbHelper.saveKey("renrenID", renrenID);
		}
		return (Boolean)result[0];
	}
	
	public Object[] getRenrenUserInfo(String rennID,String token){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("renrenID", rennID));
		params.add(new BasicNameValuePair("accessToken", token));
		Object[] result=NetworkHelper.HTTPpost("User/get_renren_info", params,
				new String[][]{{"name","string"},{"headURL","string"}});
		dbHelper.saveKey("nickName", result[0].toString());
		dbHelper.saveKey("headURL", result[1].toString());
		dbHelper.saveKey("renrenID", rennID);
		return result;
	}
	
	public void initUserInfo(){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("pushID", dbHelper.getPushID()));
		Object[] result=NetworkHelper.HTTPpost("User/get_my_info", params,
				new String[][]{{"succ","boolean"},{"userInfo","string"}});
		try {
			JSONObject json=new JSONObject((String)result[1]);
			dbHelper.saveKey("nickName", json.getString("nickname"));
			dbHelper.saveKey("renrenID", json.getString("renrenID"));
			dbHelper.saveKey("headURL", json.getString("headURL"));
			dbHelper.saveKey("phoneNum", json.getString("phoneNum"));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	public boolean bindPhone(String phoneNum){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("phoneNum", phoneNum));
		Object[] result = NetworkHelper.HTTPpost("Register/bind_phoneNum_without_message", params, 
				new String[][]{{"succ","boolean"}});
		
		if(result.length < 1){
			return false;
		}
		return (Boolean)result[0];
	}
	
	public boolean modifyInfo(String nickName){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("nickname", nickName));
		Object[] result = NetworkHelper.HTTPpost("User/modify_info", params, 
				new String[][]{{"succ","boolean"}});
		
		if(result.length < 1){
			return false;
		}
		return (Boolean)result[0];
	}
	
	public boolean modifyPass(String oldPassword,String newPassword){
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		
		params.add(new BasicNameValuePair("userName", dbHelper.getUserName()));
		params.add(new BasicNameValuePair("password", oldPassword));
		params.add(new BasicNameValuePair("newPassword", newPassword));
		
		Object[] result = NetworkHelper.HTTPpost("User/modify_password", params, 
				new String[][]{{"succ","boolean"}});
		if (result.length < 1) {
			return false;
		}
		return (Boolean)result[0];
	}
	
	public final static String MD5(String s){
		char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd','e', 'f'};
		try {
		MessageDigest mdTemp = MessageDigest.getInstance("MD5");
		mdTemp.update(s.getBytes());
		byte[] md = mdTemp.digest();
		char str[] = new char[md.length * 2];
		int k = 0;
		for (int i = 0; i < md.length; i++) {
		byte byte0 = md[i];
		str[k++] = hexDigits[byte0 >>> 4 & 0xf];
		str[k++] = hexDigits[byte0 & 0xf];
		}
		return new String(str);
		}
		catch (Exception e){
		return null;
		}
	}
}
