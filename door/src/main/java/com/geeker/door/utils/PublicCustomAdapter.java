package com.geeker.door.utils;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.geeker.door.EventDetailsActivity;
import com.geeker.door.R;
import com.geeker.door.SettingsActivity;
import com.geeker.door.businesslogic.BoardService;
import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.imgcache.ImageDownloader;
import com.geeker.door.utils.MyViewGroup.CallBack;
import com.geeker.door.utils.PinnedHeaderListView.PinnedHeaderAdapter;

public class PublicCustomAdapter extends BaseAdapter  
		implements OnScrollListener , PinnedHeaderAdapter {
	
	// ===========================================================
	// Constants
	// ===========================================================

	private static final String TAG = PublicCustomAdapter.class.getSimpleName();
	private static final int MAX_TYPE=4;
	//0单个时间未展开  1多个事件未展开
	//2单个时间展开  3多个事件展开
		
	// ===========================================================
	// Fields
	// ===========================================================

	private Context mContext;
	private List<ItemEntity> mData;
	private LayoutInflater mLayoutInflater;
	private int[] typeMap;
	
	// ===========================================================
	// Constructors
	// ===========================================================
	private DbManager dbManager;
	private BoardService boardService;
	
	
	public PublicCustomAdapter(Context pContext, List<ItemEntity> pData) {
		mContext = pContext;
		mData = pData;
		boardService=new BoardService(pContext);
		dbManager=new DbManager(pContext);
		mLayoutInflater = LayoutInflater.from(mContext);
		initTypeMap();
		//TODO 
		//typeMap[2]=1;
	}
	
	private void initTypeMap() {
		typeMap=new int[mData.size()];
		if(typeMap!=null){
			for (int i = 0; i < typeMap.length; i++) {
				typeMap[i]=0;
			}
		}
		for (int i = 0; i < mData.size(); i++) {
			if(!mData.get(i).getRelatedID().equals("null")){
				typeMap[i]=1;
			}
		}
	}

	@Override
	public void notifyDataSetChanged() {
		initTypeMap();
		super.notifyDataSetChanged();
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
        	//TODO
        	if(typeMap[position]==1){
        		convertView = mLayoutInflater.inflate(R.layout.pub_listview_multi_item, null);
        	}else{
        		convertView = mLayoutInflater.inflate(R.layout.pub_listview_item, null);
        	}
            viewHolder = new ViewHolder();
            initViewHolder(viewHolder, convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        //初始化那个最下面的那个相关事件那个按钮
        if(typeMap[position]==1){
    		initRelated(convertView,position);
    	}
        // 获取数据
        final ItemEntity itemEntity = (ItemEntity) getItem(position);
        updateData(viewHolder,itemEntity);
        if(itemEntity.getType()==EventVO.TYPE_ALARM){
        	viewHolder.button.setOnClickListener(new ExpandButtonListener(convertView,position));
        }
        if ( needTitle(position) ) {
            // 显示标题并设置内容 
            viewHolder.title.setText(itemEntity.getTitle());
            viewHolder.title.setVisibility(View.VISIBLE);
        } else {
            // 内容项隐藏标题
            viewHolder.title.setVisibility(View.GONE);
        }
        return convertView;
	}
	
	private void updateData(final ViewHolder viewHolder, final ItemEntity itemEntity) {
		 viewHolder.content.setText(itemEntity.getContent());
	        viewHolder.subContent.setText(itemEntity.getSubContent());
	        viewHolder.addTime.setText(itemEntity.getTime());
	        viewHolder.likeButton.setText(String.valueOf(itemEntity.getLikeNum()));
	        viewHolder.dislikeButton.setText(String.valueOf(itemEntity.getDislikeNum()));
	        viewHolder.commentButton.setText(String.valueOf(itemEntity.getCommentNum()));
	        if(!itemEntity.getContent2().equals("")){
	        	viewHolder.subContent2.setVisibility(View.VISIBLE);
	        	viewHolder.subContent2.setText(itemEntity.getContent2());
	        }else{
	        	viewHolder.subContent2.setVisibility(View.GONE);
	        }
	        
	        viewHolder.tagLinear.removeAllViews();
	       //if(viewHolder.tagLinear.getChildCount()<=0){
	        for (int i = 0; i < itemEntity.getTags().length; i++) {
	        	if(itemEntity.getTags()[i].equals("")){continue;}
    			TextView label = (TextView) mLayoutInflater.inflate(R.layout.label_interval,null );
    			/*LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(  
    					 LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
    					layout.setMargins(0, 3, 0, 0);  
    					label.setLayoutParams(layout); */
            	label.setText(itemEntity.getTags()[i]);
            	viewHolder.tagLinear.addView(label);
			}
	        for (int i = 0; i < itemEntity.getFriends().length; i++) {
	        	if(itemEntity.getFriends()[i].equals("")){continue;}
	        	TextView label = (TextView) mLayoutInflater.inflate(R.layout.label_interval, null);
    			/*LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(  
   					 LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);  
   					layout.setMargins(0, 3, 0, 0);  
   					label.setLayoutParams(layout);  */
	        	label.setText(itemEntity.getFriends()[i]);
	        	viewHolder.tagLinear.addView(label);
	        }
	      //}
	        switch (itemEntity.getType()) {
	        case EventVO.TYPE_ALARM:
				viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_alarm));
				if(viewHolder.tagLinear.getChildCount()<=0){
			        for (int i = 0; i < itemEntity.getTags().length; i++) {
			        	if(itemEntity.getTags()[i].equals("")){continue;}
		    			TextView label = (TextView) mLayoutInflater.inflate(R.layout.label_interval,null );
		            	label.setText(itemEntity.getTags()[i]);
		            	viewHolder.tagLinear.addView(label);
					}
			      }
				break;
			case EventVO.TYPE_SCHEDULE:
				viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_schedule));
				if(viewHolder.tagLinear.getChildCount()<=0){
			        for (int i = 0; i < itemEntity.getTags().length; i++) {
			        	if(itemEntity.getTags()[i].equals("")){continue;}
		    			TextView label = (TextView) mLayoutInflater.inflate(R.layout.label_interval,null );
		    			viewHolder.tagLinear.addView(label);
		    			LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(  
		    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
		    			layout.setMargins(0, 3, 0, 0);  
		    			label.setLayoutParams(layout);  
		            	label.setText(itemEntity.getTags()[i]);
		            	
		            	
					}
			      }
				break;
			case EventVO.TYPE_REQUEST:
				viewHolder.contentIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.item_request));
				if(viewHolder.tagLinear.getChildCount()<=0){
			        for (int i = 0; i < itemEntity.getFriends().length; i++) {
			        	if(itemEntity.getFriends()[i].equals("")){continue;}
			        	TextView label = (TextView) mLayoutInflater.inflate(R.layout.label_interval, null);
			        	viewHolder.tagLinear.addView(label);
		    			LinearLayout.LayoutParams layout = new LinearLayout.LayoutParams(  
		    			LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);  
		    			layout.setMargins(0, 3, 0, 0);  
		    			label.setLayoutParams(layout);  
			        	label.setText(itemEntity.getFriends()[i]);
			        	
			        }
			      }
				break;
			}
	        final Animation animation = AnimationUtils.loadAnimation(mContext,	R.anim.flash);
	        viewHolder.likeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					viewHolder.likeButton.startAnimation(animation);
					new LikeTask(viewHolder.likeButton,itemEntity,true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemEntity.getRequestCode());
				}
			});
	        viewHolder.dislikeButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					viewHolder.dislikeButton.startAnimation(animation);
					new LikeTask(viewHolder.dislikeButton,itemEntity,false).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, itemEntity.getRequestCode());
				}
			});
	        if(itemEntity.getHeadURL()!=null && !itemEntity.getHeadURL().equals("null")){
	        	ImageDownloader mImageDownloader = new ImageDownloader(mContext);
				mImageDownloader.download(itemEntity.getHeadURL(), viewHolder.headIcon,ScaleType.FIT_XY);
	        }else{
	        	viewHolder.headIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_launcher));
	        }
	        viewHolder.commentButton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent i=new Intent(mContext,EventDetailsActivity.class);
					i.putExtra("eventID", itemEntity.getRequestCode());
					mContext.startActivity(i);
				}
			});
	        viewHolder.linear.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent i=new Intent(mContext,EventDetailsActivity.class);
					i.putExtra("eventID", itemEntity.getRequestCode());
					mContext.startActivity(i);
				}
			});
	        if(viewHolder.awakeButton==null){return;}
	        viewHolder.awakeButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					new AlertDialog.Builder(mContext).setTitle("强制叫醒").setMessage("这将会即时的在对方的手机上设置闹钟，继续？")
					.setPositiveButton("确认", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							new AwakeTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, String.valueOf(itemEntity.getRequestCode()));
						}
					})
			          .setNegativeButton("取消", new DialogInterface.OnClickListener() {
			              public void onClick(DialogInterface dialog, int which) {
			              }
			          })
			          .show();
					}
			});

	}

	private void initViewHolder(final ViewHolder viewHolder,View convertView){
		viewHolder.title = (TextView) convertView.findViewById(R.id.title); 
        viewHolder.content = (TextView) convertView.findViewById(R.id.content);
        viewHolder.subContent=(TextView)convertView.findViewById(R.id.subcontent);
        viewHolder.subContent2=(TextView)convertView.findViewById(R.id.subcontent2);
        viewHolder.contentIcon = (ImageView) convertView.findViewById(R.id.content_icon);
        viewHolder.headIcon = (ImageView) convertView.findViewById(R.id.head);
        viewHolder.addTime=(TextView)convertView.findViewById(R.id.add_time);
        viewHolder.button=(ImageButton)convertView.findViewById(R.id.item_button);
        viewHolder.likeButton=(Button)convertView.findViewById(R.id.item_like);
        viewHolder.dislikeButton=(Button)convertView.findViewById(R.id.item_dislike);
        viewHolder.commentButton=(Button)convertView.findViewById(R.id.item_comment);
        viewHolder.awakeButton=(Button)convertView.findViewById(R.id.wake_up);
        viewHolder.linear=convertView.findViewById(R.id.linear2);
        viewHolder.tagLinear=(MyViewGroup)convertView.findViewById(R.id.tag_liner);
        viewHolder.tagLinear.setCallBack(new CallBack() {

    	    @Override
    	    public void callBack(int height) {
    	        LayoutParams lp = viewHolder.tagLinear.getLayoutParams();
    	        lp.height = height;
    	        viewHolder.tagLinear.setLayoutParams(lp);
    	        viewHolder.tagLinear.post(new Runnable() { //must be use post, if you dircetly vg.requestLayout, can't work, who can explain, appreciated!
    	            @Override
					public void run() {
    	            	viewHolder.tagLinear.requestLayout();
    	            }
    	        });

    	    }
    	});
	}
	
	@Override
	public int getViewTypeCount() {
		return MAX_TYPE;
	}
	
	@Override
	public int getItemViewType(int position) {
		return typeMap[position];
	}
	
	private void initRelated(View convertView,int position) {
		View expandIndicator=convertView.findViewById(R.id.linear_expand);
		
		expandIndicator.setOnClickListener(new ExpandButtonListener2(convertView,position));
		
	}


	@Override
	public int getCount() {
		if (null != mData) {
			return mData.size();
		}
		return 0;
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
	
	class ExpandButtonListener implements OnClickListener{
		
		View convertView;
		int position;
		
		public ExpandButtonListener(View view,int position) {
			convertView=view;
			this.position=position;
		}

		@Override
		public void onClick(View v) {
			if(typeMap[position]==0){
				typeMap[position]=2;
			}else if(typeMap[position]==2){
				typeMap[position]=0;
			}
			View toolbar1 = convertView.findViewById(R.id.expand);
			ExpandAnimation expandAni1 = new ExpandAnimation(toolbar1, 200,	null);
			toolbar1.startAnimation(expandAni1);
		}
	}
	
	//用于同类事务展开
	class ExpandButtonListener2 implements OnClickListener{
		
		View convertView;
		int position;
		public ExpandButtonListener2(View view,int position) {
			convertView=view;
			this.position=position;
		}

		@Override
		public void onClick(View v) {
			View indicator=convertView.findViewById(R.id.linear_expand);
			View progress=convertView.findViewById(R.id.related_progress);
			ImageView line1=(ImageView)convertView.findViewById(R.id.line1);
			ImageView line2=(ImageView)convertView.findViewById(R.id.line2);
			if(typeMap[position]==1){
				//关闭底边栏
				indicator.setVisibility(View.GONE);
				//颜色变化
				line1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.button_bg));
				line2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.button_bg));
			}else{
				//关闭底边栏
				indicator.setVisibility(View.VISIBLE);
				//颜色变化
				line1.setImageDrawable(mContext.getResources().getDrawable(R.drawable.red_bg));
				line2.setImageDrawable(mContext.getResources().getDrawable(R.drawable.red_bg));
			}
			progress.setVisibility(View.VISIBLE);
			new InitRelatedEventTask(convertView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,mData.get(position).getRequestCode()+"");
			if(typeMap[position]==1){
				typeMap[position]=3;
			}else if(typeMap[position]==3){
				typeMap[position]=1;
			}
		}
	}
	
	private class InitRelatedEventTask extends AsyncTask<String, Void, Object[][]> {
		
		private View convertView;
		public InitRelatedEventTask(View convertView) {
			this.convertView=convertView;
		}
		
        @Override
        protected Object[][] doInBackground(String... params) {
        	Object[][] result=boardService.getRelated(params[0]);
			return result;
        }

        @Override
        protected void onPostExecute(Object[][] result) {
        	View progress=convertView.findViewById(R.id.related_progress);
        	progress.setVisibility(View.GONE);
        	//TODO add
        	LinearLayout toolbar1 = (LinearLayout)convertView.findViewById(R.id.linear_related);
        	if(result!=null){
        	for (int i = 1; i < result.length; i++) {
        		View item1=mLayoutInflater.inflate(R.layout.public_related_item, null);
        		ViewHolder viewHolder=new ViewHolder();
        		initViewHolder(viewHolder, item1);
        		ItemEntity itemEntity=new ItemEntity((String)result[i][0], (String)result[i][1], 
        				(String)result[i][2], (String)result[i][3],(Integer)result[i][4], (Integer)result[i][5],
        				(Integer)result[i][6], (Integer)result[i][7], (Integer)result[i][8],(String)result[i][9],null,(String)result[i][11],(String[])result[i][12],(String[])result[i][13]);
        		updateData(viewHolder,itemEntity);
        		TextView dateView=(TextView)item1.findViewById(R.id.add_date);
        		dateView.setText((String)result[i][0]);
        		toolbar1.addView(item1);
			}
        	}
    		//ExpandAnimation.initViewMargin(toolbar1, Utils.dip2px(mContext, toolbar1.getChildCount()*110));
    		ExpandAnimation.initViewMargin(toolbar1, toolbar1.getHeight());
    		ExpandAnimation expandAni1 = new ExpandAnimation(toolbar1, 200,	null);
			toolbar1.startAnimation(expandAni1);
            super.onPostExecute(result);
        }
    }
	
	private class LikeTask extends AsyncTask<Integer, Void, Integer> {
		
		private TextView convertView;
		private boolean isLike;
		private ItemEntity itemEntity;

		public LikeTask(TextView convertView,ItemEntity itemEntity,boolean isLike) {
			this.convertView=convertView;
			this.isLike=isLike;
			this.itemEntity=itemEntity;
		}
		
        @Override
        protected Integer doInBackground(Integer... params) {
        	int result=0;
        	if(isLike){
        		result=boardService.like(params[0]);
        		itemEntity.setLikeNum(result);
        	}else{
        		result=boardService.dislike(params[0]);
        		itemEntity.setDislikeNum(result);
        	}
			return result;
        }

        @Override
        protected void onPostExecute(Integer result) {
        	convertView.setText(String.valueOf(result));
        	super.onPostExecute(result);
        }
    }
	
	private class AwakeTask extends AsyncTask<String, Void, Integer> {
		
        @Override
        protected Integer doInBackground(String... params) {
			return boardService.forceAlarm(params[0]);
        }

        @Override
        protected void onPostExecute(Integer result) {
        	String s="网络连接错误";
        	switch (result) {
			case -1:
				s="成功发送唤醒";
				break;
			case 1:
				s="亲密度不够哦";
				break;
			case 2:
				s="您的操作过于频繁";
				break;
			case 3:
			case 4:
				s="只能在闹钟前后半小时内唤醒哦";
				break;
			}
        	Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
        	super.onPostExecute(result);
        }
    }
	
	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================
	
	private class ViewHolder {
        TextView title;
        TextView content;
        TextView subContent;
        TextView subContent2;
        TextView addTime;
        ImageView contentIcon;
        ImageView headIcon;
        ImageButton button;
        Button likeButton;
        Button dislikeButton;
        Button commentButton;
        Button awakeButton;
        View linear;
        MyViewGroup tagLinear;
    }
	
}
