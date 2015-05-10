package com.geeker.door;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.fourmob.datetimepicker.date.DatePickerDialog.OnDateSetListener;
import com.geeker.door.EventDetailsActivity.MyAdapter;
import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.businesslogic.RequestService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.RequestVO;
import com.geeker.door.renren.RenrenService;
import com.geeker.door.utils.ExpandAnimation;
import com.geeker.door.utils.MyViewGroup;
import com.sleepbot.datetimepicker.time.RadialPickerLayout;
import com.sleepbot.datetimepicker.time.TimePickerDialog;

public class AddRequestActivity extends MyActionBarActivity {
	static final int[][] IDinfo = new int[][] {
			{ R.id.time_settings, R.id.time_bg, R.id.time_switch,R.id.time_text },
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
	EditText contentText;
	Calendar calendarSchedule;
	MyViewGroup myViewgroup;
	MyViewGroup myViewgroup2;
	boolean isAniming;
	boolean firstInit=true;
	private RenrenService renrenService;
	private DbManager dbManager;
	private RequestService requestService;
	private int reqCode=-1;
	boolean[] switches=new boolean[]{true,false,false,true};;
	private BoardService boardService;
	ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_request);
		requestService=new RequestService(this);
		boardService=new BoardService(this);
		renrenService=new RenrenService(this);
		//testMethod();
		setTitle("添加请求");
		initButtons();
		initExtra();
		initExpand();
	}
	
	private void initExtra() {
		int requestCode=getIntent().getIntExtra("requestCode", -1);
		if(requestCode!=-1){
			setTitle("修改请求");
			reqCode=requestCode;
			RequestVO vo=dbManager.getRequest(requestCode);
			switches=vo.getSwitches();
			if(vo.getOccurTime()!=null){
				calendarSchedule=vo.getOccurTime();
				SimpleDateFormat time=new SimpleDateFormat("HH : mm");
				scheduleTime.setText(time.format(new Date(calendarSchedule.getTimeInMillis())));
				scheduleDate.setText(calendarSchedule.get(Calendar.YEAR)+" 年 "+(calendarSchedule.get(Calendar.MONTH)+1)+" 月 "+calendarSchedule.get(Calendar.DAY_OF_MONTH)+" 日");
			}
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
		LinearLayout boardLinear=(LinearLayout)findViewById(R.id.board_linear);
		boardLinear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(AddRequestActivity.this, "此处不可修改哦", Toast.LENGTH_SHORT).show();
			}
		});
		calendarSchedule=Calendar.getInstance();
		calendarSchedule.add(Calendar.DATE, 1);
		dbManager=new DbManager(this);
		scheduleDate=(TextView)findViewById(R.id.request_date);
		scheduleTime=(TextView)findViewById(R.id.request_time);
		contentText=(EditText)findViewById(R.id.content_text);
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
		SimpleDateFormat time=new SimpleDateFormat("HH : mm");
		scheduleTime.setText(time.format(new Date()));
		scheduleDate.setText(calendarSchedule.get(Calendar.YEAR)+" 年 "+(calendarSchedule.get(Calendar.MONTH)+1)+" 月 "+calendarSchedule.get(Calendar.DAY_OF_MONTH)+" 日");
		ImageView addFriend=(ImageView)findViewById(R.id.friend_add);
		ImageView addLabel=(ImageView)findViewById(R.id.classify_add);
		myViewgroup = (MyViewGroup) findViewById(R.id.friend_viewgroup);
		myViewgroup2 = (MyViewGroup) findViewById(R.id.classify_viewgroup);
		addFriend.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				progressDialog = ProgressDialog.show(AddRequestActivity.this, "Do.or", "正在获取好友列表", true, true);  
				new InitFriendsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
			}
		});
		addLabel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				 final EditText inputServer = new EditText(AddRequestActivity.this);
			        AlertDialog.Builder builder = new AlertDialog.Builder(AddRequestActivity.this);
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
		ImageView okButton=(ImageView)findViewById(R.id.button_ok);
		ImageView cancelButton=(ImageView)findViewById(R.id.button_cancel);
		final Switch renren = (Switch) findViewById(R.id.switch_renren);
		okButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!switches[0]){calendarSchedule=null;}
				String[] companies=getCompanies();
				String[] tags=getTags();
				final String[] content=contentText.getText().toString().split("\n", 2);
				String contentString=content.length>1?content[1]:"";
				if(reqCode<0){
					reqCode=dbManager.saveRequest(content[0], contentString, calendarSchedule, companies, tags,switches,0);
				}else{
					dbManager.updateRequest(reqCode,content[0], contentString, calendarSchedule, companies, tags,switches);
				}
				Toast.makeText(getApplicationContext(), "添加成功",Toast.LENGTH_SHORT).show();
				new AddRequestToServer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "");
				finish();
				if(renren.isChecked()){
					new Handler().post(new Runnable() {
						@Override
						public void run() {
							renrenService.share2Renren(EventVO.TYPE_REQUEST, content[0]);
						}
					});
				}
			}

		});
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	protected String[] getTags() {
		if(!switches[2] || myViewgroup2.getChildCount()<=0){
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
		if(!switches[1] || myViewgroup.getChildCount()<=0){
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
	public void onWindowFocusChanged(boolean hasFocus) {
		if(firstInit){
			if (!switches[0]) {
				View toolbar1 = findViewById(R.id.time_settings);
				ExpandAnimation.initViewMargin(toolbar1, toolbar1.getHeight());
				toolbar1.setVisibility(View.GONE);
			}
			if (!switches[1]) {
				View toolbar2 = findViewById(R.id.friend_settings);
				ExpandAnimation.initViewMargin(toolbar2, toolbar2.getHeight());
				toolbar2.setVisibility(View.GONE);
			}
			if (!switches[2]) {
				View toolbar3 = findViewById(R.id.classify_settings);
				ExpandAnimation.initViewMargin(toolbar3, toolbar3.getHeight());
				toolbar3.setVisibility(View.GONE);
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

	private void testMethod() {
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
	}
	
	
	private void initExpand() {
		 LinearLayout time=(LinearLayout)findViewById(R.id.time_title);
			LinearLayout friend=(LinearLayout)findViewById(R.id.friend_title);
			LinearLayout classify=(LinearLayout)findViewById(R.id.classify_title);
			LinearLayout tweet=(LinearLayout)findViewById(R.id.tweet_title);
			arrow1=(ImageView)findViewById(R.id.time_arrow);
			arrow3=(ImageView)findViewById(R.id.friend_arrow);
			arrow4=(ImageView)findViewById(R.id.classify_arrow);
			arrow5=(ImageView)findViewById(R.id.tweet_arrow);
			ImageView requestArrow=(ImageView) findViewById(R.id.request_arrow);
			if(switches[0]){rotateArrow(90f,arrow1);}
			if(switches[1]){rotateArrow(90f,arrow3);}
			if(switches[2]){rotateArrow(90f,arrow4);}
			rotateArrow(90f,requestArrow);
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
			time.setOnClickListener(new ExpandClickListener(0,arrow1));
			friend.setOnClickListener(new ExpandClickListener(1,arrow3));
			classify.setOnClickListener(new ExpandClickListener(2, arrow4));
			tweet.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Toast.makeText(AddRequestActivity.this, "此处不可修改哦", Toast.LENGTH_SHORT).show();					
				}
			});
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
		
		private void refresh(){
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
	
	class AddRequestToServer extends AsyncTask<String, Integer, Integer>{

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
			return requestService.addRequest(calendarSchedule,true, content[0], contentText,list1,list2,dbManager.getEventID(EventVO.TYPE_REQUEST, reqCode));
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result!=0){
				Toast.makeText(AddRequestActivity.this, "成功添加到公共事务版", Toast.LENGTH_SHORT).show();
				dbManager.setEventID(EventVO.TYPE_REQUEST, reqCode, result);
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
			new AlertDialog.Builder(AddRequestActivity.this)  
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
	
	
}
