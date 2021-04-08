package com.pretty.demo.permissions

import android.app.AlertDialog
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.pretty.library.permissions.IPermissionCallback
import com.pretty.library.permissions.IPermissionInterceptor
import com.pretty.library.permissions.PermissionHelper
import com.pretty.library.permissions.SmartPermission

class MyPermissionInterceptor : IPermissionInterceptor {

    override fun requestPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>
    ) {
        AlertDialog.Builder(activity)
            .setTitle("授权提示")
            .setMessage("使用此功能需要先授予权限")
            .setPositiveButton("授予") { dialog, _ ->
                dialog.dismiss()
                PermissionHelper.beginRequest(activity, permissions, callback)
            }
            .setNegativeButton("拒绝") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    override fun grantedPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>,
        all: Boolean
    ) {
        callback.onGranted(permissions, all)
    }

    override fun deniedPermissions(
        activity: FragmentActivity,
        callback: IPermissionCallback,
        permissions: Array<String>,
        never: Boolean
    ) {
        callback.onDenied(permissions, never)
        if (never) {
            AlertDialog.Builder(activity)
                .setTitle("授权提示")
                .setMessage("获取权限失败，请手动授予权限")
                .setPositiveButton("前往授权") { dialog, _ ->
                    dialog.dismiss()
                    SmartPermission.startPermissionActivity(activity, permissions)
                }.show()
        } else {
            Toast.makeText(activity, "授权失败，请正确授予权限", Toast.LENGTH_SHORT)
                .show()
        }
    }
}