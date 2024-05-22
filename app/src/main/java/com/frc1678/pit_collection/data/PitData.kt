package com.frc1678.pit_collection.data

import kotlinx.serialization.json.JsonPrimitive

/** Main data **/
val PitData = mapOf<String, Map<String, JsonPrimitive>>()

/** List of datapoints **/
val datapoints = mapOf(
    "has_speaker_mech" to JsonPrimitive(false),
    "has_amp_mech" to JsonPrimitive(false),
    "has_trap_mech" to JsonPrimitive(false),
    "can_climb" to JsonPrimitive(false),
    "weight" to JsonPrimitive( 0),
    "drivetrain" to JsonPrimitive(0),
)

/** List of drivetrain types **/
val drivetrainTypes = mapOf(
    "default" to JsonPrimitive(0),
    "tank" to JsonPrimitive(1),
    "mecanum" to JsonPrimitive(2),
    "swerve" to JsonPrimitive(3),
    "other" to JsonPrimitive(4)
)

val pictureTypes = listOf(
    "full_robot",
    "side"
)

/** Map of datapoint to pair, first item in the pair represents a [false] value, second represents [true] **/
val datapointToHumanReadable = mapOf(
    "has_speaker_mech" to Pair("Doesn't Have Speaker Mechanism", "Has Speaker Mechanism"),
    "has_amp_mech" to Pair("Doesn't Have Amp Mechanism", "Has Amp Mechanism"),
    "has_trap_mech" to Pair("Doesn't Have Trap Mechanism", "Has Trap Mechanism"),
    "can_climb" to Pair("Can't Climb", "Can Climb"),


)
/** Other human readables **/
val otherToHumanReadable = mapOf(
    "drivetrain" to "Drivetrain",
    "full_robot" to "Full Robot",
    "side" to "Side",
    "weight" to "Weight (lbs.)",
    "default" to "Default Drivetrain",
    "tank" to "Tank",
    "mecanum" to "Mecanum",
    "swerve" to "Swerve",
    "other" to "Other"
)
