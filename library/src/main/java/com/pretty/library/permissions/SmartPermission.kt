package com.pretty.library.permissions

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class SmartPermission private constructor(context: Context) {

    /**
     * 弱引用上下问
     */
    private val wrContext: WeakReference<Context> = WeakReference(context)

    /** 权限列表  */
    private var mPermissions: Array<out String>? = null

    fun permission(vararg permissions: String) = apply {
        this.mPermissions = permissions
    }

    @Suppress("UNCHECKED_CAST")
    fun request(callback: IPermissionCallback) {
        val context = wrContext.get()
        val requestPermissions: Array<String> = mPermissions as Array<String>
        if (context == null || requestPermissions.isNullOrEmpty()) {
            return
        }

        if (PermissionUtils.isGrantedPermissions(context, *requestPermissions)) {
            // 证明这些权限已经全部授予过，直接回调成功
            callback.onGranted(requestPermissions, true)
            return
        }

        val fragmentActivity = PermissionUtils.findFragmentActivity(context) ?: return
        interceptor?.requestPermissions(fragmentActivity, callback, requestPermissions)
    }

    companion object {
        /** 权限设置页跳转请求码  */
        const val REQUEST_CODE = 1024 + 1

        /**
         * 权限请求拦截器
         */
        private var interceptor: IPermissionInterceptor? = null

        /**
         * 设置权限请求拦截器
         */
        fun setInterceptor(interceptor: IPermissionInterceptor) {
            this.interceptor = interceptor
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


        fun with(context: Context): SmartPermission {
            return SmartPermission(context)
        }

        fun with(fragment: Fragment): SmartPermission {
            return SmartPermission(fragment.activity!!)
        }


    }

}