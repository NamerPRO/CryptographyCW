package ru.namerpro.nchat.domain.model

import kotlinx.coroutines.CoroutineScope

data class Task(
    var progress: (Double) -> Unit,
    var coroutineScope: CoroutineScope
)