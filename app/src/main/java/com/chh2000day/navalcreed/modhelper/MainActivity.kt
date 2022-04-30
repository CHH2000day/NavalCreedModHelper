package com.chh2000day.navalcreed.modhelper

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberScaffoldState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import com.chh2000day.navalcreed.modhelper.ui.ActivityContent
import com.chh2000day.navalcreed.modhelper.ui.MainTopBar
import com.chh2000day.navalcreed.modhelper.ui.theme.NavalCreedModHelperTheme
import com.chh2000day.navalcreedmodhelper_v2.structs.AnnouncementResult
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.contentnegotiation.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    internal var newAnnouncement: AnnouncementResult? = null
    internal val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            Json {
                ignoreUnknownKeys = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NavalCreedModHelperTheme {
                val scaffoldState = rememberScaffoldState()
                val coroutineScope = rememberCoroutineScope()
                // A surface container using the 'background' color from the theme
                LaunchedEffect(this) {
                    launch {
                        fetchNewAnnouncement()
                        fetchUpdate()
                    }
                }
                Scaffold(
                    scaffoldState = scaffoldState,
                    topBar = {
                        MainTopBar()
                    }
                ) {
                    ActivityContent()
                }
            }
        }
    }

    private suspend fun fetchNewAnnouncement() {

    }

    private suspend fun fetchUpdate() {
//        withContext(Dispatchers.IO) {
//            val serverResult: ServerResult =
//                httpClient.post("${ConstStrings.ServerUrl}/updateAlpha.do") {
//                    formData {
//                        "legacy" to "0"
//                    }
//                }.body()
//            if (serverResult is VersionCheckResult.Success) {
//
//            }
//        }
    }
}
