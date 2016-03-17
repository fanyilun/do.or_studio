package com.geeker.door.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.PersonFragment.ItemEntity;
import com.geeker.door.AddAlarmActivity;
import com.geeker.door.AddRequestActivity;
import com.geeker.door.AddScheduleActivity;
import com.geeker.door.R;
import com.geeker.door.alarm.AlarmReceiver;
import com.geeker.door.businesslogic.AlarmService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.utils.PinnedHeaderListView.PinnedHeaderAdapter;
import com.geeker.door.wear.WearDataListener;


/**
 * 
 * @author Fanyl
 *
 */
public class CustomAdapter extends BaseAdapter  
		implements OnScrollListener , PinnedHeaderAdapter {
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String TAG = CustomAdapter.class.getSimpleName();
	
	// ===========================================================
	// Fields
	// ===========================================================

	private Context mContext;
	private List<ItemEntity> mData;
	private LayoutInflater mLayoutInflater;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private DbManager dbManager;
	private AlarmService alarmService;
	
	
	public CustomAdapter(Context pContext, List<ItemEntity> pData) {
		mContext = pContext;
		mData = pData;
		alarmService=new AlarmService(pContext);
		dbManager=new DbManager(pContext);
		mLayoutInflater = LayoutInflater.from(mContext);
	}
	
	
	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
	 // 常见的优化ViewHolder
        ViewHolder viewHolder = null;
        if (null == convertView) {
            convertView = mLayoutInflater.inflate(R.layout.listview_item, null);
            viewHolder = new ViewHolder();
            viewHolder.title = (TextView) convertView.findViewById(R.id.title); 
            viewHolder.content = (TextView) convertView.findViewById(R.id.content);
            viewHolder.subContent=(TextView)convertView.findViewById(R.id.subcontent);
            viewHolder.contentIcon = (ImageView) convertView.findViewById(R.id.content_icon);
            viewHolder.addTime=(TextView)convertView.findViewById(R.id.add_time);

            viewHolder.layout = (View) convertView.findViewById(R.id.layout);

            viewHolder.editLayout= convertView.findViewById(R.id.edit_layout);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // 获取数据
        final ItemEntity itemEntity = (ItemEntity) getItem(position);
        viewHolder.content.setText(itemEntity.getContent());
        viewHolder.subContent.setText(itemEntity.getSubContent());
        viewHolder.addTime.setText(itemEntity.getTime());
//        slidingMap.put(position, viewHolder.layout);
        switch (itemEntity.getType()) {
        case EventVO.TYPE_ALARM:
			viewHolder.content.setTextSize(30);
			viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_alarm));
//			viewHolder.editButton.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Toast.makeText(mContext, "闹钟没有这个功能哦", Toast.LENGTH_SHORT).show();
//				}
//			});
			break;
		case EventVO.TYPE_SCHEDULE:
			viewHolder.content.setTextSize(30);
			/*if(itemEntity.getContent().length()>8){
				viewHolder.content.setTextSize(280/itemEntity.getContent().length());
			}*/
//			viewHolder.editButton.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					new FinishEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							dbManager.getEventID(EventVO.TYPE_SCHEDULE, itemEntity.getRequestCode()));
//					mData.remove(position);
//					slidingMap.remove(position);
//					opendPosition = -1;
//					dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
//					notifyDataSetChanged();
//					new WearDataListener(mContext).sendMemoData();
//				}
//			});
			viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_schedule));
			break;
		case EventVO.TYPE_REQUEST:
			viewHolder.content.setTextSize(30);
			/*if(itemEntity.getContent().length()>8){
				viewHolder.content.setTextSize(280/itemEntity.getContent().length());
			}*/
//			viewHolder.editButton.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					new FinishEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//							dbManager.getEventID(EventVO.TYPE_REQUEST, itemEntity.getRequestCode()));
//					mData.remove(position);
//					slidingMap.remove(position);
//					opendPosition = -1;
//					dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
//					notifyDataSetChanged();
//				}
//			});
			viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_request));
			break;
		}
        //viewHolder.contentIcon.setImageResource(R.drawable.ic_launcher);
        //滑动按钮
