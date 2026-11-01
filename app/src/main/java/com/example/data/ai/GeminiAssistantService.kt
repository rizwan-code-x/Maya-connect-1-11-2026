package com.example.data.ai

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Real-time Google Gemini AI Service powering Sara voiceassistant & AI Chat.
 * Optimized for ultra-fast (<2s) analysis, multilingual accuracy (Bengali, Hindi, English),
 * and strict 7-second voice session timeout enforcement.
 */
object GeminiAssistantService {

    private const val MODEL_NAME = "gemini-2.5-flash"
    const val VOICE_SESSION_TIMEOUT_MS = 7_000L
    const val FAST_CHAT_TIMEOUT_MS = 1_500L

    // Thread-safe cache for speculative background pre-analysis while user is speaking
    private val speculativeReplyCache = java.util.concurrent.ConcurrentHashMap<String, String>()

    sealed class VoiceGenerationOutcome {
        data class Success(val replyText: String, val latencyMs: Long) : VoiceGenerationOutcome()
        data class Timeout(val elapsedMs: Long) : VoiceGenerationOutcome()
        data class Error(val message: String) : VoiceGenerationOutcome()
    }

    private fun normalizeCacheKey(prompt: String): String {
        return prompt.trim().lowercase().replace(Regex("\\s+"), " ")
    }

    /**
     * Speculatively pre-analyzes and generates the answer in the background WHILE the user is still speaking,
     * so that the moment the user stops speaking, the answer and voice audio are already ready (<2s / near-0ms).
     */
    suspend fun preGenerateSpeculativeReply(partialTranscript: String): String? = withContext(Dispatchers.IO) {
        val key = normalizeCacheKey(partialTranscript)
        if (key.length < 2) return@withContext null

        speculativeReplyCache[key]?.let { return@withContext it }

        val result = generateAnswer(
            userPrompt = partialTranscript,
            systemContext = MayaKnowledgeBase.MASTER_SYSTEM_INSTRUCTION,
            fastMode = true
        )
        val reply = result.getOrNull()
        if (!reply.isNullOrBlank()) {
            speculativeReplyCache[key] = reply
        }
        reply
    }

    fun getCachedSpeculativeReply(transcript: String): String? {
        val key = normalizeCacheKey(transcript)
        if (key.isEmpty()) return null
        return speculativeReplyCache[key]
    }

    /**
     * Generates a voice assistant response enforcing the strict 7-second (7,000 ms) timeout,
     * while utilizing speculative pre-generation to deliver replies in under 2 seconds.
     */
    suspend fun generateVoiceAnswerWithTimeout(
        userPrompt: String,
        timeoutMs: Long = VOICE_SESSION_TIMEOUT_MS,
        customTask: (suspend () -> Result<String>)? = null
    ): VoiceGenerationOutcome {
        val startMs = System.currentTimeMillis()
        return try {
            withTimeout(timeoutMs) {
                if (customTask == null) {
                    val cached = getCachedSpeculativeReply(userPrompt)
                    if (!cached.isNullOrBlank()) {
                        val elapsed = (System.currentTimeMillis() - startMs).coerceAtLeast(15L)
                        return@withTimeout VoiceGenerationOutcome.Success(cached, elapsed)
                    }
                }
                val result = customTask?.invoke() ?: generateAnswer(
                    userPrompt = userPrompt,
                    systemContext = MayaKnowledgeBase.MASTER_SYSTEM_INSTRUCTION,
                    fastMode = true
                )
                val elapsed = System.currentTimeMillis() - startMs
                result.fold(
                    onSuccess = { reply ->
                        val key = normalizeCacheKey(userPrompt)
                        if (key.isNotEmpty()) {
                            speculativeReplyCache[key] = reply
                        }
                        VoiceGenerationOutcome.Success(reply, elapsed)
                    },
                    onFailure = { err -> VoiceGenerationOutcome.Error(err.message ?: "Voice response error") }
                )
            }
        } catch (_: TimeoutCancellationException) {
            val elapsed = System.currentTimeMillis() - startMs
            VoiceGenerationOutcome.Timeout(elapsed)
        } catch (e: Exception) {
            VoiceGenerationOutcome.Error(e.message ?: "Unexpected voice error")
        }
    }

    suspend fun generateAnswer(
        userPrompt: String,
        systemContext: String = MayaKnowledgeBase.MASTER_SYSTEM_INSTRUCTION,
        fastMode: Boolean = true
    ): Result<String> = withContext(Dispatchers.IO) {
        val trimmed = userPrompt.trim()
        if (trimmed.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("Please speak or enter a question."))
        }

        val cacheKey = normalizeCacheKey(trimmed)
        speculativeReplyCache[cacheKey]?.let { cached ->
            return@withContext Result.success(cached)
        }

