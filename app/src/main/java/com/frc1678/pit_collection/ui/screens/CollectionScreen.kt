// Copyright (c) 2022 FRC Team 1678: Citrus Circuits
package com.frc1678.pit_collection.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.frc1678.pit_collection.Constants
import com.frc1678.pit_collection.LocalEventKey
import com.frc1678.pit_collection.LocalPitData
import com.frc1678.pit_collection.MainActivityViewModel
import com.frc1678.pit_collection.MainNavGraph
import com.frc1678.pit_collection.data.datapointToHumanReadable
import com.frc1678.pit_collection.data.datapoints
import com.frc1678.pit_collection.data.drivetrainTypes
import com.frc1678.pit_collection.data.otherToHumanReadable
import com.frc1678.pit_collection.data.pictureTypes
import com.frc1678.pit_collection.ui.components.createImageFile
import com.frc1678.pit_collection.ui.components.pictureFolder
import com.frc1678.pit_collection.util.TopBar
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.double
import kotlinx.serialization.json.int
import java.io.File

/** Main screen for data collection **/
@OptIn(ExperimentalLayoutApi::class)
@MainNavGraph
@Destination
@Composable
fun CollectionScreen(
    teamNumber: String, viewModel: MainActivityViewModel, navigator: DestinationsNavigator
) {
    val actionBar = LocalEventKey.current + " Version: ${Constants.VERSION}"

    Scaffold(
        topBar = {
            TopBar(
                modifier = Modifier.padding(10.dp),
                title = actionBar,
                navigator = navigator,
                true
            )
        }) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxSize()
        ) {
            var pictureExpanded by remember { mutableStateOf(false) }

            //Row with team number and camera button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
            ) {
                Text(
                    text = teamNumber,
                    fontSize = 60.sp,
                    modifier = Modifier
                        .weight(2F)
                        .align(Alignment.CenterVertically)
                )
                val hasFullRobotPicture by remember {mutableStateOf(File("/$pictureFolder/" + teamNumber + "_full_robot.jpg").exists())}
                val hasSidePicture by remember {mutableStateOf(File("/$pictureFolder/" + teamNumber + "_side.jpg").exists())}
                LargeFloatingActionButton(shape = CircleShape,
                    modifier = Modifier
                        .weight(2F)
                        .align(Alignment.CenterVertically)
                        .padding(10.dp),
                    containerColor =
                        if (
                            hasFullRobotPicture &&
                            hasSidePicture
                        )
                            Color.Green
                        else if (
                            hasFullRobotPicture ||
                            hasSidePicture
                        )
                            Color.Cyan
                        else Color.LightGray,
                    onClick = { pictureExpanded = !pictureExpanded }) {
                    Icon(Icons.Filled.CameraAlt, "camera", modifier = Modifier.scale(3F, 3F))

                    PictureDropdown(teamNumber = teamNumber,
                        expanded = pictureExpanded,
                        onDismissRequest = { pictureExpanded = !pictureExpanded })
                }
            }

            FlowRow(
                maxItemsInEachRow = 2,
                horizontalArrangement = Arrangement.Center,
                verticalArrangement = Arrangement.Center
            ) {
                val itemModifier = Modifier
                    .padding(4.dp)
                    .height(120.dp)
                    .weight(1f)

                datapoints.toList().forEach { datapoint ->
                    DatapointCollection(
                        modifier = itemModifier,
                        teamNumber = teamNumber,
                        datapoint = datapoint.first,
                    ) {
                        viewModel.updateData(teamNumber, datapoint.first, it)
                    }
                }
            }
        }
    }
}


/**Collection composable that will determine whether a popup or button will be shown **/
@Composable
fun DatapointCollection(
    modifier: Modifier,
    teamNumber: String,
    datapoint: String,
    setData: (JsonPrimitive) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }


    if (datapoint == "drivetrain") {
        Box {
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(
                        if (
                            LocalPitData.current[teamNumber]?.get("drivetrain")?.int != 0 &&
                            LocalPitData.current[teamNumber]?.get("drivetrain")?.int != null
                        )
                            Color.Green
                        else Color.LightGray
                    )
            ) {
                DrivetrainDropdownInput(
                    modifier = modifier,
                    expanded = expanded,
                    drivetrainTypes = drivetrainTypes,
                    setData = { setData(it) },
                    onDismissRequest = { expanded = !expanded },
                    teamNumber = teamNumber,
                )
            }
        }
    } else if (datapoints[datapoint]!!.content.all { char -> char.isDigit() }) {
        NumberDataInput(
            modifier = modifier,
            teamNumber = teamNumber,
            datapoint = datapoint,
            setData = setData
        )
    } else {
        BooleanDataInput(modifier, teamNumber, datapoint, setData)
    }
}