//        viewHolder.button.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Set<Integer> keySet = slidingMap.keySet();
//				for (Integer i : keySet) {
//					if (opendPosition != position) {
//						slidingMap.get(i).setDefault();
//					}
//				}
//				if (slidingMap.get(position).toggle()) {
//					opendPosition = position;
//				} else {
//					opendPosition = -1;
//				}
//			}
//		});
        //编辑
//        viewHolder.editLayout.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				ItemEntity event=mData.get(position);
//				Class<?> cls = null;
//				switch (event.getType()) {
//				case EventVO.TYPE_ALARM:
//					cls=AddAlarmActivity.class;
//					break;
//				case EventVO.TYPE_SCHEDULE:
//					cls=AddScheduleActivity.class;
//					break;
//				case EventVO.TYPE_REQUEST:
//					cls=AddRequestActivity.class;
//					break;
//				}
//				Intent i=new Intent(mContext,cls);
//				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				i.putExtra("requestCode", event.getRequestCode());
//				mContext.startActivity(i);
//			}
//		});
        //删除按钮
//        viewHolder.deleteButton.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				new DeleteEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//						dbManager.getEventID(itemEntity.getType(), itemEntity.getRequestCode()));
//				if(itemEntity.getType()==EventVO.TYPE_ALARM){
//					Intent intent = new Intent(mContext,AlarmReceiver.class);
//					PendingIntent pi = PendingIntent.getBroadcast(mContext,itemEntity.getRequestCode(),intent, PendingIntent.FLAG_CANCEL_CURRENT);
//					AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//					am.cancel(pi);
//				}
//				mData.remove(position);
//				slidingMap.remove(position);
//				opendPosition = -1;
//				dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
//				notifyDataSetChanged();
////				new DeleteEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
////						dbManager.getEventID(itemEntity.getType(), itemEntity.getRequestCode()));
//			}
//		});
        if ( needTitle(position) ) {
            // 显示标题并设置内容 
            viewHolder.title.setText(itemEntity.getTitle());
            viewHolder.title.setVisibility(View.VISIBLE);
        } else {
            // 内容项隐藏标题
            viewHolder.title.setVisibility(View.GONE);
        }
//        if (opendPosition == position) {
//			viewHolder.layout.open();
//		} else {
//			viewHolder.layout.setDefault();
//		}

		new WearDataListener(mContext).sendMemoData();
        return convertView;
	}
	
	@Override
	public int getCount() {
		if (null != mData) {
			return mData.size();
		}
		return 0;
	}

	public void clickItem(int index){
		if(index>=mData.size() || index<0){return;}
				ItemEntity event=mData.get(index);
				Class<?> cls = null;
				switch (event.getType()) {
				case EventVO.TYPE_ALARM:
					cls=AddAlarmActivity.class;
					break;
				case EventVO.TYPE_SCHEDULE:
					cls=AddScheduleActivity.class;
					break;
				case EventVO.TYPE_REQUEST:
					cls=AddRequestActivity.class;
					break;
				}
				Intent i=new Intent(mContext,cls);
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				i.putExtra("requestCode", event.getRequestCode());
				mContext.startActivity(i);
	}

	public void deleteButton(int position){
		ItemEntity itemEntity = (ItemEntity) getItem(position);
		new DeleteEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
				dbManager.getEventID(itemEntity.getType(), itemEntity.getRequestCode()));
				if(itemEntity.getType()==EventVO.TYPE_ALARM){
					Intent intent = new Intent(mContext,AlarmReceiver.class);
					PendingIntent pi = PendingIntent.getBroadcast(mContext,itemEntity.getRequestCode(),intent, PendingIntent.FLAG_CANCEL_CURRENT);
					AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
					am.cancel(pi);
				}
				mData.remove(position);
				dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
				notifyDataSetChanged();
