package com.example.moxmemorygame

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.moxmemorygame.ui.composables.AboutDialog
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AboutDialogTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun aboutDialog_displaysCorrectContentAndDismisses() {
        var dismissed = false

        composeTestRule.setContent {
            AboutDialog(onDismissRequest = { dismissed = true })
        }

        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedVersionString = context.getString(R.string.version_format, BuildConfig.VERSION_NAME)

        composeTestRule.onNodeWithText(context.getString(R.string.about_dialog_title)).assertExists()
        composeTestRule.onNodeWithText(expectedVersionString).assertExists()

        composeTestRule.onNodeWithText(context.getString(R.string.button_ok)).performClick()

        assertTrue("Dialog was not dismissed", dismissed)
    }
}