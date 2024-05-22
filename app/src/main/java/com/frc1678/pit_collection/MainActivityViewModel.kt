package com.frc1678.pit_collection

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.frc1678.pit_collection.data.PitData
import com.frc1678.pit_collection.data.datapoints
import com.frc1678.pit_collection.data.io.exportPitData
import com.frc1678.pit_collection.data.io.importPitData
import com.frc1678.pit_collection.util.sortTeamList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.OkHttpClient
import java.io.File
import java.nio.file.Files
import kotlin.io.path.listDirectoryEntries

class MainActivityViewModel : ViewModel() {
    /** Http client to request data from Grosbeak **/
    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json()
        }
        engine {
            preconfigured = OkHttpClient.Builder().dns(Ipv4OnlyDns()).build()
        }
    }
    private var _eventKey = MutableStateFlow("2024arc")

    val eventKey = _eventKey.asStateFlow()

    var eventKeyFile = File("${Constants.DOWNLOAD_FOLDER}/event_key.txt")

    /** For each event key, a folder with the event key as its title will be created.
     * The event key's team list, pit data, and starred teams will all be in that folder.
     * Files with getters to make sure updated event keys are transfering,
     * whenever these files are referenced, the eventKeyFolder will depend on the event key.
     **/
    private val eventKeyFolder get() = (File(Constants.DOWNLOAD_FOLDER, eventKey.value))
    private val teamListFile get() = File(eventKeyFolder, "team-list.json")
    private val pitDataFile get() = File(eventKeyFolder, "pit_data.json")
    private val starredTeamsFile get() = File(eventKeyFolder, "starred_teams.json")


    /** These ones are exposed to the MainActivity **/
    private var _teamList = MutableStateFlow(emptyList<String>())
    val teamList = _teamList.asStateFlow()
    private var _pitData = MutableStateFlow(PitData)
    val pitData = _pitData.asStateFlow()

    private var starredTeams = StarredTeams()
    private var _starredTeamList = MutableStateFlow(emptySet<String>())
    val starredTeamList = _starredTeamList.asStateFlow()
    var eventKeyDialog = false

    /**Whether the app is being loaded for the first time **/
    var appLoaded = false

    /** Function to initially populate the starred teams **/

    private fun populateStarredTeams() {
        _starredTeamList.value =
            if (starredTeamsFile.exists()) starredTeams.read(starredTeamsFile) else emptySet()
    }

    /**Function to star a team **/
    fun starTeam(teamNumber: String) {
        _starredTeamList.value =
            if (starredTeamList.value.contains(teamNumber)) _starredTeamList.value.toMutableSet()
                .apply { remove(teamNumber) }
            else _starredTeamList.value.toMutableSet().apply { add(teamNumber) }
    }

    /**Function that will track when the starred team list is changed to recompose **/
    private fun collectStarredTeams() {
        viewModelScope.launch(Dispatchers.IO) {
            starredTeamList.collect {
                starredTeams.write(starredTeamsFile, starredTeamList.value)
                _starredTeamList.value = starredTeams.read(starredTeamsFile)
            }
        }

    }

    /** Launch a coroutine that will update data of the file and app when pitData is edited
    the current data will be exported to the pit data file.
    Then, it will repull the data to make sure the file is updated **/
    private fun collectData() {
        viewModelScope.launch(Dispatchers.IO) {
            pitData.collect {
                exportPitData(pitDataFile, pitData.value)
                _pitData.value = importPitData(pitDataFile)
            }
        }

    }

    /**Initially add the teams from the team list to the pit data **/
    private fun addPitDataDefaults() {
        _pitData.value = teamList.value.associateWith { datapoints }
    }

    /** Function that will be passed down to update data from collection screen.
    It sets pitData to pitData with the value of the given team's datapoint updated **/
    fun updateData(teamNumber: String, datapoint: String, value: JsonPrimitive) {
        _pitData.value = pitData.value.toMutableMap().apply {
            val innerMap = getOrDefault(teamNumber, mutableMapOf()).toMutableMap()
            innerMap[datapoint] = value
            put(teamNumber, innerMap)
        }
    }
    /** Function to pull event key from file
     * Pulls event key from file, then deletes file to make sure opening the app initially won't incorrectly pull from the file
     * **/
    private fun pullEventKey() {
        val eventKeyPath = eventKeyFile.toPath()
        if (eventKeyFile.exists()) {
            val fileEventKey = eventKeyFile.readText()
            _eventKey.value = fileEventKey
            Files.deleteIfExists((eventKeyPath))
        }
    }

    /** Function to initially get data for team list.
     Because the event key's folder will be created after the event key is pulled,
     that folder will also be deleted when an error is caught.
     The event key will also be set to default to ensure that the incorrect one won't be used.
     **/

    private suspend fun pullTeamList(client: HttpClient) {
        try {
            _teamList.value =
                client.get("redacted${eventKey.value}") {
                    header("Authorization", Constants.GROSBEAK_AUTH_KEY)
                }.body<List<String>>().sortedBy { it.toIntOrNull() }

            // Cache the team list file

        } catch (t: Throwable) {
            _eventKey.value = "2024arc"
            eventKeyDialog = true
            Log.e("EVENT KEY", t.stackTraceToString())
        }

    }
    /** Separated function to write the team list to file.
     * This was separated from the function that pulls from grosbeak
     * so that the pulling could be done first to check if the event key is invalid.
     * This was added to address the previous need to delete the folder in case the key was invalid.
     * Now, if the key is changed in code, that key will be checked with grosbeak,
     * the team list will be pulled, the event key folder will be created
     * after making sure the event key is valid; then, the corresponding team list
     * will be written to that event key folder. */
    private fun writeTeamList() {
        teamListFile.writeText(Json.encodeToString(teamList.value))
    }

    /**Function to initially populate pitdata on opening from the pitData, if data doesn't exist,
     * put in false for all datapoints **/
    private fun populatePitData() {
        if (pitDataFile.exists()) {
            _pitData.value = importPitData(pitDataFile)
        } else {
            addPitDataDefaults()
        }
    }

    /**Checks if teamlist exists. If it does, it launches a coroutine to populate the teamlist **/
    private fun populateTeamList() {
        if (teamListFile.exists()) {
            // Parse the team list, which is stored as a JSON array
            viewModelScope.launch(Dispatchers.IO) {
                _teamList.value =
                    sortTeamList(Json.parseToJsonElement(teamListFile.readText()).jsonArray.map { it.jsonPrimitive.content })
            }
        } else _teamList.value = emptyList()
    }

    /**Function to load the app **/
    fun loadApp() {
        viewModelScope.launch {
            pullEventKey()
            pullTeamList(client)
            if (!eventKeyFolder.exists()) eventKeyFolder.mkdir()
            writeTeamList()
            populateTeamList()
            populateStarredTeams()
            populatePitData()
            collectData()
            collectStarredTeams()
            appLoaded = !appLoaded
        }
    }
}

/**Class containing methods for reading and writing to starred teams file **/
class StarredTeams {
    fun read(file: File): Set<String> {
        try {
            return if (fileExists(file)) Json.decodeFromString<Set<String>>(file.readText()) else emptySet()
        } catch (e: Exception) {
            Log.e("StarredTeams.read", "Failed to read starred teams file")
        }
        return emptySet()
    }

    fun write(file: File, starredTeams: Set<String>) {
        file.writeText(Json.encodeToString(starredTeams))
    }

    private fun fileExists(file: File): Boolean = file.exists()
}
