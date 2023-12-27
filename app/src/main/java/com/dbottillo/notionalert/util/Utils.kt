package com.dbottillo.notionalert.util

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.openLink(url: String) {
    val intentUrl = Intent(Intent.ACTION_VIEW)
    intentUrl.data = Uri.parse(url)
    intentUrl.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    this.startActivity(intentUrl)
}
