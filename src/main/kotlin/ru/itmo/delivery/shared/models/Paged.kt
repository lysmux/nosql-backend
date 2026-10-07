package ru.itmo.delivery.shared.models

data class Paged<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
) {
    val totalPages: Int = ((totalItems + size - 1) / size).toInt()

    fun <R> withItems(items: List<R>): Paged<R> = Paged(items, page, size, totalItems)
}
