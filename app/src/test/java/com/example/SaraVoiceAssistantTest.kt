package com.example

import com.example.data.ai.GeminiAssistantService
import com.example.data.ai.MayaKnowledgeBase
import com.example.data.audio.SaraVoiceEngine
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale

class SaraVoiceAssistantTest {

    @Test
    fun testSaraVoiceAssistantIdentityAndBranding() {
        assertEquals("Sara voiceassistant", MayaKnowledgeBase.ASSISTANT_NAME)
        assertEquals("Sara", MayaKnowledgeBase.SHORT_ASSISTANT_NAME)
        assertTrue(MayaKnowledgeBase.MASTER_SYSTEM_INSTRUCTION.contains("Sara voiceassistant"))
    }

    @Test
    fun testBengaliHindiEnglishLanguageMatchingAndPronunciation() {
        // 1. Bengali identity & greeting
        val bnReply = MayaKnowledgeBase.findLocalKnowledgeAnswer("তুমি কে?")
        assertNotNull(bnReply)
        assertTrue(bnReply!!.contains("Sara voiceassistant"))
        assertTrue(MayaKnowledgeBase.isBengaliQuery(bnReply))

        // 2. Hindi identity & greeting
        val hiReply = MayaKnowledgeBase.findLocalKnowledgeAnswer("तुम्हारा नाम क्या है?")
        assertNotNull(hiReply)
        assertTrue(hiReply!!.contains("Sara voiceassistant"))
        assertTrue(MayaKnowledgeBase.isHindiQuery(hiReply))

        // 3. English identity & greeting
        val enReply = MayaKnowledgeBase.findLocalKnowledgeAnswer("Who are you?")
        assertNotNull(enReply)
        assertTrue(enReply!!.contains("Sara voiceassistant"))

        // 4. Automatic language switch command
        val switchToHindi = MayaKnowledgeBase.findLocalKnowledgeAnswer("হিন্দিতে কথা বলো")
        assertNotNull(switchToHindi)
        assertTrue(MayaKnowledgeBase.isHindiQuery(switchToHindi!!))
        assertEquals("hi", SaraVoiceEngine.detectLanguageLocale("হিন্দিতে কথা বলো").language)
        assertEquals("bn", SaraVoiceEngine.detectLanguageLocale("বাংলায় কথা বলো").language)
        assertEquals(Locale.US, SaraVoiceEngine.detectLanguageLocale("Speak in English"))
    }

    @Test
    fun testNaturalSpeechSanitizerAndPreloadCacheNaming() {
        val raw = "**নমস্কার!** আমি *Sara voiceassistant* — আপনার #AI সহায়ক।"
        val clean = SaraVoiceEngine.sanitizeForNaturalSpeech(raw)
        assertTrue(!clean.contains("**"))
        assertTrue(!clean.contains("#"))
        assertTrue(clean.contains("নমস্কার!"))
        assertTrue(clean.contains("Sara voiceassistant"))

        val cacheFile1 = SaraVoiceEngine.buildCacheFileName("msg_1", clean)
        val cacheFile2 = SaraVoiceEngine.buildCacheFileName("msg_1", clean)
        assertEquals(cacheFile1, cacheFile2)
        assertTrue(cacheFile1.startsWith("sara_voice_"))
        assertTrue(cacheFile1.endsWith(".wav"))
    }

    @Test
    fun testFastSubTwoSecondAnalysisAndResponse() = runBlocking {
        val startMs = System.currentTimeMillis()
        val result = GeminiAssistantService.generateAnswer("তুমি কে?", fastMode = true)
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(result.isSuccess)
        assertTrue("Response should be delivered within 2 seconds, took ${elapsedMs}ms", elapsedMs < 2000L)
        assertTrue(result.getOrThrow().contains("Sara voiceassistant"))
    }

    @Test
    fun testSevenSecondTimeoutEnforcement() = runBlocking {
        assertEquals(7_000L, GeminiAssistantService.VOICE_SESSION_TIMEOUT_MS)

        // Verify that a slow voice generation task exceeding timeout is automatically cancelled
        val outcome = GeminiAssistantService.generateVoiceAnswerWithTimeout(
            userPrompt = "Slow query simulation",
            timeoutMs = 150L
        ) {
            delay(1000L)
            Result.success("Too late")
        }

        assertTrue(
            "Expected VoiceGenerationOutcome.Timeout when generation exceeds timeout",
            outcome is GeminiAssistantService.VoiceGenerationOutcome.Timeout
        )
    }

    @Test
    fun testSpeculativePreAnalysisWhileUserSpeakingAndInstantRetrieval() = runBlocking {
        val partialSpeech = "আমার কথা শুনতে পাচ্ছো"
        val preGenerated = GeminiAssistantService.preGenerateSpeculativeReply(partialSpeech)
        assertNotNull(preGenerated)
        assertTrue(preGenerated!!.isNotBlank())

        val startMs = System.currentTimeMillis()
        val cachedOutcome = GeminiAssistantService.generateVoiceAnswerWithTimeout(partialSpeech)
        val elapsedMs = System.currentTimeMillis() - startMs

        assertTrue(cachedOutcome is GeminiAssistantService.VoiceGenerationOutcome.Success)
        val success = cachedOutcome as GeminiAssistantService.VoiceGenerationOutcome.Success
        assertEquals(preGenerated, success.replyText)
        assertTrue("Cached speculative response should be instant (<200ms), took ${elapsedMs}ms", elapsedMs < 200L)
    }
}
