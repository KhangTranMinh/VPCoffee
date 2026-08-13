package com.vpcoffee.feature.catalog.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.feature.catalog.domain.model.Drink
import com.vpcoffee.feature.catalog.domain.repository.DrinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class CatalogViewModel(private val drinkRepository: DrinkRepository) : ViewModel() {
    val drinks: StateFlow<List<Drink>> = drinkRepository.observeDrinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveDrink(id: String?, name: String, priceText: String, imageUri: String?) {
        val price = priceText.filter(Char::isDigit).toLongOrNull() ?: return
        if (name.isBlank() || price < 0) return
        viewModelScope.launch {
            drinkRepository.saveDrink(Drink(id ?: UUID.randomUUID().toString(), name, price, imageUri))
        }
    }

    fun deleteDrink(id: String) {
        viewModelScope.launch { drinkRepository.deleteDrink(id) }
    }
}
