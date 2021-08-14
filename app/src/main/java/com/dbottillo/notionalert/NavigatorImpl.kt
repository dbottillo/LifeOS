package com.dbottillo.notionalert

import android.app.Activity
import android.content.Intent
import com.dbottillo.notionalert.feature.about.AboutActivity

class NavigatorImpl : Navigator {

    override fun openAboutScreen(origin: Activity) {
        origin.startActivity(Intent(origin, AboutActivity::class.java))
    }
}
