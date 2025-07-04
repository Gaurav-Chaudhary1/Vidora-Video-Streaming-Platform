package com.vidora.app.utils

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Parses this ISO‑8601 timestamp (e.g. "2025-06-19T04:44:13.791Z")
 * and returns a human‑friendly relative string:
 *   - "just now"
 *   - "5 minutes ago"
 *   - "3 hours ago"
 *   - "2 days ago"
 *   - or a date like "Jun 12, 2025" if it's more than a week old.
 */
fun String.toRelativeTime(): String {
    return try {
        val then   = Instant.parse(this)
        val now    = Instant.now()
        val diff   = Duration.between(then, now)

        when {
            diff.toMinutes() < 1  -> "just now"
            diff.toMinutes() < 60 -> "${diff.toMinutes()} minutes ago"
            diff.toHours()   < 24 -> "${diff.toHours()} hours ago"
            diff.toDays()    < 7  -> "${diff.toDays()} days ago"
            else -> {
                // Fallback to a date pattern
                val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
                    .withZone(ZoneId.systemDefault())
                formatter.format(then)
            }
        }
    } catch (e: Exception) {
        // If parsing fails, just return the original string
        this
    }
}
