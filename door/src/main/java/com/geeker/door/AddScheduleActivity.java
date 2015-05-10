package com.geeker.door;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.geeker.door.alarm.AlarmReceiver;
import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.businesslogic.ScheduleService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.MemoVO;
import com.geeker.door.database.ReminderType;
import com.geeker.door.renren.RenrenService;
import com.geeker.door.ringtone.SelectRingtoneActivity;
import com.geeker.door.utils.ExpandAnimation;
import com.geeker.door.utils.MyViewGroup;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class AddScheduleActivity extends MyActionBarActivity {
	static final int[][] IDinfo = new int[][] {
			{ R.id.time_settings, R.id.time_bg, R.id.time_switch,R.id.time_text },
			{ R.id.alert_settings, R.id.alert_bg, R.id.alert_switch,R.id.alert_text },
			{ R.id.friend_settings, R.id.friend_bg, R.id.friend_switch ,R.id.friend_text},
			{ R.id.classify_settings, R.id.classify_bg, R.id.classify_switch ,R.id.classify_text},
			{ R.id.tweet_settings, R.id.tweet_bg, R.id.tweet_switch,R.id.tweet_text } };
	
	Animation expandAnim;
	Animation retractAnim;
	ImageView arrow1;
	ImageView arrow2;
	ImageView arrow3;
	ImageView arrow4;
	ImageView arrow5;
	TextView scheduleDate;
	TextView scheduleTime;
	TextView alertDate;
	TextView alertTime;
	EditText contentText;
	SeekBar volumeBar;
	Calendar calendarSchedule;
	Calendar calendarAlert;
	TextView ringName;
	String ringURI;
	MyViewGroup myViewgroup;
	MyViewGroup myViewgroup2;
	ScheduleService scheduleService;
	AlarmService alarmService;
	private RenrenService renrenService;
	boolean isAniming;
	boolean firstInit=true;
	boolean[] switches=new boolean[]{true,false,false,false,true};//时间，闹钟，同伴，标签，分享
	private DbManager dbManager;
	private int reqCode=-1;
	public static final int CODE_RINGTONE=0;
	private BoardService boardService;
	ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_schedule);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		scheduleService=new ScheduleService(this);
		alarmService=new AlarmService(this);
		renrenService=new RenrenService(this);
		//testMethod();
		setTitle("添加备忘");
		initButtons();
		initExtra();
		initExpand();
	}
	
	private void initExtra() {
		int requestCode=getIntent().getIntExtra("requestCode", -1);
		if(requestCode!=-1){
			setTitle("修改备忘");
			reqCode=requestCode;
			MemoVO vo=dbManager.getMemo(requestCode);
			switches=vo.getSwitches();
			if(vo.getOccurTime()!=null){
				calendarSchedule=vo.getOccurTime();
				SimpleDateFormat time=new SimpleDateFormat("HH : mm");
				scheduleTime.setText(time.format(new Date(calendarSchedule.getTimeInMillis())));
				scheduleDate.setText(calendarSchedule.get(Calendar.YEAR)+" 年 "+(calendarSchedule.get(Calendar.MONTH)+1)+" 月 "+calendarSchedule.get(Calendar.DAY_OF_MONTH)+" 日");
			}
			if(vo.getReminderTime()!=null){
				calendarAlert=vo.getReminderTime();
				SimpleDateFormat time=new SimpleDateFormat("HH : mm");
				alertTime.setText(time.format(new Date(calendarAlert.getTimeInMillis())));
				alertDate.setText(calendarAlert.get(Calendar.YEAR)+" 年 "+(calendarAlert.get(Calendar.MONTH)+1)+" 月 "+calendarAlert.get(Calendar.DAY_OF_MONTH)+" 日");
			}
			//铃声
			ringURI=vo.getRing();
			try {
				ringName.setText(RingtoneManager.getRingtone(this, Uri.parse(ringURI)).getTitle(this));
			} catch (Exception e) {
			}
			//音量
			volumeBar.setProgress(vo.getVol());
			//内容
			contentText.setText(vo.getTitle()+"\n"+vo.getContent());
			String[] tags=vo.getTags();
			for (int i = 0; i < tags.length; i++) {
				TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
            	label.setText(tags[i]);
    			myViewgroup2.addView(label);
			}
			String[] comps=vo.getTags();
			for (int i = 0; i < comps.length; i++) {
				TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
            	label.setText(comps[i]);
    			myViewgroup.addView(label);
			}
		}
	}

	private void initButtons() {
		calendarSchedule=Calendar.getInstance();
		calendarAlert=Calendar.getInstance();
		calendarSchedule.add(Calendar.DATE, 1);
		calendarAlert.add(Calendar.DATE, 1);
		scheduleDate=(TextView)findViewById(R.id.schedule_date);
		scheduleTime=(TextView)findViewById(R.id.schedule_time);
		alertDate=(TextView)findViewById(R.id.alert_date);
		alertTime=(TextView)findViewById(R.id.alert_time);
		contentText=(EditText)findViewById(R.id.content_text);
		volumeBar=(SeekBar)findViewById(R.id.volume);
		ringName=(TextView)findViewById(R.id.ringtone_name);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		volumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumeBar.setProgress(volumeBar.getMax());
		final DateTimePickListener1 dateTimePickListener1=new DateTimePickListener1();
		scheduleDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(dateTimePickListener1, calendarSchedule.get(Calendar.YEAR), calendarSchedule.get(Calendar.MONTH), calendarSchedule.get(Calendar.DAY_OF_MONTH), true);
				datePickerDialog.setVibrate(true);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.show(getSupportFragmentManager(), "datepicker");
			}
		});
		scheduleTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(dateTimePickListener1, calendarSchedule.get(Calendar.HOUR_OF_DAY) ,calendarSchedule.get(Calendar.MINUTE), false, false);
				timePickerDialog.setVibrate(true);
	            timePickerDialog.show(getSupportFragmentManager(), "timepicker");
			}
		});
		final DateTimePickListener2 dateTimePickListener2=new DateTimePickListener2();
		alertDate.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				DatePickerDialog datePickerDialog = DatePickerDialog.newInstance(dateTimePickListener2, calendarAlert.get(Calendar.YEAR), calendarAlert.get(Calendar.MONTH), calendarAlert.get(Calendar.DAY_OF_MONTH), true);
				datePickerDialog.setVibrate(true);
                datePickerDialog.setYearRange(1985, 2028);
                datePickerDialog.show(getSupportFragmentManager(), "datepicker");
			}
		});
		alertTime.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePickerDialog = TimePickerDialog.newInstance(dateTimePickListener2, calendarAlert.get(Calendar.HOUR_OF_DAY) ,calendarAlert.get(Calendar.MINUTE), false, false);
				timePickerDialog.setVibrate(true);
	            timePickerDialog.show(getSupportFragmentManager(), "timepicker");
			}
		});
		SimpleDateFormat time=new SimpleDateFormat("HH : mm");
		scheduleTime.setText(time.format(new Date()));
		scheduleDate.setText(calendarAlert.get(Calendar.YEAR)+" 年 "+(calendarAlert.get(Calendar.MONTH)+1)+" 月 "+calendarAlert.get(Calendar.DAY_OF_MONTH)+" 日");
		alertTime.setText(time.format(new Date()));
		alertDate.setText(calendarAlert.get(Calendar.YEAR)+" 年 "+(calendarAlert.get(Calendar.MONTH)+1)+" 月 "+calendarAlert.get(Calendar.DAY_OF_MONTH)+" 日");
		ImageView addFriend=(ImageView)findViewById(R.id.friend_add);
		ImageView addLabel=(ImageView)findViewById(R.id.classify_add);
		myViewgroup = (MyViewGroup) findViewById(R.id.friend_viewgroup);
		myViewgroup2 = (MyViewGroup) findViewById(R.id.classify_viewgroup);
		addFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(AddScheduleActivity.this, "Do.or", "正在获取好友列表", true, true);  
				new InitFriendsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
		addLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 final EditText inputServer = new EditText(AddScheduleActivity.this);
			        AlertDialog.Builder builder = new AlertDialog.Builder(AddScheduleActivity.this);
			        builder.setTitle("请输入标签").setView(inputServer).setNegativeButton("取消", null);
			        builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
			            @Override
						public void onClick(DialogInterface dialog, int which) {
			            	TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
			            	label.setText(inputServer.getText());
			    			myViewgroup2.addView(label);
			             }
			        });
			        builder.show();
			}
		});
		LinearLayout setRingtone = (LinearLayout) findViewById(R.id.set_ringtone);
		setRingtone.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(AddScheduleActivity.this,SelectRingtoneActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivityForResult(i, CODE_RINGTONE);
				overridePendingTransition(R.anim.push_right_in, R.anim.push_left_out);  
			}
		});
		try {
			ringURI=RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM).toString();
			ringName.setText(RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)).getTitle(this));
		} catch (Exception e) {
		}
		ImageView okButton=(ImageView)findViewById(R.id.button_ok);
		ImageView cancelButton=(ImageView)findViewById(R.id.button_cancel);
		final Switch renren = (Switch) findViewById(R.id.switch_renren);
		final Switch board = (Switch) findViewById(R.id.switch_board);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!switches[0]){calendarSchedule=null;}
				if(!switches[1]){calendarAlert=null;}
				String[] companies=getCompanies();
				String[] tags=getTags();
				final String[] content=contentText.getText().toString().split("\n", 2);
				String contentString=content.length>1?content[1]:"";
				if(switches[1] && reqCode<0){
					addAlarm(content[0]);
					if(board.isChecked()){
						new AddAlarmToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, content[0]);
					}
				}
				if(reqCode<0){
					reqCode=dbManager.saveMemo(content[0], contentString, calendarSchedule, calendarAlert, ringURI, volumeBar.getProgress(), companies, tags,switches,0);
				}
				else{
					dbManager.updateMemo(reqCode,content[0], contentString, calendarSchedule, calendarAlert, ringURI, volumeBar.getProgress(), companies, tags,switches);
				}
				Toast.makeText(getApplicationContext(), "添加成功",Toast.LENGTH_SHORT).show();
				if(board.isChecked()){
					new AddScheduleToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				}
				if(renren.isChecked()){
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							renrenService.share2Renren(EventVO.TYPE_SCHEDULE, content[0]);
						}
					});
				}
				finish();
			}

		});
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	protected void addAlarm(String title) {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(),
				AlarmReceiver.class);
		calendarAlert.set(Calendar.SECOND, 0);
		int reqCode=dbManager.saveClock(calendarAlert, new ReminderType(calendarAlert.get(Calendar.DAY_OF_WEEK), ringURI, title, volumeBar.getProgress(), 
				new boolean[]{false,false,false,false}), DbManager.CANCELTYPE_SLIDE,0);
		intent.putExtra("once", true);
		intent.putExtra("requestcode", reqCode);
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(), reqCode, intent,	PendingIntent.FLAG_CANCEL_CURRENT);
		am.set(AlarmManager.RTC_WAKEUP,calendarAlert.getTimeInMillis(), sender);
	}

	protected String[] getTags() {
		if(!switches[3] || myViewgroup2.getChildCount()<=0){
			return null;
		}
		String[] result=new String[myViewgroup2.getChildCount()];
		for (int i = 0; i < myViewgroup2.getChildCount(); i++) {
			TextView text=(TextView)myViewgroup2.getChildAt(i);
			result[i]=text.getText().toString();
		}
		return result;
	}

	private String[] getCompanies() {
		if(!switches[2] || myViewgroup.getChildCount()<=0){
			return null;
		}
		String[] result=new String[myViewgroup.getChildCount()];
		for (int i = 0; i < myViewgroup.getChildCount(); i++) {
			TextView text=(TextView)myViewgroup.getChildAt(i);
			result[i]=text.getText().toString();
		}
		return result;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case CODE_RINGTONE:
			if(data==null){return;}
			String name=data.getStringExtra("name");
			ringURI=data.getStringExtra("uri");
			ringName.setText(name);
			break;
		default:
			break;
		}
		
		super.onActivityResult(requestCode, resultCode,data);
	}
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if(firstInit){
			if (!switches[0]) {
				View toolbar1 = findViewById(R.id.time_settings);
				ExpandAnimation.initViewMargin(toolbar1, toolbar1.getHeight());
				toolbar1.setVisibility(View.GONE);
			}
			if (!switches[1]) {
				View toolbar1 = findViewById(R.id.alert_settings);
				ExpandAnimation.initViewMargin(toolbar1, toolbar1.getHeight());
				toolbar1.setVisibility(View.GONE);
			}
			if (!switches[2]) {
				View toolbar2 = findViewById(R.id.friend_settings);
				ExpandAnimation.initViewMargin(toolbar2, toolbar2.getHeight());
				toolbar2.setVisibility(View.GONE);
			}
			if (!switches[3]) {
				View toolbar3 = findViewById(R.id.classify_settings);
				ExpandAnimation.initViewMargin(toolbar3, toolbar3.getHeight());
				toolbar3.setVisibility(View.GONE);
			}
			if (!switches[4]) {
				View toolbar1 = findViewById(R.id.tweet_settings);
				ExpandAnimation.initViewMargin(toolbar1, toolbar1.getHeight());
				toolbar1.setVisibility(View.GONE);
			}
		firstInit=false;
		}
		super.onWindowFocusChanged(hasFocus);
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

	/*private void testMethod() {
		// TODO only for test
		MyViewGroup myViewgroup = (MyViewGroup) findViewById(R.id.friend_viewgroup);
		LayoutInflater inflater = getLayoutInflater();
		for (int i = 0; i < 7; i++) {
			TextView label = (TextView) inflater.inflate(R.layout.label, null);
			myViewgroup.addView(label);
		}
		LinearLayout defaultLinear = (LinearLayout) findViewById(R.id.friend_default);
		for (int i = 0; i < 3; i++) {
			TextView label = (TextView) inflater.inflate(R.layout.label_dark,
					null);
			defaultLinear.addView(label);
		}
		MyViewGroup myViewgroup2 = (MyViewGroup) findViewById(R.id.classify_viewgroup);
		for (int i = 0; i < 7; i++) {
			TextView label = (TextView) inflater.inflate(R.layout.label, null);
			myViewgroup2.addView(label);
		}
		LinearLayout defaultLinear2 = (LinearLayout) findViewById(R.id.classify_default);
		for (int i = 0; i < 3; i++) {
			TextView label = (TextView) inflater.inflate(R.layout.label_dark,
					null);
			defaultLinear2.addView(label);
		}
	}*/
	
	
	private void initExpand() {
		 LinearLayout time=(LinearLayout)findViewById(R.id.time_title);
			LinearLayout alert=(LinearLayout)findViewById(R.id.alert_title);
			LinearLayout friend=(LinearLayout)findViewById(R.id.friend_title);
			LinearLayout classify=(LinearLayout)findViewById(R.id.classify_title);
			LinearLayout tweet=(LinearLayout)findViewById(R.id.tweet_title);
			arrow1=(ImageView)findViewById(R.id.time_arrow);
			arrow2=(ImageView)findViewById(R.id.alert_arrow);
			arrow3=(ImageView)findViewById(R.id.friend_arrow);
			arrow4=(ImageView)findViewById(R.id.classify_arrow);
			arrow5=(ImageView)findViewById(R.id.tweet_arrow);
			ImageView scheduleArrow=(ImageView) findViewById(R.id.schedule_arrow);
			if(switches[0]){rotateArrow(90f,arrow1);}
			if(switches[1]){rotateArrow(90f,arrow2);}
			if(switches[2]){rotateArrow(90f,arrow3);}
			if(switches[3]){rotateArrow(90f,arrow4);}
			if(switches[4]){rotateArrow(90f,arrow5);}
			rotateArrow(90f,scheduleArrow);
			AnimationListener animListener=new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					isAniming=true;
				}
				
				@Override
				public void onAnimationRepeat(Animation animation) {
					isAniming=true;
				}
				
				@Override
				public void onAnimationEnd(Animation animation) {
					isAniming=false;
				}
			};
			expandAnim=AnimationUtils.loadAnimation(this, R.anim.rotate_expand);
			expandAnim.setAnimationListener(animListener);
			retractAnim=AnimationUtils.loadAnimation(this, R.anim.rotate_retract);
			retractAnim.setAnimationListener(animListener);
			//我实在懒得改了
			time.setOnClickListener(new ExpandClickListener(0,arrow1));
			alert.setOnClickListener(new ExpandClickListener(1,arrow2));
			friend.setOnClickListener(new ExpandClickListener(2,arrow3));
			classify.setOnClickListener(new ExpandClickListener(3, arrow4));
			tweet.setOnClickListener(new ExpandClickListener(4, arrow5));
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
	
	class ExpandClickListener implements OnClickListener{
		int index;
		TextView switchText;
		TextView titleText;
		View background;
		View toolbar;
		ImageView arrow;
		public ExpandClickListener(int index,ImageView arrow){
			this.index=index;
			this.toolbar = findViewById(IDinfo[index][0]);
			this.background=findViewById(IDinfo[index][1]);
			this.switchText=(TextView)findViewById(IDinfo[index][2]);
			this.titleText=(TextView)findViewById(IDinfo[index][3]);
			this.arrow=arrow;
			refresh();
		}
		
		@Override
		public void onClick(View v) {
			if(isAniming){return;}
			ExpandAnimation expandAni1 = new ExpandAnimation(toolbar, 200,arrow);
			boolean alartExpand=expandAni1.getIsVisibleAfter();
			if(!alartExpand){
				arrow.startAnimation(expandAnim);
				rotateArrow(90f,arrow);
			}else{
				arrow.startAnimation(retractAnim);
				rotateArrow(270f,arrow);
			}
			toolbar.startAnimation(expandAni1);
			switches[index]=!switches[index];
			refresh();
		}
		
		public void refresh(){
			if(switches[index]){
				background.setBackgroundDrawable(getResources().getDrawable(R.drawable.switch_button));
				switchText.setText("开");
				switchText.setTextColor(getResources().getColorStateList(R.color.press_color));
				titleText.setTextColor(getResources().getColorStateList(R.color.press_color));
			}else{
				background.setBackgroundDrawable(getResources().getDrawable(R.drawable.switch_button_checked));
				switchText.setText("关");
				switchText.setTextColor(getResources().getColorStateList(R.color.white));
				titleText.setTextColor(getResources().getColorStateList(R.color.white));
			}
		}
		
	}
	class DateTimePickListener1 implements OnDateSetListener, TimePickerDialog.OnTimeSetListener{

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			calendarSchedule.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendarSchedule.set(Calendar.MINUTE, minute);
			SimpleDateFormat time=new SimpleDateFormat("HH : mm");
			scheduleTime.setText(time.format(new Date(calendarSchedule.getTimeInMillis())));
		}

		@Override
		public void onDateSet(DatePickerDialog datePickerDialog, int year,
				int month, int day) {
			calendarSchedule.set(Calendar.YEAR, year);
			calendarSchedule.set(Calendar.MONTH, month);
			calendarSchedule.set(Calendar.DAY_OF_MONTH, day);
			scheduleDate.setText(year+" 年 "+(month+1)+" 月 "+day+" 日");
		}
		
	}
	
	class DateTimePickListener2 implements OnDateSetListener, TimePickerDialog.OnTimeSetListener{

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			calendarAlert.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendarAlert.set(Calendar.MINUTE, minute);
			SimpleDateFormat time=new SimpleDateFormat("HH : mm");
			alertTime.setText(time.format(new Date(calendarAlert.getTimeInMillis())));
		}

		@Override
		public void onDateSet(DatePickerDialog datePickerDialog, int year,
				int month, int day) {
			calendarAlert.set(Calendar.YEAR, year);
			calendarAlert.set(Calendar.MONTH, month);
			calendarAlert.set(Calendar.DAY_OF_MONTH, day);
			alertDate.setText(year+" 年 "+(month+1)+" 月 "+day+" 日");
		}
		
	}
	
	class AddScheduleToServer extends AsyncTask<String, Integer, Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			String[] content=contentText.getText().toString().split("\n", 2);
			String contentText="";
			if(content.length>1){contentText=content[1];}
			List<String> list1=null;
			List<String> list2=null;
			if(getTags()!=null){
				list1=Arrays.asList(getTags());
			}
			if(getCompanies()!=null){
				list2=Arrays.asList(getCompanies());
			}
			return scheduleService.addSchedule(calendarSchedule,true, content[0], contentText,list1,list2,dbManager.getEventID(EventVO.TYPE_SCHEDULE, reqCode));
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result!=0){
				Toast.makeText(AddScheduleActivity.this, "成功添加到公共事务版", Toast.LENGTH_SHORT).show();
				dbManager.setEventID(EventVO.TYPE_SCHEDULE, reqCode, result);
			}
		}
		
	}
	
	private class InitFriendsTask extends AsyncTask<Void, Void, Object[][]> {
		@Override
		protected Object[][] doInBackground(Void... params) {
			return boardService.getMyFriend();
		}
		
		@Override
		protected void onPostExecute(Object[][] result) {
			progressDialog.dismiss();
			final String[] items=new String[result.length];
			final String[] usernames=new String[result.length];
			for (int i = 0; i < result.length; i++) {
				usernames[i]=(String)result[i][0];
				items[i]=(String)result[i][1];
			}
			new AlertDialog.Builder(AddScheduleActivity.this)  
            .setTitle("请选择好友：")  
            .setItems(items, new DialogInterface.OnClickListener() {  
                @Override
				public void onClick(DialogInterface dialog,int which) {  
                	TextView label = (TextView) getLayoutInflater().inflate(R.layout.label, null);
	            	label.setText(usernames[which]);
	    			myViewgroup.addView(label);
                }  
            }).show();  
			super.onPostExecute(result);
		}
	}
	
	class AddAlarmToServer extends AsyncTask<String, Integer, Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			return alarmService.addAlarm(calendarAlert, true, ringURI, params[0], volumeBar.getProgress(), "0000", "shake",0);
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result!=0){
				dbManager.setEventID(EventVO.TYPE_ALARM, reqCode, result);
			}
		}
		
	}
}
