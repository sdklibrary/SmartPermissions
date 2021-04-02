package com.pretty.library.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

object PermissionIntent {

    /**
     * 根据传入的权限自动选择最合适的权限设置页
     */
    fun getSmartPermissionIntent(context: Context, vararg permissions: String): Intent {
        // 如果失败的权限里面不包含特殊权限
        if (permissions.isEmpty() || !PermissionUtils.containsSpecialPermission(permissions)) {
            return getAppDetailsIntent(context)
        }
        if ((PermissionUtils.isAndroid11() && permissions.size == 3 &&
                    permissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) &&
                    permissions.contains(Permission.READ_EXTERNAL_STORAGE) &&
                    permissions.contains(Permission.WRITE_EXTERNAL_STORAGE))
        ) {
            return getStoragePermissionIntent(context)
        }

        // 如果当前只有一个权限被拒绝了
        if (permissions.size == 1) {
            val permission = permissions[0]
            if (permission == Permission.MANAGE_EXTERNAL_STORAGE) {
                return getStoragePermissionIntent(context)
            }
            if (permission == Permission.REQUEST_INSTALL_PACKAGES) {
                return getInstallPermissionIntent(context)
            }
            if (permission == Permission.SYSTEM_ALERT_WINDOW) {
                return getWindowPermissionIntent(context)
            }
            if (permission == Permission.NOTIFICATION_SERVICE) {
                return getNotifyPermissionIntent(context)
            }
            if (permission == Permission.WRITE_SETTINGS) {
                return getSettingPermissionIntent(context)
            }
        }
        return getAppDetailsIntent(context)
    }

    /**
     * 获取应用详情界面意图
     */
    fun getAppDetailsIntent(context: Context): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = getPackageNameUri(context)
        return intent
    }

    /**
     * 获取安装权限设置界面意图
     */
    fun getInstallPermissionIntent(context: Context): Intent {
        var intent: Intent? = null
        if (PermissionUtils.isAndroid8()) {
            intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
            intent.data = getPackageNameUri(context)
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getAppDetailsIntent(context)
        }
        return intent
    }

    /**
     * 获取悬浮窗权限设置界面意图
     */
    fun getWindowPermissionIntent(context: Context): Intent {
        var intent: Intent? = null
        if (PermissionUtils.isAndroid6()) {
            intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
            if (!PermissionUtils.isAndroid11()) {
                intent.data = getPackageNameUri(context)
            }
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getAppDetailsIntent(context)
        }
        return intent
    }

    /**
     * 获取通知栏权限设置界面意图
     */
    fun getNotifyPermissionIntent(context: Context): Intent {
        var intent: Intent? = null
        if (PermissionUtils.isAndroid8()) {
            intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
            intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getAppDetailsIntent(context)
        }
        return intent
    }

    /**
     * 获取系统设置权限界面意图
     */
    fun getSettingPermissionIntent(context: Context): Intent {
        var intent: Intent? = null
        if (PermissionUtils.isAndroid6()) {
            intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS)
            intent.data = getPackageNameUri(context)
        }
        if (intent == null || !areActivityIntent(context, intent)) {
            intent = getAppDetailsIntent(context)
        }
        return intent
    }

    /**
     * 获取存储权限设置界面意图
     */
    fun getStoragePermissionIntent(context: Context): Intent {
        var intent: Intent? = null
        if (PermissionUtils.isAndroid11()) {
            intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
            intent.data = getPackageNameUri(context)
        }
        if (intent == null || !areActivityIntent(
                context,
                intent
            )
        ) {
            intent = getAppDetailsIntent(context)
        }
        return intent
    }


    /**
     * 判断这个意图的 Activity 是否存在
     */
    private fun areActivityIntent(context: Context, intent: Intent): Boolean {
        return context.packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY
        ).isNotEmpty()
    }

    /**
     * 获取包名 Uri 对象
     */
    private fun getPackageNameUri(context: Context): Uri {
        return Uri.parse("package:" + context.packageName)
    }

}