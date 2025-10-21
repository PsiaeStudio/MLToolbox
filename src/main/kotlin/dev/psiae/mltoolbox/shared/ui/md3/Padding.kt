package dev.psiae.mltoolbox.shared.ui.md3

object Padding {
    val MD3_SPEC_PADDING_INCREMENT_VALUE_DP = 4F
}

val MD3Spec.padding
    get() = Padding

fun Padding.incrementsDp(n: Int): Float = n * MD3_SPEC_PADDING_INCREMENT_VALUE_DP