package com.geeker.door.renren;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import com.geeker.door.database.EventVO;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;
import com.renn.rennsdk.RennExecutor.CallBack;
import com.renn.rennsdk.RennResponse;
import com.renn.rennsdk.exception.RennException;
import com.renn.rennsdk.param.PutStatusParam;

public class RenrenService {
	
	private RennClient rennClient;
	private Context context;
	private Activity activity;
	public RenrenService(Activity activity) {
		this.context=activity;
		this.activity=activity;
		//RennClient初始化
				rennClient = RennClient.getInstance(context);
				rennClient.init(Constants.APP_ID, Constants.API_KEY, Constants.Secret_Key);
				rennClient.setScope(Constants.SCOPE);
				rennClient.setTokenType("bearer");
	}
	
	public void share2Renren(final int type,final String msg){
		
		rennClient.setLoginListener(new LoginListener() {
			@Override
			public void onLoginSuccess() {
				PutStatusParam param = new PutStatusParam();
				param.setContent("我在Do.or(http://page.renren.com/601900279)上发布了一个"+type2String(type)+"："+msg);
				try {
					rennClient.getRennService().sendAsynRequest(param, new CallBack() {
						
						@Override
						public void onSuccess(RennResponse arg0) {
							Toast.makeText(context, "人人发布成功", Toast.LENGTH_LONG).show();
						}
						
						@Override
						public void onFailed(String arg0, String arg1) {
							Toast.makeText(context, "人人发布失败", Toast.LENGTH_LONG).show();
						}
					});
				} catch (RennException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void onLoginCanceled() {
				Toast.makeText(context, "loginCanceled", Toast.LENGTH_SHORT).show();
			}
		});
		rennClient.login(activity);
	}
	
	private String type2String(int type){
		switch (type) {
		case EventVO.TYPE_ALARM:
			return "闹钟";
		case EventVO.TYPE_SCHEDULE:
			return "备忘";
		case EventVO.TYPE_REQUEST:
			return "请求";
		}
		return "";
	}
	
}
