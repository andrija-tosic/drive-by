package com.example.driveby.core

import android.content.Context
import android.util.Log
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.makeText
import com.example.driveby.core.Constants.LOG_TAG

class Utils {
    companion object {
        fun print(e: Exception) = Log.e(LOG_TAG, e.stackTraceToString())

        fun showToast(
            context: Context,
            message: String?
        ) = makeText(context, message, LENGTH_LONG).show()
    }
}