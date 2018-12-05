package angers.univ.ctalarmain.qrludo.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.google.api.client.util.Base64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPOutputStream;


/**
 * Created by etudiant on 26/01/18.
 * Modified by Florian Lherbeil
 */

public class FileDowloader extends AsyncTask {

    public static String DOWNLOAD_PATH = Environment.getExternalStorageDirectory().getPath()+"/qrludo/";

    String m_url;
    FileDownloaderObserverInterface m_user;
    String m_id;
    String m_path;




    public FileDowloader(String url, FileDownloaderObserverInterface user){
        m_url = url;
        m_user = user;
        // On compresse l'url du fichier pour pouvoir lui donner un nom reconnaissable lors du stockage
        m_id=CompressionString.compress(url);
        m_path = FileDowloader.DOWNLOAD_PATH+m_id+".mp3";


        // Creating qrludo dir if doesn't exist
        File targetDir = new File(FileDowloader.DOWNLOAD_PATH);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }

    }

    @Override
    protected Object doInBackground(Object[] objects) {

        try {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            URL url = new URL(m_url);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            System.out.println(url.toString());

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.v("test", "Mauvaise réponse http");
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }


            // download the file
            input = connection.getInputStream();
            output = new FileOutputStream(m_path);

            byte data[] = new byte[4096];
            int count;
            while ((count = input.read(data)) != -1) {
                // allow canceling with back button
                if (isCancelled()) {
                    input.close();
                    return null;
                }

                output.write(data, 0, count);
            }

            output.close();

            m_user.onDownloadComplete();


        }
        catch (Exception e) {
            Log.e("test", e.getMessage());
        }

        return null;

    }


    /**
     * Interface needed by the clients of FileDownloader
     */
    public interface FileDownloaderObserverInterface {
        void onDownloadComplete();
    }

    public static boolean viderMemoire(){
        File targetDir = new File(FileDowloader.DOWNLOAD_PATH);
        if (targetDir.isDirectory()){
            File[] files=targetDir.listFiles();
            for(File f : files){
                f.delete();
            }
            return true;
        }
        return false;
    }

}
