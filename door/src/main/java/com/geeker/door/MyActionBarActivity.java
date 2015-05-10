package com.geeker.door;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.TextView;


public class MyActionBarActivity extends FragmentActivity{


    SelectAddPopupWindow addWindow;
    SelectSettingPopupWindow settingWindow;
    ImageButton addButton;
    ImageButton settingButton ;
    
    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		int titleId = getResources().getIdentifier("action_bar_title", "id", "android");
		TextView abTitle = (TextView) findViewById(titleId);
		abTitle.setTextColor(Color.WHITE);
	}
    @Override
	public void onWindowFocusChanged(boolean hasFocus) {
		getActionBar().setDisplayUseLogoEnabled(true);
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		addButton = (ImageButton) menu.findItem(R.id.menu_add).getActionView();
		settingButton = (ImageButton) menu.findItem(R.id.menu_setting).getActionView();
        addButton.setImageDrawable(getResources().getDrawable(R.drawable.actionbar_add));
        addButton.setBackgroundColor(getResources().getColor(R.color.main_color));
        addButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					addButton.setBackgroundColor(getResources().getColor(R.color.press_color));
					break;
				case MotionEvent.ACTION_UP:
					addButton.setBackgroundColor(getResources().getColor(R.color.main_color));
					break;
				}
				return false;
			}
		});
        //addButton.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_add));
        settingButton.setImageDrawable(getResources().getDrawable(R.drawable.actionbar_setting));
        settingButton.setBackgroundColor(getResources().getColor(R.color.main_color));
        settingButton.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					settingButton.setBackgroundColor(getResources().getColor(R.color.press_color));
					break;
				case MotionEvent.ACTION_UP:
					settingButton.setBackgroundColor(getResources().getColor(R.color.main_color));
					break;
				}
				return false;
			}
		});
        addButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddWindow();
			}
		});
        settingButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showSettingWindow();
			}
		});
		return true;
	}
	
	protected void showSettingWindow() {
		settingWindow = new SelectSettingPopupWindow(this);
		int xoffInPixels = settingWindow.getWidth() - settingButton.getWidth() + 10;
		settingWindow.showAsDropDown(settingButton, -xoffInPixels, 0);
	}

    
    private void showAddWindow() {
    	addWindow = new SelectAddPopupWindow(this);
		int xoffInPixels = addWindow.getWidth() - addButton.getWidth() + 10;
		addWindow.showAsDropDown(addButton, -xoffInPixels, 0);
	}
    
}
