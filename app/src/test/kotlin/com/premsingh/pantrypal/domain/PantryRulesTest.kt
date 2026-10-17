package com.premsingh.pantrypal.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class PantryRulesTest {
    @Test fun groupsItemsByExpiryWindow() {
        assertEquals(PantrySummary(4, 2, 1), summarizeExpiry(listOf(98, 100, 103, 110), todayEpochDay = 100))
    }

    @Test fun formatsExpiryLabels() {
        assertEquals("Expires today", daysLabel(100, 100))
        assertEquals("Expires tomorrow", daysLabel(101, 100))
        assertEquals("Expired 2 days ago", daysLabel(98, 100))
    }
}
