package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import com.example.helion.core.HelionAppContainer
import com.example.helion.core.notification.HelionNotificationManager
import com.example.helion.ui.HelionApp
import com.example.ui.theme.HelionTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: HelionAppContainer
    private val notificationDestination = mutableStateOf<String?>(null)
    private val notificationEntityId = mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = HelionAppContainer(applicationContext)
        extractNotificationExtras(intent)

        setContent {
            HelionTheme {
                HelionApp(
                    container = appContainer,
                    initialDestination = notificationDestination.value,
                    initialEntityId = notificationEntityId.value,
                    onDestinationHandled = {
                        notificationDestination.value = null
                        notificationEntityId.value = null
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationExtras(intent)
    }

    private fun extractNotificationExtras(intent: Intent?) {
        val dest = intent?.getStringExtra(HelionNotificationManager.EXTRA_DESTINATION)
        val entityId = intent?.getStringExtra(HelionNotificationManager.EXTRA_ENTITY_ID)
        if (dest != null) {
            notificationDestination.value = dest
            notificationEntityId.value = entityId
        }
    }
}
