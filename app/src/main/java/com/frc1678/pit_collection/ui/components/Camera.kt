package com.frc1678.pit_collection.ui.components

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.frc1678.pit_collection.Constants
import java.io.File

val pictureFolder = File(Constants.DOWNLOAD_FOLDER, "robot_pictures")

/** imageFile will be one of the robot files types
Creates the file that is named and located correctly **/
fun createImageFile(context: Context, teamNumber: String, pictureType: String): Uri {
    // Generate a unique file name for the image
    val fileName = teamNumber + "_$pictureType.jpg"

    // Get the directory where you want to save the image
    if (!pictureFolder.exists()) {
        pictureFolder.mkdirs() // Create the directory if it doesn't exist
    }

    // Create the file object with the directory and file name
    return FileProvider.getUriForFile(
        context,
        "com.frc1678.pit_collection.provider",
        File(pictureFolder, fileName)
    )
}
