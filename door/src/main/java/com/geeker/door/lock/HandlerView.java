package com.geeker.door.lock;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.geeker.door.R;

public class HandlerView extends ImageView{
	private static int PADDING=40;
	OnViewSelectListener viewSelectListener;
	int selectedView=-1;
	int[] temp = new int[] { 0, 0 };
	float[] origin=new float[]{0,0};
	List<View> nodes;
	int state;//1为横向 2为竖向 0为二者都可以
	int centerPositionX;
	int centerPositionY;
	boolean transparent;
	//下面两个变量防止闪烁
	//为什么闪烁我现在也太明白
	boolean disableShow;
	boolean nextRefesh;
	
	public HandlerView(Context context, AttributeSet attrs) {
		super(context, attrs);
		nodes=new ArrayList<View>();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		//if(disableShow){System.out.println("disable");return;}
		super.onDraw(canvas);
	}
	

	@Override
	public void layout(int l, int t, int r, int b) {
		if(disableShow){return;}
		super.layout(l, t, r, b);
	}
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int eventaction = event.getAction();
		int x = (int) event.getRawX();
		int y = (int) event.getRawY();
		switch (eventaction) {
		case MotionEvent.ACTION_DOWN: // touch down so check if the
			temp[0] = (int) event.getX();
			temp[1] = y - getTop();
			origin[0]=getX();
			origin[1]=getY();
			centerPositionX=(int)origin[0]+getWidth()/2;
			centerPositionY=(int)origin[1]+getHeight()/2;
			setImageDrawable(getResources().getDrawable(R.drawable.lock_pressed));
			viewSelectListener.handleStart();
			break;
		case MotionEvent.ACTION_MOVE: // touch drag with the ball
			
			int selected=-1;
			
			//下面完成T字拖动
			boolean isX=Math.abs(x-centerPositionX)<=PADDING && getTop()+(int)event.getY()<centerPositionY;
			boolean isY=Math.abs(getTop()+(int)event.getY()-centerPositionY)<=PADDING;
			if(isX && isY){
				state=0;
			}else if(isY){
				state=1;
			}else if(isX){
				state=2;
			}
			for (int i = 0; i < 2; i++) {
				if(getRectInScreen(nodes.get(i)).intersect(x,getTop()+(int)event.getY(),x+1,getTop()+(int)event.getY()+1)){
					transparent=true;
					setAlpha(0);
					disableShow=true;
					((ImageView)nodes.get(i)).setImageDrawable(getResources().getDrawable(R.drawable.lock_unlock_pressed));
					selected=i;
					continue;
				}
				((ImageView)nodes.get(i)).setImageDrawable(getResources().getDrawable(R.drawable.lock_unlock));
			}
			
			for (int i = 2; i < nodes.size(); i++) {
				if(state!=1 &&getRectInScreen(nodes.get(i)).intersect(x,getTop()+(int)event.getY(),x+1,getTop()+(int)event.getY()+1)){
					nodes.get(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.lock_linear));
					selected=i;
					continue;
				}
				nodes.get(i).setBackgroundDrawable(getResources().getDrawable(R.drawable.lock_linear_null));
			}
			selectedView=selected;
			if(nextRefesh){
				disableShow=false;
				nextRefesh=!nextRefesh;
			}

			if(selected==-1&&transparent){
				setAlpha(255);
				transparent=false;
				nextRefesh=true;
			}
			
			if(state==1){
				layout(x - temp[0],(int)origin[1], x + getWidth()
						- temp[0], (int)origin[1] + getHeight());
			}else if(state==2){
				layout((int)origin[0], y - temp[1], (int)origin[0] + getWidth()
						, y - temp[1] + getHeight());
			}else{
				layout(x - temp[0], y - temp[1], x + getWidth()	- temp[0], y - temp[1] + getHeight());
			}
			invalidate();
			break;
		case MotionEvent.ACTION_UP:
			if(selectedView!=-1){
				//说明选中了某项
				viewSelectListener.viewSelect(selectedView);
				break;
			}else if(y<centerPositionY-200){
				//滑到最上面也算解锁吧
				System.out.println("哈哈"+y+" "+centerPositionY);
				viewSelectListener.viewSelect(0);
				break;
			}
			viewSelectListener.handleEnd();
			setImageDrawable(getResources().getDrawable(R.drawable.lock_normal));
			layout((int)origin[0],(int)origin[1], (int)origin[0]+ getWidth()
					, (int)origin[1] + getHeight());
			invalidate();
			break;
		}
		
		return super.onTouchEvent(event);
	}
	
	public void addView(View view){
		nodes.add(view);
	}
	
	private Rect getRectInScreen(View v) {  
	    final int w = v.getWidth();  
	    final int h = v.getHeight();  
	    Rect r=new Rect();
	    r.left = v.getLeft();  
	    r.top = v.getTop();  
	    r.right = r.left + w;  
	    r.bottom = r.top + h;
	    //对于边界做延伸处理
	    if(r.left<centerPositionX){
	    	r.left=0;
	    }else if(r.right>centerPositionX){
	    	r.right=centerPositionX*2;
	    }
	    //为方便解锁，解锁按钮做一下拓展
	    if(state==1 && r.bottom>centerPositionY){
	    		r.top=0;
	    		r.bottom=centerPositionY*2;
	    }
	    return r;
	} 
	
	public void setViewSelectListener(OnViewSelectListener listener){
		this.viewSelectListener=listener;
	}
	
	public interface OnViewSelectListener{
		public void viewSelect(int viewID);
		public void handleStart();
		public void handleEnd();
	}
	
}
