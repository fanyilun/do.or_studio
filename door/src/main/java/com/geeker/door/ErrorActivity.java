package com.geeker.door;

import com.geeker.door.database.DbManager;

import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class ErrorActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.error);
		TextView text=(TextView)findViewById(R.id.textview1);
		text.setText(new DbManager(this).getKey("error"));
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
	
}
