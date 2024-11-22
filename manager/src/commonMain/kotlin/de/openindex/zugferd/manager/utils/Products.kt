package de.openindex.zugferd.manager.utils

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableStateOf
import de.openindex.zugferd.manager.model.Product
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class Products(data: List<Product>) {
    private val _products = mutableStateOf(data)
    val products get() = _products.value.sortedBy { it.name.lowercase() }

    private val nextKey: UInt
        get() = if (_products.value.isNotEmpty()) {
            _products.value.maxOf { it._key } + 1.toUInt()
        } else {
            1.toUInt()
        }

    fun put(product: Product) {
        _products.value = if (product._key == 0.toUInt()) {
            _products.value
                .plus(product.copy(_key = nextKey))
        } else {
            _products.value
                .filter { it._key != product._key }
                .plus(product)
        }
    }

    fun remove(product: Product) {
        remove(product._key)
    }

    @Suppress("MemberVisibilityCanBePrivate")
    fun remove(key: UInt) {
        _products.value = _products.value
            .filter { it._key != key }
    }

    suspend fun save() {
        saveProductsData(
            data = products,
        )
    }
}

val LocalProducts = compositionLocalOf { loadProducts() }

fun loadProducts(): Products =
    runBlocking(Dispatchers.IO) {
        Products(loadProductsData())
    }

expect suspend fun loadProductsData(): List<Product>

expect suspend fun saveProductsData(data: List<Product>)
