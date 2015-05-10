package com.geeker.door.network;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Build;

public class NetworkHelper {
	
	public NetworkHelper(){
		
	}
	public static Object[] HTTPpost(String urlEnding,List<NameValuePair> requestParam,String[][] resultParam){
		// 网络连接不能放在主线程里
		String url = "http://1.doorserver.sinaapp.com/index.php/" + urlEnding;
		System.setProperty("http.keepAlive", "false");
		// 第一步，创建HttpPost对象
		HttpPost httpPost = new HttpPost(url);
		// 设置HTTP POST请求参数必须用NameValuePair对象
		HttpResponse httpResponse = null;
		Object[] objResult=new Object[resultParam.length];
		try {
			System.out.println("stratHttp"+requestParam);
			// 设置httpPost请求参数
			httpPost.setEntity(new UrlEncodedFormEntity(requestParam, HTTP.UTF_8));
			DefaultHttpClient dc=new DefaultHttpClient();
			httpResponse = dc.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {// 200表示请求成功
				// 第三步，使用getEntity方法活得返回结果
				String result = EntityUtils.toString(httpResponse.getEntity());
				if(result.contains("{")){
					result =result.substring(result.indexOf("{"),result.lastIndexOf("}")+1);
				}
				String s = new String(result);
				
				System.out.println("result:" + s);
				JSONObject json = new JSONObject(result);
				for (int i = 0; i < resultParam.length; i++) {
					if(resultParam[i][1].equals("int")){
						objResult[i]=json.getInt(resultParam[i][0]);
					}else if(resultParam[i][1].equals("boolean")){
						objResult[i]=json.getBoolean(resultParam[i][0]);
					}else if(resultParam[i][1].equals("double")){
						objResult[i]=json.getDouble(resultParam[i][0]);
					}else if(resultParam[i][1].equals("long")){
						objResult[i]=json.getLong(resultParam[i][0]);
					}else{
						objResult[i]=json.getString(resultParam[i][0]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(e.toString().contains("ECONNRESET") && !urlEnding.contains("SetEvent")){
				return HTTPpost(urlEnding,requestParam,resultParam);
			}else{
				return new Object[0];
			}
		}
		return objResult;
	}
	
	public static String[] HTTPGetWeather(String city) {
		// 第1步：创建HttpGet对象
		String[] result=new String[2];
		System.setProperty("http.keepAlive", "false");
		HttpGet httpGet = new HttpGet("http://www.weather.com.cn/data/cityinfo/"+city+".html");
		HttpResponse httpResponse = null;
		// 第2步：使用execute方法发送HTTPGET请求，并返回HttpResponse对象
		try {
			httpResponse = new DefaultHttpClient().execute(httpGet);
			// 判断请求响应状态码，状态码为200表示服务端成功响应了客户端的请求
			if (httpResponse.getStatusLine().getStatusCode() == 200){
				// 第3步：使用getEntity方法获得返回结果
				String resultJson = EntityUtils.toString(httpResponse.getEntity());
				// 去掉返回结果中的"\r"字符，否则会在结果字符串后面显示一个小方格
				resultJson.replaceAll("\r", "");
				//json处理
				JSONObject json = new JSONObject(resultJson);
				json=json.getJSONObject("weatherinfo");
				result[0]=json.getString("temp1")+"~"+json.getString("temp2");
				result[1]=json.getString("weather");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static Object[][] HTTPpostForArray(String urlEnding,List<NameValuePair> requestParam,String[][] resultParam){
		// 网络连接不能放在主线程里
		String url = "http://1.doorserver.sinaapp.com/index.php/" + urlEnding;
		System.setProperty("http.keepAlive", "false");
		// 第一步，创建HttpPost对象
		HttpPost httpPost = new HttpPost(url);
		// 设置HTTP POST请求参数必须用NameValuePair对象
		HttpResponse httpResponse = null;
		Object[][] objResult=new Object[0][0];
		try {
			System.out.println("stratHttp"+requestParam);
			// 设置httpPost请求参数
			httpPost.setEntity(new UrlEncodedFormEntity(requestParam, HTTP.UTF_8));
			DefaultHttpClient dc=new DefaultHttpClient();
			httpResponse = dc.execute(httpPost);
			if (httpResponse.getStatusLine().getStatusCode() == 200) {// 200表示请求成功
				String result = EntityUtils.toString(httpResponse.getEntity());
				if(result.contains("[")){
					result =result.substring(result.indexOf("["),result.lastIndexOf("]")+1);
				}
				String s = new String(result);
				System.out.println("result:" + s);
				JSONArray jsonArr=new JSONArray(s);
				objResult=new Object[jsonArr.length()][resultParam.length];
				for (int j = 0; j < objResult.length; j++) {
					JSONObject json=jsonArr.getJSONObject(j);
					for (int i = 0; i < resultParam.length; i++) {
						if(resultParam[i][1].equals("int")){
							objResult[j][i]=json.getInt(resultParam[i][0]);
						}else if(resultParam[i][1].equals("boolean")){
							objResult[j][i]=json.getBoolean(resultParam[i][0]);
						}else if(resultParam[i][1].equals("double")){
							objResult[j][i]=json.getDouble(resultParam[i][0]);
						}else if(resultParam[i][1].equals("long")){
							objResult[j][i]=json.getLong(resultParam[i][0]);
						}else{
							objResult[j][i]=json.getString(resultParam[i][0]);
						}
					}
				}
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			if(e.toString().contains("ECONNRESET")&&!urlEnding.contains("SetEvent")){
				return HTTPpostForArray(urlEnding,requestParam,resultParam);
			}else{
				return new Object[0][0];
			}
		}
		return objResult;
	}
	
	
	
}
