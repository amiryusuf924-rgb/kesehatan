package id.dietku.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Forest = Color(0xFF176B52)
private val LightGreen = Color(0xFFE0F2E9)
private val DeepGreen = Color(0xFF123D31)
private val WarmWhite = Color(0xFFF8FAF7)

private val LightColors = lightColorScheme(
    primary = Forest,
    onPrimary = Color.White,
    secondary = Color(0xFF52675B),
    background = WarmWhite,
    surface = Color.White,
    surfaceVariant = LightGreen,
    onSurface = Color(0xFF18211C),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DD3B1),
    onPrimary = DeepGreen,
    secondary = Color(0xFFB7CBBB),
    background = Color(0xFF101713),
    surface = Color(0xFF18211C),
    surfaceVariant = Color(0xFF28382F),
    onSurface = Color(0xFFE5EEE7),
)

@Composable
fun DietKuTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content,
    )
}