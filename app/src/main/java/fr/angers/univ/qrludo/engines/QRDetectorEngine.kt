package fr.angers.univ.qrludo.engines

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.core.app.ActivityCompat
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import fr.angers.univ.qrludo.R
import fr.angers.univ.qrludo.utils.Logger
import fr.angers.univ.qrludo.utils.MainApplication

/**
 * QRDetector Engine Singleton
 *
 * In Kotlin, Singleton is performed using an object.
 * The QRDetector engine initialize the Camera, the detector then starts
 * looking for any QR code in the camera field.
 * It links the camera field to a SurfaceView in order to allow the user
 * to see for the searching area.
 */
object QRDetectorEngine {
    // The states of the Engine
    enum class ENGINE_STATE {
        IDLE,         // Ready to handle new event
        IS_SCANNING   // Uses the camera to search for a QR code
    }

    private var _state = ENGINE_STATE.IDLE;
    private var _camera_source: CameraSource? = null
    private var _detector: BarcodeDetector? = null
    private var _call_on_qr_found: ((String) -> Unit)? = null // Callback called when a new QR code has been found
    private var _call_on_qr_abort: (() -> Unit)? = null       // Callback called when the QR detector has been canceled

    private fun context(): Context {
        return MainApplication.application_context()
    }

    private fun logger(msg: String, level: Logger.DEBUG_LEVEL) {
        Logger.log("QRDetectorEngine", msg, level)
    }

    fun is_scanning(): Boolean {
        return (_state == ENGINE_STATE.IS_SCANNING)
    }

    // Init the QR detector engine
    fun init_engine() {
        if ((MainApplication.Main_Activity == null) || (MainApplication.Camera_View == null))
        {
            logger(context().getString(R.string.qre_no_activity), Logger.DEBUG_LEVEL.ERROR)
            return
        }
        val activity = MainApplication.Main_Activity!!
        val surface_view = MainApplication.Camera_View!!

        // create barcode detector instance
        // in my case I set barcode format to read QR type
        _detector =
            BarcodeDetector.Builder(context())
                .setBarcodeFormats(Barcode.QR_CODE)
                .build()

        if (!_detector!!.isOperational()) {
            logger(context().getString(R.string.qre_not_available), Logger.DEBUG_LEVEL.ERROR)
            return
        }

        // configure Camera source builder to capture frames and send it to the detector
        _camera_source = CameraSource
            .Builder(context(), _detector!!)
            .setRequestedPreviewSize(1024, 768)
            .setAutoFocusEnabled(true)
            .build();

        // implement callback in camera service to start camera whenever surface created
        surface_view.holder.addCallback(object : SurfaceHolder.Callback2 {
            override fun surfaceRedrawNeeded(holder: SurfaceHolder) {
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
            }

            // Callback called when the UI surface (widget) is destroyed in order
            // to stop the camera
            override fun surfaceDestroyed(holder: SurfaceHolder) {
                logger(context().getString(R.string.qre_stopped), Logger.DEBUG_LEVEL.INFO)
                _camera_source?.stop()
                _state = ENGINE_STATE.IDLE
            }

            // Callback called when the UI surface (widget) is created in order
            // to attach the camera
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ActivityCompat.checkSelfPermission(context(),
                        Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                ) {
                    logger(context().getString(R.string.qre_missing_permission), Logger.DEBUG_LEVEL.ERROR)
                    return
                }
                _state = ENGINE_STATE.IS_SCANNING
                logger(context().getString(R.string.qre_starting), Logger.DEBUG_LEVEL.INFO)
                _camera_source!!.start(surface_view.getHolder());
            }
        })
        surface_view.visibility = View.INVISIBLE

        //implement processor interface to catch the barcode scanner result
        _detector?.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
            }

            // Callback called when a new QR code has been detected
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                // Be carefull, not called on UI Thread
                val barcode = detections.detectedItems
                if (barcode.size() > 0) {
                    activity.runOnUiThread() {
                        if (_state == ENGINE_STATE.IS_SCANNING) {
                            val back_call_on_qr_found = _call_on_qr_found
                             stop()

                            // use barcode content value
                            logger(context().getString(R.string.qre_qr_found) + " : " + barcode.valueAt(0)?.displayValue, Logger.DEBUG_LEVEL.INFO)
                            back_call_on_qr_found?.invoke(barcode.valueAt(0)?.displayValue ?: "")
                        }
                    }
                }
            }
        })
        logger(context().getString(R.string.qre_initialized), Logger.DEBUG_LEVEL.INFO)
    }

    // Release the QR detector engine, detector and camera
    fun destroy_engine() {
        _detector?.release()
        _camera_source?.stop()
        _camera_source?.release()
        _camera_source = null
        _call_on_qr_found = null
        _call_on_qr_abort = null
        logger(context().getString(R.string.qre_destroyed), Logger.DEBUG_LEVEL.INFO)
    }

    // Start a new QR code detection
    fun start(call_on_qr_found: (String) -> Unit, call_on_qr_abort: () -> Unit) {
        if (_state == ENGINE_STATE.IS_SCANNING) {
            call_on_qr_abort()
            return
        }

        logger(context().getString(R.string.qre_starting_bef), Logger.DEBUG_LEVEL.VERBOSE)
        _call_on_qr_found = call_on_qr_found
        _call_on_qr_abort = call_on_qr_abort

        // By setting the Camera_View visible, we trigger the listener surfaceCreated()
        // (see above) which start the camera.
        if (MainApplication.Camera_View == null)
            logger(context().getString(R.string.qre_no_view), Logger.DEBUG_LEVEL.ERROR)
        else
            MainApplication.Camera_View!!.visibility = View.VISIBLE
    }

    // Stop the QR code detection
    private fun stop() {
        logger(context().getString(R.string.qre_stopped_bef), Logger.DEBUG_LEVEL.VERBOSE)
        _call_on_qr_found = null
        _call_on_qr_abort = null
        if (MainApplication.Camera_View == null)
            logger(context().getString(R.string.qre_no_view), Logger.DEBUG_LEVEL.ERROR)
        else
            MainApplication.Camera_View!!.visibility = View.INVISIBLE
    }

    // Abort the current QR code detection step (only when in use)
    fun cancel() {
        logger(context().getString(R.string.qre_stopped_bef), Logger.DEBUG_LEVEL.VERBOSE)
        if (MainApplication.Camera_View == null)
            logger(context().getString(R.string.qre_no_view), Logger.DEBUG_LEVEL.ERROR)
        else
            MainApplication.Camera_View!!.visibility = View.INVISIBLE
        _call_on_qr_abort?.invoke()
        _call_on_qr_found = null
        _call_on_qr_abort = null
    }
}