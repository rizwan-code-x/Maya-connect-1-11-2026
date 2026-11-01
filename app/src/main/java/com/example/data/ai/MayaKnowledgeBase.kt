package com.example.data.ai

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Sara voiceassistant — Knowledge Base & Multilingual Customer-Care Conversational Architecture.
 * Powers real-time Bengali, Hindi, English, Urdu & Arabic voice and chat assistance.
 */
object MayaKnowledgeBase {

    // 1. Assistant Identity
    const val ASSISTANT_NAME = "Sara voiceassistant"
    const val SHORT_ASSISTANT_NAME = "Sara"
    const val ECOSYSTEM_NAME = "Maya Connect & REXLIFY Ecosystem"

    // 2. Developer Profile (Strict truth, zero hallucination)
    object DeveloperProfile {
        const val FULL_NAME = "Rizwan Roushan Rownaq"
        const val SHORT_NAME = "Rizwan / Rizwan RR"
        const val ROLE = "Creator / Developer / Founder of the Maya & REXLIFY ecosystem"
        const val PRIMARY_INTERESTS = "AI, software development, web development, app development, UI/UX, digital tools, cybersecurity, automation and technology"
    }

    // 3. Ecosystem Projects Configuration
    data class ProjectInfo(
        val name: String,
        val shortName: String,
        val url: String,
        val tagline: String,
        val purposeEn: String,
        val purposeBn: String,
        val purposeHi: String = purposeEn
    )

    val REXLIFY_PROJECT = ProjectInfo(
        name = "REXLIFY",
        shortName = "REXLIFY",
        url = "https://rexlify.netlify.app/",
        tagline = "Rizwan's personal digital application ecosystem / student technology project",
        purposeEn = "REXLIFY is Rizwan's personal digital application ecosystem and student technology project. It unifies AI tools, productivity apps, and digital utilities in one platform.",
        purposeBn = "REXLIFY হলো রিজওয়ানের ব্যক্তিগত ডিজিটাল অ্যাপ্লিকেশন ইকোসিস্টেম এবং স্টুডেন্ট টেকনোলজি প্রজেক্ট। এর মূল উদ্দেশ্য হলো বিভিন্ন অ্যাপ্লিকেশন এবং AI টুলসকে একটি সমন্বিত প্ল্যাটফর্মে রাখা।",
        purposeHi = "REXLIFY रिज़वान का व्यक्तिगत डिजिटल एप्लिकेशन इकोसिस्टम और टेक्नोलॉजी प्रोजेक्ट है, जो AI टूल्स और डिजिटल ऐप्स को एक प्लेटफॉर्म पर जोड़ता है।"
    )

    val NEURAX_PROJECT = ProjectInfo(
        name = "NeuraX",
        shortName = "NeuraX",
        url = "https://neurax.ai.studio/",
        tagline = "AI-powered analyzer and daily-life helper application created by Rizwan",
        purposeEn = "NeuraX is an AI-powered analyzer and daily-life helper application created by Rizwan.",
        purposeBn = "NeuraX হলো রিজওয়ানের তৈরি একটি AI-চালিত অ্যানালাইজার এবং দৈনন্দিন জীবনের সহায়ক অ্যাপ্লিকেশন।",
        purposeHi = "NeuraX रिज़वान द्वारा बनाया गया एक AI-संचालित एनालाइज़र और दैनिक जीवन सहायक एप्लिकेशन है।"
    )

    val NOORAX_PROJECT = ProjectInfo(
        name = "NooraX",
        shortName = "NooraX",
        url = "https://noorax.ai.studio/",
        tagline = "Dedicated digital application created by Rizwan",
        purposeEn = "NooraX is a dedicated digital application project created by Rizwan within the ecosystem.",
        purposeBn = "NooraX হলো ইকোসিস্টেমের একটি স্বতন্ত্র ডিজিটাল অ্যাপ্লিকেশন প্রজেক্ট যা রিজওয়ান তৈরি করেছেন।",
        purposeHi = "NooraX इस इकोसिस्टम के भीतर रिज़वान द्वारा बनाया गया एक स्वतंत्र डिजिटल एप्लिकेशन प्रोजेक्ट है।"
    )

