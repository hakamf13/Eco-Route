package com.capstone.eco_route.datasource.db.other

import android.graphics.Color

object ContantsToken {

    const val TRACKER_DATABASE_NAME = "tracker_db"

    const val PERMISSIONS_CODE_REQUEST_LOCATION = 0

    const val START_OR_RESUME_ACTION_SERVICE = "START_OR_RESUME_ACTION_SERVICE"
    const val PAUSE_ACTION_SERVICE = "PAUSE_ACTION_SERVICE"
    const val STOP_ACTION_SERVICE = "STOP_ACTION_SERVICE"
    const val SHOW_ACTION_TRACKER_ACTIVITY = "SHOW_ACTION_TRACKER_ACTIVITY"

    const val NORMAL_LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL = 2000L

    const val POLYLINE_COLOR = Color.GREEN
    const val POLYLINE_WIDTH = 7f
    const val MAP_ZOOM = 17f

    const val CHANNEL_NOTIFICATION_ID = "tracker_channel"
    const val CHANNEL_NOTIFICATION_NAME = "Tracker"
    const val NOTIFICATION_ID = 1

}
