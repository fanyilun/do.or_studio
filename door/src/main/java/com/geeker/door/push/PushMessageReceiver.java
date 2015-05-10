package com.geeker.door.push;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.geeker.door.EventDetailsActivity;
import com.geeker.door.MessageCenterActivity;
import com.geeker.door.alarm.ForceAlarmActivity;
import com.geeker.door.database.DbManager;

/**
 * Push消息处理receiver
 */
public class PushMessageReceiver extends BroadcastReceiver {
	/** TAG to Log */
	public static final String TAG = PushMessageReceiver.class.getSimpleName();

	AlertDialog.Builder builder;

	/**
	 * @param context
	 *            Context
	 * @param intent
	 *            接收的intent
	 */
	@Override
	public void onReceive(final Context context, Intent intent) {
		Log.d(TAG, ">>> Receive intent: \r\n" + intent);

		if (intent.getAction().equals(PushConstants.ACTION_MESSAGE)) {
			//获取消息内容
			String message = intent.getExtras().getString(
					PushConstants.EXTRA_PUSH_MESSAGE_STRING);
			//消息的用户自定义内容读取方式
			message.replace("\n", "");
			Log.i(TAG, "onMessage: " + message);
			String key1=null;
			String key3=null;
			try {
				JSONObject json=new JSONObject(message);
				json=json.getJSONObject("custom_content");
				key1=json.getString("key1");
				key3=json.getString("key3");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			if(key1!=null && key1.equals("闹钟强制叫醒") && new DbManager(context).isForceAwake()){
				Intent i=new Intent(context, ForceAlarmActivity.class);
				i.putExtra("waker", key3);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
			//自定义内容的json??
        	Log.d(TAG, "EXTRA_EXTRA = " + intent.getStringExtra(PushConstants.EXTRA_EXTRA));

		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVE)) {
			//处理绑定等方法的返回数据
			//PushManager.startWork()的返回??通过PushConstants.METHOD_BIND得到
			
			//获取方法
			final String method = intent
					.getStringExtra(PushConstants.EXTRA_METHOD);
			//方法返回错误码??若绑定返回错误（??），则应用将不能正常接收消息??
			//绑定失败的原因有多种，如网络原因，或access token过期??
			//请不要在出错时进行简单的startWork调用，这有可能导致死循环??
			//可以通过限制重试次数，或者在其他时机重新调用来解决??
			int errorCode = intent
					.getIntExtra(PushConstants.EXTRA_ERROR_CODE,
							PushConstants.ERROR_SUCCESS);
			String content = "";
			if (intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT) != null) {
				//返回内容
				content = new String(
					intent.getByteArrayExtra(PushConstants.EXTRA_CONTENT));
			}

			
			//用户在此自定义处理消??以下代码为demo界面展示??
			Log.d(TAG, "onMessage: method : " + method);
			Log.d(TAG, "onMessage: result : " + errorCode);
			Log.d(TAG, "onMessage: content : " + content);
			/*Toast.makeText(
					context,
					"method : " + method + "\n result: " + errorCode
							+ "\n content = " + content, Toast.LENGTH_LONG)
					.show();*/
			System.out.println("method : " + method + "\n result: " + errorCode
							+ "\n content = " + content);
			if(method.equals("method_bind")){
				try {
					JSONObject json=new JSONObject(content);
					json=json.getJSONObject("response_params");
					//保存pushID
					new DbManager(context).savePushID(json.getString("user_id"));
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
			
			
		//可??。??知用户点击事件处??
		} else if (intent.getAction().equals(PushConstants.ACTION_RECEIVER_NOTIFICATION_CLICK)) {
			Log.d(TAG, "intent=" + intent.toUri(0));
        	Log.d(TAG, "EXTRA_EXTRA = " + intent.getStringExtra(PushConstants.EXTRA_EXTRA));
        	Intent i;
			if(intent.getStringExtra("key2").equals("")){
				i=new Intent(context,MessageCenterActivity.class);
			}else{
				i=new Intent(context,EventDetailsActivity.class);
				i.putExtra("eventID",Integer.parseInt(intent.getStringExtra("key2")));
			}
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			context.startActivity(i);
			/*Intent aIntent = new Intent();
			aIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			aIntent.setClass(context, CustomActivity.class);
			String title = intent
					.getStringExtra(PushConstants.EXTRA_NOTIFICATION_TITLE);
			aIntent.putExtra(PushConstants.EXTRA_NOTIFICATION_TITLE, title);
			String content = intent
					.getStringExtra(PushConstants.EXTRA_NOTIFICATION_CONTENT);
			aIntent.putExtra(PushConstants.EXTRA_NOTIFICATION_CONTENT, content);

			context.startActivity(aIntent);*/
		}
	}
	
	

}
