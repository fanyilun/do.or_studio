package com.geeker.door;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataItemBuffer;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;

public class WeatherActivity extends Activity implements  GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private TextView title;
    private TextView today;
    private TextView nextday;
    private TextView nextday2;
    private TextView nextday3;
    private GoogleApiClient mGoogleApiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
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
//        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addApi(Wearable.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();


    }



    private void initData() {
//        PendingResult<DataItemBuffer> results = Wearable.DataApi.getDataItems(mGoogleApiClient);
//        results.setResultCallback(new ResultCallback<DataItemBuffer>() {
//            @Override
//            public void onResult(DataItemBuffer dataItems) {
//
//                if (dataItems.getCount() != 0) {
//                    for (int i = 0; i < dataItems.getCount(); i++) {
//                        DataMapItem dataMapItem = DataMapItem.fromDataItem(dataItems.get(i));
//                        if (dataMapItem.getDataMap().containsKey("weather")) {
//                            String[] elements = dataMapItem.getDataMap().getStringArray("weather");
//                            title.setText(elements[0]);
//                            today.setText(elements[1]);
//                            nextday.setText(elements[2]);
//                            nextday2.setText(elements[3]);
//                            nextday3.setText(elements[4]);
//                        }
//                    }
//                }
//            }
//        });
    }

    @Override
    protected void onResume() {
        super.onResume();
//        mGoogleApiClient.connect();
        initData();
    }

    @Override
    protected void onPause() {
        super.onPause();
//        mGoogleApiClient.disconnect();
    }
    @Override
    public void onConnected(Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }
}
