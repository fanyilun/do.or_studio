package com.geeker.door;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.wearable.view.GridPagerAdapter;
import android.support.wearable.view.GridViewPager;
import android.support.wearable.view.WatchViewStub;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;


public class MainActivity extends Activity   implements WearableListView.ClickListener,DataApi.DataListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private TextView mTextView;
    String[] elements = { "暂无备忘"};
    private GoogleApiClient mGoogleApiClient;
    Adapter adapter;
    Handler handler;
    private TextView title;
    private TextView today;
    private TextView nextday;
    private TextView nextday2;
    private TextView nextday3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.viewpagerlayout);
        handler=new Handler();
        GridViewPager gridViewPager = (GridViewPager) findViewById(R.id.pager);
        gridViewPager.setAdapter(new MyGridViewPagerAdapter(this));

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

    }

    private void initData() {
        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
            @Override
            public void onResult(DataItemBuffer dataItems) {
                Log.v("","init onresult  "+dataItems.getCount());
                if (dataItems.getCount() != 0) {
                    for (int i = 0; i < dataItems.getCount(); i++) {
                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
                        if (dataMapItem.getDataMap().containsKey("list")) {
                            elements = dataMapItem.getDataMap().getStringArray("list");
                            if(elements==null){continue;}
                            adapter.setData(elements);
                            adapter.notifyDataSetChanged();
                        }
                        if (dataMapItem.getDataMap().containsKey("weather")) {
                            String[] s = dataMapItem.getDataMap().getStringArray("weather");
                            if(s==null){continue;}
                            title.setText(s[0]);
                            today.setText(s[1]);
                            nextday.setText(s[2]);
                            nextday2.setText(s[3]);
                            nextday3.setText(s[4]);
                        }
                    }
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Wearable.DataApi.removeListener(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onClick(WearableListView.ViewHolder viewHolder) {
        Log.v("", "click !");


    }

    @Override
    public void onTopEmptyRegionClick() {

    }


    @Override
    public void onConnected(Bundle bundle) {
        Log.v("","wear connect");
        Wearable.DataApi.addListener(mGoogleApiClient, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("","wear connect suspend");
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        Log.v("","wear data changed");
        for (DataEvent event : dataEvents) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                // DataItem changed
                DataItem item = event.getDataItem();
                if (item.getUri().getPath().compareTo("/door") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    elements = dataMap.getStringArray("list");
                    if(elements==null){continue;}
                    adapter.setData(elements);
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });

                }
                if (item.getUri().getPath().compareTo("/door/weather") == 0) {
                    DataMap dataMap = DataMapItem.fromDataItem(item).getDataMap();
                    final String[] s = dataMap.getStringArray("weather");
                    if(s==null){continue;}
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            title.setText(s[0]);
                            today.setText(s[1]);
                            nextday.setText(s[2]);
                            nextday2.setText(s[3]);
                            nextday3.setText(s[4]);
                        }
                    });
                }
            } else if (event.getType() == DataEvent.TYPE_DELETED) {
                // DataItem deleted
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("","wear connect fail");
    }

    private class MyGridViewPagerAdapter extends GridPagerAdapter {
        Context c;
        public MyGridViewPagerAdapter(Context c){
            this.c=c;
        }
        @Override
        public int getColumnCount(int arg0) {
            return 2;
        }

        @Override
        public int getRowCount() {
            return 1;
        }

        @Override
        protected Object instantiateItem(ViewGroup container, int row, int col) {
            if(col==0){
                final View view = LayoutInflater.from(c).inflate(R.layout.activity_main, container, false);
                WearableListView listView =
                        (WearableListView) view.findViewById(com.geeker.door.R.id.wearable_list);
                listView.setGreedyTouchMode( true );
                // Assign an adapter to the list
                adapter=new Adapter(MainActivity.this, elements);
                listView.setAdapter(adapter);
                // Set a click listener
                listView.setClickListener(MainActivity.this);
                container.addView(view);
                return view;
            }else{
                final View view = LayoutInflater.from(c).inflate(R.layout.activity_weather, container, false);
                final WatchViewStub stub = (WatchViewStub) view.findViewById(R.id.watch_view_stub);
                stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
                    @Override
                    public void onLayoutInflated(WatchViewStub stub) {
                        title = (TextView) stub.findViewById(R.id.head_text);
                        today = (TextView) stub.findViewById(R.id.today_text);
                        nextday = (TextView) stub.findViewById(R.id.tomorrow_text);
                        nextday2 = (TextView) stub.findViewById(R.id.aftertom_text);
                        nextday3 = (TextView) stub.findViewById(R.id.afteraftertom_text);

                    }
                });
                container.addView(view);
                return view;
            }

        }

        @Override
        protected void destroyItem(ViewGroup container, int row, int col, Object view) {
            container.removeView((View)view);
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view==object;
        }
    }
}
