package com.vpcoffee.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.model.OrderItem
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(
    orderRepository: OrderRepository,
    drinkRepository: DrinkRepository,
) : ViewModel() {

    private val drinks: StateFlow<List<Drink>> = drinkRepository.observeDrinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val orders: StateFlow<List<Order>> = combine(
        orderRepository.observeOrders(),
        drinks,
    ) { orders, drinks ->
        val drinkNames = drinks.associate { it.id to it.name }
        orders.map { order ->
            order.copy(
                items = order.items.map { item ->
                    item.copy(drinkName = drinkNames[item.drinkId] ?: item.drinkName)
                }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
