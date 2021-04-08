package com.pretty.library.permissions

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.SparseBooleanArray
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import java.util.*

class PermissionHelper : Fragment(), Runnable {

    /**
     * 是否申请了特殊权限
     */
    private var mSpecialRequest = false

    /**
     * 是否申请了危险权限
     */
    private var mDangerousRequest = false

    /**
     * 屏幕方向
     */
    private var mScreenOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    /**
     * 权限回调对象
     */
    private var mCallBack: IPermissionCallback? = null

    override fun onAttach(
        context: Context
    ) {
        super.onAttach(context)
        val activity = activity ?: return
        mScreenOrientation = activity.requestedOrientation
        if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            return
        }
        val activityOrientation = activity.resources.configuration.orientation
        try {
            if (activityOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else if (activityOrientation == Configuration.ORIENTATION_PORTRAIT) {
                activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (mSpecialRequest) {
            return
        }
        mSpecialRequest = true
        requestSpecialPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        val activity = activity ?: return
        val arguments = arguments
        if (arguments == null || mCallBack == null || requestCode != arguments.getInt(REQUEST_CODE)) {
            detachActivity(activity)
            return
        }
        val allPermissions = arguments.getStringArray(REQUEST_PERMISSIONS)
        if (allPermissions.isNullOrEmpty()) {
            detachActivity(activity)
            return
        }
        allPermissions.forEachIndexed { i, permission ->
            if (PermissionUtils.isSpecialPermission(permission)) {
                // 如果这个权限是特殊权限，那么就重新进行权限检测
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission)
                return@forEachIndexed
            }

            // 重新检查 Android 11 后台定位权限
            if (PermissionUtils.isAndroid11() &&
                permission == Permission.ACCESS_BACKGROUND_LOCATION
            ) {
                // 这个权限是后台定位权限并且当前手机版本是 Android 11 及以上，那么就需要重新进行检测
                // 因为只要申请这个后台定位权限，grantResults 数组总对这个权限申请的结果返回 -1（拒绝）
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission)
                return@forEachIndexed
            }

            // 重新检查 Android 10.0 的三个新权限
            if (!PermissionUtils.isAndroid10() &&
                (permission == Permission.ACCESS_BACKGROUND_LOCATION ||
                        permission == Permission.ACTIVITY_RECOGNITION ||
                        permission == Permission.ACCESS_MEDIA_LOCATION)
            ) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission)
                return@forEachIndexed
            }

