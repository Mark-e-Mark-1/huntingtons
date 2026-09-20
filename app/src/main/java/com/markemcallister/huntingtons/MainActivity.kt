package com.markemcallister.huntingtons

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.markemcallister.huntingtons.ui.HuntingtonsApp
import com.markemcallister.huntingtons.ui.theme.HuntingtonsTheme

class MainActivity : ComponentActivity() {
    private val viewModel: HuntingtonsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val catalogState by viewModel.catalogState.collectAsStateWithLifecycle()
            val screen by viewModel.screen.collectAsStateWithLifecycle()
            val refreshing by viewModel.refreshing.collectAsStateWithLifecycle()
            HuntingtonsTheme {
                HuntingtonsApp(
                    catalogState = catalogState,
                    screen = screen,
                    refreshing = refreshing,
                    messages = viewModel.messages,
                    onAcceptDisclaimer = viewModel::acceptDisclaimer,
                    onOpenDisclaimer = viewModel::openDisclaimer,
                    onOpenHome = viewModel::openHome,
                    onOpenOverview = viewModel::openOverview,
                    onOpenTreatments = viewModel::openTreatments,
                    onOpenResearch = viewModel::openResearch,
                    onOpenResources = viewModel::openResources,
                    onOpenGlossary = viewModel::openGlossary,
                    onOpenAbout = viewModel::openAbout,
                    onRefresh = viewModel::refreshResearch,
                    onBack = viewModel::goBack,
                )
            }
        }
    }
}
