package fr.angers.univ.qrludo.utils

import android.annotation.TargetApi
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import fr.angers.univ.qrludo.R

/**
 * Class used to check many Android permissions at once.
 * This source code is based and adapted from an Internet source:
 *     https://gist.github.com/zeero0/fd314a281d007283fc7931f1c5da634c#file-permissionutil-java
 */
object PermissionUtil {
    val REQUEST_CODE_PERMISSIONS : Int = 100

    private fun context(): Context {
        return MainApplication.application_context()
    }

    /**
     * Check if multiple permissions are granted, if not request them.
     *
     * @param activity calling activity which needs permissions.
     * @param permissions one or more permissions, such as {@link android.Manifest.permission#CAMERA}.
     * @return true if all permissions are granted, false if at least one is not granted yet.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun check_and_request_permissions(activity: Activity, vararg permissions: String) : Boolean {
        val permissions_list = mutableListOf<String>()
        for (p in permissions) {
            val permission_state = activity.checkSelfPermission(p)
            if (permission_state == PackageManager.PERMISSION_DENIED)
                permissions_list.add(p)
        }

        if (!permissions_list.isEmpty()) {
            ActivityCompat.requestPermissions(activity,
                permissions_list.toTypedArray(),
                REQUEST_CODE_PERMISSIONS)
            return false
        }
        return true
    }

    /**
     * Check if multiple permissions are granted, if not request them.
     *
     * @param activity calling activity which needs permissions.
     * @param request_code request code used to ask permission
     * @param permissions one or more permissions, such as {@link android.Manifest.permission#CAMERA}.
     * @param grant_results result codes of permission asking
     * @param permissions_callback_granted callback function called when all permissions have been granted
     * @param permissions_callback_denied callback function called when at least one permission has been denied
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    fun on_request_permissions_result(
        activity: Activity,
        request_code: Int,
        permissions: Array<out String>,
        grant_results: IntArray,
        permissions_callback_granted: (() -> Unit)? = null,
        permissions_callback_denied: (() -> Unit)? = null) {

        if (request_code == PermissionUtil.REQUEST_CODE_PERMISSIONS && (grant_results.size > 0)) {
            val permissions_list = mutableListOf<String>()
            for (i in permissions.indices) {
                if (grant_results[i] == PackageManager.PERMISSION_DENIED)
                    permissions_list.add(permissions[i])
            }

            if (permissions_list.isEmpty() && (permissions_callback_granted != null))
                permissions_callback_granted()
            else {
                var show_rationale = false
                for (p in permissions_list) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, p)) {
                        show_rationale = true
                        break
                    }
                }

                if (show_rationale) {
                    show_alert_dialog(
                        activity,
                        object : DialogInterface.OnClickListener {
                            @TargetApi(Build.VERSION_CODES.M)
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                check_and_request_permissions(activity, *permissions_list.toTypedArray());
                            }
                        },
                        object : DialogInterface.OnClickListener {
                            override fun onClick(dialog: DialogInterface?, which: Int) {
                                permissions_callback_denied?.invoke()
                            }
                        }
                    )
                }
            }
        }
    }

    /**
     * Show alert if any permission is denied and ask again for it.
     *
     * @param activity calling activity which needs permissions.
     * @param ok_listener
     * @param cancel_listener
     */
    fun show_alert_dialog(
        activity: Activity,
        ok_listener : DialogInterface.OnClickListener,
        cancel_listener : DialogInterface.OnClickListener) {
        AlertDialog.Builder(activity)
            .setMessage(context().getString(R.string.permissions_denied_ask_again))
            .setPositiveButton(context().getString(R.string.ok), ok_listener)
            .setNegativeButton(context().getString(R.string.no), cancel_listener)
            .create()
            .show()
    }
}