package com.pretty.library.permissions

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.fragment.app.Fragment
import java.lang.ref.WeakReference

class SmartPermission private constructor(context: Context) {

    private val wrContext: WeakReference<Context> = WeakReference(context)


    companion object {
        /** 权限设置页跳转请求码  */
        const val REQUEST_CODE = 1024 + 1

        /** 调试模式  */
        private var sDebugMode: Boolean? = null

        /**
         * 设置权限请求拦截器
         */
        private var interceptor: IPermissionInterceptor? = null

        fun setDebugMode(debug: Boolean) {
            sDebugMode = debug
        }

        fun isDebugMode(context: Context): Boolean {
            if (sDebugMode == null) {
                sDebugMode =
                    context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0
            }
            return sDebugMode!!
        }

        fun setInterceptor(interceptor: IPermissionInterceptor) {
            this.interceptor = interceptor
        }

        fun getInterceptor(): IPermissionInterceptor {
            return this.interceptor ?: object : IPermissionInterceptor {}
        }

        fun isGranted(context: Context, permissions: Array<String>): Boolean {
            return PermissionUtils.isGrantedPermissions(context, permissions)
        }


        fun with(context: Context): SmartPermission {
            return SmartPermission(context)
        }

        fun with(fragment: Fragment): SmartPermission {
            return SmartPermission(fragment.activity!!)
        }


    }

}