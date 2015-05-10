package com.geeker.door.lock;

import java.util.List;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;

import com.geeker.door.alarm.AlarmActivity;

public class LauncherActivity extends Activity{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if(AlarmActivity.isRunning||MyLockActivity.isShowing){
			finish();
			return;
		}
		callSystemLauncher();
		finish();
	}

	private void callSystemLauncher() {
		try {
			PackageManager localPackageManager = getPackageManager();
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			List<ResolveInfo> resolveInfos = localPackageManager
					.queryIntentActivities(intent,
							PackageManager.MATCH_DEFAULT_ONLY);
			for (int i = 0; i < resolveInfos.size(); i++) {
				ResolveInfo resolveInfo = resolveInfos.get(i);
				ActivityInfo activityInfo = resolveInfo.activityInfo;
				if (!activityInfo.name.endsWith("LauncherActivity")) {
					ComponentName componentName = new ComponentName(
							activityInfo.packageName, activityInfo.name);
					Intent intent1 = new Intent();
					intent1.setAction("android.intent.action.MAIN");
					intent1.setComponent(componentName);
					startActivity(intent1);
					//System.exit(0);	//直接关闭system会导致后台service和receiver直接关闭，宁可不完全关闭也不能这么写
					break;
				}
			}
		} catch (Exception e) {
		}
	}

}
