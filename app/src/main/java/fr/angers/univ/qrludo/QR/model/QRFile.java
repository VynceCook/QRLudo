package fr.angers.univ.qrludo.QR.model;


import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import fr.angers.univ.qrludo.utils.CompressionString;
import fr.angers.univ.qrludo.utils.FileDownloader;

/**
 * Created by Jules Leguy on 20/01/18.
 */

public class QRFile extends QRContent implements FileDownloader.FileDownloaderObserverInterface {

    private boolean m_isFileInMemory;
    private String m_download_path;
    private QRFileObserverInterface m_QRFileObserver;

    public QRFile(String fileUrl) {
        super(fileUrl);

        m_download_path = FileDownloader.DOWNLOAD_PATH+(CompressionString.compress(fileUrl))+".mp3";
        m_isFileInMemory =  (new File(m_download_path).exists());

        downloadIfNotInMemory();
    }

    public String getDownloadedFilePath()
    {
        return m_download_path;
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
        FileDownloader dowloader = new FileDownloader(m_content,this);
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
