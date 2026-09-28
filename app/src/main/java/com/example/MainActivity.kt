package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.MainViewModel
import com.example.ui.components.CalmaTopAppBar
import com.example.ui.components.CrisisHelpDialog
import com.example.ui.screens.CbtReframingScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CheckInScreen
import com.example.ui.screens.RegulationScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CalmaApp()
            }
        }
    }
}

@Composable
fun CalmaApp(
    viewModel: MainViewModel = viewModel()
) {
    val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
    val showCrisisDialog by viewModel.showCrisisDialog.collectAsStateWithLifecycle()

    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val chatInput by viewModel.chatInput.collectAsStateWithLifecycle()
    val isSending by viewModel.isSending.collectAsStateWithLifecycle()

    val breathingState by viewModel.breathingState.collectAsStateWithLifecycle()
    val groundingState by viewModel.groundingState.collectAsStateWithLifecycle()

    val reframings by viewModel.allReframings.collectAsStateWithLifecycle()
    val checkIns by viewModel.allCheckIns.collectAsStateWithLifecycle()

    // Handle back button if not in Chat (Tab 0)
    BackHandler(enabled = currentTab != 0) {
        viewModel.selectTab(0)
    }

    if (showCrisisDialog) {
        CrisisHelpDialog(
            onDismissRequest = { viewModel.showCrisisModal(false) }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CalmaTopAppBar(
                currentTab = currentTab,
                onOpenCrisisDialog = { viewModel.showCrisisModal(true) },
                onClearChat = { viewModel.clearChatHistory() }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        Icon(imageVector = Icons.Default.ChatBubbleOutline, contentDescription = "Asistente CALMA")
                    },
                    label = { Text("CALMA") },
                    modifier = Modifier.testTag("nav_tab_chat"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = {
                        Icon(imageVector = Icons.Default.Air, contentDescription = "Regulación y Ejercicios")
                    },
                    label = { Text("Ejercicios") },
                    modifier = Modifier.testTag("nav_tab_regulation"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = {
                        Icon(imageVector = Icons.Default.Psychology, contentDescription = "Reframing TCC")
                    },
                    label = { Text("Reframing") },
                    modifier = Modifier.testTag("nav_tab_reframing"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )

                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = {
                        Icon(imageVector = Icons.Default.FavoriteBorder, contentDescription = "Check-in Emocional")
                    },
                    label = { Text("Check-in") },
                    modifier = Modifier.testTag("nav_tab_checkin"),
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(targetState = currentTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    0 -> ChatScreen(
                        messages = chatMessages,
                        inputText = chatInput,
                        isSending = isSending,
                        onInputChange = { viewModel.onChatInputChange(it) },
                        onSendMessage = { viewModel.sendMessage(it) },
                        onOpenCrisisDialog = { viewModel.showCrisisModal(true) }
                    )
                    1 -> RegulationScreen(
                        breathingState = breathingState,
                        groundingState = groundingState,
                        onSelectBreathingMode = { viewModel.setBreathingMode(it) },
                        onToggleBreathing = { viewModel.toggleBreathing() },
                        onResetBreathing = { viewModel.stopBreathing() },
                        onToggleGroundingItem = { viewModel.toggleGroundingItem(it) },
                        onSetGroundingStep = { viewModel.setGroundingStep(it) },
                        onResetGrounding = { viewModel.resetGrounding() }
                    )
                    2 -> CbtReframingScreen(
                        reframings = reframings,
                        onSaveReframing = { thought, facts, interp, ctrl, unctrl, step, before, after, onSuccess ->
                            viewModel.saveCbtReframing(thought, facts, interp, ctrl, unctrl, step, before, after, onSuccess)
                        },
                        onDeleteReframing = { viewModel.deleteReframing(it) }
                    )
                    3 -> CheckInScreen(
                        checkIns = checkIns,
                        onSaveCheckIn = { level, emotion, note, breathing, grounding, onSuccess ->
                            viewModel.saveMoodCheckIn(level, emotion, note, breathing, grounding, onSuccess)
                        },
                        onDeleteCheckIn = { viewModel.deleteMoodCheckIn(it) }
                    )
                }
            }
        }
    }
}
