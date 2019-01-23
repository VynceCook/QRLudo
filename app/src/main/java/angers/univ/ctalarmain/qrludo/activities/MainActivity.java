package angers.univ.ctalarmain.qrludo.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import angers.univ.ctalarmain.qrludo.QR.handling.QRCodeDefaultDetectionModeStrategy;
import angers.univ.ctalarmain.qrludo.QR.handling.QRCodeDetectionModeStrategy;
import angers.univ.ctalarmain.qrludo.QR.model.QRCode;
import angers.univ.ctalarmain.qrludo.QR.handling.QRCodeBuilder;
import angers.univ.ctalarmain.qrludo.QR.model.QRCodeCollection;
import angers.univ.ctalarmain.qrludo.QR.model.QRContent;
import angers.univ.ctalarmain.qrludo.QR.model.QRFile;
import angers.univ.ctalarmain.qrludo.QR.model.QRText;
import angers.univ.ctalarmain.qrludo.R;
import angers.univ.ctalarmain.qrludo.exceptions.UnhandledQRException;
import angers.univ.ctalarmain.qrludo.exceptions.UnsupportedQRException;
import angers.univ.ctalarmain.qrludo.utils.CompressionString;
import angers.univ.ctalarmain.qrludo.utils.FileDowloader;
import angers.univ.ctalarmain.qrludo.utils.InternetBroadcastReceiver;
import angers.univ.ctalarmain.qrludo.utils.OnSwipeTouchListener;
import angers.univ.ctalarmain.qrludo.utils.QDCResponse;
import angers.univ.ctalarmain.qrludo.utils.ContentDelayCounter;
import angers.univ.ctalarmain.qrludo.utils.ToneGeneratorSingleton;


/**
 * MainActivity is the main activity of the application, this is where the user will be able to detect QRCodes and hear the result
 *
 * 1st version (detection and reading of unstructured QRCodes) :
 * @author Corentin Talarmain
 *
 * 2nd version (partial detection and reading of structured QRCodes) :
 * @author David Dembele
 *
 * 3rd version (complete detection and reading of structured QRCodes) :
 * @author Jules Leguy
 *
 *
 * Notes pour les futurs développeurs :
 *
 * Parmi les extensions intéressantes des deux applications qui pourraient être développées, il y a avant tout un meilleur système de compression
 * des données, basé sur un dictionnaire fixe externe et utilisant un algorithme comme celui du format zip, beaucoup plus efficace et plus
 * facilement utilisable sur un dictionnaire de grande taille que la version ad-hoc utilisée jusque ici. La difficulté se trouvera principalement
 * sur l'application de bureau, car beaucoup moins de bibliothèques sont disponibles en JavaScript. C'est d'ailleurs pour cette raison que nous
 * avons développé le système de compression/décompression actuel. Mais c'est envisageable si vous commencez ce travail dès le début du projet.
 * Le dictionnaire pourra contenir tous les mots du français, en plus des chaînes XML compressées jusque là, ce qui permettrait une compression
 * considérable et donc de générer des QRCodes pouvant contenir beaucoup de données. Il serait en revanche préférable que l'application mobile
 * soit toujours capable de décompresser les QRCodes qui ont été générés jusque là (voir notre rapport de projet et les commentaires dans la classe
 * qui compresse les chaînes XML dans l'application de bureau).
 *
 * L'interprétation des QRCodes appartenant a une famille n'est actuellement pas idéale, car l'application android n'a aucun moyen de savoir si
 * tous les QRCodes de la famille ont été interprétés. Il faudrait pour cela modifier la structure XML afin d'ajouter la taille de la famille et
 * pouvoir signaler à l'utilisateur si tous les QRCodes de la famille n'ont pas été détectés. Il faudra toutefois penser au fait que cela changera
 * en partie les chaînes de caractères utilisées pour la compression/décompression des QRCodes.
 *
 * On pourrait également penser à un système permettant de convertir du texte en fichier audio généré par une voix de synthèse sur l'application
 * de bureau, stocké ensuite automatiquement sur internet et générant un QRCode possédant un lien vers ce fichier. Cette solution permettrait de
 * générer des QRCodes de petite taille et contenant beaucoup de "texte", mais ne serait pas idéale à cause de l'aspect peu chaleureux de la voix
 * de synthèse. Elle pourrait toutefois être une alternative à la compression par dictionnaire fixe externe si celle-ci se révèle impossible à
 * mettre en place.
 *
 * Pour bien comprendre et pouvoir modifier le code de l'application android, il faut s'intéresser aux designs patterns et notamment au Composite
 * (https://sourcemaking.com/design_patterns/composite) pour les classes implémentant QRCodeComponent, à la Stratégie
 * (https://sourcemaking.com/design_patterns/strategy) pour les sous-classes de QRCodeDetectionModeStrategy, et à l'Observateur
 * (https://sourcemaking.com/design_patterns/observer) pour les classes utilisant FileDownloader et InternetBroadcastReceiver.
 *
 *
 */
public class MainActivity extends AppCompatActivity implements SensorEventListener, QDCResponse, QRFile.QRFileObserverInterface, InternetBroadcastReceiver.InternetBroadcastReceiverObserverInterface{

    /**
     * The name of the file containing the user's settings.
     */
    public static final String PREFS_NAME = "MyPrefsFile";

    /**
     * The default language that will be applied to the Text To Speech engine.
     */
    public static final Locale LOCALE_DEFAULT = Locale.FRANCE;

    /**
     * The default speed applied to the Text To Speech Engine.
     */
    public static final float SPEEDSPEECH_DEFAULT = (float)1.2;

