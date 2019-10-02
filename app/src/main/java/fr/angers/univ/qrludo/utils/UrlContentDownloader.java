package fr.angers.univ.qrludo.utils;

import android.content.Context;
import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bastien PIGACHE
 * Download the content of an URL with an asynctask
 */

public class UrlContentDownloader extends AsyncTask<String, Void, String> {

    private UrlContentCallback m_activity;

    public UrlContentDownloader(Context context){
        m_activity = (UrlContentCallback)context;
    }

    @Override
    protected String doInBackground(String... urls) {
        String urlContent = "";

        BufferedReader in = null;

        try {
            StringBuilder response = new StringBuilder();
            String USER_AGENT = "Mozilla/5.0", inputLine;

            HttpURLConnection connection =
                    (HttpURLConnection) new URL(urls[0]).openConnection();

            //Headers
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "text/html; charset=UTF-8");
            connection.setRequestProperty("User-Agent", USER_AGENT);

            //Get contents in the buffer
            in = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()));

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }

            //Set the content of the buffer into our returned string
            urlContent = response.toString();

            in.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        return urlContent;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        m_activity.onWebsiteContent(s);
    }
}
