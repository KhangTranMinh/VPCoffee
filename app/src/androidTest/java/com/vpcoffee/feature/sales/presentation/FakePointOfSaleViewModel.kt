package com.vpcoffee.feature.sales.presentation

import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.model.OrderItem
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

/**
 * Creates a real PointOfSaleViewModel with fake repositories for UI testing.
 */
fun createFakePointOfSaleViewModel(
    drinks: List<Drink> = emptyList(),
    cart: List<OrderItem> = emptyList(),
): PointOfSaleViewModel {
    val fakeDrinkRepository = FakeDrinkRepository(drinks)
    val fakeOrderRepository = FakeOrderRepository()
    return PointOfSaleViewModel(fakeDrinkRepository, fakeOrderRepository)
}

private class FakeDrinkRepository(initialDrinks: List<Drink>) : DrinkRepository {
    private val drinks = MutableStateFlow(initialDrinks)

    override fun observeDrinks(): Flow<List<Drink>> = drinks
    override suspend fun saveDrink(drink: Drink): String = drink.id
    override suspend fun deleteDrink(id: String) {}
}

private class FakeOrderRepository : OrderRepository {
    private val orders = MutableStateFlow<List<Order>>(emptyList())

    override fun observeOrders(): Flow<List<Order>> = orders
    override suspend fun saveOrder(order: Order): String = order.id
    override suspend fun markOrdersAsSent(orderIds: List<String>) {}
}
