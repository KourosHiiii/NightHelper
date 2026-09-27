package com.nighthelper.app.data

import kotlinx.serialization.Serializable

@Serializable
enum class ItemType {
    YES_NO,
    TEXT
}

@Serializable
data class ChecklistItem(
    val id: String,
    val title: String,
    val subtitle: String = "",
    val iconKey: String = "moon",
    val type: ItemType = ItemType.YES_NO,
    val isBuiltIn: Boolean = false,
    val order: Int = 0
)

@Serializable
data class ItemState(
    val done: Boolean = false,
    val textValue: String = ""
)

@Serializable
data class DayRecord(
    val date: String,
    val totalItems: Int = 0,
    val completedItems: Int = 0,
    val completedTitles: List<String> = emptyList(),
    val missedTitles: List<String> = emptyList(),
    val gratitude: String? = null,
    val closedAt: Long? = null
)

@Serializable
data class SettingsState(
    val flipGestureEnabled: Boolean = true,
    val persistentNotificationEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

object DefaultItems {
    fun create(): List<ChecklistItem> = listOf(
        ChecklistItem(
            id = "pill",
            title = "دارو رو خوردی؟",
            subtitle = "مهم‌ترین کار شب، همین‌جاست",
            iconKey = "pill",
            type = ItemType.YES_NO,
            isBuiltIn = true,
            order = 0
        ),
        ChecklistItem(
            id = "tooth",
            title = "مسواک زدی؟",
            subtitle = "لبخند سفید برای فردا",
            iconKey = "tooth",
            type = ItemType.YES_NO,
            isBuiltIn = true,
            order = 1
        ),
        ChecklistItem(
            id = "book",
            title = "Duolingo رو انجام دادی؟",
            subtitle = "پنج دقیقه هم کافیه",
            iconKey = "book",
            type = ItemType.YES_NO,
            isBuiltIn = true,
            order = 2
        ),
        ChecklistItem(
            id = "heart",
            title = "امروز برای چی شکرگزار هستی؟",
            subtitle = "یه جمله کوتاه بنویس",
            iconKey = "heart",
            type = ItemType.TEXT,
            isBuiltIn = true,
            order = 3
        )
    )
}

object NightTips {
    val all = listOf(
        "سه نفس عمیق بکش و شونه‌هات رو رها کن",
        "یه لیوان آب بخور و گوشی رو کنار بذار",
        "گردن و شونه‌هات رو ۳۰ ثانیه کشش بده",
        "چشم‌هات رو ببند و فقط به نفس کشیدنت گوش بده"
    )
}
