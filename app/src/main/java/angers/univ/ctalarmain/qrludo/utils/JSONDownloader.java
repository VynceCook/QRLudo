package angers.univ.ctalarmain.qrludo.utils;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Florian Lherbeil
 */

public class JSONDownloader extends AsyncTask<Void, Void, String> {

    final static String TAG = Activity.class.getName();
    private String _urlString;
    private String _id;

    public JSONDownloader(String id){
        _urlString = "https://drive.google.com/uc?export=download&id="+id;
        _id= id;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        Log.i(TAG,"fin");
    }

    protected String doInBackground(Void... voids) {

        try {
            URL url = new URL(_urlString);
            Log.i(TAG, "En cours d'execution sur : " + url.toString());
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            InputStream is =connection.getInputStream();
            InputStreamReader lecture=new InputStreamReader(is);
            BufferedReader buff=new BufferedReader(lecture);
            String ligne;
            String result="";
            while ((ligne=buff.readLine())!=null){
                result+=ligne;
                System.out.println(ligne);
            }
            buff.close();
            return result;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "Le QR code de peut pas etre lu";
    }




}
