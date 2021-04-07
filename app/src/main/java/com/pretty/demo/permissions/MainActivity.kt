package com.pretty.demo.permissions

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.pretty.library.permissions.IPermissionCallback
import com.pretty.library.permissions.IPermissionInterceptor
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
//                    .setInterceptor(object : IPermissionInterceptor {
//
//                    })
                    .request(object : IPermissionCallback {
                        override fun onGranted(permissions: Array<String>, all: Boolean) {

                        }
                    })
            }
        }
    }
}