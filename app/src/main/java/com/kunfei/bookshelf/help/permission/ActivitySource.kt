package com.kunfei.bookshelf.help.permission

import android.app.Activity
import android.content.Context
import android.content.Intent
import java.lang.ref.WeakReference

internal class ActivitySource(activity: Activity) : RequestSource {

    private val actRef: WeakReference<Activity> = WeakReference(activity)

    override val context: Context?
        get() = actRef.get()

    override fun startActivity(intent: Intent) {
        actRef.get()?.startActivity(intent)
    }

}
