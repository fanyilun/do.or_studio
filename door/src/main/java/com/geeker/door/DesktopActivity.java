package com.geeker.door;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.geeker.door.database.DbHelper;
import com.geeker.door.database.DbManager;
/**
 * 本activity为点击任务栏图标后弹出的快捷输入框
 * 讲一下遇到的问题吧：
 * 打开这个activity之前，如果主程序没有完全退出（home键强退），那么显示这个界面的时候还是会把背景当做未退出界面的背景。就是说再次按返回键会返回到主界面而不是桌面
 * 解决方法。就是在本activity的Manifest设置
 * android:taskAffinity="com.example.clockdemo2"
 * 其实taskAffinity就是activity的task堆栈的名字，默认都是包名。换一个名字就相当于新建一个堆栈了
 *
 */
public class DesktopActivity extends Activity{
	EditText editText;
	Button button1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		//注意要先setContentView再findViewById
		setContentView(R.layout.desktop);
		View v = findViewById(R.id.desktopLayout);//找到你要设透明背景的layout 的id
		v.getBackground().setAlpha(100);//0~255透明度值 ，0为完全透明，255为不透明
		v.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		initButton();
	}

	private void initButton() {
		editText=(EditText)findViewById(R.id.edittext);
		button1=(Button)findViewById(R.id.testButton);
		editText.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE ) { 
					clickButton();
				}
				return false;
			}
		});
		button1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				clickButton();
			}
		});
		//editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
	}
	
	private void clickButton() {
		DbManager dbManager=new DbManager(this);
		dbManager.saveMemo(editText.getText().toString(), "", null, null, "", 0, new String[0], new String[0],new boolean[]{true,false,false,false,true},0);
		Toast.makeText(getApplicationContext(), "添加成功", Toast.LENGTH_SHORT).show(); 
		button1.setText("");
	}

}
