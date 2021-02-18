package fr.angers.univ.qrludo.utils

import android.os.Handler
import android.os.Looper
import android.view.GestureDetector
import android.view.MotionEvent

/**
 * OnSwipeTouchListener designs a class that implement advances gestures:
 * swipe, double swipe and double tap
 * It recognizes the gesture and triggers the corresponding callback
 */
open class OnSwipeTouchListener : GestureDetector.SimpleOnGestureListener() {
    /// The available simple gestures
    enum class SWIPE_GESTURE {
        NONE,
        SWIPE_LEFT,
        SWIPE_RIGHT,
        SWIPE_UP,
        SWIPE_DOWN
    }

    private val SWIPE_THRESHOLD : Int = 100
    private val SWIPE_VELOCITY_THRESHOLD : Int = 100
    private val DOUBLE_SWIPE_DELAY : Int = 400
    private var _last_swipe_gesture : SWIPE_GESTURE = SWIPE_GESTURE.NONE

    open fun on_double_tap() {
        // To override
    }
    open fun on_swipe_left() {
        // To override
    }
    open fun on_swipe_right() {
        // To override
    }
    open fun on_swipe_up() {
        // To override
    }
    open fun on_swipe_down() {
        // To override
    }
    open fun on_double_swipe_left() {
        // To override
    }
    open fun on_double_swipe_right() {
        // To override
    }
    open fun on_double_swipe_up() {
        // To override
    }
    open fun on_double_swipe_down() {
        // To override
    }

    override fun onDown(e: MotionEvent?): Boolean {
        return true
    }

    // Double tap is already handle by the base class
    override fun onDoubleTap(e: MotionEvent?): Boolean {
        on_double_tap()
        return true
    }

    // Function called each time a simple gesture is detected (a swipe or a tap).
    // It decides if the gesture must be extended to more advanced one (double swipe)
    private fun new_gesture_found(g : SWIPE_GESTURE) {
        //Log.d(MainApplication.application_context().getString(R.string.qrludo_tag_name), "SwipeListener: " + MainApplication.application_context().getString(R.string.swipe_listener) + ": " + g.name)
        if (_last_swipe_gesture == SWIPE_GESTURE.NONE) {
            if (g == SWIPE_GESTURE.NONE) return // Old handler
            _last_swipe_gesture = g
            // If nothing comes in the next DOUBLE_SWIPE_DELAY msec, we run
            // new_gesture_found() with NONE in order to purge the last swipe event
            Handler(Looper.getMainLooper()).postDelayed(object : Runnable {
                override fun run() {
                    new_gesture_found(SWIPE_GESTURE.NONE)
                }
            }, DOUBLE_SWIPE_DELAY.toLong())
        } else if (g != _last_swipe_gesture) {
            val back_last_swipe_gesture = _last_swipe_gesture
            _last_swipe_gesture = g
            when (back_last_swipe_gesture) {
                SWIPE_GESTURE.SWIPE_LEFT -> on_swipe_left()
                SWIPE_GESTURE.SWIPE_RIGHT -> on_swipe_right()
                SWIPE_GESTURE.SWIPE_DOWN -> on_swipe_down()
                SWIPE_GESTURE.SWIPE_UP -> on_swipe_up()
                else -> Unit
            }
        } else {
            _last_swipe_gesture = SWIPE_GESTURE.NONE
            when (g) {
                SWIPE_GESTURE.SWIPE_LEFT -> on_double_swipe_left()
                SWIPE_GESTURE.SWIPE_RIGHT -> on_double_swipe_right()
                SWIPE_GESTURE.SWIPE_UP -> on_double_swipe_up()
                SWIPE_GESTURE.SWIPE_DOWN -> on_double_swipe_down()
                else -> Unit
            }
        }
    }

    // Called each time a finger movement is detected
    override fun onFling(
        e1: MotionEvent?,
        e2: MotionEvent?,
        velocityX: Float,
        velocityY: Float,
    ): Boolean {
        var result = false;
        try {
            val diffY : Float = e2!!.y - e1!!.y
            val diffX : Float = e2.x - e1.x
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        new_gesture_found(SWIPE_GESTURE.SWIPE_RIGHT)
                    } else {
                        new_gesture_found(SWIPE_GESTURE.SWIPE_LEFT)
                    }
                    result = true;
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    new_gesture_found(SWIPE_GESTURE.SWIPE_DOWN)
                } else {
                    new_gesture_found(SWIPE_GESTURE.SWIPE_UP)
                }
                result = true;
            }
        } catch (e : Exception) {
            e.printStackTrace();
        }
        return result;
    }
}