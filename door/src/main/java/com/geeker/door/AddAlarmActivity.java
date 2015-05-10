package com.geeker.door;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.alarm.AlarmReceiver;
import com.geeker.door.alarm.PreAlarmReceiver;
import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.database.ClockVO;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.ReminderType;
import com.geeker.door.renren.RenrenService;
import com.geeker.door.ringtone.SelectRingtoneActivity;
import com.geeker.door.utils.ExpandAnimation;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class AddAlarmActivity extends MyActionBarActivity {

	ImageView arrow1;
	ImageView arrow2;
	ImageView arrow3;
	boolean isAniming;
	Calendar calendar;
	TextView timePicker;
	TextView ringName;
	TextView ampm;
	Button[] buttons;
	boolean[] buttonsChecked = new boolean[11];
	AlarmService alarmService;
	RadioGroup radioGroup;
	EditText labelText;
	SeekBar volumeBar;
	Handler handler;
	String ringURI;
	int reqCode=-1;
	private DbManager dbManager;
	private RenrenService renrenService;
	public static final int CODE_RINGTONE=0;
	public static int PRE_ALARM_MINUTE=10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_alarm);
		this.alarmService = new AlarmService(this);
		dbManager=new DbManager(this);
		handler = new Handler();
		initExpand();
		initButtons();
		renrenService=new RenrenService(this);
		setTitle("添加闹钟");
		initExtra();
		
	}

	
	
	private void initExtra() {
		int requestCode=getIntent().getIntExtra("requestCode", -1);
		if(requestCode!=-1){
			setTitle("修改闹钟");
			reqCode=requestCode;
			ClockVO alarm=dbManager.getClock(requestCode);
			//初始化闹钟时间
			calendar=alarm.getTime();
			Date date = new Date(calendar.getTimeInMillis());
			SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.CHINESE);
			timePicker.setText(format.format(date) + "  ");
			if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
				ampm.setText("上午");
			} else {
				ampm.setText("下午");
			}
			//铃声
			ringURI=alarm.getType().getRing();
			try {
				ringName.setText(RingtoneManager.getRingtone(this, Uri.parse(ringURI)).getTitle(this));
			} catch (Exception e) {
			}
			//标签
			labelText.setText(alarm.getType().getTag());
			//音量
			volumeBar.setProgress(alarm.getType().getVolume());
			int dateOfWeek=alarm.getType().getDay();
			buttons[dateOfWeek-1].callOnClick();
			for (int i = 0; i < alarm.getType().getReminder().length; i++) {
				if(alarm.getType().getReminder()[i]!=0){
					buttons[7+i].callOnClick();
				}
			}
			switch (alarm.getCancleType()) {
			case DbManager.CANCELTYPE_SLIDE:
				radioGroup.check(R.id.relieve_slide);
				break;
			case DbManager.CANCELTYPE_SHAKE:
				radioGroup.check(R.id.relieve_shake);
				break;
			}
		}
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

	private void initButtons() {
		timePicker = (TextView) findViewById(R.id.alarm_time);
		ringName=(TextView)findViewById(R.id.ring_name);
		ampm = (TextView) findViewById(R.id.alarm_ampm);
		radioGroup=(RadioGroup)findViewById(R.id.radiogroup);
		labelText=(EditText)findViewById(R.id.label_text);
		volumeBar=(SeekBar)findViewById(R.id.volume);
		AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		volumeBar.setMax(am.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volumeBar.setProgress(volumeBar.getMax());
		
		final DateTimePickListener dateTimePickListener = new DateTimePickListener();
		initCalendarText();
		timePicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TimePickerDialog timePickerDialog = TimePickerDialog
						.newInstance(dateTimePickListener,
								calendar.get(Calendar.HOUR_OF_DAY),
								calendar.get(Calendar.MINUTE), false, false);
				timePickerDialog.setVibrate(true);
				timePickerDialog
						.show(getSupportFragmentManager(), "timepicker");
			}
		});
		int[] buttonsID = new int[] { R.id.alarm_1, R.id.alarm_2, R.id.alarm_3,
				R.id.alarm_4, R.id.alarm_5, R.id.alarm_6, R.id.alarm_7,
				R.id.alarm_setting1, R.id.alarm_setting2, R.id.alarm_setting3,
				R.id.alarm_setting4 };
		buttons = new Button[11];
		for (int i = 0; i < buttons.length; i++) {
			final int index = i;
			buttonsChecked[i] = false;
			buttons[i] = (Button) findViewById(buttonsID[i]);
			buttons[i].setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (!buttonsChecked[index]) {
						if (index == 0 || index == 7) {
							buttons[index]
									.setBackgroundDrawable(getResources()
											.getDrawable(
													R.drawable.left_radius_button_pressed));
						} else if (index == 6 || index == 10) {
							buttons[index]
									.setBackgroundDrawable(getResources()
											.getDrawable(
													R.drawable.right_radius_button_pressed));
						} else {
							buttons[index].setBackgroundDrawable(getResources()
									.getDrawable(
											R.drawable.button_pressed_color));
						}
					} else {
						if (index == 0 || index == 7) {
							buttons[index].setBackgroundDrawable(getResources()
									.getDrawable(
											R.drawable.left_radius_button_bg));
						} else if (index == 6 || index == 10) {
							buttons[index].setBackgroundDrawable(getResources()
									.getDrawable(
											R.drawable.right_radius_button_bg));
						} else {
							buttons[index].setBackgroundDrawable(getResources()
									.getDrawable(R.drawable.buttoncolor));
						}
					}
					buttonsChecked[index] = !buttonsChecked[index];

				}
			});
		}
		final Switch renren = (Switch) findViewById(R.id.switch_renren);
		final Switch board = (Switch) findViewById(R.id.switch_board);
		final ImageButton okButton = (ImageButton) findViewById(R.id.button_ok);
		ImageView cancelButton=(ImageView)findViewById(R.id.button_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(multiChecked()){
					Toast.makeText(AddAlarmActivity.this, "闹钟只能选择一个日期哦", Toast.LENGTH_SHORT).show();
					return;
				}
				final Thread thread=new Thread(new Runnable() {
					@Override
					public void run() {
						calendar.set(Calendar.DATE,
								Calendar.getInstance().get(Calendar.DATE));
						// 防止多个进程同时运行导致数据混乱
						handler.post(new Runnable() {
							@Override
							public void run() {
								okButton.setEnabled(false);
							}
						});
						boolean[] switchBool = new boolean[] {
								renren.isChecked(), board.isChecked() };
						// 设定闹钟
						AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
						Intent intent = new Intent(getApplicationContext(),
								AlarmReceiver.class);
						calendar.set(Calendar.SECOND, 0);
						long timeOffset = Long.MAX_VALUE;
						for (int j = 0; j < 7; j++) {
							if (buttonsChecked[j]) {
								calendar.set(Calendar.DATE,	Calendar.getInstance().get(Calendar.DATE));
								// 保存到本地数据库
								if(reqCode<0){
								reqCode=dbManager.saveClock(calendar, new ReminderType(j+1, ringURI, labelText.getText().toString(), volumeBar.getProgress(), 
										new boolean[]{buttonsChecked[7],buttonsChecked[8],buttonsChecked[9],buttonsChecked[10]}), getCancelType(),0);
								}else{
								dbManager.updateClock(reqCode,calendar, new ReminderType(j+1, ringURI, labelText.getText().toString(), volumeBar.getProgress(), 
											new boolean[]{buttonsChecked[7],buttonsChecked[8],buttonsChecked[9],buttonsChecked[10]}), getCancelType());
								}
								intent = new Intent(getApplicationContext(),AlarmReceiver.class);
								Intent preintent = new Intent(AddAlarmActivity.this,PreAlarmReceiver.class);
								if(!buttonsChecked[10]){
									preintent=new Intent();
								}
								intent.putExtra("requestcode", reqCode);
								PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(),reqCode,intent,PendingIntent.FLAG_CANCEL_CURRENT);
								PendingIntent presender = PendingIntent.getBroadcast(getApplicationContext(),reqCode,preintent,PendingIntent.FLAG_CANCEL_CURRENT);
								//如果当天就有闹钟
								if(calendar.get(Calendar.DAY_OF_WEEK) == j + 1 && calendar.getTimeInMillis()<=Calendar.getInstance().getTimeInMillis()){
									calendar.add(Calendar.DAY_OF_MONTH, 7);
								}
								// 调整calendar到下次闹钟响铃的日期
								while (calendar.get(Calendar.DAY_OF_WEEK) != j + 1) {
									// 应该可以跨月加的吧。。。
									calendar.add(Calendar.DAY_OF_MONTH, 1);
								}
								//TODO 处理当天之前的闹钟
								am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),sender);
								//提前唤醒
								am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()-(PRE_ALARM_MINUTE*60*1000),presender);
								if (calendar.getTimeInMillis()- Calendar.getInstance().getTimeInMillis() < timeOffset) {
									timeOffset = calendar.getTimeInMillis()	- Calendar.getInstance().getTimeInMillis();
								}
							}
						}
						// 说明没有选择日期。此时设置最近日期的单次闹钟
						if (timeOffset == Long.MAX_VALUE) {
							while(calendar.getTimeInMillis()<Calendar.getInstance().getTimeInMillis()){ 
								calendar.add(Calendar.DAY_OF_MONTH, 1); 
							}
							if(reqCode<0){
								reqCode=dbManager.saveClock(calendar, new ReminderType(calendar.get(Calendar.DAY_OF_WEEK), ringURI, labelText.getText().toString(), volumeBar.getProgress(), 
									new boolean[]{buttonsChecked[7],buttonsChecked[8],buttonsChecked[9],buttonsChecked[10]}), getCancelType(),0);
							}else{
								dbManager.updateClock(reqCode,calendar, new ReminderType(calendar.get(Calendar.DAY_OF_WEEK), ringURI, labelText.getText().toString(), volumeBar.getProgress(), 
										new boolean[]{buttonsChecked[7],buttonsChecked[8],buttonsChecked[9],buttonsChecked[10]}), getCancelType());
							}
							//对于单次闹钟。
							intent.putExtra("once", true);
							intent.putExtra("requestcode", reqCode);
							Intent preintent = new Intent(AddAlarmActivity.this,PreAlarmReceiver.class);
							if(!buttonsChecked[10]){
								preintent=new Intent();
							}
							PendingIntent sender = PendingIntent.getBroadcast(
									getApplicationContext(), reqCode, intent,
									PendingIntent.FLAG_CANCEL_CURRENT);
							PendingIntent presender = PendingIntent.getBroadcast(getApplicationContext(),reqCode,preintent,PendingIntent.FLAG_CANCEL_CURRENT);
							am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(), sender);
							//提前唤醒
							
							am.set(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis()-(PRE_ALARM_MINUTE*60*1000),presender);
							timeOffset = calendar.getTimeInMillis()
									- Calendar.getInstance().getTimeInMillis();
						}
						final long timeOff = timeOffset;
						handler.post(new Runnable() {
							@Override
							public void run() {
								Toast.makeText(	getApplicationContext(),"距此次闹钟还有" + timeOff / (1000 * 60 * 60)	+ "时" + timeOff / (1000 * 60)
												% 60 + "分", Toast.LENGTH_SHORT)
										.show();
								okButton.setEnabled(true);
							}
						});
						if(switchBool[0]){
							handler.post(new Runnable() {
								@Override
								public void run() {
									String date=calendar.get(Calendar.MONTH)+1+"月"+calendar.get(Calendar.DAY_OF_MONTH)+"日";
									SimpleDateFormat timeformat=new SimpleDateFormat("HH:mm");
									String time=timeformat.format(new Date(calendar.getTimeInMillis()));
									renrenService.share2Renren(EventVO.TYPE_ALARM, date+" "+time+" "+labelText.getText().toString());
								}
							});
						}
						if(switchBool[1]){
							new AddAlarmToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
						}
						finish();
					}
				});
				if(buttonsChecked[9] && getCancelType()==DbManager.CANCELTYPE_SHAKE){
					new AlertDialog.Builder(AddAlarmActivity.this).setTitle("温馨小贴士").setMessage("强烈建议在设置摇动解锁的同时不要设置振动，继续？")
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							thread.start();
						}
					})
			          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int which) {
			              }
			          })
			          .show();
				}else{
					thread.start();
				}
			}

			private boolean multiChecked() {
				int num=0;
				for (int i = 0; i < 7; i++) {
					if(buttonsChecked[i]){num++;}
				}
				if(num>1){return true;}
				return false;
			}
		});
		LinearLayout setRingtone = (LinearLayout) findViewById(R.id.set_ringtone);
		setRingtone.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				/*
				 * List<String> resArr = new ArrayList<String>();
				 * RingtoneManager manager = new
				 * RingtoneManager(AddAlarmActivity.this);
				 * manager.setType(RingtoneManager.TYPE_ALARM); Cursor cursor =
				 * manager.getCursor(); if(cursor.moveToFirst()){ do{
				 * resArr.add(
				 * cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX));
				 * }while(cursor.moveToNext()); } System.out.println(resArr);
				 */
				/*String[] media_info = new String[] {
						MediaStore.Audio.Media.TITLE,
						MediaStore.Audio.Media.DURATION,
						MediaStore.Audio.Media.ARTIST,
						MediaStore.Audio.Media._ID,
						MediaStore.Audio.Media.DISPLAY_NAME,
						MediaStore.Audio.Media.DATA,
						MediaStore.Audio.Media.ALBUM_ID };
				Cursor cursor = getContentResolver().query(
						MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
						media_info, null, null,
						MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
				int[] _ids;
				String[] _artists;
				String[] _titles = null;
				if (null != cursor && cursor.getCount() > 0) {
					cursor.moveToFirst();// 将游标移动到初始位置
					_ids = new int[cursor.getCount()];// 返回INT的一个列
					_artists = new String[cursor.getCount()];// 返回String的一个列
					_titles = new String[cursor.getCount()];// 返回String的一个列
					for (int i = 0; i < cursor.getCount(); i++) {
						_ids[i] = cursor.getInt(3);
						_titles[i] = cursor.getString(0);
						_artists[i] = cursor.getString(2);
						cursor.moveToNext();// 将游标移到下一行
					}
				}
				for (int i = 0; i < _titles.length; i++) {
					System.out.println(_titles[i]);
				}*/
				Intent i=new Intent(AddAlarmActivity.this,SelectRingtoneActivity.class);
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
	
	private int getCancelType() {
		if(radioGroup.getCheckedRadioButtonId()==R.id.relieve_slide){
			return DbManager.CANCELTYPE_SLIDE;
		}else{
			return DbManager.CANCELTYPE_SHAKE;
		}
	}
	
	private void initExpand() {
		LinearLayout alert = (LinearLayout) findViewById(R.id.alert_title);
		LinearLayout relieve = (LinearLayout) findViewById(R.id.relieve_title);
		LinearLayout tweet = (LinearLayout) findViewById(R.id.tweet_title);
		arrow1 = (ImageView) findViewById(R.id.alert_arrow);
		arrow2 = (ImageView) findViewById(R.id.relieve_arrow);
		arrow3 = (ImageView) findViewById(R.id.tweet_arrow);
		rotateArrow(90f, arrow1);
		rotateArrow(90f, arrow2);
		rotateArrow(90f, arrow3);
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
				View toolbar1 = findViewById(R.id.alert_settings);
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
				View toolbar2 = findViewById(R.id.relieve_settings);
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
		tweet.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isAniming) {
					return;
				}
				View toolbar3 = findViewById(R.id.tweet_settings);
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
		});

	}

	private void initCalendarText() {
		calendar = Calendar.getInstance();
		Date date = new Date(calendar.getTimeInMillis());
		SimpleDateFormat format = new SimpleDateFormat("hh:mm", Locale.CHINESE);
		timePicker.setText(format.format(date) + "  ");
		if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
			ampm.setText("上午");
		} else {
			ampm.setText("下午");
		}
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

	class DateTimePickListener implements TimePickerDialog.OnTimeSetListener {

		@Override
		public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
			calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
			calendar.set(Calendar.MINUTE, minute);
			Date date = new Date(calendar.getTimeInMillis());
			SimpleDateFormat format = new SimpleDateFormat("hh:mm",
					Locale.CHINESE);
			timePicker.setText(format.format(date) + "  ");
			if (calendar.get(Calendar.AM_PM) == Calendar.AM) {
				ampm.setText("上午");
			} else {
				ampm.setText("下午");
			}
		}
		
	}
	
	class AddAlarmToServer extends AsyncTask<String, Integer, Integer>{

		@Override
		protected Integer doInBackground(String... params) {
			return alarmService.addAlarm(calendar, true, ringURI, labelText.getText().toString(), volumeBar.getProgress(), "0000", "shake",dbManager.getEventID(EventVO.TYPE_ALARM, reqCode));
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result!=0){
				Toast.makeText(AddAlarmActivity.this, "成功添加到公共事务版", Toast.LENGTH_SHORT).show();
				dbManager.setEventID(EventVO.TYPE_ALARM, reqCode, result);
			}
		}
		
	}
}
