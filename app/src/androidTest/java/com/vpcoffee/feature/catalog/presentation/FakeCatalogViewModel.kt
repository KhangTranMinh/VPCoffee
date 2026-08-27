package com.vpcoffee.feature.catalog.presentation

import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Creates a real CatalogViewModel with fake repositories for UI testing.
 */
fun createFakeCatalogViewModel(
    drinks: List<Drink> = emptyList(),
): CatalogViewModel {
    val fakeDrinkRepository = FakeDrinkRepository(drinks)
    return CatalogViewModel(fakeDrinkRepository)
}

private class FakeDrinkRepository(initialDrinks: List<Drink>) : DrinkRepository {
    private val drinks = MutableStateFlow(initialDrinks)

    override fun observeDrinks(): Flow<List<Drink>> = drinks
    override suspend fun saveDrink(drink: Drink): String = drink.id
    override suspend fun deleteDrink(id: String) {}
}