/**Button for boolean inputs **/
@Composable
fun BooleanDataInput(
    modifier: Modifier,
    teamNumber: String,
    datapoint: String,
    setData: (JsonPrimitive) -> Unit,
) {
    val localPitData = LocalPitData.current

    OutlinedCard(
        modifier = modifier,
        onClick = { setData(JsonPrimitive(!(localPitData[teamNumber]?.get(datapoint)?.boolean?: false))) },
        colors = if (localPitData[teamNumber]?.get(datapoint)?.boolean == true) CardDefaults.outlinedCardColors(
            Color.Green
        ) else CardDefaults.outlinedCardColors(
            Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            Text(
                if (localPitData[teamNumber]?.get(datapoint)?.boolean == true) datapointToHumanReadable[datapoint]!!.second
                else datapointToHumanReadable[datapoint]!!.first, textAlign = TextAlign.Center
            )
        }
    }
}

/** Composable for integer display. The value variable contains by default the value of the given datapoint.
 * It is converted to a string in order to have it be set in the text field.
 * The value of that string can always be changed, but the actual data is only set
 * when the value can be converted to double without becoming null.
 * If it is null after conversion, the isError variable will be set to true
 * and the label text will turn red. In addition, the edited string will not be written to the data.*/
@Composable
fun NumberDataInput(
    modifier: Modifier,
    teamNumber: String,
    datapoint: String,
    setData: (JsonPrimitive) -> Unit,
) {
    val localPitData = LocalPitData.current

    OutlinedCard(
        modifier = modifier,
        colors = CardDefaults.outlinedCardColors(
            if (
                localPitData[teamNumber]?.get(datapoint)?.double != 0.0 &&
                localPitData[teamNumber]?.get(datapoint)?.double != null
            )
                Color.Green
            else Color.LightGray
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize()
        ) {
            var value by rememberSaveable {
                mutableStateOf((localPitData[teamNumber]?.get(datapoint)?.double?: 0).toString())
            }

            TextField(
                value = value,
                onValueChange = {
                    value = it
                    if (it.toDoubleOrNull() != null) {
                        setData(JsonPrimitive(it.toDouble()))
                    }
                },
                label = {
                    Text(text = otherToHumanReadable[datapoint]!!)
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    keyboardType = KeyboardType.Number
                ),
                isError = value.toDoubleOrNull() == null,
                singleLine = true
            )
        }
    }
}

/** Loop through drivetrainTypes, for each it will have text of the item be the key of the given drivetrainType.
 *  On click, it will set the given team's datapoint value, and then set it.
 *  selectedText keeps track of what the value of your datapoint is.
 *  It also determines what is being displayed in the text field, which shows which item is selected.
 **/
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DrivetrainDropdownInput(
    modifier: Modifier,
    expanded: Boolean,
    drivetrainTypes: Map<String, JsonPrimitive>,
    setData: (JsonPrimitive) -> Unit,
    onDismissRequest: () -> Unit,
    teamNumber: String,
) {
    val localPitData = LocalPitData.current

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { onDismissRequest() },
    ) {
        TextField(modifier = modifier
            .fillMaxSize()
            .menuAnchor(),
            value = otherToHumanReadable[drivetrainTypes.keys.toList()[localPitData[teamNumber]?.get(
                "drivetrain"
            )?.int ?: 0]]!!,
            onValueChange = {},
            readOnly = true,
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(
                    expanded = expanded
                )
            }
        )

        DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
            drivetrainTypes.forEach { item ->
                DropdownMenuItem(
                    text = { Text(otherToHumanReadable[item.key]!!) },
                    trailingIcon = {
                        if (localPitData[teamNumber]?.get("drivetrain")?.int == (drivetrainTypes[item.key]?.int
                                ?: 0)
                        ) {
                            Icon(Icons.Default.Check, "")
                        }
                    },
                    onClick = {
                        setData(item.value)
                        onDismissRequest()
                    },
                )
            }
        }
    }
}

/** Dropdown menu with two items: full robot or side.
 * For each one, clicking on the menu item will launch the camera activity, passing in a Uri file location.
 * Launching from ActivityResultsContracts.Picture takes that Uri and saves the taken image to a designated file.
 * That designated location is defined with createImageFile()
 **/
@Composable
fun PictureDropdown(teamNumber: String, expanded: Boolean, onDismissRequest: () -> Unit) {
    val takePictureLauncher =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicture()) {}
    val pictureContext = LocalContext.current


    DropdownMenu(expanded = expanded, onDismissRequest = onDismissRequest) {
        pictureTypes.forEach { item ->
            DropdownMenuItem(
                text = { Text(otherToHumanReadable[item]!!) },
                trailingIcon = {
                    if (File("/$pictureFolder/" + teamNumber + "_$item.jpg").exists()) {
                        Icon(Icons.Default.Check, "selected")
                    }
                },
                onClick = {
                    takePictureLauncher.launch(
                        createImageFile(
                            teamNumber = teamNumber,
                            pictureType = item,
                            context = pictureContext
                        )
                    )
                    onDismissRequest()
                }
            )
        }
    }
}