            // 重新检查 Android 9.0 的一个新权限
            if (!PermissionUtils.isAndroid9() &&
                permission == Permission.ACCEPT_HANDOVER
            ) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission)
                return@forEachIndexed
            }

            // 重新检查 Android 8.0 的两个新权限
            if (!PermissionUtils.isAndroid8() &&
                (permission == Permission.ANSWER_PHONE_CALLS || permission == Permission.READ_PHONE_NUMBERS)
            ) {
                // 如果当前版本不符合最低要求，那么就重新进行权限检测
                grantResults[i] = PermissionUtils.getPermissionStatus(activity, permission)
            }
        }
        // 释放对这个请求码的占用
        REQUEST_CODE_ARRAY.delete(requestCode)// 将 Fragment 从 Activity 移除
        // 获取已授予的权限
        val grantedPermission = PermissionUtils.getGrantedPermissions(allPermissions, grantResults)

        if (grantedPermission.size == allPermissions.size) {
            // 代表申请的所有的权限都授予了
            SmartPermission.getInterceptor().grantedPermissions(
                activity,
                mCallBack!!,
                grantedPermission.toTypedArray(),
                true
            )
            return
        }
        // 获取被拒绝的权限
        val deniedPermission = PermissionUtils.getDeniedPermissions(allPermissions, grantResults)

        // 代表申请的权限中有不同意授予的，如果有某个权限被永久拒绝就返回 true 给开发人员，让开发者引导用户去设置界面开启权限
        SmartPermission.getInterceptor().deniedPermissions(
            activity,
            mCallBack!!,
            deniedPermission.toTypedArray(),
            PermissionUtils.isPermissionPermanentDenied(activity, deniedPermission)
        )

        // 证明还有一部分权限被成功授予，回调成功接口
        if (grantedPermission.isNotEmpty()) {
            SmartPermission.getInterceptor().grantedPermissions(
                activity, mCallBack!!,
                grantedPermission.toTypedArray(),
                false
            )
        }

        // 将 Fragment 从 Activity 移除
        detachActivity(activity)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        val activity = activity ?: return
        val arguments = arguments
        if (arguments == null || requestCode != arguments.getInt(REQUEST_CODE) || mDangerousRequest) {
            return
        }
        mDangerousRequest = true
        // 需要延迟执行，不然有些华为机型授权了但是获取不到权限
        activity.window.decorView.postDelayed(this, 200)
    }

    override fun onDetach() {
        super.onDetach()
        activity?.let {
            if (mScreenOrientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                return
            }
            it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mCallBack = null
    }

    override fun run() {
        if (!isAdded) {
            return
        }
        requestDangerousPermission()
    }

    /**
     * 申请特殊权限
     */
    private fun requestSpecialPermission() {
        val activity = activity ?: return
        val arguments = arguments
        if (arguments == null) {
            detachActivity(activity)
            return
        }

        val allPermissions = arguments.getStringArray(REQUEST_PERMISSIONS)
        if (allPermissions == null || allPermissions.isEmpty()) {
            detachActivity(activity)
            return
        }

        // 是否需要申请特殊权限
        var requestSpecialPermission = false
        if (PermissionUtils.containsSpecialPermission(allPermissions)) {
            if (allPermissions.contains(Permission.MANAGE_EXTERNAL_STORAGE) &&
                !PermissionUtils.isGrantedStoragePermission(activity)
            ) {
                // 跳转到存储权限设置界面
                if (PermissionUtils.isAndroid11()) {
                    startActivityForResult(
                        PermissionIntent.getStoragePermissionIntent(activity),
                        getArguments()!!.getInt(REQUEST_CODE)
                    )
                    requestSpecialPermission = true
                }
            }

            if (allPermissions.contains(Permission.REQUEST_INSTALL_PACKAGES) &&
                !PermissionUtils.isGrantedInstallPermission(activity)
            ) {
                // 跳转到安装权限设置界面
                startActivityForResult(
                    PermissionIntent.getInstallPermissionIntent(activity),
                    getArguments()!!.getInt(REQUEST_CODE)
                )
                requestSpecialPermission = true
            }

            if (allPermissions.contains(Permission.SYSTEM_ALERT_WINDOW) &&
                !PermissionUtils.isGrantedWindowPermission(activity)
            ) {
                // 跳转到悬浮窗设置页面
                startActivityForResult(
                    PermissionIntent.getWindowPermissionIntent(activity),
                    getArguments()!!.getInt(REQUEST_CODE)
                )
                requestSpecialPermission = true
            }

            if (allPermissions.contains(Permission.NOTIFICATION_SERVICE) &&
                !PermissionUtils.isGrantedNotifyPermission(activity)
            ) {
                // 跳转到通知栏权限设置页面
                startActivityForResult(
                    PermissionIntent.getNotifyPermissionIntent(activity),
                    getArguments()!!.getInt(REQUEST_CODE)
                )
                requestSpecialPermission = true
            }

            if (allPermissions.contains(Permission.WRITE_SETTINGS) &&
                !PermissionUtils.isGrantedSettingPermission(activity)
            ) {
                // 跳转到系统设置权限设置页面
                startActivityForResult(
                    PermissionIntent.getSettingPermissionIntent(activity),
                    getArguments()!!.getInt(REQUEST_CODE)
                )
                requestSpecialPermission = true
            }
        }

        if (!requestSpecialPermission) {
            //没有特色权限申请危险权限
            requestDangerousPermission()
        }

    }

    /**
     * 申请危险权限
     */
    private fun requestDangerousPermission() {
        val activity = activity ?: return
        val arguments = arguments
        if (arguments == null) {
            detachActivity(activity)
            return
        }

        val allPermissions = arguments.getStringArray(REQUEST_PERMISSIONS)
        if (allPermissions == null || allPermissions.isEmpty()) {
            detachActivity(activity)
            return
        }
        val locationPermission: ArrayList<String> = ArrayList()
        if (PermissionUtils.isAndroid10() && allPermissions.contains(Permission.ACCESS_BACKGROUND_LOCATION)) {
            if (allPermissions.contains(Permission.ACCESS_COARSE_LOCATION) &&
                !PermissionUtils.isGrantedPermission(activity, Permission.ACCESS_COARSE_LOCATION)
            ) {
                locationPermission.add(Permission.ACCESS_COARSE_LOCATION)
            }
            if (allPermissions.contains(Permission.ACCESS_FINE_LOCATION) &&
                !PermissionUtils.isGrantedPermission(activity, Permission.ACCESS_FINE_LOCATION)
            ) {
                locationPermission.add(Permission.ACCESS_FINE_LOCATION)
            }
        }

        if (locationPermission.isEmpty()) {
            requestPermissions(allPermissions, getArguments()!!.getInt(REQUEST_CODE))
            return
        }

        // 在 Android 10 的机型上，需要先申请前台定位权限，再申请后台定位权限
        beginRequest(activity, locationPermission.toTypedArray(), object : IPermissionCallback {

            override fun onGranted(permissions: Array<String>, all: Boolean) {
                if (!all || !isAdded) {
                    return
                }
                requestPermissions(allPermissions, arguments.getInt(REQUEST_CODE))
            }

            override fun onDenied(permissions: Array<String>, never: Boolean) {
                if (!isAdded) {
                    return
                }
                if (permissions.size == allPermissions.size - 1) {
                    val grantResults = IntArray(allPermissions.size)
                    Arrays.fill(grantResults, PackageManager.PERMISSION_DENIED)
                    onRequestPermissionsResult(
                        arguments.getInt(REQUEST_CODE), allPermissions, grantResults
                    )
                    return
                }

                requestPermissions(allPermissions, arguments.getInt(REQUEST_CODE))
            }
        })

    }

    /**
     * 设置权限申请回调
     */
    private fun setCallback(callback: IPermissionCallback) {
        this.mCallBack = callback
    }

    /**
     * 添加到Activity
     * @param activity 添加的目标Activity
     */
    private fun attachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().add(this, this.toString())
            .commitAllowingStateLoss()
    }

    /**
     * 从Activity中移除
     * @param activity 移除的目标Activity
     */
    private fun detachActivity(activity: FragmentActivity) {
        activity.supportFragmentManager.beginTransaction().remove(this)
            .commitAllowingStateLoss()
        Log.i("SmartPermission", "PermissionHelper DetachActivity")
    }

    companion object {
        /**
         * 请求的权限组
         */
        private const val REQUEST_PERMISSIONS = "request_permissions"

        /**
         * 请求码
         */
        private const val REQUEST_CODE = "request_code"

        /**
         * 权限请求码存放集合
         */
        private val REQUEST_CODE_ARRAY = SparseBooleanArray()

        /**
         * 开始请求权限
         */
        fun beginRequest(
            activity: FragmentActivity,
            permissions: Array<String>,
            callback: IPermissionCallback
        ) {
            var requestCode: Int
            do {
                requestCode = PermissionUtils.getRandomRequestCode()
            } while (REQUEST_CODE_ARRAY.get(requestCode))
            REQUEST_CODE_ARRAY.put(requestCode, true)
            val permissionHelper = PermissionHelper()
            permissionHelper.arguments = Bundle().apply {
                putInt(REQUEST_CODE, requestCode)
                putStringArray(REQUEST_PERMISSIONS, permissions)
            }
            // 设置保留实例，不会因为屏幕方向或配置变化而重新创建
            permissionHelper.retainInstance = true
            // 设置权限回调监听
            permissionHelper.setCallback(callback)
            // 绑定到 Activity 上面
            permissionHelper.attachActivity(activity)
        }
    }
}