package com.frc1678.pit_collection.data.io

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

fun exportPitData(file: File, pitData: Map<String, Map<String, JsonPrimitive>>) {
    file.writeText(Json.encodeToString(pitData))
}