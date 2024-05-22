package com.frc1678.pit_collection.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.frc1678.pit_collection.MainNavGraph
import com.ramcosta.composedestinations.annotation.Destination

/** Dialog that appears initially with an incorrect event key **/
@MainNavGraph
@Destination
@Composable
fun EventKeyDialog(
    onDismissRequest: () -> Unit,
    errorMessage: String,
    onValueChange: (value: String) -> Unit,
    defaultKey: String
) {
    var value by remember { mutableStateOf(TextFieldValue(defaultKey)) }

    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp)
                )

                OutlinedTextField(
                    value = value,
                    onValueChange = {
                        value = it
                        onValueChange(value.text)
                    },
                    label = { Text("Enter an event key") }
                )

                TextButton(onClick = { onDismissRequest() }) {
                    Text("Confirm")
                }
            }
        }
    }
}
