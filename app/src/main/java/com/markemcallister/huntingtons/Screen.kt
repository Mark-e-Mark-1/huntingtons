package com.markemcallister.huntingtons

sealed class Screen {
    data object Disclaimer : Screen()
    data object Home : Screen()
    data object Overview : Screen()
    data object Treatments : Screen()
    data object Research : Screen()
    data object Resources : Screen()
    data class Glossary(val initialQuery: String = "") : Screen()
    data object About : Screen()
}
