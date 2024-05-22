package com.frc1678.pit_collection.util

import android.content.Context
import android.content.Intent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import java.io.File

/** Function to reload the app when event key is edited **/
fun triggerRebirth(context: Context) {
    val packageManager = context.packageManager
    val intent = packageManager?.getLaunchIntentForPackage(context.packageName)
    val componentName = intent!!.component
    val mainIntent = Intent.makeRestartActivityTask(componentName)
    context.startActivity(mainIntent)
    Runtime.getRuntime().exit(0)
}

/** Function to write to event key file, which also creates it if it doesn't exist **/
fun setFileEventKey(file: File, eventKey: String) {
    file.writeText(eventKey)
}

/**Top bar for Team list screen and Collection screen **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier,
    title: String,
    navigator: DestinationsNavigator,
    backButton: Boolean
) {
    CenterAlignedTopAppBar(
        navigationIcon = { if (backButton)
            IconButton(onClick = { navigator.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "back")
            }
        },
        modifier = modifier,
        title = {
            Text(title)
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.primary,
        )
    )
}