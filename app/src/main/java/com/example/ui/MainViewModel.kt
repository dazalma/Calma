package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.CbtReframingEntity
import com.example.data.local.ChatMessageEntity
import com.example.data.local.MoodCheckInEntity
import com.example.data.repository.CalmaRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class BreathingMode(val title: String, val description: String, val inhaleSec: Int, val holdSec: Int, val exhaleSec: Int, val pauseSec: Int) {
    FOUR_SEVEN_EIGHT(
        title = "Técnica 4-7-8",
        description = "Ideal para desacelerar pulsaciones, calmar el nervio vago y conciliar el sueño.",
        inhaleSec = 4,
        holdSec = 7,
        exhaleSec = 8,
        pauseSec = 0
    ),
    DIAPHRAGMATIC(
        title = "Diafragmática (Abdominal)",
        description = "Inhala expandiendo el abdomen y exhala lento para oxigenar tu cuerpo y relajar el pecho.",
        inhaleSec = 4,
        holdSec = 1,
        exhaleSec = 6,
        pauseSec = 1
    ),
    BOX(
        title = "Respiración Cuadrada (Box)",
        description = "Ritmo simétrico para recuperar la concentración y el control emocional en picos de estrés.",
        inhaleSec = 4,
        holdSec = 4,
        exhaleSec = 4,
        pauseSec = 4
    )
}

enum class BreathPhase(val label: String, val instruction: String) {
    INHALE("Inhala", "Toma aire suavemente por la nariz inflando el abdomen..."),
    HOLD("Sostén", "Mantén el aire con calma, sin tensión en hombros..."),
    EXHALE("Exhala", "Suelta el aire lentamente por la boca como apagando una vela..."),
    PAUSE("Pausa", "Descansa un instante antes del siguiente ciclo...")
}

data class BreathingState(
    val isRunning: Boolean = false,
    val mode: BreathingMode = BreathingMode.FOUR_SEVEN_EIGHT,
    val currentPhase: BreathPhase = BreathPhase.INHALE,
    val secondsLeftInPhase: Int = 4,
    val progress: Float = 0f, // 0f to 1f for smooth visual expansion/contraction
    val completedCycles: Int = 0
)

