package com.frc1678.pit_collection.data.io

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import java.io.File

fun importPitData(file: File): Map<String, Map<String, JsonPrimitive>> {
    return Json.decodeFromString<Map<String, Map<String, JsonPrimitive>>>(file.readText())
}