package com.frc1678.pit_collection

import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.json.JsonPrimitive

val LocalTeamList = compositionLocalOf<List<String>> { error("Team list not provided") }

val LocalPitData = compositionLocalOf<Map<String, Map<String, JsonPrimitive>>> { error("Pit data not provided") }

val LocalStarredTeamList = compositionLocalOf<Set<String>> { error("Starred team list not provided") }

val LocalEventKey = compositionLocalOf<String> { error("Action bar not provided") }
