package fr.angers.univ.qrludo.QR.model;


import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import fr.angers.univ.qrludo.utils.CompressionString;
import fr.angers.univ.qrludo.utils.FileDowloader;

/**
 * Created by Jules Leguy on 20/01/18.
 */

public class QRFile extends QRContent implements FileDowloader.FileDownloaderObserverInterface {

    private boolean m_isFileInMemory;
    private QRFileObserverInterface m_QRFileObserver;

    public QRFile(String fileUrl) {
        super(fileUrl);


        m_isFileInMemory =  (new File(FileDowloader.DOWNLOAD_PATH+(CompressionString.compress(fileUrl))+".mp3").exists());

        downloadIfNotInMemory();
    }

    public boolean isFileInMemory(){
        return m_isFileInMemory;
    }

    public void downloadIfNotInMemory(){
        if (!isFileInMemory()) {
            Log.v("test", "fichier inconnu, lancement téléchargement");
            fetchFile();
        }
        else{
            Log.v("test", "fichier déjà téléchargé");
        }
    }

    private void fetchFile(){
        FileDowloader dowloader = new FileDowloader(m_content,this);
        dowloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }


    //Called by the FileDownloader when the downloading is done
    @Override
    public void onDownloadComplete() {

        Log.v("plop", "fichier téléchargé");

        //registers that the downloading is over
        m_isFileInMemory = true;

        //notifies the potential observer
        if (m_QRFileObserver != null){
            m_QRFileObserver.onQRFileDownloadComplete();
        }
    }

    public void registerAsDownloadListener(QRFileObserverInterface observer){
        m_QRFileObserver = observer;
    }

    public void unregisterAsDownloadListener(){
        m_QRFileObserver = null;
    }


    /**
     * Interface needed by the clients of QRFile
     */
    public interface QRFileObserverInterface {
        public void onQRFileDownloadComplete();
    }

}