//				new DeleteEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
//						dbManager.getEventID(itemEntity.getType(), itemEntity.getRequestCode()));
	}

	public void finishButton(int position){
		ItemEntity itemEntity = (ItemEntity) getItem(position);
		switch (itemEntity.getType()) {
			case EventVO.TYPE_SCHEDULE:
				new FinishEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						dbManager.getEventID(EventVO.TYPE_SCHEDULE, itemEntity.getRequestCode()));
				mData.remove(position);
				dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
				notifyDataSetChanged();
				new WearDataListener(mContext).sendMemoData();
				break;
			case EventVO.TYPE_ALARM:
				Toast.makeText(mContext, "闹钟没有这个功能哦", Toast.LENGTH_SHORT).show();
				break;
			case EventVO.TYPE_REQUEST:
				new FinishEventTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,
						dbManager.getEventID(EventVO.TYPE_REQUEST, itemEntity.getRequestCode()));
				mData.remove(position);
				dbManager.deleteItem(itemEntity.getType(), itemEntity.getRequestCode());
				notifyDataSetChanged();
				break;

		}
	}

	@Override
	public Object getItem(int position) {
		if (null != mData && position < getCount()) {
			return mData.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	
	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		if ( view instanceof PinnedHeaderListView) {
			((PinnedHeaderListView) view).controlPinnedHeader(firstVisibleItem);	
		}
	}
	
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}
	
	
	@Override
	public int getPinnedHeaderState(int position) {
		if (getCount() == 0 || position <= 1) {
			return PinnedHeaderAdapter.PINNED_HEADER_GONE;
		}
		
		if (isMove(position-2) == true) {
			return PinnedHeaderAdapter.PINNED_HEADER_PUSHED_UP;
		}
		
		return PinnedHeaderAdapter.PINNED_HEADER_VISIBLE;
	}


	@Override
	public void configurePinnedHeader(View headerView, int position, int alpaha) {
		// 设置标题的内容
		ItemEntity itemEntity = (ItemEntity) getItem(position-2);
		String headerValue = itemEntity.getTitle();
		if (!TextUtils.isEmpty(headerValue)) {
			TextView headerTextView = (TextView) headerView.findViewById(R.id.header);
			headerTextView.setText( headerValue );
		}
		
	}
	
	// ===========================================================
	// Methods
	// ===========================================================

	/**
	 * 判断是否需要显示标题
	 * 
	 * @param position
	 * @return
	 */
	private boolean needTitle(int position) {
		// 第一个肯定是分类
		if (position == 0) {
			return true;
		}
		
		// 异常处理
        if (position < 0) {
            return false;
        }
		 
		// 当前  // 上一个
		ItemEntity currentEntity = (ItemEntity) getItem(position);
		ItemEntity previousEntity = (ItemEntity) getItem(position - 1);
		if (null == currentEntity || null == previousEntity) {
            return false;
        }
		
		String currentTitle = currentEntity.getTitle();
		String previousTitle = previousEntity.getTitle();
		if (null == previousTitle || null == currentTitle) {
            return false;
        }
		
		// 当前item分类名和上一个item分类名不同，则表示两item属于不同分类
		if (currentTitle.equals(previousTitle)) {
			return false;
		}
		
		return true;
	}


	private boolean isMove(int position) {
	    // 获取当前与下一项
	    ItemEntity currentEntity = (ItemEntity) getItem(position);
	    ItemEntity nextEntity = (ItemEntity) getItem(position + 1);
	    if (null == currentEntity || null == nextEntity) {
            return false;
        }

	    // 获取两项header内容
	    String currentTitle = currentEntity.getTitle();
	    String nextTitle = nextEntity.getTitle();
	    if (null == currentTitle || null == nextTitle) {
            return false;
        }
	    
	    // 当前不等于下一项header，当前项需要移动了
	    if (!currentTitle.equals(nextTitle)) {
            return true;
        }
	    
	    return false;
	}
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	class FinishEventTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			return alarmService.finishAlarm(params[0].toString());
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
	
	class DeleteEventTask extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... params) {
			return alarmService.deleteAlarm(params[0].toString());
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}
	}
	
	
	
	private class ViewHolder {
        TextView title;
        TextView content;
        TextView subContent;
        TextView addTime;
        ImageView contentIcon;
        View layout;


        View editLayout;
    }
	
}
