package com.geeker.door;

import android.app.Application;

import com.geeker.door.error.CrashHandler;

public class CrashApplication extends Application {  
    @Override  
    public void onCreate() {  
        super.onCreate();  
        CrashHandler crashHandler = CrashHandler.getInstance();  
        crashHandler.init(getApplicationContext());  
    }  
}  