// Copyright (c) 2022 FRC Team 1678: Citrus Circuits
package com.frc1678.pit_collection.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.frc1678.pit_collection.Constants
import com.frc1678.pit_collection.LocalEventKey
import com.frc1678.pit_collection.LocalPitData
import com.frc1678.pit_collection.LocalStarredTeamList
import com.frc1678.pit_collection.LocalTeamList
import com.frc1678.pit_collection.MainActivityViewModel
import com.frc1678.pit_collection.MainNavGraph
import com.frc1678.pit_collection.ui.components.pictureFolder
import com.frc1678.pit_collection.ui.destinations.CollectionScreenDestination
import com.frc1678.pit_collection.ui.destinations.PicturesScreenDestination
import com.frc1678.pit_collection.util.TopBar
import com.frc1678.pit_collection.util.setFileEventKey
import com.frc1678.pit_collection.util.triggerRebirth
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.navigation.EmptyDestinationsNavigator
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import java.io.File

/** Initial screen with team list **/
@MainNavGraph(start = true)
@Destination
@Composable
fun TeamListScreen(viewModel: MainActivityViewModel, navigator: DestinationsNavigator) {
    val starredTeamList = LocalStarredTeamList.current
    val context = LocalContext.current
    val teamList = LocalTeamList.current
    val eventKeyFile = viewModel.eventKeyFile

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.padding(10.dp),
                title = LocalEventKey.current + " Version: ${Constants.VERSION}",
                navigator = navigator,
                false
            )
        }
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding)) {
            EventKeyHeader(
                modifier = Modifier.padding(10.dp),
                onValueChange = {
                    setFileEventKey(eventKeyFile, it)
                },
                onClick = { triggerRebirth(context) }
            )

            TeamListView(
                teamList = teamList,
                starredTeams = starredTeamList,
                starTeam = { viewModel.starTeam(it) },
                navigator = navigator
            )
        }
    }
}


@Composable

fun TeamListView(
    teamList: List<String>,
    starredTeams: Set<String>,
    starTeam: (teamNumber: String) -> Unit,
    navigator: DestinationsNavigator
) {
    LazyColumn(
        Modifier.fillMaxSize()
    ) {
        items(teamList) { team ->
            TeamListUnit(
                team,
                starredTeams,
                Modifier
                    .clickable { navigator.navigate(CollectionScreenDestination(team)) }
                    .fillMaxWidth(1F)
                    .padding(10.dp),
                { starTeam(team) },
                navigator
            )
        }
    }
}

@Composable
fun TeamListUnit(
    team: String,
    starredTeams: Set<String>,
    modifier: Modifier,
    starTeam: (team: String) -> Unit,
    navigator: DestinationsNavigator
) {
    val localPitData = LocalPitData.current
    val hasData by remember {
        mutableStateOf(
            localPitData[team]?.get("drivetrain")?.int != 0 &&
            localPitData[team]?.get("drivetrain")?.int != null &&
            localPitData[team]?.get("weight")?.double != 0.0 &&
            localPitData[team]?.get("weight")?.double != null
        )
    }
    val hasPictures by remember {
        mutableStateOf(
            File("/$pictureFolder/" + team + "_full_robot.jpg").exists() &&
            File("/$pictureFolder/" + team + "_side.jpg").exists()
        )
    }
    Card(modifier = modifier, onClick = { navigator.navigate(CollectionScreenDestination(team)) }) {
        Row(modifier = Modifier.padding(4.dp).background(if (hasData && hasPictures) Color.Green else if (hasData || hasPictures) Color.Cyan else Color.LightGray)) {
            val mod = Modifier
                .weight(1F)
                .align(Alignment.CenterVertically)
            Box(modifier = mod) {
                Text(text = team, modifier = Modifier.padding(4.dp))
            }
            Box(modifier = mod) {
                OtherIcons(teamNumber = team, navigator = navigator)
            }
            Box(modifier = mod) {
                IconButton(onClick = { starTeam(team) }) {
                    Icon(
                        Icons.Outlined.Star,
                        contentDescription = null,
                        tint = if (starredTeams.contains(team)) Color.Yellow else Color.Black
                    )
                }
            }
        }
    }
}

/** Header to edit event key **/
@Composable
fun EventKeyHeader(
    modifier: Modifier,
    onValueChange: (String) -> Unit,
    onClick: () -> Unit,
) {
    Row(modifier = modifier) {
        var value by remember { mutableStateOf("") }
        val mod = Modifier
            .weight(1F)
            .align(Alignment.CenterVertically)
            .padding(2.dp)

        Box(modifier = mod) {
            Text("Edit Event Key:")
        }

        Box(modifier = mod) {
            OutlinedTextField(
                value = value, onValueChange = {
                    value = it
                    onValueChange(value)
                }
            )
        }

        Box(modifier = mod) {
            Button(onClick = { onClick() }) {
                Text("Confirm Key")
            }
        }
    }
}

/** Function to display whether a team has photos or data
 * A team has data when their drivetrain is inputted **/
@Composable
fun OtherIcons(teamNumber: String, navigator: DestinationsNavigator) {
    Row {
        if (LocalPitData.current[teamNumber]?.get("drivetrain")?.int != 0 && LocalPitData.current[teamNumber]?.get(
                "drivetrain"
            )?.int != null
        ) {
            IconButton(onClick = {}) {
                Icon(
                    Icons.Outlined.Download,
                    "has data",
                    modifier = Modifier.align(Alignment.CenterVertically)
                )
            }
        } else IconButton(onClick = {}, enabled = false) {}
        if (File("/$pictureFolder/" + teamNumber + "_full_robot.jpg").exists() && File("/$pictureFolder/" + teamNumber + "_side.jpg").exists()) {
            IconButton(onClick = {
                navigator.navigate(PicturesScreenDestination(teamNumber))
            }) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null)
            }
        }
    }
}

@Composable
@Preview
fun TeamListScreenPreview() {
    val testTeamList = listOf(
        "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13"
    )

    CompositionLocalProvider(
        LocalTeamList provides testTeamList,
        LocalPitData provides emptyMap(),
        LocalStarredTeamList provides emptySet(),
        LocalEventKey provides "2024arc" + " Version: ${Constants.VERSION}"
    ) {
        Surface {
            TeamListView(
                teamList = testTeamList,
                starredTeams = emptySet(),
                starTeam = {},
                navigator = EmptyDestinationsNavigator
            )
        }
    }
}


