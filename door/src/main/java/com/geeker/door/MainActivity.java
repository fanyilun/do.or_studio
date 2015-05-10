package com.geeker.door;


import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v4.widget.DrawerLayout;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.friends.FriendsActivity;
import com.geeker.door.imgcache.ImageDownloader;
import com.geeker.door.lock.MyLockService;
import com.geeker.door.lock.WeatherRefreshReceiver;
import com.geeker.door.renren.Constants;
import com.geeker.door.utils.MyViewPager;
import com.geeker.door.welcome.LoginActivity;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;

public class MainActivity extends MyActionBarActivity {
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private ImageView imageView;
	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度
	private static final int COLUMN_NUM=2;
	private DbManager dbManager;
	private BoardService boardService;
	private TextView messageNum;
	private RennClient rennClient;
	private PublicFragment publicFragment;
	String accessToken;
	TextView levelText;
	TextView signing;
	ProgressBar levelBar;
	
	private BroadcastReceiver receiver1=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			levelText.setText("Lv"+intent.getStringExtra("lev")+"("+intent.getIntExtra("exp", 0)+"/"+intent.getIntExtra("max", 0)+")");
			levelBar.setMax(intent.getIntExtra("max", 0));
			levelBar.setProgress(intent.getIntExtra("exp", 0));
		}
	};
	private BroadcastReceiver receiver2=new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			signing.setText("连续签到："+intent.getStringExtra("sign")+"天");
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
//		mDrawerList = (ListView) findViewById(R.id.left_drawer);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		dbManager=new DbManager(this);
		boardService=new BoardService(this);
		initLeftDrawer();
		initCursor();
		initViewPager();
		setTitle("");
		//TODO
		if(dbManager.isDoorLock()){
		Intent intent=new Intent(this,MyLockService.class);
		startService(intent);
		}
		initWeatherReceiver();
		/*PushManager.startWork(getApplicationContext(),
				PushConstants.LOGIN_TYPE_API_KEY, 
				Utils.getMetaValue(this, "api_key"));*/
		registerReceiver(receiver1, new IntentFilter("com.geeker.door.EXPLEV"));
		registerReceiver(receiver2, new IntentFilter("com.geeker.door.SIGNDAYS"));
	}

	@Override
	protected void onStart() {
		new InitMsgTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		super.onStart();
	}
	
	private void initWeatherReceiver() {
		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getApplicationContext(),	WeatherRefreshReceiver.class);
		//requestCode=1
		PendingIntent sender = PendingIntent.getBroadcast(getApplicationContext(),1,intent,	PendingIntent.FLAG_CANCEL_CURRENT);
		//每一个小时更新一次
		am.setRepeating(AlarmManager.RTC_WAKEUP,Calendar.getInstance().getTimeInMillis(),3600000, sender);
	}
	
	private void initCursor() {
		imageView= (ImageView) findViewById(R.id.cursor);
		bmpW = BitmapFactory.decodeResource(getResources(), R.drawable.cursor).getWidth();// 获取图片宽度
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		int screenW = dm.widthPixels;// 获取分辨率宽度
		offset = (screenW / COLUMN_NUM - bmpW) / 2;// 计算偏移量
		Matrix matrix = new Matrix();
		//matrix.postTranslate(offset, 0);
		matrix.postScale((float)screenW/(float)COLUMN_NUM/bmpW, 1);
		imageView.setImageMatrix(matrix);// 设置动画初始位置
	}

	private void initViewPager() {
		final MyViewPager pager=(MyViewPager)findViewById(R.id.vPager);
		List<Fragment> views=new ArrayList<Fragment>();
		views.add(new PersonFragment());
		publicFragment=new PublicFragment();
		views.add(publicFragment);
		pager.setAdapter(new ContentViewPagerAdapter(getSupportFragmentManager(),views));
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new MyOnPageChangeListener());
		TextView indicator1=(TextView)findViewById(R.id.indicator1);
		TextView indicator2=(TextView)findViewById(R.id.indicator2);
		indicator1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(0);
			}
		});
		indicator2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				pager.setCurrentItem(1);
			}
		});
	}

	private void initLeftDrawer() {
		TextView message=(TextView) findViewById(R.id.message_textview);
		TextView account=(TextView) findViewById(R.id.leftmenu_account_textview);
		TextView name=(TextView) findViewById(R.id.leftmenu_username_textview);
		TextView friend=(TextView) findViewById(R.id.friend_manage_text);
		TextView quit=(TextView) findViewById(R.id.leftmenu_logout_textview);
		TextView achievement=(TextView) findViewById(R.id.leftmenu_chengjiu_textview);
		levelText=(TextView) findViewById(R.id.leftmenu_level_textview);
		signing=(TextView) findViewById(R.id.leftmenu_signing_textview);
		levelBar=(ProgressBar) findViewById(R.id.leftmenu_progressBar);
		
		ImageView img=(ImageView)findViewById(R.id.leftmenu_header_imageview);
		messageNum=(TextView)findViewById(R.id.msg_num);
		account.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(MainActivity.this,AccountInfoActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
			}
		});
		quit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				dbManager.saveUserName("");
				Intent i=new Intent(MainActivity.this,LoginActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
			}
		});
		message.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i=new Intent(MainActivity.this,MessageCenterActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
			}
		});
		friend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO
				//initToken();
				Intent i=new Intent(MainActivity.this,FriendsActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				startActivity(i);
			}
		});
		achievement.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(MainActivity.this).setTitle("成就中心").setMessage("由于美工罢工，此项功能尚未开发")
		          .setNegativeButton("美工是大坏蛋", new DialogInterface.OnClickListener() {
		              public void onClick(DialogInterface dialog, int which) {
		              }
		          })
		          .show();
			}
		});
		mDrawerLayout.closeDrawers();
		String url=dbManager.getKey("headURL");
		if(url!=null && !url.equals("null")){
			ImageDownloader mImageDownloader = new ImageDownloader(this);
			mImageDownloader.download(url, img,ScaleType.FIT_XY);
		}
		String nameText=dbManager.getKey("nickName");
		if(nameText!=null && !nameText.equals("null")){
			name.setText(nameText);
		}
	}
	
	private void initToken() {
		rennClient = RennClient.getInstance(this);
		rennClient.init(Constants.APP_ID, Constants.API_KEY, Constants.Secret_Key);
		rennClient.setScope(Constants.SCOPE);
		rennClient.setTokenType("bearer");
		rennClient.setLoginListener(new LoginListener() {
			@Override
			public void onLoginSuccess() {
				accessToken=rennClient.getAccessToken().accessToken;
				Intent i=new Intent(MainActivity.this,RecommendActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
				i.putExtra("token", accessToken);
				System.out.println("rennClient.getUid()="+rennClient.getUid());
				i.putExtra("renrenID", rennClient.getUid().toString());
				startActivity(i);
			}
			
			@Override
			public void onLoginCanceled() {
			}
		});
		rennClient.login(this);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		return super.onOptionsItemSelected(item);
	}
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggls
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    public class ContentViewPagerAdapter extends FragmentPagerAdapter {

		private List<Fragment> list;

		public ContentViewPagerAdapter(FragmentManager fm, List<Fragment> list) {
			super(fm);
			
			this.list = list;
		}

		@Override
		public android.support.v4.app.Fragment getItem(int arg0) {
			return list.get(arg0);
		}
		
		
		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}

		@Override
		public int getCount() {
			return list.size();
		}
	}
    
    public class MyOnPageChangeListener implements OnPageChangeListener{

    	int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量
		@Override
		public void onPageScrollStateChanged(int arg0) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			
		}

		@Override
		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			imageView.startAnimation(animation);
			if(arg0==1){
				publicFragment.refreshBoard();
			}
		}
    }
    
    
    private class InitMsgTask extends AsyncTask<Void, Void, Integer> {
		@Override
		protected Integer doInBackground(Void... params) {
			return boardService.getNotifictionNum();
		}
		
		@Override
		protected void onPostExecute(Integer result) {
			if(result>0){
				getActionBar().setLogo(getResources().getDrawable(R.drawable.actionbar_icon_red));
				messageNum.setVisibility(View.VISIBLE);
				messageNum.setText(String.valueOf(result));
			}else{
				getActionBar().setLogo(getResources().getDrawable(R.drawable.actionbar_icon));
				messageNum.setVisibility(View.GONE);
			}
			super.onPostExecute(result);
		}
	}
    
    @Override
    protected void onDestroy() {
    	unregisterReceiver(receiver1);
    	unregisterReceiver(receiver2);
    	super.onDestroy();
    }

}
