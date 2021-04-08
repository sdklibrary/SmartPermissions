package com.pretty.demo.permissions

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pretty.library.permissions.IPermissionCallback
import com.pretty.library.permissions.Permission
import com.pretty.library.permissions.SmartPermission

class MainActivity : AppCompatActivity(), View.OnClickListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_main_request_1).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_2).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_3).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_4).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_5).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_6).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_7).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_request_8).setOnClickListener(this)
        findViewById<Button>(R.id.btn_main_app_details).setOnClickListener(this)

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btn_main_request_1 -> {
                SmartPermission.with(this)
                    .permission(Permission.CAMERA)
                    .setInterceptor(MyPermissionInterceptor())
                    .request(object : IPermissionCallback {
                        override fun onGranted(permissions: Array<String>, all: Boolean) {
                            if (all) {
                                showToast("全部权限授权 1")
                            } else {
                                showToast("部分权限授权 1")
                            }
                        }
                    })
            }
            R.id.btn_main_request_2 -> {
                SmartPermission.with(this)
                    .permission(Permission.RECORD_AUDIO, *Permission.CALENDAR)
                    .setInterceptor(MyPermissionInterceptor())
                    .request(object : IPermissionCallback {
                        override fun onGranted(permissions: Array<String>, all: Boolean) {
                            if (all) {
                                showToast("全部权限授权 2")
                            } else {
                                showToast("部分权限授权 2")
                            }
                        }
                    })
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SmartPermission.REQUEST_CODE) {
            showToast("检测到你刚刚从权限设置界面返回回来")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
            .show()
    }
}