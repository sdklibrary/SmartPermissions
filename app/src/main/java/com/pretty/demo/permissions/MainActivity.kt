package com.pretty.demo.permissions

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pretty.library.permissions.Permission

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fields = Permission::class.java.declaredFields

        fields.forEach {field->
            if (String::class.java == field.type) {
                Log.i("TAG", "-->>>  ${field.get("")}")
            }
        }
    }
}