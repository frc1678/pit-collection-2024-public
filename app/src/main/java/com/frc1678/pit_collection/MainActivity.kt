package com.frc1678.pit_collection

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.frc1678.pit_collection.ui.NavGraphs
import com.frc1678.pit_collection.ui.components.EventKeyDialog
import com.frc1678.pit_collection.ui.theme.PitCollectionTheme
import com.frc1678.pit_collection.util.setFileEventKey
import com.frc1678.pit_collection.util.triggerRebirth
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.navigation.dependency
import okhttp3.Dns
import java.net.Inet4Address
import java.net.InetAddress

class Ipv4OnlyDns : Dns {
    override fun lookup(hostname: String): List<InetAddress> {
        val defaultAddresses = Dns.SYSTEM.lookup(hostname)
        val sortedAddresses = defaultAddresses.sortedBy {
            val isIpv4 = it is Inet4Address
            return@sortedBy isIpv4.not()
        }
        return sortedAddresses
    }
}

/**
 * Downloads the team list file from Grosbeak or uses a cached team list. If this fails, the error
 * is shown. Otherwise, opens TeamListScreen.
 */
class MainActivity : ComponentActivity() {
    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        /** Function to check if all files access permission is granted **/
        fun checkSettings() {
            if (!Environment.isExternalStorageManager()) {
                val intent =
                    Intent().apply {
                        action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                    }
                startActivity(intent)
            }

            if (
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                try {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(android.Manifest.permission.CAMERA),
                        100
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        //If it the app has not been loaded, check the settings.
        if (!viewModel.appLoaded) {
            checkSettings()
            viewModel.loadApp()
        }

        setContent {
            //Local data managed by the real ones in the view model
            val pitData by viewModel.pitData.collectAsStateWithLifecycle()
            val teamList by viewModel.teamList.collectAsStateWithLifecycle()
            val starredTeamList by viewModel.starredTeamList.collectAsStateWithLifecycle()
            val eventKey by viewModel.eventKey.collectAsStateWithLifecycle()
            val eventKeyFile = viewModel.eventKeyFile
            PitCollectionTheme {
                CompositionLocalProvider(
                    LocalTeamList provides teamList,
                    LocalPitData provides pitData,
                    LocalStarredTeamList provides starredTeamList,
                    LocalEventKey provides eventKey
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.background,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (viewModel.eventKeyDialog) {
                            val context = LocalContext.current
                            //Event key dialog in case the event key is incorrect
                            EventKeyDialog(
                                onDismissRequest = { triggerRebirth(context) },
                                errorMessage = "Incorrect Event Key",
                                onValueChange = {
                                    setFileEventKey(eventKeyFile, it)
                                },
                                defaultKey = eventKey
                            )
                        }

                        //Instantiates the nav host, which creates the normal navigation
                        DestinationsNavHost(
                            NavGraphs.main,
                            dependenciesContainerBuilder = { dependency(viewModel) }
                        )
                    }
                }
            }
        }
    }
}
