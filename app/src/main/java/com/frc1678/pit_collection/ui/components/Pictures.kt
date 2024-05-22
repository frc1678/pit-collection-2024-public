package com.frc1678.pit_collection.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.frc1678.pit_collection.Constants
import com.frc1678.pit_collection.LocalEventKey
import com.frc1678.pit_collection.MainNavGraph
import com.frc1678.pit_collection.data.otherToHumanReadable
import com.frc1678.pit_collection.data.pictureTypes
import com.frc1678.pit_collection.util.TopBar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import java.io.File

@OptIn(ExperimentalFoundationApi::class)
@MainNavGraph
@Destination
@Composable
fun PicturesScreen(teamNumber: String, navigator: DestinationsNavigator) {
    val actionBar = LocalEventKey.current + " Version: ${Constants.VERSION}"

    Scaffold(topBar = {
        TopBar(
            modifier = Modifier.padding(10.dp), title = actionBar, navigator = navigator, true
        )
    }) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            for (picture in pictureTypes) {
                val fileName = teamNumber + "_$picture.jpg"
                val imageFile = File(pictureFolder, fileName)
                if (imageFile.exists()) {
                    stickyHeader {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp)
                        ) {
                            Text(
                                "${teamNumber}: " + otherToHumanReadable[picture]!!,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.headlineLarge
                            )
                        }
                    }
                    item {
                        LoadImage(imageFile)
                    }
                }

            }
        }
    }
}

@Composable
fun LoadImage(file: File) {
    AsyncImage(
        file, contentDescription = null, modifier = Modifier.padding(10.dp)
    )
}

@Composable
@Preview
fun PreviewImages() {
    CompositionLocalProvider(
        LocalEventKey provides "2024arc" + " Version: ${Constants.VERSION}"
    ) {
        Surface {
            PicturesScreen(teamNumber = "100", navigator = EmptyDestinationsNavigator)
        }
    }
}