    /**
     * The default delay for current_text to get resetted.
     */
    public static final float DEFAULT_CONTENT_RESET_TIME = 60;

    /**
     * The integer corresping to the code identifying the option intent used to launch the option activity.
     * @see OptionActivity
     */
    static final int OPTION_REQUEST = 90;  // The request code

    public static final int MULTIPLE_PERMISSIONS = 10; // code you want.


    /**
     * The integer corresponding to the default time during which the application is trying to detect a new QRCode during a multiple detection
     */
    public static final float DEFAULT_MULTIPLE_DETECTION_TIME = 3;


    /*
     * ----------------------------------------- CAMERA STATES -----------------------------------------
     */


    /**
     * The integer corresponding to the current camera state
     */
    private int cameraState;

    /**
     *  The integer corresponding to the start state of the application.
     *  This state is corresponding to the camera being inactive.
     */
    private final int CAMERA_INACTIVE_STATE = 30;

    /**
     * The integer corresponding to the detecting state of the application
     * This state is corresponing to the camera being active.
     */
    private final int CAMERA_DETECTING_STATE = 40;


    /*
     * ----------------------------------------- DETECTION PROGRESS -----------------------------------------
     */

    /**
     * The integer corresponding to the current detection progressing of the application
     */
    private int m_detectionProgress;

    /**
     * This state corresponds to the state where no QRCode has been detected and thus no text is printed.
     */
    public final static int NO_QR_DETECTED = 50;

    /**
     * This state corresponds to the state where exactly one QRCode has been detected during the current detection. It may or not have been played
     */
    public final static int FIRST_QR_DETECTED = 60;

    /**
     * This state corresponds to the state where multiple QR Codes have been detected. They may or not have been played.
     */
    public final static int MULTIPLE_QR_DETECTED = 100;

    /**
     * Boolean whose value is true if the application is trying to detect new QRCodes and false if not
     */
    private boolean m_isApplicationDetecting;


    /**
     * The integer corresponding to the speed of the text to speech engine
     */
    private float m_speechSpeed;

    /**
     * The current language of the text to speech engine.
     */
    private Locale m_ttslanguage;

    /**
     * The current delay between reset of the contents
     */
    private float m_content_reset_time;

    /*
    *--------------------------------------Layouts--------------------------------------
     */

    /**
     * The main layout where all happens
     */
    private RelativeLayout m_mainLayout;

    /**
     * The layout used to display the current_text / answer
     */
    private LinearLayout m_contentLayout;

    /**
     * The text view containing the current current_text / answer
     */
    private TextView m_text_space;

    /**
     * The view containing the camera preview and detector
     */
    private SurfaceView m_cameraView;

    /**
     * The object used to get the camera
     */
    private CameraSource m_cameraSource;

    /**
     * The text to speech engine object
     */
    private TextToSpeech m_ttobj;

    /**
     * The progress bar for the text to speech loading.
     */
    private ProgressBar m_ttsprogress;

    /**
     * The text view indicating the text to speech loading
     */
    private TextView m_text_progress;


    /**
     * The boolean indicating that the sdk version of the phone is greater than marshamallow (API 23)
     */
    private boolean m_marshmallowOrHigher;


    /**
     * The boolean indicating if the sdk version of the phone is greater than lollipop
     */
    private boolean m_lollipopOrHigher;


    /**
     * The boolean indicating if the text to speech engine is done initializing
     */
    private boolean m_ttsready;

    /**
     * This bundle is used to pass a list of languages available for the text to speech engine
     */
    private Bundle m_locals;

    /**
     * The asynchronous task used to countdown the time before the current reading has to be canceled
     */
    private ContentDelayCounter m_qdc;

    /**
     * The asynchronous task used to countdown the time during which the application is trying to detect a new QRCode during a multiple detection
     */
    private MultipleDetectionTimer m_mdt;


    /**
     * This object is used to manage the different sensors used
     */
    private SensorManager m_sensorManager;

    /**
     * The m_accelerometer sensor, used to manage the movements of the phone
     */
    private Sensor m_accelerometer;

    /**
     * The m_proximity sensor, used to manage the m_proximity of the phone from another object
     */
    private Sensor m_proximity;

    /**
     * Boolean indicating if the device has an accelerometer
     */
    private boolean m_hasAccelerometer;

    /**
     * Boolean indicating if the device has a proximity sensor
     */
    private boolean m_hasProximity;

    /**
     * Float used for the m_proximity sensor
     */
    float m_p;

    /**
     * long used to know when the last sensor event occurred
     */
    private long m_lastSensorUpdate = 0;

    /**
     * Last inclination detected
     */
    private int m_lastInclination = -1;

    private int m_inclinationCounter = 0;

    /**
     * The object used to make the phone vibrate
     */
    private Vibrator m_vibrator;

    GoogleApiAvailability m_gga;

    /**
     * Time during which the application tries to detect another QR after having detecting one
     */
    private float m_multiple_detection_time;

    /**
     * Object used to play sound files
     */
    private MediaPlayer m_mediaPlayer;

    /**
     * List of the QRCodes which are detected but not yet played
     * QRCollection does a lot of processing to simplify the code of MainActivity
     */
    private QRCodeCollection m_detectedQRCodes;

    /**
     * List of the current QRContent (url or text) which have been extracted from the detected QRCodes
     */
    private List<QRContent> m_currentReading;

    /**
     * Strategy in charge of the behaviour of detecting the QRCodes, starting the reading and handling the user actions
     * It depends on the type of QRCodes that the user is currently detecting
     */
    private QRCodeDetectionModeStrategy m_currentDetectionModeStrategy;

    /**
     * Current position in m_currentReading
     */
    private int m_currentPos;

