package com.geeker.door.ringtone;

import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class UserRingtoneFragment extends Fragment implements OnItemClickListener,OnItemSelectedListener{
	
	RingtoneCallback myCallback;
	String[] ringURI;
	RingtoneManager manager;
	String[] contentString;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ListView list=new ListView(getActivity());
		contentString = getSystemRingtoneName();
        ListAdapter arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice,  
                contentString);  
        list.setAdapter(arrayAdapter);  
        list.setOnItemClickListener(this);  
        list.setOnItemSelectedListener(this);  
        list.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);  
        list.setItemChecked(0, true);
		return list;
	}

	private String[] getSystemRingtoneName() {
		String[] media_info = new String[] {
				MediaColumns.TITLE,
				AudioColumns.DURATION,
				AudioColumns.ARTIST,
				BaseColumns._ID,
				MediaColumns.DISPLAY_NAME,
				MediaColumns.DATA,
				AudioColumns.ALBUM_ID };
		Cursor cursor = getActivity().getContentResolver().query(
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
			ringURI = new String[cursor.getCount()];
			for (int i = 0; i < cursor.getCount(); i++) {
				_ids[i] = cursor.getInt(3);
				_titles[i] = cursor.getString(0);
				_artists[i] = cursor.getString(2);
				ringURI[i]=Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "" + _ids[i]).toString();
				cursor.moveToNext();// 将游标移到下一行
			}
		}
	    return _titles;
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
		myCallback.onRingtoneSelected(SelectRingtoneActivity.TYPE_SYSTEM, ringURI[position], contentString[position]);
	}
	
	public void setMyCallback(RingtoneCallback myCallback){
		this.myCallback=myCallback;
	}
	
}
