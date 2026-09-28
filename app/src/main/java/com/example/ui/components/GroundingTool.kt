package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.GroundingState

data class GroundingStepDef(
    val count: Int,
    val senseName: String,
    val prompt: String,
    val icon: ImageVector,
    val suggestions: List<String>
)

val GROUNDING_STEPS = listOf(
    GroundingStepDef(
        count = 5,
        senseName = "Vista",
        prompt = "Mira a tu alrededor sin prisa y encuentra 5 cosas que puedas ver con claridad:",
        icon = Icons.Default.Visibility,
        suggestions = listOf(
            "Un objeto de color verde o azul",
            "Un reflejo o juego de luz",
            "La textura o borde de un mueble",
            "Una planta, ventana o cuadro",
            "Un detalle pequeño que antes no habías notado"
        )
    ),
    GroundingStepDef(
        count = 4,
        senseName = "Tacto",
        prompt = "Nota 4 sensaciones físicas o texturas que puedas tocar en este instante:",
        icon = Icons.Default.PanTool,
        suggestions = listOf(
            "El peso de tu cuerpo apoyado en el asiento",
            "La textura de la tela de tu ropa con las yemas de tus dedos",
            "La temperatura fresca o cálida del aire en tus manos",
            "La firmeza del suelo bajo tus pies descalzos o calzados"
        )
    ),
    GroundingStepDef(
        count = 3,
        senseName = "Oído",
        prompt = "Cierra los ojos un segundo y distingue 3 sonidos diferentes a tu alrededor:",
        icon = Icons.Default.Hearing,
        suggestions = listOf(
            "Un sonido lejano (tráfico exterior, viento o pájaros)",
            "Un sonido cercano (el zumbido de un ventilador o la nevera)",
            "El sonido sutil de tu propia respiración"
        )
    ),
    GroundingStepDef(
        count = 2,
        senseName = "Olfato",
        prompt = "Inhala suavemente e identifica 2 aromas presentes en el ambiente:",
        icon = Icons.Default.WaterDrop,
        suggestions = listOf(
            "El aroma de tu ropa recién lavada o jabón de manos",
            "El olor del ambiente, café, madera o infusión"
        )
    ),
    GroundingStepDef(
        count = 1,
        senseName = "Gusto",
        prompt = "Enfoca tu atención en 1 sensación o sabor en tu boca:",
        icon = Icons.Default.Restaurant,
        suggestions = listOf(
            "Traga saliva despacio y siente el movimiento de tu garganta, o el sabor residual de agua fresca"
        )
    )
)

@Composable
fun GroundingTool(
    state: GroundingState,
    onToggleItem: (String) -> Unit,
    onSetStep: (Int) -> Unit,
    onReset: () -> Unit
) {
    val step = GROUNDING_STEPS[state.currentStepIndex]
    val totalItems = GROUNDING_STEPS.sumOf { it.suggestions.size }
    val checkedCount = state.checkedItems.size
    val overallProgress = checkedCount.toFloat() / totalItems.coerceAtLeast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("grounding_tool_root")
    ) {
        // Step progress header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Técnica de Enraizamiento 5-4-3-2-1",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Paso ${state.currentStepIndex + 1} de 5: Conectar con tus sentidos",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = step.senseName,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LinearProgressIndicator(
            progress = { overallProgress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Step tabs selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GROUNDING_STEPS.forEachIndexed { idx, s ->
                val isSelected = state.currentStepIndex == idx
                val allStepItemsChecked = s.suggestions.all { state.checkedItems.contains("${idx}_$it") }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                allStepItemsChecked -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                        .clickable { onSetStep(idx) },
                    contentAlignment = Alignment.Center
                ) {
                    if (allStepItemsChecked && !isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completado",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    } else {
                        Text(
                            text = "${s.count}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompt Card
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${step.count} cosas para el sentido de la ${step.senseName.uppercase()}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = step.prompt,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                step.suggestions.forEachIndexed { sIdx, suggestion ->
                    val key = "${state.currentStepIndex}_$suggestion"
                    val isChecked = state.checkedItems.contains(key)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                else Color.Transparent
                            )
                            .clickable { onToggleItem(key) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isChecked) "Marcado" else "Sin marcar",
                            tint = if (isChecked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isChecked) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Navigation between steps
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state.currentStepIndex > 0) {
                OutlinedButton(
                    onClick = { onSetStep(state.currentStepIndex - 1) },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text("Anterior")
                }
            } else {
                Spacer(modifier = Modifier.width(8.dp))
            }

            if (state.currentStepIndex < GROUNDING_STEPS.size - 1) {
                Button(
                    onClick = { onSetStep(state.currentStepIndex + 1) },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Siguiente paso")
                }
            } else {
                Button(
                    onClick = onReset,
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reiniciar ejercicio")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Psychoeducational rationale
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
                    text = "El grounding reconduce la actividad desde la amígdala (alerta y catastrofismo) hacia la corteza sensorial y prefrontal, trayendo tu mente de vuelta a la seguridad del presente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
