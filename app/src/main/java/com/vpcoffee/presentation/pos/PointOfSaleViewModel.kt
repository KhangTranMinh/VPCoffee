package com.vpcoffee.presentation.pos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.domain.model.Drink
import com.vpcoffee.domain.model.Order
import com.vpcoffee.domain.model.OrderItem
import com.vpcoffee.domain.repository.DrinkRepository
import com.vpcoffee.domain.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PointOfSaleViewModel(
    drinkRepository: DrinkRepository,
    private val orderRepository: OrderRepository,
) : ViewModel() {
    val drinks: StateFlow<List<Drink>> = drinkRepository.observeDrinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _cart = MutableStateFlow<List<OrderItem>>(emptyList())
    val cart: StateFlow<List<OrderItem>> = _cart.asStateFlow()

    fun addDrink(drink: Drink) {
        _cart.value = _cart.value.toMutableList().also { items ->
            val index = items.indexOfFirst { it.drinkId == drink.id }
            if (index >= 0) items[index] = items[index].copy(quantity = items[index].quantity + 1)
            else items += OrderItem(drink.id, drink.name, drink.price, 1)
        }
    }

    fun changeQuantity(drinkId: Long, quantity: Int) {
        _cart.value = _cart.value.mapNotNull { item ->
            when {
                item.drinkId != drinkId -> item
                quantity > 0 -> item.copy(quantity = quantity)
                else -> null
            }
        }
    }

    fun completeOrder(onComplete: () -> Unit) {
        val items = _cart.value
        if (items.isEmpty()) return
        viewModelScope.launch {
            orderRepository.saveOrder(Order(createdAt = System.currentTimeMillis(), items = items))
            _cart.value = emptyList()
            onComplete()
        }
    }
}
