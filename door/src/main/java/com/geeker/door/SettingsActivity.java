package com.geeker.door;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.ProgressDialog;
import android.app.KeyguardManager.KeyguardLock;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.geeker.door.businesslogic.SettingsService;
import com.geeker.door.database.DbManager;
import com.geeker.door.lock.MyLockService;
import com.geeker.door.push.Utils;
import com.geeker.door.utils.ExpandAnimation;

/**
 * 
 * @author Fanyl
 *
 */
public class SettingsActivity extends MyActionBarActivity{

	
	ImageView arrow1;
	ImageView arrow2;
	boolean isAniming;
	DbManager dbManager;
	SettingsService settingsService;
	ProgressDialog progressDialog;
	ProgressDialog progressDialog2;
	
	@SuppressLint("ResourceAsColor")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		dbManager=new DbManager(this);
		settingsService=new SettingsService(getApplication());
		setTitle("设置");
		initSwitches();
		initExpand();
	}
	
	private void initSwitches() {
		Switch push = (Switch) findViewById(R.id.switch_push);
		Switch alarm = (Switch) findViewById(R.id.switch_alarm);
		Switch lock = (Switch) findViewById(R.id.switch_lock);
		View closeSystemLock=findViewById(R.id.set_syslock);
		View setHomeKey=findViewById(R.id.set_homekey);
		View setLockBackground=findViewById(R.id.set_locktheme);
		push.setChecked(dbManager.isPush());
		alarm.setChecked(dbManager.isForceAwake());
		lock.setChecked(dbManager.isDoorLock());
		push.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dbManager.setPush(isChecked);
				if(isChecked){
					PushManager.startWork(getApplicationContext(),PushConstants.LOGIN_TYPE_API_KEY, Utils.getMetaValue(getApplicationContext(), "api_key"));
					Toast.makeText(SettingsActivity.this, "推送已开启", Toast.LENGTH_SHORT).show();
				}else{
					PushManager.stopWork(getApplicationContext());
					Toast.makeText(SettingsActivity.this, "推送已关闭", Toast.LENGTH_SHORT).show();
				}
			}
		});
		alarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dbManager.setForceAwake(isChecked);
			}
		});
		lock.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				dbManager.setDoorLock(isChecked);
				if(isChecked){
					Intent i=new Intent(SettingsActivity.this,MyLockService.class);
					startService(i);
				}else{
					Intent stopIntent = new Intent("com.geeker.door.CLOSE_DOOR_LOCK");
					sendBroadcast(stopIntent);
				}
			}
		});
		closeSystemLock.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				try {
					Intent intent = new Intent();
					ComponentName cm = new ComponentName("com.android.settings","com.android.settings.ChooseLockGeneric");
					intent.setComponent(cm);
					intent.setAction("android.intent.action.VIEW");
					startActivityForResult(intent,0);
				} catch (Exception e) {
				}
			}
		});
		setHomeKey.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this).setTitle("锁定HOME键").setMessage("请点击HOME键，并在完成动作的方式中选择Do.or即可")
		          .setNegativeButton("OK", new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int which) {
		              }
		          })
		          .show();
			}
		});
		setLockBackground.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(SettingsActivity.this).setTitle("设置主题").setMessage("由于美工罢工，此项功能尚未开发")
		          .setNegativeButton("美工是大坏蛋", new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int which) {
		              }
		          })
		          .show();
			}
		});
		
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish(); // finish当前activity
			overridePendingTransition(R.anim.back_left_in,
					R.anim.back_right_out);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	private void initExpand() {
		LinearLayout alert = (LinearLayout) findViewById(R.id.message_title);
		LinearLayout relieve = (LinearLayout) findViewById(R.id.lock_title);
		arrow1 = (ImageView) findViewById(R.id.message_arrow);
		arrow2 = (ImageView) findViewById(R.id.lock_arrow);
		rotateArrow(90f, arrow1);
		rotateArrow(90f, arrow2);
		AnimationListener animListener = new AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				isAniming = true;
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				isAniming = true;
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				isAniming = false;
			}
		};
		final Animation expandAnim = AnimationUtils.loadAnimation(this,
				R.anim.rotate_expand);
		expandAnim.setAnimationListener(animListener);
		final Animation retractAnim = AnimationUtils.loadAnimation(this,
				R.anim.rotate_retract);
		retractAnim.setAnimationListener(animListener);
		alert.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAniming) {
					return;
				}
				View toolbar1 = findViewById(R.id.message_content);
				ExpandAnimation expandAni1 = new ExpandAnimation(toolbar1, 200,
						arrow1);
				boolean alartExpand = expandAni1.getIsVisibleAfter();
				if (!alartExpand) {
					arrow1.startAnimation(expandAnim);
					rotateArrow(90f, arrow1);
				} else {
					arrow1.startAnimation(retractAnim);
					rotateArrow(270f, arrow1);
				}
				toolbar1.startAnimation(expandAni1);
			}
		});
		relieve.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAniming) {
					return;
				}
				View toolbar2 = findViewById(R.id.lock_content);
				ExpandAnimation expandAni2 = new ExpandAnimation(toolbar2, 200,
						arrow2);
				boolean relieveExpand = expandAni2.getIsVisibleAfter();
				if (!relieveExpand) {
					arrow2.startAnimation(expandAnim);
					rotateArrow(90f, arrow2);
				} else {
					arrow2.startAnimation(retractAnim);
					rotateArrow(270f, arrow2);
				}
				toolbar2.startAnimation(expandAni2);
			}
		});
		/*tweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAniming) {
					return;
				}
				View toolbar3 = findViewById(R.id.system_content);
				ExpandAnimation expandAni3 = new ExpandAnimation(toolbar3, 200,
						arrow3);
				boolean tweetExpand = expandAni3.getIsVisibleAfter();
				if (!tweetExpand) {
					arrow3.startAnimation(expandAnim);
					rotateArrow(90f, arrow3);
				} else {
					arrow3.startAnimation(retractAnim);
					rotateArrow(270f, arrow3);
				}
				toolbar3.startAnimation(expandAni3);
			}
		});*/

	}
	
	private void rotateArrow(float angle, ImageView arrow) {
		Drawable drawable = arrow.getDrawable();
		Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(), Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.save();
		canvas.rotate(angle, canvas.getWidth() / 2.0f,
				canvas.getHeight() / 2.0f);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		canvas.restore();
		arrow.setImageBitmap(bitmap);
	}
	
	
}
