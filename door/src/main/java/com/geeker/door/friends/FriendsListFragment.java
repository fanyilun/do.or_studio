package com.geeker.door.friends;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sourceforge.pinyin4j.PinyinHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.AdapterView.OnItemClickListener;

import com.geeker.door.R;
import com.geeker.door.businesslogic.FriendsService;
import com.geeker.door.utils.FriendCustomAdapter;
import com.geeker.door.utils.PinnedHeaderListView;

public class FriendsListFragment extends Fragment{
	
	PinnedHeaderListView listview;
	FriendsService friendsService;
	List<ItemEntity> data;
	List<ItemEntity> filterData;
	FriendCustomAdapter adapter;
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
		View v=inflater.inflate(R.layout.friends_list,null);
		friendsService=new FriendsService(getActivity());
		listview=(PinnedHeaderListView)v.findViewById(R.id.listview);
		listview.setDragable(false);
		initSearch(v);
		new InitTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
		return v;
	}

	/**
	 * 就是不写注释气死你
	 * @param v
	 */
	private void initSearch(View v) {
		final EditText edittext=(EditText)v.findViewById(R.id.search);
		edittext.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {	}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
			
			@Override
			public void afterTextChanged(Editable s) {
				filterData.clear();
				for (ItemEntity itemEntity : data) {
					if(itemEntity.getUserName().contains(s) || itemEntity.getNickName().contains(s)){
						filterData.add(itemEntity);
						System.out.println("加入");
					}
				}
				Collections.sort(filterData);
				adapter.notifyDataSetChanged();
			}
		});
		
	}

	private void initListView() {
		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,long arg3) {
				System.out.println(filterData.get(arg2).nickName);
			}
			
		});
	}

	
	private class InitTask extends AsyncTask<Void, Void, Object[][]> {
        @Override
        protected Object[][] doInBackground(Void... params) {
			return friendsService.getFriendsList();
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	//初始化ListView的数据
        	data = new ArrayList<ItemEntity>();
        	for (int i = 0; i < result.length; i++) {
        		if(result[i].length<4){continue;}
        		data.add(new ItemEntity(result[i][0].toString(), result[i][1].toString(), result[i][2].toString(),result[i][3].toString()));
			}
        	filterData=new ArrayList<FriendsListFragment.ItemEntity>(data);
        	Collections.sort(filterData);
        	adapter=new FriendCustomAdapter(getActivity(), filterData);
        	listview.setAdapter(adapter);
        	listview.setOnScrollListener(adapter);
        	initListView();
            super.onPostExecute(result);
        }
    }
	
	
	public class ItemEntity implements Comparable<ItemEntity>{
		String userName;
		String nickName;
		String headURL;
		String intimacy;
		String title;
		public ItemEntity(String userName, String nickName, String headURL,String intimacy) {
			super();
			this.userName = userName;
			this.nickName = nickName;
			this.headURL = headURL;
			this.intimacy=intimacy;
			this.title=getPinYinHeadChar(nickName);
		}
		public String getUserName() {
			return userName;
		}
		public String getNickName() {
			return nickName;
		}
		public String getHeadURL() {
			return headURL;
		}
		public String getTitle() {
			return title;
		}
		
		public String getIntimacy() {
			return intimacy;
		}
		
		@Override
		public int compareTo(ItemEntity another) {
			return this.title.compareTo(another.title);
		}
		
	}
	
	
	   /**
	    * 提取每个汉字的首字母(大写)
	    * 
	    * @param str
	    * @return
	    */
	    public static String getPinYinHeadChar(String str) {
	        if (isNull(str)) {
	            return "";
	        }
	        String convert = "";
	        for (int j = 0; j < 1; j++) {
	            char word = str.charAt(j);
	            // 提取汉字的首字母
	            String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(word);
	            if (pinyinArray != null) {
	                convert += pinyinArray[0].charAt(0);
	            }
	            else {
	                convert += word;
	            }
	        }
	        convert = string2AllTrim(convert);
	        return convert.toUpperCase();
	    }
	    
	    /**
	     * 去掉字符串包含的所有空格
	     * 
	     * @param value
	     * @return
	     */
	     public static String string2AllTrim(String value) {
	         if (isNull(value)) {
	             return "";
	         }
	         return value.trim().replace(" ", "");
	     }
	     
	     /**
	      * 判断字符串是否为空
	      */
	      public static boolean isNull(Object strData) {
	          if (strData == null || String.valueOf(strData).trim().equals("")) {
	              return true;
	          }
	          return false;
	      }
}
