package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.AppCategory
import com.example.data.model.AppItem
import com.example.data.model.AppStatus

@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val tagline: String,
    val fullDescription: String,
    val url: String,
    val categoryName: String,
    val statusName: String,
    val isFeatured: Boolean,
    val isFavorite: Boolean,
    val isPinnedHome: Boolean,
    val isEnabled: Boolean = true,
    val iconKey: String,
    val primaryColorHex: Long,
    val secondaryColorHex: Long,
    val lastOpenedTimestamp: Long,
    val launchCount: Int,
    val isCustom: Boolean,
    val customTags: String
) {
    fun toDomainModel(): AppItem {
        val cat = try {
            AppCategory.valueOf(categoryName)
        } catch (_: Exception) {
            AppCategory.PRODUCTIVITY
        }
        val stat = try {
            AppStatus.valueOf(statusName)
        } catch (_: Exception) {
            AppStatus.AVAILABLE
        }
        return AppItem(
            id = id,
            name = name,
            tagline = tagline,
            fullDescription = fullDescription,
            url = url,
            category = cat,
            status = stat,
            isFeatured = isFeatured,
            isFavorite = isFavorite,
            isPinnedHome = isPinnedHome,
            isEnabled = isEnabled,
            iconKey = iconKey,
            primaryColorHex = primaryColorHex,
            secondaryColorHex = secondaryColorHex,
            lastOpenedTimestamp = lastOpenedTimestamp,
            launchCount = launchCount,
            isCustom = isCustom,
            customTags = customTags
        )
    }

    companion object {
        fun fromDomainModel(item: AppItem): AppEntity {
            return AppEntity(
                id = item.id,
                name = item.name,
                tagline = item.tagline,
                fullDescription = item.fullDescription,
                url = item.url,
                categoryName = item.category.name,
                statusName = item.status.name,
                isFeatured = item.isFeatured,
                isFavorite = item.isFavorite,
                isPinnedHome = item.isPinnedHome,
                isEnabled = item.isEnabled,
                iconKey = item.iconKey,
                primaryColorHex = item.primaryColorHex,
                secondaryColorHex = item.secondaryColorHex,
                lastOpenedTimestamp = item.lastOpenedTimestamp,
                launchCount = item.launchCount,
                isCustom = item.isCustom,
                customTags = item.customTags
            )
        }
    }
}
