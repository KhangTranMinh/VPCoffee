package com.vpcoffee.feature.orders.presentation

import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Creates a real ReportsViewModel with fake repositories for UI testing.
 */
fun createFakeReportsViewModel(
    orders: List<Order> = emptyList(),
): ReportsViewModel {
    val fakeOrderRepository = FakeOrderRepository(orders)
    val fakeDrinkRepository = FakeDrinkRepository()
    return ReportsViewModel(fakeOrderRepository, fakeDrinkRepository)
}

private class FakeOrderRepository(initialOrders: List<Order>) : OrderRepository {
    private val orders = MutableStateFlow(initialOrders)

    override fun observeOrders(): Flow<List<Order>> = orders
    override suspend fun saveOrder(order: Order): String = order.id
    override suspend fun markOrdersAsSent(orderIds: List<String>) {}
}

private class FakeDrinkRepository : DrinkRepository {
    private val drinks = MutableStateFlow<List<Drink>>(emptyList())

    override fun observeDrinks(): Flow<List<Drink>> = drinks
    override suspend fun saveDrink(drink: Drink): String = drink.id
    override suspend fun deleteDrink(id: String) {}
}
