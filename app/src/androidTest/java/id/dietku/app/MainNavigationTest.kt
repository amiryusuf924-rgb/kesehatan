package id.dietku.app

import androidx.compose.ui.test.assertExists
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun mainTabsOpenFoodAndProgressScreens() {
        composeRule.onNodeWithText("Hari ini").assertExists()

        composeRule.onNodeWithText("Makan").performClick()
        composeRule.onNodeWithText("Catat makan").assertExists()

        composeRule.onNodeWithText("Progres").performClick()
        composeRule.onNodeWithText("Kemajuan").assertExists()
    }
}