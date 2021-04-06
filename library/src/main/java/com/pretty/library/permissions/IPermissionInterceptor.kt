package com.pretty.library.permissions

import androidx.fragment.app.FragmentActivity

interface IPermissionInterceptor {

    fun requestPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>
    ) {
        PermissionHelper.beginRequest(activity, permissions, callback)
    }

    /**
     * 权限授予回调拦截，参见 [IPermissionCallback.onGranted]
     */
    fun grantedPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>,
        all: Boolean
    ) {
        callback.onGranted(permissions, all)
    }

    /**
     * 权限拒绝回调拦截，参见 [IPermissionCallback.onDenied]
     */
    fun deniedPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>,
        never: Boolean
    ) {
        callback.onDenied(permissions, never)
    }
}