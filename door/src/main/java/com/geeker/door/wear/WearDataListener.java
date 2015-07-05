package com.geeker.door.wear;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.geeker.door.database.DbManager;
import com.geeker.door.database.EventVO;
import com.geeker.door.database.MemoVO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.wearable.DataApi;

import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2015/5/28.
 */
public class WearDataListener implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient mGoogleApiClient;
    private DbManager dbManager;
    boolean memo;
    String[] weather;
    public WearDataListener(Context c){
        mGoogleApiClient = new GoogleApiClient.Builder(c)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        dbManager=new DbManager(c);
        mGoogleApiClient.connect();
    }

    public void sendMemoData(){
        memo=true;

    }

    public void sendWeatherData(String[] weather){
        this.weather=weather;
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.v("","Connection start");
        if(memo){
            try{
                String[] result=new String[]{"暂无备忘"};
                List<EventVO> schedules=dbManager.getMemoByOccurTime();
                if(schedules.size()!=0){
                    result=new String[schedules.size()];
                    for (int i = 0; i < schedules.size(); i++) {
                        MemoVO memo=dbManager.getMemo(schedules.get(i).getRequestCode());
                        result[i]=memo.getTitle();
                    }
                }
                //send data
                Log.v("","start sending:"+result[0]+" and "+mGoogleApiClient.isConnected());
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/door");;
                putDataMapReq.getDataMap().putStringArray("list",result);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult =
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                mGoogleApiClient.disconnect();
                memo=false;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(weather!=null){
            try{
                //整合数据
                String[] sendData=new String[5];
                String s=weather[2];
                sendData[0]=weather[0]+" "+s.substring(0, s.indexOf(' '))+s.substring(s.lastIndexOf(' '),s.length());
                weather[2]="今天";
                for (int i=1;i<sendData.length;i++){
                    sendData[i]=weather[i*3-1]+"："+weather[i*3+1]+" "+weather[i*3];
                }
                PutDataMapRequest putDataMapReq = PutDataMapRequest.create("/door/weather");;
                putDataMapReq.getDataMap().putStringArray("weather",sendData);
                PutDataRequest putDataReq = putDataMapReq.asPutDataRequest();
                PendingResult<DataApi.DataItemResult> pendingResult =
                        Wearable.DataApi.putDataItem(mGoogleApiClient, putDataReq);
                mGoogleApiClient.disconnect();
                weather=null;
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.v("","Connection suspend");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v("","Connection fail"+connectionResult);
    }



}
