package dev.psiae.mltoolbox.shared.ui.composeui.theme.md3
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import dev.psiae.mltoolbox.shared.ui.md3.MD3Theme
import dev.psiae.mltoolbox.shared.user.data.model.UserProfileSetting

private var lightScheme by mutableStateOf(MD3ColorPalette_Green.lightColorScheme)
private var darkScheme by mutableStateOf (MD3ColorPalette_Green.darkColorScheme)



@Immutable
data class ColorFamily(
    val color: Color,
    val onColor: Color,
    val colorContainer: Color,
    val onColorContainer: Color
)

val unspecified_scheme = ColorFamily(
    Color.Unspecified, Color.Unspecified, Color.Unspecified, Color.Unspecified
)

val MD3Theme.darkColorScheme
    get() = darkScheme

val MD3Theme.lightColorScheme
    get() = lightScheme


val LocalIsDarkTheme = staticCompositionLocalOf<Boolean> { false }

val MD3Theme.colorScheme
    @Composable get() = if (LocalIsDarkTheme.current) darkColorScheme else lightColorScheme

@Composable
fun MD3Theme.currentLocalAbsoluteOnSurfaceColor() = if (LocalIsDarkTheme.current) Color.White else Color.Black


fun MD3Theme.colorSchemeForSeedCode(
    seed: String,
    isDarkTheme: Boolean
): ColorScheme {
    return when (seed) {
        UserProfileSetting.Personalization.Theme.COLOR_SEED_GREEN -> if (isDarkTheme) MD3ColorPalette_Green.darkColorScheme else MD3ColorPalette_Green.lightColorScheme
        UserProfileSetting.Personalization.Theme.COLOR_SEED_ORANGE -> if (isDarkTheme) MD3ColorPalette_Orange.darkColorScheme else MD3ColorPalette_Orange.lightColorScheme
        UserProfileSetting.Personalization.Theme.COLOR_SEED_BLUE -> if (isDarkTheme) MD3ColorPalette_Blue.darkColorScheme else MD3ColorPalette_Blue.lightColorScheme
        UserProfileSetting.Personalization.Theme.COLOR_SEED_YELLOW -> if (isDarkTheme) MD3ColorPalette_Yellow.darkColorScheme else MD3ColorPalette_Yellow.lightColorScheme
        UserProfileSetting.Personalization.Theme.COLOR_SEED_PURPLE -> if (isDarkTheme) MD3ColorPalette_Purple.darkColorScheme else MD3ColorPalette_Purple.lightColorScheme
        else -> error("Unknown color seed: $seed")
    }
}