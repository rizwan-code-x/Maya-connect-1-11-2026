package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

data class SupportMessage(
    val sender: String, // "USER" or "ADMIN"
    val senderName: String,
    val text: String,
    val timestamp: Long
)

@Entity(tableName = "support_tickets")
data class SupportTicketEntity(
    @PrimaryKey
    val id: String, // e.g. "#MC-1042"
    val userDisplayName: String = "Rizwan RR",
    val subject: String,
    val category: String, // "General", "Bug Report", "App Request", "Feedback"
    val initialMessage: String,
    val status: String = "OPEN", // "OPEN", "IN_PROGRESS", "RESOLVED"
    val conversationJson: String = "[]",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val hasUnreadReply: Boolean = false
) {
    fun getMessages(): List<SupportMessage> {
        val list = mutableListOf<SupportMessage>()
        try {
            val array = JSONArray(conversationJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    SupportMessage(
                        sender = obj.optString("sender", "USER"),
                        senderName = obj.optString("senderName", "Rizwan RR"),
                        text = obj.optString("text", ""),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    companion object {
        fun buildConversationJson(messages: List<SupportMessage>): String {
            val array = JSONArray()
            messages.forEach { msg ->
                val obj = JSONObject()
                obj.put("sender", msg.sender)
                obj.put("senderName", msg.senderName)
                obj.put("text", msg.text)
                obj.put("timestamp", msg.timestamp)
                array.put(obj)
            }
            return array.toString()
        }
    }
}