    /**
     * True if a multiple detection is running and not yet ended
     */
    private boolean m_isMultipleDetectionTimerRunning;



    /**
     * The OnCreate event of the main activity, called at the creation.
     * Initializes all the attributes and check for a bundle to restore a defined state.
     * Initializes also the listeners and check for the permissions.
     * @param savedInstanceState The bundle saving data if a state has to be saved
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        m_gga = GoogleApiAvailability.getInstance();
        int status = m_gga.isGooglePlayServicesAvailable(getApplicationContext());
        if(status != ConnectionResult.SUCCESS) {
            int GOOGLE_SERVICES_REQUEST = 500;
            Dialog errorDia = m_gga.getErrorDialog(this,status, GOOGLE_SERVICES_REQUEST);
            errorDia.show();
        }
        initializeAttributes();

        if(savedInstanceState != null) {

            cameraState = savedInstanceState.getInt("STATE");
            switch (cameraState) {
                case CAMERA_DETECTING_STATE: {
                    startDetection();
                    break;
                }
            }
        }

        initializeListeners();

        checkPermissions();

    }






    /**
     * Method defined from the QDCResponse interface.
     * Called when the ContentDelayCounter AsyncTask is over
     * @see ContentDelayCounter
     * @see QDCResponse
     * @param output The output of the Async Task, true if the current_text has to be reset, false otherwise
     */
    @Override
    public void processFinish(Boolean output) {
        startNewDetection("");
    }

    /**
     * Called by InternetBroadcastReceiver when the internet connection is opened
     */
    @Override
    public void onConnectionOpened() {
        Log.v("test", "call onConnectionOpened");
        m_detectedQRCodes.downloadAllFiles();

        //Saying "Téléchargement en cours" if the current QRFile is now being downloaded
        if (m_currentReading.get(m_currentPos) instanceof QRFile){

            QRFile qrFile = (QRFile) m_currentReading.get(m_currentPos);

            if (!qrFile.isFileInMemory()){
                toSpeech("Téléchargement en cours",TextToSpeech.QUEUE_ADD);
            }

        }


    }

    /**
     * The AsyncTask used to detect when the Text To Speech engine talk for the first time
     */
    @SuppressLint("StaticFieldLeak")
    private class detectionOnTTSInitialization extends AsyncTask<TextToSpeech,Void,Integer>{

