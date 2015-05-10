package com.geeker.door.ringtone;

import java.util.ArrayList;
import java.util.List;

import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class SystemRingtoneFragment extends Fragment implements OnItemClickListener,OnItemSelectedListener{
	
	RingtoneCallback myCallback;
	List<Ringtone> ringtones;
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
        list.setChoiceMode(ListView.CHOICE_MODE_SINGLE);  
        list.setItemChecked(0, true);
        
		return list;
	}

	private String[] getSystemRingtoneName() {
		List<Ringtone> ringtones = new ArrayList<Ringtone>(); 
		List<String> resArr = new ArrayList<String>();
	    manager = new RingtoneManager(getActivity()); 
	    manager.setType(RingtoneManager.TYPE_ALARM); 
	    Cursor cursor = manager.getCursor(); 
	    int count = cursor.getCount(); 
	    for(int i = 0 ; i < count ; i ++){ 
	    	ringtones.add(manager.getRingtone(i)); 
	    } 
	    if(cursor.moveToFirst()){ 
	        do{ 
	            resArr.add(cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX)); 
	        }while(cursor.moveToNext()); 
	    } 
	    return resArr.toArray(new String[0]);
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
		System.out.println("click");
		myCallback.onRingtoneSelected(SelectRingtoneActivity.TYPE_SYSTEM,manager.getRingtoneUri(position).toString(), contentString[position]);
	}
	
	public void setMyCallback(RingtoneCallback myCallback){
		this.myCallback=myCallback;
	}
	
}
