/*
 * XStopwatch / XTimer
 * Copyright (C) 2014 by Dan Wallach
 * Home page: http://www.cs.rice.edu/~dwallach/xstopwatch/
 * Licensing: http://www.cs.rice.edu/~dwallach/xstopwatch/licensing.html
 */
package org.dwallach.xstopwatch

import android.content.Intent
import android.util.Log


/**
 * Dumps all of the interesting contents of an intent to the log.
 */
fun Intent.log(tag: String) {
    Log.v(tag, "intent action($action), dataString($dataString), flags($flags), type($type)")

    categories?.forEach {
        Log.v(tag, "--- found category: ${it}")
    }

    extras?.keySet()?.forEach {
        Log.v(tag, "--- found extra: %s -> %s".format(it, extras[it].toString()))
    }
}