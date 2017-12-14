package angers.univ.ctalarmain.qrludo.utils;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.Context;
import android.inputmethodservice.ExtractEditText;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 * Created by etudiant on 23/11/17.
 */

public class DLManager {
    @SuppressLint("NewApi")
    public void useDownloadManager(String url, Context c) {
        DownloadManager dm = (DownloadManager) c
                .getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request dlrequest = new DownloadManager.Request(
                Uri.parse("https://drive.google.com/uc?export=download&id="+url));
        dlrequest.setDescription("Debut du telechargement ...  ");
        dlrequest.setTitle("telechargement son");
        dlrequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, url+".mp3");
        dlrequest.allowScanningByMediaScanner();

        dm.enqueue(dlrequest);
    }

}