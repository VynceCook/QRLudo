package angers.univ.ctalarmain.qrludo.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;



/**
 * Notifies MainActivity when the internet connection is opened
 * from https://stackoverflow.com/questions/15698790/broadcast-receiver-for-checking-internet-connection-in-android-app
 */
public class InternetBroadcastReceiver extends BroadcastReceiver{

    //static field because new InternetBroadcastReceiver is creating at each change of connection
    public static InternetBroadcastReceiverObserverInterface m_observer;

    public InternetBroadcastReceiver(InternetBroadcastReceiverObserverInterface observer){
       m_observer = observer;
    }

    public InternetBroadcastReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        try
        {
            if (isOnline(context)) {
                //notifying MainActivity that the connection has been opened
                m_observer.onConnectionOpened();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Interface needed by the client of InternetBroadcastReceiver
     */
    public interface InternetBroadcastReceiverObserverInterface {
        void onConnectionOpened();
    }


}

