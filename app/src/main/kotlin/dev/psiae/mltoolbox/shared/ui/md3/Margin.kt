package dev.psiae.mltoolbox.shared.ui.md3

object Margin {

    val COMPACT_WIDTH_BOUND = 600 - 1
    val MEDIUM_WIDTH_BOUND = 840 - 1
    val EXPANDED_WIDTH_BOUND = 1200 - 1
    val LARGE_WIDTH_BOUND = 1600 - 1
    val EXTRA_LARGE_WIDTH_BOUND = Int.MAX_VALUE

    val COMPACT_SPACING = 16f
    val MEDIUM_SPACING = 24f
    val EXPANDED_SPACING = 24f
    val LARGE_SPACING = 24f
    val EXTRA_LARGE_SPACING = 24f
}

val MD3Spec.margin get() = Margin

fun Margin.spacingOfWindowWidthDp(width: Float): Float {
    if (width.isNaN()) return Float.NaN
    // compact
    if (width <= COMPACT_WIDTH_BOUND) return COMPACT_SPACING
    // medium
    if (width <= MEDIUM_WIDTH_BOUND) return MEDIUM_SPACING
    // expanded
    if (width <= EXPANDED_WIDTH_BOUND) return EXPANDED_SPACING
    // large
    if (width <= LARGE_WIDTH_BOUND) return LARGE_SPACING
    // extra-large and larger
    return EXTRA_LARGE_SPACING
}