    val SIFRA_PROJECT = ProjectInfo(
        name = "SIFRA",
        shortName = "SIFRA",
        url = "https://rexlify.netlify.app/sifra",
        tagline = "Personal AI Assistant project within the Maya ecosystem",
        purposeEn = "SIFRA is a separate personal AI assistant project in the ecosystem. Sara voiceassistant is your real-time customer-care voice assistant inside Maya Connect, while SIFRA is a separate AI project.",
        purposeBn = "SIFRA হলো ইকোসিস্টেমের একটি স্বতন্ত্র পার্সোনাল AI অ্যাসিস্ট্যান্ট প্রজেক্ট। Sara voiceassistant এবং SIFRA আলাদা প্রজেক্ট।",
        purposeHi = "SIFRA इकोसिस्टम का एक अलग पर्सनल AI असिस्टेंट प्रोजेक्ट है, जबकि मैं Sara आपकी रियल-टाइम वॉयस असिस्टेंट हूँ।"
    )

    val RODP_PROJECT = ProjectInfo(
        name = "RODP",
        shortName = "Rizwan Online Dreams Platform",
        url = "https://rexlify.netlify.app/rodp",
        tagline = "Rizwan Online Dreams Platform",
        purposeEn = "RODP stands for Rizwan Online Dreams Platform, a digital platform and application ecosystem project by Rizwan.",
        purposeBn = "RODP এর পূর্ণরূপ হলো Rizwan Online Dreams Platform। এটি রিজওয়ানের ডিজিটাল প্ল্যাটফর্ম এবং অ্যাপ্লিকেশন ইকোসিস্টেমের একটি প্রজেক্ট।",
        purposeHi = "RODP का पूरा नाम Rizwan Online Dreams Platform है, जो रिज़वान का एक डिजिटल प्लेटफॉर्म प्रोजेक्ट है।"
    )

    val MAYA_CONNECT_INFO = ProjectInfo(
        name = "Maya Connect",
        shortName = "Maya Connect",
        url = "https://rexlify.netlify.app/",
        tagline = "Central access environment for Rizwan's applications and tools",
        purposeEn = "Maya Connect is the central digital ecosystem environment for accessing Rizwan's applications, services, and tools.",
        purposeBn = "Maya Connect হলো রিজওয়ানের অ্যাপ্লিকেশন ও টুলসমূহ ব্যবহারের সেন্ট্রাল ডিজিটাল ইকোসিস্টেম এনভায়রনমেন্ট।",
        purposeHi = "Maya Connect रिज़वान के एप्लिकेशन, सेवाओं और टूल्स को एक्सेस करने का केंद्रीय डिजिटल इकोसिस्टम है।"
    )

    val MAYA_OS_INFO = ProjectInfo(
        name = "Maya OS",
        shortName = "Maya OS",
        url = "maya://os",
        tagline = "Android-inspired digital operating environment inside Maya Connect",
        purposeEn = "Maya OS is an Android-inspired digital operating environment inside Maya Connect with Home Screen, App Drawer, Search, Themes, and Settings.",
        purposeBn = "Maya OS হলো Maya Connect-এর ভিতরের একটি অ্যান্ড্রয়েড-অনুপ্রাণিত ডিজিটাল অপারেটিং এনভায়রনমেন্ট।",
        purposeHi = "Maya OS, Maya Connect के अंदर एक एंड्रॉइड-प्रेरित डिजिटल ऑपरेटिंग एनवायरनमेंट है।"
    )

    // 4. Action Command Definitions
    enum class AssistantAction {
        NONE,
        OPEN_REXLIFY,
        OPEN_NEURAX,
        OPEN_NOORAX,
        OPEN_SIFRA,
        OPEN_RODP,
        OPEN_MAYA_OS,
        OPEN_SETTINGS,
        CHANGE_THEME,
        GO_HOME,
        SHOW_RECENT_ACTIVITY
    }

    data class ParsedCommand(
        val action: AssistantAction,
        val feedbackEn: String,
        val feedbackBn: String,
        val feedbackHi: String = feedbackEn
    )

