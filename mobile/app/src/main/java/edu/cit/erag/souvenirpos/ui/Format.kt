package edu.cit.erag.souvenirpos.ui

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Formats a monetary amount as Philippine Peso with two decimals (SRS BR-007). */
fun peso(amount: Double): String = "₱" + String.format(Locale.US, "%.2f", amount)

private val saleDisplayFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy · h:mm a", Locale.US)

/** Formats an ISO-8601 sale timestamp (e.g. "2026-07-04T22:10:27.312") for display. */
fun formatSaleDateTime(iso: String): String = try {
    LocalDateTime.parse(iso).format(saleDisplayFormatter)
} catch (e: Exception) {
    iso
}
