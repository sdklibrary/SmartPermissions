package com.pretty.library.permissions

import android.app.Activity
import android.content.Context
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

object SmartPermission {

    /** 权限设置页跳转请求码  */
    const val REQUEST_CODE = 1024 + 1

    /** 权限列表  */
    private lateinit var mPermissions: Array<String>

    /**
     * 弱引用上下问
     */
    private lateinit var wrContext: WeakReference<Context>

    /**
     * 权限请求拦截器
     */
    private var interceptor: IPermissionInterceptor? = null

    fun with(context: Context) = apply {
        wrContext = WeakReference(context)
    }

    fun with(fragment: Fragment) = apply {
        wrContext = WeakReference(fragment.activity!!)
    }

    /**
     * 添加要请求的权限
     */
    fun permission(vararg permissions: String) = apply {
        this.mPermissions = Array(permissions.size) {
            permissions[it]
        }
    }

    /**
     * 设置权限请求拦截器
     */
    fun setInterceptor(interceptor: IPermissionInterceptor) = apply {
        this.interceptor = interceptor
    }

    /**
     * 开始请求权限
     */
    fun request(callback: IPermissionCallback) {
        val context = wrContext.get()
        if (context == null || mPermissions.isNullOrEmpty()) {
            return
        }

        if (PermissionUtils.isGrantedPermissions(context, *mPermissions)) {
            // 证明这些权限已经全部授予过，直接回调成功
            callback.onGranted(mPermissions, true)
            return
        }

        val fragmentActivity = PermissionUtils.findFragmentActivity(context) ?: return
        getInterceptor().requestPermissions(fragmentActivity, callback, mPermissions)
    }

    /**
     * 获取权限请求拦截器
     */
    fun getInterceptor(): IPermissionInterceptor {
        return this.interceptor ?: object : IPermissionInterceptor {}
    }

    /**
     * 判断一个或多个权限是否全部授予了
     */
    fun isGranted(context: Context, permissions: ArrayList<String>): Boolean {
        return isGranted(context, *permissions.toTypedArray())
    }

    /**
     * 判断一个或多个权限是否全部授予了
     */
    fun isGranted(context: Context, vararg permissions: String): Boolean {
        return PermissionUtils.isGrantedPermissions(context, *permissions)
    }

    /**
     * 判断某个权限是否是特殊权限
     */
    fun isSpecial(permission: String): Boolean {
        return PermissionUtils.isSpecialPermission(permission)
    }

    /**
     * 更加权限类型调整到合适的权限设置页
     */
    fun startPermissionActivity(
        activity: Activity,
        permissions: Array<String>,
        requestCode: Int = REQUEST_CODE
    ) {
        activity.startActivityForResult(
            PermissionIntent.getSmartPermissionIntent(
                activity,
                *permissions
            ), requestCode
        )
    }
}