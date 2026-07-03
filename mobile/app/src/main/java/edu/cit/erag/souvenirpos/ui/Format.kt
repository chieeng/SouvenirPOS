package edu.cit.erag.souvenirpos.ui

import java.util.Locale

/** Formats a monetary amount as Philippine Peso with two decimals (SRS BR-007). */
fun peso(amount: Double): String = "₱" + String.format(Locale.US, "%.2f", amount)
