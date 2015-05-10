package com.geeker.door;

import android.content.Context;

public class Utils {
	public static int getAndroidSDKVersion() {  
        int version = 0;  
        try {  
            version = Integer.valueOf(android.os.Build.VERSION.SDK);  
        } catch (NumberFormatException e) {  
        }  
        return version;  
    } 
	
	 public static int dip2px(Context context, float dpValue) {  
	        final float scale = context.getResources().getDisplayMetrics().density;  
	        return (int) (dpValue * scale + 0.5f);  
	 }  

}
