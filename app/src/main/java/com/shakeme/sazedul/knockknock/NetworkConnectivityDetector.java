package com.shakeme.sazedul.knockknock;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Sazedul on 05-Dec-14.
 */
public class NetworkConnectivityDetector {
    private Context context;

    public NetworkConnectivityDetector(Context context){
        this.context = context;
    }

    /**
     * Checking for all possible internet providers
     */
    public boolean isConnectionToInternetAvailable(){
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }
}
