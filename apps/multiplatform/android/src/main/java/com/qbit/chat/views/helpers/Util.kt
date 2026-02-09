package com.qbit.chat.views.helpers

import android.app.KeyguardManager
import android.content.Context
import android.os.Build
import android.view.WindowManager
import com.qbit.chat.SimplexApp

fun getKeyguardManager(context: Context): KeyguardManager? {
  return context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
}