data class GroundingState(
    val currentStepIndex: Int = 0, // 0 = 5 vistas, 1 = 4 tactos, 2 = 3 sonidos, 3 = 2 olores, 4 = 1 sabor
    val checkedItems: Set<String> = emptySet(),
    val isFinished: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CalmaRepository

    init {
        val db = AppDatabase.getDatabase(application)
        repository = CalmaRepository(db.calmaDao())
    }

    // Navigation state
    private val _currentTab = MutableStateFlow(0) // 0: Chat, 1: Herramientas, 2: Reframing, 3: Diario
    val currentTab: StateFlow<Int> = _currentTab.asStateFlow()

    // Chat
    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.allMessages
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    // Crisis Emergency Dialog
    private val _showCrisisDialog = MutableStateFlow(false)
    val showCrisisDialog: StateFlow<Boolean> = _showCrisisDialog.asStateFlow()

    // Breathing
    private val _breathingState = MutableStateFlow(BreathingState())
    val breathingState: StateFlow<BreathingState> = _breathingState.asStateFlow()
    private var breathingJob: Job? = null

    // Grounding 5-4-3-2-1
    private val _groundingState = MutableStateFlow(GroundingState())
    val groundingState: StateFlow<GroundingState> = _groundingState.asStateFlow()

    // Check-in and Moods
    val allCheckIns: StateFlow<List<MoodCheckInEntity>> = repository.allCheckIns
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // CBT Reframings
    val allReframings: StateFlow<List<CbtReframingEntity>> = repository.allReframings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun selectTab(tabIndex: Int) {
        _currentTab.value = tabIndex
    }

    fun onChatInputChange(newText: String) {
        _chatInput.value = newText
    }

    fun showCrisisModal(show: Boolean) {
        _showCrisisDialog.value = show
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    fun sendMessage(text: String? = null) {
        val messageToSend = (text ?: _chatInput.value).trim()
        if (messageToSend.isEmpty() || _isSending.value) return

        _chatInput.value = ""
        _isSending.value = true

        viewModelScope.launch {
            val userMsg = repository.saveUserMessage(messageToSend)
            if (userMsg.isCrisis) {
                _showCrisisDialog.value = true
            }
            val currentList = chatMessages.value
            repository.generateAssistantResponse(messageToSend, currentList)
            _isSending.value = false
        }
    }

    // --- Breathing Logic ---
    fun setBreathingMode(mode: BreathingMode) {
        stopBreathing()
        _breathingState.value = BreathingState(
            isRunning = false,
            mode = mode,
            currentPhase = BreathPhase.INHALE,
            secondsLeftInPhase = mode.inhaleSec,
            progress = 0f,
            completedCycles = 0
        )
    }

    fun toggleBreathing() {
        if (_breathingState.value.isRunning) {
            stopBreathing()
        } else {
            startBreathing()
        }
    }

    private fun startBreathing() {
        breathingJob?.cancel()
        _breathingState.value = _breathingState.value.copy(
            isRunning = true,
            currentPhase = BreathPhase.INHALE,
            secondsLeftInPhase = _breathingState.value.mode.inhaleSec,
            progress = 0f
        )

        breathingJob = viewModelScope.launch {
            while (_breathingState.value.isRunning) {
                val mode = _breathingState.value.mode
                // INHALE
                runPhase(BreathPhase.INHALE, mode.inhaleSec, isExpanding = true)
                // HOLD
                if (mode.holdSec > 0) {
                    runPhase(BreathPhase.HOLD, mode.holdSec, isExpanding = false, keepFull = true)
                }
                // EXHALE
                runPhase(BreathPhase.EXHALE, mode.exhaleSec, isExpanding = false, keepFull = false)
                // PAUSE
                if (mode.pauseSec > 0) {
                    runPhase(BreathPhase.PAUSE, mode.pauseSec, isExpanding = false, keepEmpty = true)
                }
                _breathingState.value = _breathingState.value.copy(
                    completedCycles = _breathingState.value.completedCycles + 1
                )
            }
        }
    }

    private suspend fun runPhase(phase: BreathPhase, totalSec: Int, isExpanding: Boolean = false, keepFull: Boolean = false, keepEmpty: Boolean = false) {
        val totalMs = totalSec * 1000L
        val intervalMs = 50L
        val steps = (totalMs / intervalMs).toInt()

        for (i in 0..steps) {
            if (!_breathingState.value.isRunning) break
            val fraction = i.toFloat() / steps
            val currentProgress = when {
                keepFull -> 1f
                keepEmpty -> 0f
                isExpanding -> fraction
                else -> 1f - fraction
            }
            val secondsRemaining = ((steps - i) * intervalMs / 1000L).toInt() + 1

            _breathingState.value = _breathingState.value.copy(
                currentPhase = phase,
                secondsLeftInPhase = secondsRemaining.coerceAtLeast(1),
                progress = currentProgress
            )
            delay(intervalMs)
        }
    }

    fun stopBreathing() {
        breathingJob?.cancel()
        _breathingState.value = _breathingState.value.copy(isRunning = false, progress = 0f)
    }

    // --- Grounding Logic ---
    fun toggleGroundingItem(itemKey: String) {
        val currentSet = _groundingState.value.checkedItems.toMutableSet()
        if (currentSet.contains(itemKey)) {
            currentSet.remove(itemKey)
        } else {
            currentSet.add(itemKey)
        }
        _groundingState.value = _groundingState.value.copy(checkedItems = currentSet)
    }

    fun setGroundingStep(stepIndex: Int) {
        _groundingState.value = _groundingState.value.copy(
            currentStepIndex = stepIndex.coerceIn(0, 4)
        )
    }

    fun resetGrounding() {
        _groundingState.value = GroundingState()
    }

    // --- CBT Reframing Save ---
    fun saveCbtReframing(
        thought: String,
        facts: String,
        interpretation: String,
        controllable: String,
        uncontrollable: String,
        smallStep: String,
        anxietyBefore: Int,
        anxietyAfter: Int,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveReframing(
                CbtReframingEntity(
                    situationOrThought = thought,
                    observableFacts = facts,
                    fearfulInterpretation = interpretation,
                    controllableAspect = controllable,
                    uncontrollableAspect = uncontrollable,
                    smallNextStep = smallStep,
                    anxietyBefore = anxietyBefore,
                    anxietyAfter = anxietyAfter
                )
            )
            onSuccess()
        }
    }

    fun deleteReframing(id: Long) {
        viewModelScope.launch {
            repository.deleteReframing(id)
        }
    }

    // --- Mood Check-In Save ---
    fun saveMoodCheckIn(
        anxietyLevel: Int,
        dominantEmotion: String,
        note: String,
        tookBreathing: Boolean,
        tookGrounding: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            repository.saveMoodCheckIn(
                MoodCheckInEntity(
                    anxietyLevel = anxietyLevel,
                    dominantEmotion = dominantEmotion,
                    note = note,
                    tookBreathing = tookBreathing,
                    tookGrounding = tookGrounding
                )
            )
            onSuccess()
        }
    }

    fun deleteMoodCheckIn(id: Long) {
        viewModelScope.launch {
            repository.deleteMoodCheckIn(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopBreathing()
    }
}
