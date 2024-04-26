package ru.namerpro.nchat.domain.model

data class TypedMessage(
    val type: Int,
    val text: String?,
    val file: String?,
    val date: String
)