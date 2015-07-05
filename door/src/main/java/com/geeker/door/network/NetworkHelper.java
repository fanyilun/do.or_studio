package com.geeker.door.network;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import android.util.Log;

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
		String[] result=new String[14];//城市 PM2.5  今天 现在温度  现在天气  明天  明天温度  明天天气  后天  后天温度  后天天气 大后天 大后天温度 大后天天气
		System.setProperty("http.keepAlive", "false");
		// 第2步：使用execute方法发送HTTPGET请求，并返回HttpResponse对象
		try {
			String sn=getSN(city);
			HttpGet httpGet = new HttpGet("http://api.map.baidu.com/telematics/v3/weather?location="+city+"&output=json&ak=EEc1678d133a9029a1958802fad9927f&sn="+sn);
			HttpResponse httpResponse = new DefaultHttpClient().execute(httpGet);
			// 判断请求响应状态码，状态码为200表示服务端成功响应了客户端的请求
			if (httpResponse.getStatusLine().getStatusCode() == 200){
				// 第3步：使用getEntity方法获得返回结果
				String resultJson = EntityUtils.toString(httpResponse.getEntity());
				// 去掉返回结果中的"\r"字符，否则会在结果字符串后面显示一个小方格
				resultJson.replaceAll("\r", "");
				//json处理
				JSONObject json = new JSONObject(resultJson);
				json=json.getJSONArray("results").getJSONObject(0);

				result[0]=json.getString("currentCity");
				result[1]=json.getString("pm25");

				JSONArray arr=json.getJSONArray("weather_data");
				for (int i=0;i<arr.length();i++){
					json=arr.getJSONObject(i);
					result[i*3+2]=json.getString("date");
					result[i*3+3]=json.getString("temperature");
					result[i*3+4]=json.getString("weather");
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}

	private static String getSN(String city) throws Exception{
			Map paramsMap = new LinkedHashMap<String, String>();
			paramsMap.put("location", city);
			paramsMap.put("output", "json");
			paramsMap.put("ak", "EEc1678d133a9029a1958802fad9927f");

			// 调用下面的toQueryString方法，对LinkedHashMap内所有value作utf8编码，拼接返回结果address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourak
			String paramsStr = toQueryString(paramsMap);

			// 对paramsStr前面拼接上/geocoder/v2/?，后面直接拼接yoursk得到/geocoder/v2/?address=%E7%99%BE%E5%BA%A6%E5%A4%A7%E5%8E%A6&output=json&ak=yourakyoursk
			String wholeStr = new String("/telematics/v3/weather?" + paramsStr + "CA0f85f1e52e9de1d31c5675a49c7bbd");

			// 对上面wholeStr再作utf8编码
			String tempStr = URLEncoder.encode(wholeStr, "UTF-8");
			return MD5(tempStr);
	}


	private static String toQueryString(Map<?, ?> data) throws UnsupportedEncodingException
	{
		StringBuffer queryString = new StringBuffer();
		for (Map.Entry<?, ?> pair : data.entrySet())
		{
			queryString.append(pair.getKey() + "=");
			queryString.append(URLEncoder.encode((String) pair.getValue(), "UTF-8") + "&");
		}
		if (queryString.length() > 0)
		{
			queryString.deleteCharAt(queryString.length() - 1);
		}
		return queryString.toString();
	}

	// 来自stackoverflow的MD5计算方法，调用了MessageDigest库函数，并把byte数组结果转换成16进制
	private static String MD5(String md5)
	{
		try
		{
			java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
			byte[] array = md.digest(md5.getBytes());
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < array.length; ++i)
			{
				sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
			}
			return sb.toString();
		} catch (java.security.NoSuchAlgorithmException e)
		{
		}
		return null;
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
