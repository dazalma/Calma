package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.BreathingMode
import com.example.ui.BreathingState
import com.example.ui.GroundingState
import com.example.ui.components.BreathingPacer
import com.example.ui.components.GroundingTool

@Composable
fun RegulationScreen(
    breathingState: BreathingState,
    groundingState: GroundingState,
    onSelectBreathingMode: (BreathingMode) -> Unit,
    onToggleBreathing: () -> Unit,
    onResetBreathing: () -> Unit,
    onToggleGroundingItem: (String) -> Unit,
    onSetGroundingStep: (Int) -> Unit,
    onResetGrounding: () -> Unit
) {
    var selectedSubTab by remember { mutableIntStateOf(0) }
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TabRow(
            selectedTabIndex = selectedSubTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                text = { Text("Respiración") },
                icon = { Icon(imageVector = Icons.Default.Air, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_sub_breathing")
            )
            Tab(
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                text = { Text("Grounding") },
                icon = { Icon(imageVector = Icons.Default.PanTool, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_sub_grounding")
            )
            Tab(
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                text = { Text("Pausa Corporal") },
                icon = { Icon(imageVector = Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("tab_sub_body")
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            when (selectedSubTab) {
                0 -> {
                    BreathingPacer(
                        state = breathingState,
                        onSelectMode = onSelectBreathingMode,
                        onToggleBreathing = onToggleBreathing,
                        onReset = onResetBreathing
                    )
                }
                1 -> {
                    GroundingTool(
                        state = groundingState,
                        onToggleItem = onToggleGroundingItem,
                        onSetStep = onSetGroundingStep,
                        onReset = onResetGrounding
                    )
                }
                2 -> {
                    BodyScanPmrGuide()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Fixed disclaimer
            Text(
                text = "Nota: Este espacio es de apoyo psicoeducativo y no sustituye la terapia psicológica profesional.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun BodyScanPmrGuide() {
    val pmrSteps = listOf(
        Pair(
            "1. Hombros y Trapecios",
            "Sube ambos hombros suavemente hacia tus orejas generando una ligera tensión durante 4 segundos. Luego, suéltalos de golpe exhalando por la boca y nota cómo se hunden agradablemente."
        ),
        Pair(
            "2. Mandíbula y Rostro",
            "La tensión suele acumularse en el bruxismo inconsciente. Separa conscientemente tus muelas superiores de las inferiores, deja caer la mandíbula floja y despega la lengua del paladar."
        ),
        Pair(
            "3. Manos y Brazos",
            "Cierra tus puños con un 50% de fuerza, mantén 3 segundos y ábrelos despacio, apoyando las palmas sobre tus muslos. Siente el calor y hormigueo de la circulación recuperándose."
        ),
        Pair(
            "4. Abdomen y Apoyo",
            "Permite que tu barriga se infle sin apretar el pantalón. Siente la superficie de la silla o el suelo soportando todo tu peso. No tienes que sostener nada ahora mismo."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("body_scan_guide")
    ) {
        Text(
            text = "Pausa Corporal y Desactivación Muscular",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = "Libera la tensión física acumulada en 4 zonas clave",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        pmrSteps.forEachIndexed { index, item ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.first,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 19.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "La relajación muscular progresiva ayuda a cortar el bucle neurofisiológico: al relajar conscientemente los músculos, el cerebro interpreta que no hay peligro inminente y reduce la liberación de adrenalina.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
