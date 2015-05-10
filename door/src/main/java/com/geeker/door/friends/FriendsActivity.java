package com.geeker.door.friends;

import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.geeker.door.MyActionBarActivity;
import com.geeker.door.R;
import com.geeker.door.ringtone.SystemRingtoneFragment;
import com.geeker.door.ringtone.UserRingtoneFragment;

public class FriendsActivity extends MyActionBarActivity{

	 private ImageView imageView;
		private int offset = 0;// 动画图片偏移量
		private int currIndex = 0;// 当前页卡编号
		private int bmpW;// 动画图片宽度
		private static final int COLUMN_NUM=2;
	public static final int NEW_FRIEND=1;
	public static final int FRIENDS_LIST=2;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friends);
		initCursor();
		initViewPager();
		setTitle("好友管理");
	} 
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish(); // finish当前activity
			return true;
		}
		return super.onOptionsItemSelected(item);
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
		final ViewPager pager=(ViewPager)findViewById(R.id.vPager);
		List<Fragment> views=new ArrayList<Fragment>();
		NewfriendFragment newfriend=new NewfriendFragment();
		FriendsListFragment friendslist=new FriendsListFragment();
		views.add(newfriend);
		views.add(friendslist);
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
		}
    }
	
}
