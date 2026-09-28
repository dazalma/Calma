package com.example.data.repository

import com.example.BuildConfig
import com.example.data.local.CalmaDao
import com.example.data.local.ChatMessageEntity
import com.example.data.local.CbtReframingEntity
import com.example.data.local.MoodCheckInEntity
import com.example.data.network.GeminiApiService
import com.example.data.network.GeminiContent
import com.example.data.network.GeminiGenerationConfig
import com.example.data.network.GeminiPart
import com.example.data.network.GeminiRequest
import com.example.data.network.GeminiSystemInstruction
import com.example.data.network.NetworkClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class CalmaRepository(
    private val dao: CalmaDao,
    private val apiService: GeminiApiService = NetworkClient.geminiApi
) {
    val allMessages: Flow<List<ChatMessageEntity>> = dao.getAllMessages()
    val allCheckIns: Flow<List<MoodCheckInEntity>> = dao.getAllCheckIns()
    val allReframings: Flow<List<CbtReframingEntity>> = dao.getAllReframings()

    companion object {
        const val DISCLAIMER = "Nota: Este espacio es de apoyo psicoeducativo y no sustituye la terapia psicológica profesional."

        val SYSTEM_PROMPT = """
            Eres CALMA, un asistente digital de apoyo para el autocuidado emocional de personas adultas.
            Tu función es ayudar al usuario a atravesar momentos de estrés, preocupación, tensión o ansiedad mediante conversación breve, ejercicios de regulación y reflexión personal.
            CALMA NO es un médico, psicólogo ni terapeuta. No diagnosticas enfermedades, no realizas psicoterapia ni recetas medicamentos.
            
            Sigue estrictamente las siguientes reglas en cada respuesta:
            1. ROL Y TONO:
               - Adopta una personalidad empática, cálida, profesional y calmada.
               - Utiliza un lenguaje sencillo, claro y accesible para no abrumar al usuario. Usa el pronombre "tú".
               - Evita respuestas excesivamente largas. No bombardees al usuario. Una sola pregunta a la vez si es necesario.
            
            2. ALCANCE DE LAS RESPUESTAS:
               - Ofrece técnicas de respiración (ej. diafragmática o 4-7-8), ejercicios de enraizamiento (grounding 5-4-3-2-1), reframing cognitivo (cuestionar pensamientos catastróficos, separar hechos de preocupaciones) y hábitos saludables (sueño, pausas, ejercicio suave).
               - Mantén tus consejos breves y estructurados (usa viñetas o pasos cortos).
               - Cuando la persona esté muy alterada, prioriza la regulación corporal antes que la reflexión mental.
            
            3. LIMITACIONES Y SEGURIDAD (CRÍTICO):
               - No diagnostiques ningún trastorno clínico.
               - Si el usuario menciona autolesiones, ideación suicida, ataques de pánico severos que simulen emergencias médicas o crisis extremas, responde de inmediato con un mensaje prioritario de alerta pidiendo ayuda profesional urgente y facilitando contacto con servicios de emergencia locales.
               - Incluye SIEMPRE al final de tus respuestas el descargo fijo:
                 "Nota: Este espacio es de apoyo psicoeducativo y no sustituye la terapia psicológica profesional."
            
            4. FORMATO DE SALIDA FINAL DE CADA CONSEJO O INTERVENCIÓN:
               - [Un saludo empático o validación breve de la emoción]
               - [Explicación breve de por qué sucede eso en el cuerpo o la mente]
               - [Ejercicio práctico o consejo en 3 pasos sencillos]
               - "Nota: Este espacio es de apoyo psicoeducativo y no sustituye la terapia psicológica profesional."
        """.trimIndent()

        private val CRISIS_KEYWORDS = listOf(
            "suicid", "matarm", "quitarme la vida", "morirm", "no quiero vivir",
            "autolesi", "cortarm", "hacerme daño", "ahorcar", "pastillas para morir",
            "no aguanto mas vivir", "desaparecer para siempre"
        )
    }

    suspend fun saveUserMessage(text: String): ChatMessageEntity = withContext(Dispatchers.IO) {
        val isCrisis = checkCrisisIntent(text)
        val entity = ChatMessageEntity(
            sender = "user",
            content = text,
            isCrisis = isCrisis
        )
        val id = dao.insertMessage(entity)
        entity.copy(id = id)
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        dao.clearMessages()
    }

    suspend fun generateAssistantResponse(
        userMessage: String,
        recentHistory: List<ChatMessageEntity>
    ): ChatMessageEntity = withContext(Dispatchers.IO) {
        val isCrisis = checkCrisisIntent(userMessage)

        if (isCrisis) {
            val crisisResponse = """
                Percibo que estás atravesando un momento de dolor muy profundo e intenso, y tu vida y bienestar son lo más importante. 
                
                Por favor, busca ayuda profesional y apoyo humano inmediato ahora mismo:
                • Llama a emergencias (112 en España/Europa, 911 en América).
                • Líneas gratuitas de prevención de crisis:
                  - España: 024 o 717 003 717
                  - México: Línea de la Vida 800 911 2000
                  - Argentina: 135 o (011) 5275-1135
                  - Colombia: 106
                  - Chile: *4141
                  - EE. UU.: 988 (opción en español)
                • Si puedes, acércate al servicio de urgencias más cercano o comunícate de inmediato con un familiar o persona de total confianza.
                
                No tienes que transitar esto a solas. Hay profesionales preparados para escucharte y acompañarte en este momento.
                
                $DISCLAIMER
            """.trimIndent()

            val alertEntity = ChatMessageEntity(
                sender = "calma",
                content = crisisResponse,
                isCrisis = true
            )
            val id = dao.insertMessage(alertEntity)
            return@withContext alertEntity.copy(id = id)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val hasValidKey = apiKey.isNotEmpty() && apiKey != "MY_GEMINI_API_KEY"

        var responseText: String? = null

        if (hasValidKey) {
            try {
                val contentsList = mutableListOf<GeminiContent>()
                // Add last 6 messages for context
                recentHistory.takeLast(6).forEach { msg ->
                    val role = if (msg.sender == "user") "user" else "model"
                    contentsList.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(text = msg.content))
                        )
                    )
                }
                // Add current message
                contentsList.add(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = userMessage))
                    )
                )

                val request = GeminiRequest(
                    contents = contentsList,
                    systemInstruction = GeminiSystemInstruction(
                        parts = listOf(GeminiPart(text = SYSTEM_PROMPT))
                    ),
                    generationConfig = GeminiGenerationConfig(
                        temperature = 0.5f,
                        topP = 0.95f
                    )
                )

                val response = apiService.generateContent(apiKey, request)
                val candidateText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!candidateText.isNullOrBlank()) {
                    responseText = candidateText
                }
            } catch (e: Exception) {
                // Graceful fallback to offline psychological support logic
                responseText = null
            }
        }

        // If API wasn't configured, failed, or returned empty, provide thoughtful structured response
        val finalContent = responseText ?: getStructuredOfflineGuidance(userMessage)

        val assistantEntity = ChatMessageEntity(
            sender = "calma",
            content = finalContent,
            isCrisis = false
        )
        val id = dao.insertMessage(assistantEntity)
        assistantEntity.copy(id = id)
    }

    private fun checkCrisisIntent(text: String): Boolean {
        val lower = text.lowercase()
        return CRISIS_KEYWORDS.any { lower.contains(it) }
    }

    private fun getStructuredOfflineGuidance(input: String): String {
        val lower = input.lowercase()

        return when {
            lower.contains("pecho") || lower.contains("respir") || lower.contains("ahogo") || lower.contains("aire") -> {
                """
                Hola. Es completamente comprensible que sientas inquietud ante esa sensación en tu respiración; quiero que sepas que estás a salvo en este instante.

                Cuando el cuerpo percibe estrés, el sistema nervioso simpático activa la respiración superficial y acelera el ritmo cardíaco para protegernos, lo que paradójicamente produce esa falsa sensación de falta de aire.

                Prueba este ejercicio de Respiración 4-7-8 en 3 pasos:
                1. Suelta todo el aire por la boca lentamente.
                2. Inhala suavemente por la nariz contando en silencio 1, 2, 3, 4.
                3. Retén el aire durante 7 segundos y luego exhala despacio por la boca durante 8 segundos. Repítelo 3 ciclos a tu ritmo.

                $DISCLAIMER
                """.trimIndent()
            }
            lower.contains("dormir") || lower.contains("sueño") || lower.contains("noche") || lower.contains("insomnio") -> {
                """
                Hola. Comprendo lo agotador que es cuando el cuerpo necesita descansar pero la mente continúa activa.

                Al llegar la noche y apagarse los estímulos externos, el cerebro aprovecha el silencio para revisar preocupaciones pendientes, aumentando el estado de alerta cerebral.

                Practiquemos este ritual de desactivación nocturna en 3 pasos:
                1. Descarga mental: Anota en un papel físico una o dos cosas que te preocupen para decirte "está anotado, mañana me ocupo de ello".
                2. Desconexión sensorial: Aleja la pantalla de tu vista y baja la intensidad de la luz ambiental.
                3. Pausa corporal: Al recostarte, siente el peso de tu cuerpo apoyado firmemente sobre el colchón y afloja los hombros y la mandíbula.

                $DISCLAIMER
                """.trimIndent()
            }
            lower.contains("pensamiento") || lower.contains("cabeza") || lower.contains("bucle") || lower.contains("peor") || lower.contains("miedo") -> {
                """
                Hola. Te escucho y valido lo abrumador que resulta sentir que la mente no se detiene y anticipa escenarios difíciles.

                Nuestra mente evolutiva está diseñada para anticipar amenazas y protegernos; sin embargo, un pensamiento es solo una hipótesis mental, no una predicción certera de la realidad.

                Hagamos este ejercicio de reframing cognitivo en 3 pasos:
                1. Nombra el pensamiento: Di para tus adentros "Mi mente me está contando la historia de que todo saldrá mal".
                2. Separa hechos de temores: Pregúntate "¿Qué evidencia objetiva y real tengo ahora mismo de que esto vaya a ocurrir?".
                3. Próxima pequeña acción: Identifica un solo aspecto que sí esté en tus manos en los próximos 10 minutos y enfócate en él.

                $DISCLAIMER
                """.trimIndent()
            }
            lower.contains("tension") || lower.contains("cuello") || lower.contains("hombros") || lower.contains("cuerpo") -> {
                """
                Hola. Reconozco lo incómoda y desgastante que es la tensión física acumulada en tu cuerpo tras momentos de tensión.

                El cuerpo retiene la tensión muscular como una respuesta refleja de defensa; cuando el estrés se mantiene, los trapecios y la mandíbula suelen contracturarse de forma automática.

                Realiza esta pausa de relajación progresiva en 3 pasos:
                1. Aprieta suavemente los hombros subiéndolos hacia tus orejas durante 3 segundos.
                2. Suéltalos de golpe dejando que caigan con pesadez, notando la diferencia de alivio.
                3. Separa conscientemente los dientes, permitiendo que la mandíbula descanse floja y la lengua baje del paladar.

                $DISCLAIMER
                """.trimIndent()
            }
            else -> {
                """
                Hola. Valoro mucho que te des este espacio para hacer una pausa; lo que estás experimentando es válido y no tienes que resolverlo todo de inmediato.

                En momentos de ansiedad o sobrecarga, el cerebro activa un estado de alerta que nos hace sentir que todo urge, nublando nuestra capacidad de ver con serenidad el presente.

                Hagamos este ejercicio de anclaje (Grounding) en 3 pasos sencillos:
                1. Mira a tu alrededor e identifica con calma 3 objetos que sean de color azul o verde.
                2. Apoya con firmeza ambos pies en el suelo y siente el contacto sólido de la superficie sosteniéndote.
                3. Haz una inhalación profunda por la nariz y exhala el doble de lento por la boca.

                $DISCLAIMER
                """.trimIndent()
            }
        }
    }

    // Check-ins
    suspend fun saveMoodCheckIn(checkIn: MoodCheckInEntity) = withContext(Dispatchers.IO) {
        dao.insertCheckIn(checkIn)
    }

    suspend fun deleteMoodCheckIn(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteCheckIn(id)
    }

    // Reframings
    suspend fun saveReframing(reframing: CbtReframingEntity) = withContext(Dispatchers.IO) {
        dao.insertReframing(reframing)
    }

    suspend fun deleteReframing(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteReframing(id)
    }
}
