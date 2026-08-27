package com.vpcoffee.feature.catalog.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.vpcoffee.feature.catalog.domain.model.Drink
import org.junit.Rule
import org.junit.Test

class CatalogScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun drinkList_showsDrinks() {
        val viewModel = createFakeCatalogViewModel(
            drinks = listOf(
                Drink("1", "Coffee", 25000),
                Drink("2", "Tea", 20000),
            ),
        )

        composeTestRule.setContent {
            CatalogScreen(viewModel = viewModel, contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tea").assertIsDisplayed()
    }

    @Test
    fun drinkList_showsMultipleDrinks() {
        val viewModel = createFakeCatalogViewModel(
            drinks = listOf(
                Drink("1", "Coffee", 25000),
                Drink("2", "Tea", 20000),
                Drink("3", "Americano", 22000),
            ),
        )

        composeTestRule.setContent {
            CatalogScreen(viewModel = viewModel, contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tea").assertIsDisplayed()
        composeTestRule.onNodeWithText("Americano").assertIsDisplayed()
    }
}
