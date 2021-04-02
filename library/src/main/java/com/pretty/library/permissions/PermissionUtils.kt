package com.pretty.library.permissions

import android.app.Activity
import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.Settings
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.pow

object PermissionUtils {

    /**
     * 是否是 Android 11 及以上版本
     */
    fun isAndroid11(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    /**
     * 是否是 Android 10 及以上版本
     */
    fun isAndroid10(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }

    /**
     * 是否是 Android 9.0 及以上版本
     */
    fun isAndroid9(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
    }

    /**
     * 是否是 Android 8.0 及以上版本
     */
    fun isAndroid8(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    }

    /**
     * 是否是 Android 7.0 及以上版本
     */
    fun isAndroid7(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    }

    /**
     * 是否是 Android 6.0 及以上版本
     */
    fun isAndroid6(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
    }

    /**
     * 是否有存储权限
     */
    fun isGrantedStoragePermission(context: Context): Boolean {
        return if (isAndroid11()) {
            Environment.isExternalStorageManager()
        } else {
            SmartPermission.isGranted(context, Permission.STORAGE)
        }
    }

    /**
     * 是否有安装权限
     */
    fun isGrantedInstallPermission(context: Context): Boolean {
        return if (isAndroid8()) {
            context.packageManager.canRequestPackageInstalls()
        } else true
    }

    /**
     * 是否有悬浮窗权限
     */
    fun isGrantedWindowPermission(context: Context): Boolean {
        return if (isAndroid6()) {
            Settings.canDrawOverlays(context)
        } else true
    }

    /**
     * 是否有通知栏权限
     */
    fun isGrantedNotifyPermission(context: Context): Boolean {
        if (isAndroid7()) {
            return context.getSystemService(NotificationManager::class.java)
                .areNotificationsEnabled()
        }
        if (isAndroid6()) {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            return try {
                val method = appOps.javaClass.getMethod(
                    "checkOpNoThrow", Integer.TYPE, Integer.TYPE,
                    String::class.java
                )
                val field = appOps.javaClass.getDeclaredField("OP_POST_NOTIFICATION")
                val value = field[Int::class.java] as Int
                method.invoke(
                    appOps,
                    value,
                    context.applicationInfo.uid,
                    context.packageName
                ) as Int == AppOpsManager.MODE_ALLOWED
            } catch (e: NoSuchMethodException) {
                true
            }
        }
        return true
    }

    /**
     * 是否有系统设置权限
     */
    fun isGrantedSettingPermission(context: Context): Boolean {
        return if (isAndroid6()) {
            Settings.System.canWrite(context)
        } else {
            true
        }
    }

    /**
     * 判断某个权限集合是否包含特殊权限
     * @param permissions 权限集合
     */
    fun containsSpecialPermission(permissions: Array<out String>): Boolean {
        if (permissions.isEmpty()) {
            return false
        }
        permissions.forEach {
            if (isSpecialPermission(it))
                return true
        }
        return false
    }

    /**
     * 判断某个权限是否是特殊权限
     */
    fun isSpecialPermission(permission: String): Boolean {
        return permission in Permission.SPECIAL_PERMISSION
    }

    /**
     * 判断某些权限是否全部被授予
     */
    fun isGrantedPermissions(context: Context, permissions: Array<String>): Boolean {
        // 如果是安卓 6.0 以下版本就直接返回 true
        if (!isAndroid6()) {
            return true
        }
        for (permission in permissions) {
            if (!isGrantedPermission(context, permission)) {
                return false
            }
        }
        return true
    }

    /**
     * 判断某个权限是否授予
     */
    fun isGrantedPermission(context: Context, permission: String): Boolean {
        // 如果是安卓 6.0 以下版本就默认授予
        if (!isAndroid6()) {
            return true
        }

        // 检测存储权限
        if (permission == Permission.MANAGE_EXTERNAL_STORAGE) {
            return isGrantedStoragePermission(context)
        }

        // 检测安装权限
        if (permission == Permission.REQUEST_INSTALL_PACKAGES) {
            return isGrantedInstallPermission(context)
        }

        // 检测悬浮窗权限
        if (permission == Permission.SYSTEM_ALERT_WINDOW) {
            return isGrantedWindowPermission(context)
        }

        // 检测通知栏权限
        if (permission == Permission.NOTIFICATION_SERVICE) {
            return isGrantedNotifyPermission(context)
        }

        // 检测系统权限
        if (permission == Permission.WRITE_SETTINGS) {
            return isGrantedSettingPermission(context)
        }

        // 检测 10.0 的三个新权限
        if (!isAndroid10()) {
            if (permission == Permission.ACCESS_BACKGROUND_LOCATION ||
                permission == Permission.ACCESS_MEDIA_LOCATION
            ) {
                return true
            }
            if (permission == Permission.ACTIVITY_RECOGNITION) {
                return context.checkSelfPermission(Permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED
            }
        }

        // 检测 9.0 的一个新权限
        if (!isAndroid9()) {
            if (permission == Permission.ACCEPT_HANDOVER) {
                return true
            }
        }

        // 检测 8.0 的两个新权限
        if (!isAndroid8()) {
            if (permission == Permission.ANSWER_PHONE_CALLS) {
                return true
            }
            if (permission == Permission.READ_PHONE_NUMBERS) {
                return context.checkSelfPermission(Permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
            }
        }
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * 获取某个权限的状态
     *
     * @return    已授权返回  [PackageManager.PERMISSION_GRANTED] 未授权返回  [PackageManager.PERMISSION_DENIED]
     */
    fun getPermissionStatus(context: Context, permission: String): Int {
        return if (isGrantedPermission(context, permission)) {
            PackageManager.PERMISSION_GRANTED
        } else {
            PackageManager.PERMISSION_DENIED
        }
    }

    /**
     * 在权限组中检查是否有某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permissions            请求的权限
     */
    fun isPermissionPermanentDenied(activity: Activity, permissions: ArrayList<String>): Boolean {
        for (permission in permissions) {
            if (isPermissionPermanentDenied(activity, permission)) {
                return true
            }
        }
        return false
    }

    /**
     * 判断某个权限是否被永久拒绝
     *
     * @param activity              Activity对象
     * @param permission            请求的权限
     */
    fun isPermissionPermanentDenied(activity: Activity, permission: String): Boolean {
        if (!isAndroid6()) {
            return false
        }

        // 特殊权限不算，本身申请方式和危险权限申请方式不同，因为没有永久拒绝的选项，所以这里返回 false
        if (isSpecialPermission(permission)) {
            return false
        }

        // 检测 10.0 的三个新权限
        if (!isAndroid10()) {
            if (permission == Permission.ACCESS_BACKGROUND_LOCATION ||
                permission == Permission.ACCESS_MEDIA_LOCATION
            ) {
                return false
            }
            if (permission == Permission.ACTIVITY_RECOGNITION) {
                return activity.checkSelfPermission(Permission.BODY_SENSORS) == PackageManager.PERMISSION_DENIED &&
                        !activity.shouldShowRequestPermissionRationale(permission)
            }
        }

        // 检测 9.0 的一个新权限
        if (!isAndroid9()) {
            if (permission == Permission.ACCEPT_HANDOVER) {
                return false
            }
        }

        // 检测 8.0 的两个新权限
        if (!isAndroid8()) {
            if (permission == Permission.ANSWER_PHONE_CALLS) {
                return true
            }
            if (permission == Permission.READ_PHONE_NUMBERS) {
                return activity.checkSelfPermission(Permission.READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED &&
                        !activity.shouldShowRequestPermissionRationale(permission)
            }
        }
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED &&
                !activity.shouldShowRequestPermissionRationale(permission)
    }

    /**
     * 获取没有授予的权限
     *
     * @param permissions           需要请求的权限组
     * @param grantResults          允许结果组
     */
    fun getDeniedPermissions(
        permissions: Array<String>,
        grantResults: IntArray
    ): ArrayList<String> {
        val deniedPermissions: ArrayList<String> = ArrayList()
        for (i in grantResults.indices) {
            // 把没有授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                deniedPermissions.add(permissions[i])
            }
        }
        return deniedPermissions
    }

    /**
     * 获取已授予的权限
     *
     * @param permissions       需要请求的权限组
     * @param grantResults      允许结果组
     */
    fun getGrantedPermissions(permissions: Array<out String>, grantResults: IntArray): ArrayList<String> {
        val grantedPermissions: ArrayList<String> = ArrayList()
        for (i in grantResults.indices) {
            // 把授予过的权限加入到集合中
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPermissions.add(permissions[i])
            }
        }
        return grantedPermissions
    }

    /**
     * 获得随机的 RequestCode
     */
    fun getRandomRequestCode(): Int {
        return Random().nextInt(2.0.pow(8.0).toInt())
    }

}