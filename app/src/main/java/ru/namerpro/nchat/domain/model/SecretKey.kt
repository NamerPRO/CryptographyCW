package ru.namerpro.nchat.domain.model

import java.math.BigInteger

data class SecretKey(
    val chatId: Long,
    val partOfKey: BigInteger
)