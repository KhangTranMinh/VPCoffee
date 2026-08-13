package com.vpcoffee.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vpcoffee.domain.model.Drink
import com.vpcoffee.domain.repository.DrinkRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CatalogViewModel(private val drinkRepository: DrinkRepository) : ViewModel() {
    val drinks: StateFlow<List<Drink>> = drinkRepository.observeDrinks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveDrink(id: Long, name: String, priceText: String, imageUri: String?) {
        val price = priceText.toLongOrNull() ?: return
        if (name.isBlank() || price < 0) return
        viewModelScope.launch {
            drinkRepository.saveDrink(Drink(id, name, price, imageUri))
        }
    }

    fun deleteDrink(id: Long) {
        viewModelScope.launch { drinkRepository.deleteDrink(id) }
    }
}
