package com.vpcoffee.pushtest

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import org.junit.Rule
import org.junit.Test

class PushTestScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun screen_showsTitle() {
        composeTestRule.setContent {
            PushTestScreen(contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Push Notification Test").assertIsDisplayed()
    }

    @Test
    fun screen_showsStatus() {
        composeTestRule.setContent {
            PushTestScreen(contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Ready").assertIsDisplayed()
    }

    @Test
    fun screen_showsVersion() {
        composeTestRule.setContent {
            PushTestScreen(contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        // Version text contains version name and code
        composeTestRule.onNodeWithText("v1.0.0 (6)").assertIsDisplayed()
    }
}