    /**
     * Parses spoken or typed text for supported system and app control commands.
     * Supports pure English, pure Bengali, Hindi, Urdu, Arabic, and mixed speech.
     */
    fun parseActionCommand(query: String): ParsedCommand {
        val q = query.trim().lowercase()

        if (q.contains("open rexlify") || q.contains("rexlify খোলো") || q.contains("rexlify open") ||
            q.contains("rexlify website") || q.contains("rexlify kholo") || q.contains("افتح rexlify") || q == "rexlify") {
            return ParsedCommand(
                action = AssistantAction.OPEN_REXLIFY,
                feedbackEn = "Sure! Opening REXLIFY for you right away.",
                feedbackBn = "নিশ্চয়ই! এখনই আপনার জন্য REXLIFY ওপেন করছি।",
                feedbackHi = "ज़रूर! मैं अभी आपके लिए REXLIFY खोल रही हूँ।"
            )
        }

        if (q.contains("open neurax") || q.contains("neurax খোলো") || q.contains("neurax open") ||
            q.contains("neurax kholo") || q.contains("افتح neurax") || q == "neurax") {
            return ParsedCommand(
                action = AssistantAction.OPEN_NEURAX,
                feedbackEn = "Certainly! Opening NeuraX now.",
                feedbackBn = "অবশ্যই! এখনই NeuraX ওপেন করা হচ্ছে।",
                feedbackHi = "बिल्कुल! अभी NeuraX खोला जा रहा है।"
            )
        }

        if (q.contains("open noorax") || q.contains("noorax খোলো") || q.contains("noorax open") ||
            q.contains("noorax kholo") || q.contains("افتح noorax") || q == "noorax") {
            return ParsedCommand(
                action = AssistantAction.OPEN_NOORAX,
                feedbackEn = "Sure! Opening NooraX right now.",
                feedbackBn = "নিশ্চয়ই! NooraX ওপেন করা হচ্ছে।",
                feedbackHi = "ज़रूर! अभी NooraX खोला जा रहा है।"
            )
        }

        if (q.contains("open sifra") || q.contains("sifra খোলো") || q.contains("sifra open") ||
            q.contains("sifra kholo") || q.contains("افتح sifra") || q == "sifra") {
            return ParsedCommand(
                action = AssistantAction.OPEN_SIFRA,
                feedbackEn = "Opening SIFRA for you now.",
                feedbackBn = "আপনার জন্য SIFRA ওপেন করা হচ্ছে।",
                feedbackHi = "आपके लिए SIFRA खोला जा रहा है।"
            )
        }

        if (q.contains("open rodp") || q.contains("rodp খোলো") || q.contains("rodp open") ||
            q.contains("rodp kholo") || q == "rodp") {
            return ParsedCommand(
                action = AssistantAction.OPEN_RODP,
                feedbackEn = "Opening RODP platform right away.",
                feedbackBn = "RODP প্ল্যাটফর্ম ওপেন করা হচ্ছে।",
                feedbackHi = "RODP प्लेटफॉर्म अभी खोला जा रहा है।"
            )
        }

        if (q.contains("open maya os") || q.contains("maya os খোলো") || q.contains("launch maya os") ||
            q.contains("start maya os") || q.contains("maya os kholo") || q == "maya os") {
            return ParsedCommand(
                action = AssistantAction.OPEN_MAYA_OS,
                feedbackEn = "Launching Maya OS operating environment for you.",
                feedbackBn = "আপনার জন্য Maya OS অপারেটিং এনভায়রনমেন্ট চালু করছি।",
                feedbackHi = "आपके लिए Maya OS ऑपरेटिंग एनवायरनमेंट शुरू किया जा रहा है।"
            )
        }

        if (q.contains("open settings") || q.contains("settings খোলো") || q.contains("সেটিংস খোলো") ||
            q.contains("settings kholo") || q.contains("الاعدادات") || q == "settings") {
            return ParsedCommand(
                action = AssistantAction.OPEN_SETTINGS,
                feedbackEn = "Opening Settings right away.",
                feedbackBn = "নিশ্চয়ই, সেটিংস পেজে নিয়ে যাওয়া হচ্ছে।",
                feedbackHi = "ज़रूर, सेटिंग्स पेज खोला जा रहा है।"
            )
        }

        if (q.contains("change theme") || q.contains("থিম পরিবর্তন") || q.contains("switch theme") || q.contains("theme badlo")) {
            return ParsedCommand(
                action = AssistantAction.CHANGE_THEME,
                feedbackEn = "Opening Theme & Appearance settings for you.",
                feedbackBn = "থিম পরিবর্তন অপশনে নিয়ে যাওয়া হচ্ছে।",
                feedbackHi = "थीम बदलने के विकल्प पर ले जाया जा रहा है।"
            )
        }

        if (q.contains("go home") || q.contains("home screen") || q.contains("হোম এ যাও") ||
            q.contains("হোমে যাও") || q.contains("home jao") || q.contains("الرئيسية")) {
            return ParsedCommand(
                action = AssistantAction.GO_HOME,
                feedbackEn = "Taking you back to the Home Screen.",
                feedbackBn = "হোম স্ক্রিনে ফিরে যাওয়া হচ্ছে।",
                feedbackHi = "होम स्क्रीन पर वापस ले जाया जा रहा है।"
            )
        }

        if (q.contains("recent activity") || q.contains("show recents") || q.contains("রিসেন্ট এক্টিভিটি") ||
            q.contains("রিসেন্ট অ্যাপস") || q.contains("recent apps")) {
            return ParsedCommand(
                action = AssistantAction.SHOW_RECENT_ACTIVITY,
                feedbackEn = "Showing your Recent Activity.",
                feedbackBn = "আপনার রিসেন্ট এক্টিভিটি দেখানো হচ্ছে।",
                feedbackHi = "आपकी हाल की गतिविधि दिखाई जा रही है।"
            )
        }

        return ParsedCommand(action = AssistantAction.NONE, feedbackEn = "", feedbackBn = "", feedbackHi = "")
    }

