package com.vpcoffee.feature.orders.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.feature.orders.domain.model.Order
import com.vpcoffee.feature.orders.domain.repository.OrderRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ReportsViewModel(orderRepository: OrderRepository) : ViewModel() {
    val orders: StateFlow<List<Order>> = orderRepository.observeOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
