package com.vpcoffee.feature.sales.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.dp
import com.vpcoffee.feature.catalog.domain.model.Drink
import org.junit.Rule
import org.junit.Test

class PointOfSaleScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun drinkGrid_showsDrinks() {
        val viewModel = createFakePointOfSaleViewModel(
            drinks = listOf(
                Drink("1", "Coffee", 25000),
                Drink("2", "Tea", 20000),
            ),
        )

        composeTestRule.setContent {
            PointOfSaleScreen(viewModel = viewModel, contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tea").assertIsDisplayed()
    }

    @Test
    fun drinkGrid_showsMultipleDrinks() {
        val viewModel = createFakePointOfSaleViewModel(
            drinks = listOf(
                Drink("1", "Coffee", 25000),
                Drink("2", "Tea", 20000),
                Drink("3", "Americano", 22000),
            ),
        )

        composeTestRule.setContent {
            PointOfSaleScreen(viewModel = viewModel, contentPadding = PaddingValues(0.dp))
        }

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Coffee").assertIsDisplayed()
        composeTestRule.onNodeWithText("Tea").assertIsDisplayed()
        composeTestRule.onNodeWithText("Americano").assertIsDisplayed()
    }
}