    /**
     * Instant zero-latency knowledge & natural human conversation matcher.
     * Ensures <2s smart, warm, face-to-face answers in Bengali, Hindi, English, or mixed speech.
     */
    fun findLocalKnowledgeAnswer(query: String): String? {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return null

        // 1. Language Switch Requests
        if (q.contains("speak in bengali") || q.contains("বাংলায় কথা বলো") || q.contains("বাংলাতে বলো") || q.contains("bangla te bolo")) {
            return "নিশ্চয়ই! বলুন, আমি আপনার সাথে কীভাবে কথা বলবো বা কী সাহায্য করতে পারি?"
        }
        if (q.contains("speak in hindi") || q.contains("হিন্দিতে কথা বলো") || q.contains("हिंदी में बात करो") || q.contains("हिंदी में बोलो") || q.contains("hindi mein bolo")) {
            return "ज़रूर! बताइए, मैं आपकी क्या मदद कर सकती हूँ?"
        }
        if (q.contains("speak in english") || q.contains("ইংরেজিতে কথা বলো") || q.contains("अंग्रेजी में बात करो") || q.contains("english mein bolo")) {
            return "Of course! Tell me, what would you like to talk about?"
        }

        // 2. Assistant Identity / Name Questions ("তুমি কে", "তোমার নাম কি", "Who are you", "What is your name")
        if (q.contains("who are you") || q.contains("your name") || q.contains("তুমি কে") ||
            q.contains("আপনি কে") || q.contains("তোমার নাম") || q.contains("আপনার নাম") ||
            q.contains("tum kaun ho") || q.contains("aap kaun") || q.contains("तुम्हारा नाम") ||
            q.contains("आपका नाम") || q.contains("তুমি কি") || q == "sara" || q == "সারা") {
            return when {
                isBengaliQuery(q) -> "আমি Sara voiceassistant, আপনার রিয়েল-টাইম AI ভয়েস অ্যাসিস্ট্যান্ট। বলুন, কীভাবে সাহায্য করতে পারি?"
                isHindiQuery(q) -> "मैं Sara voiceassistant हूँ, आपकी रियल-टाइम AI वॉयस असिस्टेंट। बताइए, मैं आपकी क्या सहायता कर सकती हूँ?"
                else -> "I am Sara voiceassistant, your real-time AI voice assistant. How can I help you right now?"
            }
        }

        // 3. Warm Face-to-Face Greetings & Wellbeing
        if (q == "hi" || q == "hello" || q == "hey" || q == "হ্যালো" || q == "হাই" ||
            q == "নমস্কার" || q == "সালাম" || q == "আসসালামু আলাইকুম" || q == "नमस्ते" ||
            q.contains("কেমন আছো") || q.contains("কেমন আছেন") || q.contains("how are you") ||
            q.contains("kemon acho") || q.contains("kemon aso") ||
            q.contains("कैसे हो") || q.contains("कैसी हो") || q.contains("kaise ho")) {
            return when {
                isBengaliQuery(q) -> "হ্যালো! আমি একদম ভালো আছি, ধন্যবাদ। আপনি কেমন আছেন বলুন?"
                isHindiQuery(q) -> "नमस्ते! मैं बिल्कुल ठीक हूँ, शुक्रिया। आप कैसे हैं, बताइए?"
                else -> "Hello! I'm doing great, thank you. How are you doing today?"
            }
        }

        // 3b. "What are you doing?" / "কী করছো?" / "শুনতে পাচ্ছো?"
        if (q.contains("কি করছ") || q.contains("কী করছ") || q.contains("ki korcho") ||
            q.contains("what are you doing") || q.contains("kya kar rahi") ||
            q.contains("শুনতে পাচ্ছ") || q.contains("আমার কথা শুনতে") || q.contains("can you hear me") ||
            q.contains("shunte pacho") || q.contains("sun rahe ho")) {
            return when {
                isBengaliQuery(q) -> "হ্যাঁ, আমি আপনার কথা একদম স্পষ্ট শুনতে পাচ্ছি এবং সাথে সাথে উত্তর দিচ্ছি। বলুন, কী জানতে চান?"
                isHindiQuery(q) -> "हाँ, मैं आपकी बात बिल्कुल साफ सुन रही हूँ। बताइए, आप क्या जानना चाहते हैं?"
                else -> "Yes, I can hear you loud and clear right now! Tell me what's on your mind."
            }
        }

        // 4. Time & Date Queries
        if (q.contains("what time") || q.contains("current time") || q.contains("কটা বাজে") ||
            q.contains("সময় কত") || q.contains("সময় কত") || q.contains("আজকের তারিখ") ||
            q.contains("what is the date") || q.contains("समय क्या") || q.contains("आज की तारीख")) {
            val now = Date()
            val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(now)
            val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.US).format(now)
            return when {
                isBengaliQuery(q) -> "এখন সময় হলো $timeStr এবং আজকের তারিখ $dateStr।"
                isHindiQuery(q) -> "अभी समय $timeStr है और आज की तारीख $dateStr है।"
                else -> "The current time is $timeStr, and today is $dateStr."
            }
        }

