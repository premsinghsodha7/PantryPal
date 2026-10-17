package com.premsingh.pantrypal.domain

data class PantrySummary(val itemCount: Int, val expiringSoonCount: Int, val expiredCount: Int)

fun summarizeExpiry(expiryEpochDays: List<Long>, todayEpochDay: Long): PantrySummary = PantrySummary(
    itemCount = expiryEpochDays.size,
    expiringSoonCount = expiryEpochDays.count { it in todayEpochDay..(todayEpochDay + 3) },
    expiredCount = expiryEpochDays.count { it < todayEpochDay }
)

fun daysLabel(expiryEpochDay: Long, todayEpochDay: Long): String = when (val delta = expiryEpochDay - todayEpochDay) {
    0L -> "Expires today"
    1L -> "Expires tomorrow"
    in 2L..Int.MAX_VALUE.toLong() -> "Expires in $delta days"
    else -> "Expired ${-delta} days ago"
}
