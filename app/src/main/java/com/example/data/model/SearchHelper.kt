package com.example.data.model

object SearchHelper {
    fun matchesQuery(app: AppItem, query: String): Boolean {
        if (query.isBlank()) return true
        val cleanQuery = query.trim().lowercase()

        // 1. Direct name or ID contains
        if (app.name.lowercase().contains(cleanQuery) || app.id.lowercase().contains(cleanQuery)) {
            return true
        }

        // 2. Tagline contains
        if (app.tagline.lowercase().contains(cleanQuery)) {
            return true
        }

        // 3. Category display name
        if (app.category.displayName.lowercase().contains(cleanQuery)) {
            return true
        }

        // 4. Custom tags match
        if (app.customTags.lowercase().contains(cleanQuery)) {
            return true
        }

        // 5. Full description contains
        if (app.fullDescription.lowercase().contains(cleanQuery)) {
            return true
        }

        // 6. Token/prefix match for each word in query
        val queryTokens = cleanQuery.split(" ", "_", "-").filter { it.isNotBlank() }
        val targetTokens = (app.name + " " + app.tagline + " " + app.customTags + " " + app.category.displayName)
            .lowercase()
            .split(" ", "_", "-")
            .filter { it.isNotBlank() }

        return queryTokens.all { qToken ->
            targetTokens.any { tToken ->
                tToken.startsWith(qToken) || tToken.contains(qToken)
            }
        }
    }
}