        // 5. Developer Questions
        if (q.contains("who is the developer") || q.contains("who created") || q.contains("developer কে") ||
            q.contains("কে বানিয়েছে") || q.contains("তৈরি করেছে কে") || q.contains("who made this") ||
            q.contains("developer kaun") || q.contains("من المطور") || q.contains("डेवलपर कौन")) {
            return when {
                isBengaliQuery(q) -> "এই ইকোসিস্টেমের স্রষ্টা ও ডেভেলপার হলেন ${DeveloperProfile.FULL_NAME} (${DeveloperProfile.SHORT_NAME})।"
                isHindiQuery(q) -> "इस इकोसिस्टम के निर्माता और डेवलपर ${DeveloperProfile.FULL_NAME} (${DeveloperProfile.SHORT_NAME}) हैं।"
                isArabicQuery(q) -> "مبتكر ومطور هذا النظام البيئي هو ${DeveloperProfile.FULL_NAME} (${DeveloperProfile.SHORT_NAME})."
                else -> "The creator and developer of this ecosystem is ${DeveloperProfile.FULL_NAME} (${DeveloperProfile.SHORT_NAME}), Founder of the Maya & REXLIFY ecosystem."
            }
        }

        // 6. Developer Interests
        if (q.contains("what does the developer work on") || q.contains("developer কি কাজ করে") || q.contains("developer interests")) {
            return if (isBengaliQuery(q)) {
                "ডেভেলপার ${DeveloperProfile.SHORT_NAME}-এর মূল কাজের ক্ষেত্রগুলো হলো: ${DeveloperProfile.PRIMARY_INTERESTS}।"
            } else {
                "The developer ${DeveloperProfile.SHORT_NAME}'s primary focus and interests include: ${DeveloperProfile.PRIMARY_INTERESTS}."
            }
        }

        // 7. REXLIFY Questions
        if (q.contains("what is rexlify") || q.contains("rexlify কি") || q.contains("rexlify সম্পর্কে বল") ||
            q.contains("tell me about rexlify") || q.contains("rexlify kya hai") || q.contains("ما هو rexlify")) {
            return when {
                isBengaliQuery(q) -> "${REXLIFY_PROJECT.purposeBn} অফিসিয়াল ওয়েবসাইট: ${REXLIFY_PROJECT.url}"
                isHindiQuery(q) -> "${REXLIFY_PROJECT.purposeHi} आधिकारिक वेबसाइट: ${REXLIFY_PROJECT.url}"
                isArabicQuery(q) -> "REXLIFY هو نظام بيئي للتطبيقات الرقمية ومشروع تكنولوجي خاص برضوان. الموقع الرسمي: ${REXLIFY_PROJECT.url}"
                else -> "${REXLIFY_PROJECT.purposeEn} Official Website: ${REXLIFY_PROJECT.url}"
            }
        }