        /**
         * Get out of a while waiting for the text to speech to speak, and then initiate the application
         * detector
         * @param params The Text To Speech engine on 0
         * @return An integer meaning it's ok
         */
        @Override
        protected Integer doInBackground(TextToSpeech... params) {
            while(!params[0].isSpeaking()){
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.v("test", "Fin m_ttsprogress");
                    m_ttsprogress.setVisibility(View.GONE);
                    m_text_progress.setVisibility(View.GONE);
                }
            });
            m_ttsready = true;
            return 1;
        }


    }

    /**
     * The method setting up the text to speech engine.
     */
    private void SetUpTTS() {

        //The text to speech
        m_ttobj = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {

                Log.v("test", "status : "+String.valueOf(status));

                m_ttobj.setLanguage(m_ttslanguage);
                m_ttobj.setSpeechRate(m_speechSpeed);
                toSpeech("Application lancée.", TextToSpeech.QUEUE_FLUSH);
                fillLocals();
                new detectionOnTTSInitialization().execute(m_ttobj);


            }
        });

        m_ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String utteranceId) {
                if (utteranceId.equals("synthetizeKey")) {
                    Log.d("UTTERANCE", "Start");
                }
            }

            @Override
            public void onDone(String utteranceId) {
                if (utteranceId.equals("synthetizeKey")) {
                    Log.d("UTTERANCE", "Done");
                }
            }

            @Override
            public void onError(String utteranceId) {
                Log.e("UTTERANCE", "Error while trying to synthesize sample text");
            }
        });

    }

    private void fillLocals() {

        Locale[] locales = Locale.getAvailableLocales();


        for (Locale l : locales) {
            try {
                if (m_ttobj.isLanguageAvailable(l) >= TextToSpeech.LANG_COUNTRY_AVAILABLE) {
                    m_locals.putString(l.getCountry(), l.getLanguage());
                }
            }
            catch(IllegalArgumentException e){
                e.printStackTrace();
            }
        }

    }


    /**
     * OnPause event for the activity, unregister the sensors
     */
    @Override
    protected void onPause() {
        // unregister the sensor (désenregistrer le capteur)
        if(m_hasAccelerometer)
            m_sensorManager.unregisterListener(this, m_accelerometer);
        if(m_hasProximity)
            m_sensorManager.unregisterListener(this, m_proximity);
        makeSilence();
        super.onPause();
    }

    /**
     * The OnResume event for the activity
     * Register the sensors
     */
    @Override
    protected void onResume() {

        /* Ce qu'en dit Google&#160;dans le cas de l'accéléromètre :
         * «&#160; Ce n'est pas nécessaire d'avoir les évènements des capteurs à un rythme trop rapide.
         * En utilisant un rythme moins rapide (SENSOR_DELAY_UI), nous obtenons un filtre
         * automatique de bas-niveau qui "extrait" la gravité  de l'accélération.
         * Un autre bénéfice étant que l'on utilise moins d'énergie et de CPU.&#160;»
         */
        if(m_hasAccelerometer)
            m_sensorManager.registerListener(this, m_accelerometer, SensorManager.SENSOR_DELAY_UI);
        if(m_hasProximity)
            m_sensorManager.registerListener(this, m_proximity,SensorManager.SENSOR_DELAY_UI);
        super.onResume();

    }

    /**
     * Initialization of the attributes.
     */
    private void initializeAttributes() {

        /*
        * --------------- LAYOUTS ---------------
         */

        m_ttsprogress = (ProgressBar) findViewById(R.id.tts_progress);

        m_text_progress = (TextView) findViewById(R.id.text_loading);

        m_mainLayout = (RelativeLayout) findViewById(R.id.main_layout);

        //Content Layout
        m_contentLayout = (LinearLayout) findViewById(R.id.content_layout);

        m_text_space = (TextView) findViewById(R.id.text_space);

        m_cameraView = (SurfaceView) findViewById(R.id.camera_view);

        m_cameraView.setVisibility(View.INVISIBLE);

        m_ttsready = false;

        m_locals = new Bundle();

        m_vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

        m_speechSpeed = settings.getFloat("m_speechSpeed", SPEEDSPEECH_DEFAULT);

        m_ttslanguage = new Locale(settings.getString("speechLanguage", LOCALE_DEFAULT.getLanguage()),settings.getString("speechCountry", LOCALE_DEFAULT.getCountry()));

        m_content_reset_time = settings.getFloat("resetTime2", DEFAULT_CONTENT_RESET_TIME);

        m_multiple_detection_time = settings.getFloat("MDTime",DEFAULT_MULTIPLE_DETECTION_TIME);

        SetUpTTS();

        PackageManager manager = getPackageManager();

        m_hasAccelerometer = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_ACCELEROMETER);

        m_hasProximity = manager.hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);

        // Instancier le gestionnaire des capteurs, le SensorManager
        m_sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // Instancier l'accéléromètre
        if(m_hasAccelerometer) {
            //Not null because already tested
            assert m_sensorManager != null;
            m_accelerometer = m_sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        }

        if(m_hasProximity) {
            //Not null because already tested
            assert m_sensorManager != null;
            m_proximity = m_sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        m_lastSensorUpdate = System.currentTimeMillis();

        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        cameraState = CAMERA_INACTIVE_STATE;

        m_detectionProgress = NO_QR_DETECTED;

        m_isMultipleDetectionTimerRunning = false;

        m_marshmallowOrHigher = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;

        m_lollipopOrHigher = android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ;

        m_detectedQRCodes = new QRCodeCollection();

        m_currentReading = new ArrayList<>();

        m_currentDetectionModeStrategy = new QRCodeDefaultDetectionModeStrategy(this);

        setUpDetector();

        m_mediaPlayer = new MediaPlayer();

        //Starting InternetBroadcastReceiver which will call onConnectionOpened() if the internet connection is opened
        new InternetBroadcastReceiver(this);

    }

    private void setUpDetector() {
        /*
      The barcode detector
     */
        BarcodeDetector detector = new BarcodeDetector.Builder(getApplicationContext())
                .setBarcodeFormats(Barcode.DATA_MATRIX | Barcode.QR_CODE)
                .build();

        if (!detector.isOperational()) {
            Log.e("DETECTOR","Could not set up detector.");
            return;
        }



        m_cameraSource = new CameraSource
                .Builder(this, detector)
                .setRequestedPreviewSize(640, 480)
                .setAutoFocusEnabled(true)
                .build();



        m_cameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {

                    m_cameraSource.start(m_cameraView.getHolder());
                    cameraState = CAMERA_DETECTING_STATE;

                    Log.d("CAMERA_START", "Camera started");
                } catch(SecurityException se)
                {
                    Log.e("CAMERA SECURITY", se.getMessage());
                }
                catch (Exception e)
                {
                    Log.e("CAMERA SOURCE", e.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                try {

                    if(cameraState == CAMERA_DETECTING_STATE) {

                        cameraState = CAMERA_INACTIVE_STATE;
                    }
                    m_cameraSource.stop();
                } catch (Exception e)
                {
                    Log.e("CAMERA SOURCE", e.getMessage());
                }
            }
        });


        /*
      The processor of the detector, where the events from the detector are handled
     */
            Detector.Processor<Barcode> detector_processor = new Detector.Processor<Barcode>() {

            @Override
            public void release() {
            }

            /**
             * Method called when QRCodes have been detected by the camera
             * @param detections
             */
            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                if (barcodes.size() != 0) {

                    for (int i = 0; i < barcodes.size(); i++) {
                        String rawValue = barcodes.valueAt(i).rawValue;
                        //ignoring if the QRCode has already been recorded or ignored
                        if (m_detectedQRCodes.isAlreadyInCollection(rawValue) || m_detectedQRCodes.isAlreadyIgnored(rawValue)) {
                            Log.v("test", "ignoring QR");
                            break;
                        }

                        try {

                            int version = MainActivity.this.getResources().getInteger(R.integer.version_qrludo);
                            QRCode detectedQR = QRCodeBuilder.build(rawValue, version);
                            //If first QR detected of the current detection
                            if (m_detectionProgress == NO_QR_DETECTED) {
                                m_currentDetectionModeStrategy.onFirstDetectionWithTimeNotNull(detectedQR);
                            }
                            //If at least one QR has already been detected during the current detection
                            else{
                                m_currentDetectionModeStrategy.onNextDetectionWithTimeNotNull(detectedQR);
                            }


                        } catch (UnhandledQRException e) {
                            ToneGeneratorSingleton.getInstance().ignoredQRCodeTone();
                        } catch (UnsupportedQRException e) {
                            toSpeech(e.getMessage(), TextToSpeech.QUEUE_ADD);
                            stopDetection();
                        }

                    }
                }
            }
        };

        detector.setProcessor(detector_processor);
    }


    /**
     * Called when a single QRCode has been detected.
     * Reads the first content of the QRCode and allows the user to hear the others content by swiping the screen
     */
    public void singleReading(){

        //Getting the first detected QRCode only
        m_currentReading = m_detectedQRCodes.getContentFirstQR();

        readCurrentContent();
    }

    /**
     * Called in case of multiple reading of QR Codes which don't belong to a family
     * Also called during a family reading if QRCodes not belonging to a family have been detected first
     * In those cases, a QR code is already printed/told
     */
    public void classicMultipleReading(){

        //Fetching the content of all the detected QR but the first because it has already been read by singleReading()
        m_currentReading.addAll(m_detectedQRCodes.getContentAllQRButFirst());

    }

    /**
     * Called in case of detection of QRCodeEnsemble
     * Can be called by QRCodeEnsemble at the end of a multiple detection or by the MultipleDetectionTimer if only one QRCodeEnsemble has been detected
     */
    public void ensembleReading(){

        //Fetching the content of all the detected QRs
        m_currentReading.addAll(m_detectedQRCodes.getContentAllQR());

        Log.v("test", "size of current reading : "+m_currentReading.size());

        if (areAllQRFilesDownloaded()){
            //If all the QRFiles have already been downloaded, going back to the detection
            ensembleReadingCompleted();
            Log.v("test", "calling ensembleReadingCompleted() from ensembleReading()");
        }
        else{

            //If all the QRFiles haven't been downloaded, registering as listener of their downloading
            for (QRContent qrContent : m_currentReading){
                ((QRFile) qrContent).registerAsDownloadListener(this);
            }

            if (isOnline()){
                toSpeech("Téléchargement en cours", TextToSpeech.QUEUE_ADD);
            }
            else{
                toSpeech("Pas de connexion internet, téléchargement impossible", TextToSpeech.QUEUE_ADD);
            }
        }

    }

    /**
     * Detects the type of the current content and calls the methods adequately
     * If the current content is a QRFile, tests if it has already been downloaded. If not, waits for it
     */
    public void readCurrentContent(){
        QRContent currentContent = m_currentReading.get(m_currentPos);

        //Stops potential current m_mediaPlayer
        makeSilence();

        if (currentContent instanceof QRText){
            sayTextContent();
        }
        else if (currentContent instanceof QRFile){
            //If the file is already downloaded, playing the m_mediaPlayer
            if (((QRFile) currentContent).isFileInMemory()){
                playCurrentSoundContent("Fichier audio");
            }
            //The file is not downloaded
            else {

                //Registering as listener of the QRFile. The method onQRFileDownloadComplete() will then be called when the downloading is over.
                ((QRFile) currentContent).registerAsDownloadListener(this);

                if (isOnline()) {
                    toSpeech("Téléchargement en cours", TextToSpeech.QUEUE_ADD);
                } else {
                    toSpeech("Pas de connexion internet, téléchargement impossible.", TextToSpeech.QUEUE_ADD);
                }

            }

        }
    }

    public void readAllContent(){
        for(m_currentPos = 0; m_currentPos<m_currentReading.size();m_currentPos++){
            readCurrentContent();
        }
    }

    /**
     * Plays the current QRFile
     */
    public void playCurrentSoundContent(String textToPrint){

        //Stopping current text to speech speaking or sound if necessary
        makeSilence();

        printText(textToPrint);

        Log.v("test", "source : "+FileDowloader.DOWNLOAD_PATH+CompressionString.compress(m_currentReading.get(m_currentPos).getContent())+".mp3");
        //Playing the sound
        try {
            m_mediaPlayer.stop();
            m_mediaPlayer = new MediaPlayer();
            m_mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            m_mediaPlayer.setDataSource(FileDowloader.DOWNLOAD_PATH+CompressionString.compress(m_currentReading.get(m_currentPos).getContent())+".mp3");
            m_mediaPlayer.prepare();
            m_mediaPlayer.start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void pauseCurrentReading(){
        if(m_mediaPlayer.isPlaying()){
            m_mediaPlayer.pause();
        }
        else{
            m_mediaPlayer.start();
        }
    }

    /**
     * Called by the QRFiles MainActivity as registered as listener when their downloading is over
     * If the current strategy is QRCodeDefaultDetectionModeStrategy or QRCodeFamilyDetectionModeStrategy, MainActivity has registered to at most 1 QRFile (the current one if needed)
     * If the current strategy is QRCodeEnsembleDetectionModeStrategy, MainActivity has registered to each QRFile which wasn't already downloaded
     */
    public void onQRFileDownloadComplete() {
        //Relying on the detection strategy to manage the info
        m_currentDetectionModeStrategy.onQRFileDownloadComplete();
    }

    /**
     * Returns true if all the QRFiles belonging to m_currentReading have been downloaded
     * Used in case of QRCodeEnsembleDetection
     * @return
     */
    public boolean areAllQRFilesDownloaded(){
        boolean allQRFilesDownloaded = true;

        for (QRContent qrContent : m_currentReading){
            if(qrContent instanceof QRFile){
                if (!((QRFile)qrContent).isFileInMemory()){
                    allQRFilesDownloaded = false;
                    break;
                }
            }
        }

        return allQRFilesDownloaded;
    }

    /**
     * Called when all the files have been downloaded if the activity is currently reading a set of QRCodeEnsemble
     * Notifies the user that the files have been downloaded
     */
    public void ensembleReadingCompleted(){
        toSpeech("Tous les fichiers ont été téléchargés", TextToSpeech.QUEUE_ADD);
    }

    /**
     * The text to speech engines says the current text associated with the current QRText
     * The text is also printed on the screen
     */
    private void sayTextContent() {
        final AppCompatActivity activity = this;
        activity.runOnUiThread(new Runnable() {
            public void run() {

                //printing the text
                printText(m_currentReading.get(m_currentPos).getContent());

                m_ttobj.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                    @Override
                    public void onStart(String utteranceId) {
                    }

                    @Override
                    public void onDone(String utteranceId) {

                        playCurrentSoundContent(m_currentReading.get(m_currentPos).getContent());

                        if(m_content_reset_time > 0) {
                            if (m_qdc != null)
                                m_qdc.cancel(true);
                            m_qdc = new ContentDelayCounter();
                            m_qdc.delegate = (QDCResponse) activity;
                            m_qdc.execute((int)m_content_reset_time);
                        }

                    }

                    @Override
                    public void onError(String utteranceId) {
                    }
                });

                HashMap<String, String> myHashRender = new HashMap<String, String>();
                myHashRender.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,this.hashCode() + " ");

                File file = new File(FileDowloader.DOWNLOAD_PATH + CompressionString.compress(m_currentReading.get(m_currentPos).getContent())  + ".mp3");
                file.setReadable(true, false);
                if(file.exists()){
                    playCurrentSoundContent(m_currentReading.get(m_currentPos).getContent());
                } else {
                    int r = m_ttobj.synthesizeToFile(m_currentReading.get(m_currentPos).getContent(), myHashRender,FileDowloader.DOWNLOAD_PATH + CompressionString.compress(m_currentReading.get(m_currentPos).getContent())  + ".mp3");
                }

            }
        });
    }

    /**
     * Prints the given text in the zone on top of the screen
     * @param text
     */
    private void printText(String text){
        final AppCompatActivity activity = this;
        final String textToPrint = text;
        activity.runOnUiThread(new Runnable() {
            public void run() {

                m_text_space.setText(textToPrint);
                m_text_space.setVisibility(View.VISIBLE);
                m_contentLayout.setVisibility(View.VISIBLE);
            }
        });
    }

    /**
     * Stops the text to speech engine or the MediaPlayer if they are talking or playing
     */
    public void makeSilence(){

        if (m_ttobj.isSpeaking())
            m_ttobj.stop();

        if (m_mediaPlayer!=null && m_mediaPlayer.isPlaying())
            m_mediaPlayer.stop();


    }

    /**
     * Unregisters the MainActivity as a listener of the current QRFile if necessary
     * Called when the user swipes right or left
     */
    public void unregisterToQRFile(){

        QRContent currentContent = m_currentReading.get(m_currentPos);

        if (currentContent != null && currentContent instanceof QRFile){
            ((QRFile) currentContent).unregisterAsDownloadListener();
        }

    }


    /**
     * Starts a new MultipleDetectionTimer
     */
    public void startMultipleDetectionTimer(){

        //Canceling potentially running MultipleDetectionTimer
        if (m_mdt !=null){
            Log.v("test", "trying to cancel m_mdt");
            m_mdt.cancel(true);
        }

        m_mdt = new MultipleDetectionTimer();
        m_mdt.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (int)m_multiple_detection_time * 1000);
    }


    /**
     * AsyncTask whose role is to launch the reading of the detected qr codes after a defined time in case of multiple detection
     * If interrupted because it has been replaced with another MultipleDetectionTimer, ends without launching reading
     */
    private class MultipleDetectionTimer extends AsyncTask<Integer,Void,Boolean>{

        @Override
        protected Boolean doInBackground(Integer... params) {

            Log.v("test", "Lancement MultipleDetectionTimer");
            try {

                m_isMultipleDetectionTimerRunning = true;

                Thread.sleep(params[0]);

                m_isMultipleDetectionTimerRunning = false;

                //the current detection strategy chooses how to manage the end of the MultipleTimerDetection
                m_currentDetectionModeStrategy.onEndOfMultipleDetectionTimer();


            } catch (InterruptedException e) {
                Log.v("test", "end of multipleDetectionTimer without launching reading");
                m_isMultipleDetectionTimerRunning = false;
                e.printStackTrace();
            }

            return true;
        }


    }

    /**
     * Ends the current reading and go back to detection
     * Can be called at the end or in the middle of a reading
     */
    public void startNewDetection(String message) {

        //Getting back at the first state of the detection
        m_detectionProgress = NO_QR_DETECTED;
        m_currentDetectionModeStrategy = new QRCodeDefaultDetectionModeStrategy(this);

        //Cancelling current mdt if necessary
        if (m_mdt!=null) {
            m_mdt.cancel(true);
        }

        //Stop talking or making sound
        makeSilence();

        toSpeech(message, TextToSpeech.QUEUE_ADD);

        ToneGeneratorSingleton.getInstance().startingDetectionTone();



        this.runOnUiThread(new Runnable() {
            public void run() {
               //Hiding graphical elements
               m_text_space.setVisibility(View.INVISIBLE);
               m_contentLayout.setVisibility(View.INVISIBLE);
            }
        });

        //Removing the current list of QRContent
        m_currentReading.clear();

        //Removing the list of QRCodeComponents which have been created after their detection. Also removes the ignored QRCodes
        m_detectedQRCodes.clear();

        m_currentPos = 0;

        //Restarting detection
        startDetection();

    }

    public void readQuestion(final String question){
        final AppCompatActivity activity = this;
        activity.runOnUiThread(new Runnable() {
            public void run() {

                //printing the text
                printText(question);

                //Using text to speech engine to say the text
                toSpeech(question, TextToSpeech.QUEUE_ADD);
            }
        });
    }

    public void reponseFind(final String message){
        final AppCompatActivity activity = this;
        activity.runOnUiThread(new Runnable() {
            public void run() {

                //printing the text
                printText(message);

                //Using text to speech engine to say the text
                toSpeech(message, TextToSpeech.QUEUE_ADD);

                if(m_content_reset_time > 0) {
                    if (m_qdc != null)
                        m_qdc.cancel(true);
                    m_qdc = new ContentDelayCounter();
                    m_qdc.delegate = (QDCResponse) activity;
                    m_qdc.execute((int)m_content_reset_time);
                }

                if(m_ttsready) {
                    m_cameraView.setVisibility(View.INVISIBLE);
                    m_isApplicationDetecting = false;
                }else{
                    cameraState = CAMERA_INACTIVE_STATE;
                }
            }
        });
    }

    private void initializeListeners() {


        m_mainLayout.setOnTouchListener(new OnSwipeTouchListener(MainActivity.this) {

            /**
             * Relying on m_currentDetectionModeStrategy to manage the swipe top
             */
            public void onSwipeTop() {
                m_currentDetectionModeStrategy.onSwipeTop();
            }

            /**
             * Relying on m_currentDetectionModeStrategy to manage the swipe right
             */
            public void onSwipeRight(){
                m_currentDetectionModeStrategy.onSwipeRight();
            }

            /**
             * Relying on m_currentDetectionModeStrategy to manage the swipe left
             * @throws IOException
             */
            public void onSwipeLeft(){
                m_currentDetectionModeStrategy.onSwipeLeft();
            }

            /**
             * Relying on m_currentDetectionModeStrategy to manage the swipe bottom
             */
            public void onSwipeBottom() {
                m_currentDetectionModeStrategy.onSwipeBottom();
            }

            @Override
            public void onLongClick() {
                m_currentDetectionModeStrategy.onLongClick();
            }
        });
    }

    /**
     * Checks if the device is connected to the Internet
     * @return
     */
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        if (m_marshmallowOrHigher) {

            List<String> neededPermissions = new ArrayList<String>();

            Log.v("test", "m_marshmallowOrHigher");

            Log.d("PERMISSION_CHECK","---------Marshallow----------");

            // Here, thisActivity is the current activity
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.CAMERA)) {

                } else {
                    Log.d("PERMISSION_CHECK","---------NoExplanation----------");
                    // No explanation needed, we can request the permission.
                    neededPermissions.add(Manifest.permission.CAMERA);

                }
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.INTERNET)
                    != PackageManager.PERMISSION_GRANTED) {

                // Should we show an explanation?
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.INTERNET)) {


                } else {
                    Log.d("PERMISSION_CHECK","---------NoExplanation----------");
                    // No explanation needed, we can request the permission.
                    neededPermissions.add(Manifest.permission.INTERNET);

                }
            }
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.VIBRATE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.VIBRATE)) {

                } else {
                    Log.d("PERMISSION_CHECK","---------NoExplanation----------");
                    // No explanation needed, we can request the permission.

                    neededPermissions.add(Manifest.permission.VIBRATE);
                }
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                } else {
                    Log.d("PERMISSION_CHECK","---------NoExplanation----------");
                    // No explanation needed, we can request the permission.

                    neededPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                }
            }

            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {

                } else {
                    Log.d("PERMISSION_CHECK","---------NoExplanation----------");
                    // No explanation needed, we can request the permission.

                    neededPermissions.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }

            //Asking for needed permissions
            if (!neededPermissions.isEmpty()) {
                ActivityCompat.requestPermissions(this, neededPermissions.toArray(new String[neededPermissions.size()]),MULTIPLE_PERMISSIONS );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Restarting the application on permissions granted
        if (requestCode==MULTIPLE_PERMISSIONS){

            Log.v("test", "restarting activity");

            Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
            int mPendingIntentId = 123456;
            PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId, mStartActivity,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager mgr = (AlarmManager) MainActivity.this.getSystemService(Context.ALARM_SERVICE);
            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
            System.exit(0);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent pickOptionIntent = new Intent(getApplicationContext(), OptionActivity.class);
            /*if(ice_cream && !m_lollipopOrHigher) {
                pickOptionIntent.putExtra("DefaultsEnforced", m_ttobj.areDefaultsEnforced());
            }*/
            pickOptionIntent.putExtra("languages", m_locals);
            startActivityForResult(pickOptionIntent, OPTION_REQUEST);
            /*Intent intent = new Intent(getApplicationContext(), OptionActivity.class);
            startActivity(intent);*/
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean allCompulsoryAuthorizationsGranted(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            Log.v("test", "pas le droit caméra");
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED){
            Log.v("test", "pas le droit internet");
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.VIBRATE)
                != PackageManager.PERMISSION_GRANTED){
            Log.v("test", "pas le droit de vibrer");
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            Log.v("test", "pas le droit de vibrer");
            return false;
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            Log.v("test", "pas le droit de vibrer");
            return false;
        }

        Log.v("test", "Toutes les permissions sont allouées");

        return true;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to

        Log.v("test", "activity result");


        // On demande les permissions obligatoires si elles ne sont pas toutes autorisées
        if (!allCompulsoryAuthorizationsGranted()){
            Log.v("test", "on result : pas toutes les autorisations, on lance checkPermissions()");
            checkPermissions();
        }


        if (requestCode == OPTION_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);

                m_speechSpeed = settings.getFloat("m_speechSpeed", SPEEDSPEECH_DEFAULT);

                m_ttslanguage = new Locale(settings.getString("speechLanguage",LOCALE_DEFAULT.getLanguage()),settings.getString("speechCountry",LOCALE_DEFAULT.getCountry()));
                m_ttobj.setLanguage(m_ttslanguage);

                m_content_reset_time = settings.getFloat("resetTime2", DEFAULT_CONTENT_RESET_TIME);

                m_multiple_detection_time = settings.getFloat("MDTime",DEFAULT_MULTIPLE_DETECTION_TIME);


            }
        }

    }

    /**
     * Stops the detection : the application is no longer trying to detect QRCodes
     * The cameraView becomes white
     */
    public void stopDetection() {

        ToneGeneratorSingleton.getInstance().endOfDetectionTone();

        final AppCompatActivity activity = this;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                if(m_ttsready) {
                    m_cameraView.setVisibility(View.INVISIBLE);
                    m_isApplicationDetecting = false;
                }else{
                    cameraState = CAMERA_INACTIVE_STATE;
                }
            }
        });
    }

    /**
     * Starts the detection of new QRCodes
     */
    private void startDetection() {

        ToneGeneratorSingleton.getInstance().startingDetectionTone();

        final AppCompatActivity activity = this;
        activity.runOnUiThread(new Runnable() {
            public void run() {

                if(m_ttsprogress.getVisibility() == View.VISIBLE){
                    m_ttsprogress.setVisibility(View.GONE);
                    m_text_progress.setVisibility(View.GONE);
                }
                if(m_ttsready) {
                    m_cameraView.setVisibility(View.VISIBLE);
                    m_isApplicationDetecting = true;
                }else{
                    cameraState = CAMERA_DETECTING_STATE;
                }
            }
        });
    }




    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1)
    private void toSpeech20(String str, int queue)
    {
        m_ttobj.speak(str, queue, null);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void toSpeech21(String str, int queue)
    {
        String utteranceId = this.hashCode() + "";
        m_ttobj.speak(str, queue, null, utteranceId);
    }

    private void toSpeech(String str, int queue) {

        Log.v("double_question", str);
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager !=null){
            if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) == 0){
                m_vibrator.vibrate(1000);
                Toast.makeText(getApplicationContext(),"Le volume est à 0.",Toast.LENGTH_SHORT).show();
            }
        }

        if (m_lollipopOrHigher) {
            toSpeech21(str, queue);
        } else{
            toSpeech20(str,queue);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

       if(m_hasAccelerometer)
        {
            long curTime = System.currentTimeMillis();
            if ((curTime - m_lastSensorUpdate) > 2000) {
                if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    /*
      The vector used to gather datas from the m_accelerometer sensor
     */
                    float[] acceleromterVector = event.values.clone();

                    double norm_Of_g = Math.sqrt(acceleromterVector[0] * acceleromterVector[0] + acceleromterVector[1] * acceleromterVector[1] + acceleromterVector[2] * acceleromterVector[2]);

                    // Normalize the m_accelerometer vector
                    acceleromterVector[0] = acceleromterVector[0] / (float)norm_Of_g;
                    acceleromterVector[1] = acceleromterVector[1] / (float)norm_Of_g;
                    acceleromterVector[2] = acceleromterVector[2] / (float)norm_Of_g;

                    int inclination = (int) Math.round(Math.toDegrees(Math.acos(acceleromterVector[2])));
                    if(m_lastInclination == -1){
                        m_lastInclination = inclination;
                    }else{
                        if(Math.abs(inclination - m_lastInclination) < 75){
                            if (inclination < 40 && cameraState == CAMERA_INACTIVE_STATE && m_ttsready && m_detectionProgress == NO_QR_DETECTED) {
                                startDetection();
                                m_lastSensorUpdate = curTime;
                            } else if (inclination > 140 && cameraState == CAMERA_DETECTING_STATE && m_ttsready && m_detectionProgress == NO_QR_DETECTED) {
                                stopDetection();
                                m_lastSensorUpdate = curTime;
                            }
                        }
                    }
                    if(m_inclinationCounter == 7){
                        m_lastInclination = inclination;
                        m_inclinationCounter = 0;
                    }else{
                        m_inclinationCounter++;
                    }

                }
            }
        }else if(m_hasProximity) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

                m_lastSensorUpdate = System.currentTimeMillis();
                // La valeur de la lumière
                m_p = event.values[0];
                if (m_p < 5 && cameraState == CAMERA_DETECTING_STATE && m_detectionProgress == NO_QR_DETECTED) {
                    stopDetection();

                } else if (m_p >= 5 && cameraState == CAMERA_INACTIVE_STATE && m_detectionProgress == NO_QR_DETECTED) {
                    startDetection();
                }
            }
        }else{
            if(cameraState == CAMERA_INACTIVE_STATE && m_ttsready && m_detectionProgress == NO_QR_DETECTED) {
                startDetection();
            }
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }



    /* ************************************************************************************** */
    /*  Getters and setters of the fields. Used by the subclasses of QRCodeDetectionModeStrategy  */
    /* ************************************************************************************** */


    public void setDetectionProgress(int state){
        m_detectionProgress = state;
    }

    public int getDetectionProgress(){
        return m_detectionProgress;
    }

    public QRCodeCollection getDetectedQRCodes(){
        return m_detectedQRCodes;
    }

    public void setDetectionStrategy(QRCodeDetectionModeStrategy strategy){
        m_currentDetectionModeStrategy = strategy;
    }

    public boolean isApplicationDetecting(){
        return m_isApplicationDetecting;
    }

    public int getCurrentPos(){
        return m_currentPos;
    }

    public void incrementCurrentPos(){
        m_currentPos++;
    }

    public void decrementCurrentPos(){
        m_currentPos--;
    }

    public void setCurrentReading(List<QRContent> contents, int currentPos){
        m_currentReading = contents;
        m_currentPos = currentPos;
    }

    public int getContentSize(){
        return m_currentReading.size();
    }

    public boolean isTTSReady(){
        return m_ttsready;
    }
}