        // 1. Instant Local Knowledge & Face-to-Face Conversational Match (<20ms latency)
        val localMatch = MayaKnowledgeBase.findLocalKnowledgeAnswer(trimmed)
        if (localMatch != null) {
            speculativeReplyCache[cacheKey] = localMatch
            return@withContext Result.success(localMatch)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            val fallback = MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)
            speculativeReplyCache[cacheKey] = fallback
            return@withContext Result.success(fallback)
        }

        try {
            val endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connectTimeout = if (fastMode) 950 else 3500
                readTimeout = if (fastMode) 1250 else 4500
                doOutput = true
            }

            val payload = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemContext))
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().put("text", trimmed))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.65)
                    put("maxOutputTokens", 140)
                })
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = BufferedReader(InputStreamReader(responseStream, Charsets.UTF_8)).use {
                it.readText()
            }

            if (statusCode in 200..299) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val replyText = parts.getJSONObject(0).optString("text", "").trim()
                        if (replyText.isNotEmpty()) {
                            speculativeReplyCache[cacheKey] = replyText
                            return@withContext Result.success(replyText)
                        }
                    }
                }
                val fallback = MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)
                speculativeReplyCache[cacheKey] = fallback
                Result.success(fallback)
            } else {
                val fallback = MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)
                speculativeReplyCache[cacheKey] = fallback
                Result.success(fallback)
            }
        } catch (e: Exception) {
            val fallback = MayaKnowledgeBase.generateSmartCustomerCareFallback(trimmed)
            speculativeReplyCache[cacheKey] = fallback
            Result.success(fallback)
        }
    }

    /**
     * Multimodal Audio Input directly to Gemini API:
     * Sends raw 16kHz PCM WAV audio captured from microphone so Gemini natively hears
     * Bengali, English, Hindi, Urdu, and Arabic speech and responds in under 7 seconds.
     */
    suspend fun generateMultimodalAudioAnswer(
        base64WavAudio: String,
        transcriptHint: String = "",
        systemContext: String = MayaKnowledgeBase.MASTER_SYSTEM_INSTRUCTION
    ): Result<Pair<String, String>> = withContext(Dispatchers.IO) {
        val hintTrimmed = transcriptHint.trim()
        if (hintTrimmed.isNotEmpty()) {
            val localMatch = MayaKnowledgeBase.findLocalKnowledgeAnswer(hintTrimmed)
            if (localMatch != null) {
                return@withContext Result.success(Pair(hintTrimmed, localMatch))
            }
        }

        val apiKey = BuildConfig.GEMINI_API_KEY.trim()
        if (apiKey.isEmpty()) {
            if (hintTrimmed.isNotEmpty()) {
                val fallback = MayaKnowledgeBase.generateSmartCustomerCareFallback(hintTrimmed)
                return@withContext Result.success(Pair(hintTrimmed, fallback))
            }
            return@withContext Result.failure(
                IllegalStateException("GEMINI_API_KEY is not configured. Add GEMINI_API_KEY in AI Studio Secrets.")
            )
        }

        try {
            val endpoint =
                "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent?key=$apiKey"
            val url = URL(endpoint)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                connectTimeout = 2500
                readTimeout = 4000
                doOutput = true
            }

            val promptInstruction = if (hintTrimmed.isNotEmpty()) {
                "Listen carefully to this spoken user audio (speech recognizer hint: \"$hintTrimmed\"). Reply naturally as Sara voiceassistant in the exact language spoken (1-3 concise sentences)."
            } else {
                "Listen carefully to this spoken user audio. Transcribe what the user said on the first line prefixed with 'TRANSCRIPT:', and on the next line provide your concise spoken reply as Sara voiceassistant in the exact language the user spoke."
            }

            val payload = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().put("text", systemContext))
                    })
                })
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "user")
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("inlineData", JSONObject().apply {
                                    put("mimeType", "audio/wav")
                                    put("data", base64WavAudio)
                                })
                            })
                            put(JSONObject().put("text", promptInstruction))
                        })
                    })
                })
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.65)
                    put("maxOutputTokens", 220)
                })
            }

            OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                writer.write(payload.toString())
                writer.flush()
            }

            val statusCode = connection.responseCode
            val responseStream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseBody = BufferedReader(InputStreamReader(responseStream, Charsets.UTF_8)).use {
                it.readText()
            }

            if (statusCode in 200..299) {
                val json = JSONObject(responseBody)
                val candidates = json.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val content = candidates.getJSONObject(0).optJSONObject("content")
                    val parts = content?.optJSONArray("parts")
                    if (parts != null && parts.length() > 0) {
                        val rawReply = parts.getJSONObject(0).optString("text", "").trim()
                        if (rawReply.isNotEmpty()) {
                            val lines = rawReply.lines()
                            if (lines.first().startsWith("TRANSCRIPT:", ignoreCase = true)) {
                                val extractedTranscript = lines.first().substringAfter(":").trim()
                                val spokenAnswer = lines.drop(1).joinToString("\n").trim().ifEmpty { rawReply }
                                return@withContext Result.success(Pair(extractedTranscript, spokenAnswer))
                            }
                            return@withContext Result.success(Pair(hintTrimmed.ifEmpty { "Voice Input" }, rawReply))
                        }
                    }
                }
            }

            if (hintTrimmed.isNotEmpty()) {
                return@withContext generateAnswer(hintTrimmed, systemContext, fastMode = true).map { Pair(hintTrimmed, it) }
            }
            Result.failure( IllegalStateException("Could not hear audio clearly. Please speak again."))
        } catch (e: Exception) {
            if (hintTrimmed.isNotEmpty()) {
                return@withContext generateAnswer(hintTrimmed, systemContext, fastMode = true).map { Pair(hintTrimmed, it) }
            }
            Result.failure(e)
        }
    }
}