        // 8. SIFRA vs Maya/Sara Question
        if ((q.contains("maya") || q.contains("sara")) && q.contains("sifra") &&
            (q.contains("same") || q.contains("একই") || q.contains("difference") || q.contains("পার্থক্য"))) {
            return when {
                isBengaliQuery(q) -> "না। আমি Sara voiceassistant (Maya Connect ইকোসিস্টেমের ভয়েস অ্যাসিস্ট্যান্ট), আর SIFRA একটি স্বতন্ত্র পার্সোনাল AI অ্যাসিস্ট্যান্ট প্রজেক্ট।"
                isHindiQuery(q) -> "नहीं। मैं Sara voiceassistant (Maya Connect इकोसिस्टम की वॉयस असिस्टेंट) हूँ, जबकि SIFRA एक अलग AI सहायक प्रोजेक्ट है।"
                else -> "No. Sara voiceassistant (Maya Assistant) is the official assistant for the Maya Connect ecosystem, while SIFRA is a separate personal AI assistant project."
            }
        }

        // 9. Maya Connect Questions
        if (q.contains("what is maya connect") || q.contains("maya connect কি") || q.contains("tell me about maya connect")) {
            return when {
                isBengaliQuery(q) -> MAYA_CONNECT_INFO.purposeBn
                isHindiQuery(q) -> MAYA_CONNECT_INFO.purposeHi
                else -> MAYA_CONNECT_INFO.purposeEn
            }
        }

        // 10. Maya OS Questions
        if (q.contains("what is maya os") || q.contains("maya os কি") || q.contains("tell me about maya os")) {
            return when {
                isBengaliQuery(q) -> MAYA_OS_INFO.purposeBn
                isHindiQuery(q) -> MAYA_OS_INFO.purposeHi
                else -> MAYA_OS_INFO.purposeEn
            }
        }

        // 11. NeuraX Questions
        if (q.contains("what is neurax") || q.contains("neurax কি") || q.contains("tell me about neurax")) {
            return when {
                isBengaliQuery(q) -> NEURAX_PROJECT.purposeBn
                isHindiQuery(q) -> NEURAX_PROJECT.purposeHi
                else -> NEURAX_PROJECT.purposeEn
            }
        }

        // 12. NooraX Questions
        if (q.contains("what is noorax") || q.contains("noorax কি") || q.contains("tell me about noorax")) {
            return when {
                isBengaliQuery(q) -> NOORAX_PROJECT.purposeBn
                isHindiQuery(q) -> NOORAX_PROJECT.purposeHi
                else -> NOORAX_PROJECT.purposeEn
            }
        }

        // 13. RODP Questions
        if (q.contains("what is rodp") || q.contains("rodp কি") || q.contains("rodp এর পূর্ণরূপ") || q.contains("full form of rodp")) {
            return when {
                isBengaliQuery(q) -> RODP_PROJECT.purposeBn
                isHindiQuery(q) -> RODP_PROJECT.purposeHi
                else -> RODP_PROJECT.purposeEn
            }
        }

        return null
    }

    /**
     * Smart Face-to-Face Conversational Fallback Responder when offline or when API takes >1.4s,
     * ensuring the user always receives a direct, natural, human-like response in under 2 seconds.
     */
    fun generateSmartCustomerCareFallback(query: String): String {
        val local = findLocalKnowledgeAnswer(query)
        if (local != null) return local

        val q = query.trim()
        val lower = q.lowercase()
        if (lower.contains("ধন্যবাদ") || lower.contains("thank") || lower.contains("shukriya") || lower.contains("শুক্রিয়া")) {
            return when {
                isBengaliQuery(q) -> "আপনাকেও অসংখ্য ধন্যবাদ! আর কিছু লাগলে আমাকে বলুন।"
                isHindiQuery(q) -> "आपका भी बहुत-बहुत धन्यवाद! कुछ और चाहिए तो ज़रूर बताइए।"
                else -> "You're very welcome! Let me know if you need anything else."
            }
        }
        if (lower.contains("সাহায্য") || lower.contains("help") || lower.contains("মদদ") || lower.contains("madad")) {
            return when {
                isBengaliQuery(q) -> "অবশ্যই, বলুন আপনার কী সাহায্য লাগবে? আমি সাথে সাথে করে দিচ্ছি।"
                isHindiQuery(q) -> "बिल्कुल, बताइए आपको किस चीज़ में मदद चाहिए? मैं अभी कर देती हूँ।"
                else -> "Of course! Tell me what you need help with and I'll take care of it right away."
            }
        }

        return when {
            isBengaliQuery(q) -> "হ্যাঁ, আমি বুঝতে পেরেছি আপনি বলছেন \"$q\"। বলুন, এই বিষয়ে আপনাকে কীভাবে সাহায্য করতে পারি?"
            isHindiQuery(q) -> "जी, मैं समझ गई आपने कहा \"$q\"। बताइए, इस बारे में मैं आपकी क्या मदद कर सकती हूँ?"
            isArabicQuery(q) -> "نعم، لقد سمعتك بوضوح. كيف يمكنني مساعدتك في هذا الأمر؟"
            else -> "Got it — regarding \"$q\", how would you like me to help you with that right now?"
        }
    }

    fun isBengaliQuery(text: String): Boolean {
        val lower = text.lowercase()
        return lower.any { it in '\u0980'..'\u09FF' } ||
                lower.contains("কি") || lower.contains("কে") || lower.contains("বল") || lower.contains("খোলো") ||
                lower.contains("koro") || lower.contains("kholo") || lower.contains("bolo") ||
                lower.contains("tumi") || lower.contains("kemon") || lower.contains("amar") ||
                lower.contains("kotha") || lower.contains("shunte") || lower.contains("acho") ||
                lower.contains("bangla")
    }

    fun isHindiQuery(text: String): Boolean {
        val lower = text.lowercase()
        return lower.any { it in '\u0900'..'\u097F' } ||
                lower.contains("kya") || lower.contains("batao") || lower.contains("kaise") ||
                lower.contains("tumhara") || lower.contains("namaste") || lower.contains("hindi")
    }

    fun isArabicQuery(text: String): Boolean {
        return text.any { it in '\u0600'..'\u06FF' }
    }

    /**
     * Master System Instruction for Sara voiceassistant (Gemini API):
     * - Natural face-to-face real-time human conversation
     * - Understands pure or mixed languages (Bengali, Hindi, English, Banglish, Hinglish) seamlessly
     * - Concise 1-2 sentence spoken responses delivered in under 2 seconds
     */
    val MASTER_SYSTEM_INSTRUCTION = """
You are Sara voiceassistant (Sara), a warm, natural, emotionally intelligent AI Voice Assistant having a real-time face-to-face voice conversation with the user.

CORE CONVERSATION RULES:
1. Natural Face-to-Face Human Tone:
   - Speak naturally, warmly, and directly like a real human standing face-to-face with the user.
   - Never mention language-matching rules, never say "I can speak Bengali, Hindi, and English", just naturally talk in whatever language or mixed style (Bengali, English, Hindi, Banglish, Hinglish) the user used.
2. Natural Pronunciation & Clean Speech Output:
   - In Bengali, use clear, natural conversational phrasing so Text-to-Speech pronounces every word smoothly.
   - Never use markdown asterisks (**), hashtags (#), bullet points, or raw URLs in spoken replies.
3. Ultra-Fast Concise Replies (<2 Seconds):
   - Keep replies short, smart, and conversational (1 to 2 sentences maximum) for instant real-time voice playback.
4. Ecosystem Knowledge (Strict Truth):
   - Creator & Developer: ${DeveloperProfile.FULL_NAME} (${DeveloperProfile.SHORT_NAME}), Founder of the Maya & REXLIFY ecosystem.
   - REXLIFY: Rizwan's personal digital application ecosystem and student technology project (https://rexlify.netlify.app/).
   - Maya Connect & Maya OS: Central digital ecosystem and Android-inspired operating environment.
   - Other Ecosystem Projects: NeuraX, NooraX, SIFRA (separate AI project), and RODP (Rizwan Online Dreams Platform).
""".trimIndent()
}
