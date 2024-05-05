package ru.namerpro.nchat.domain.model

data class Task(
    var progress: (Double) -> Unit,
    var isCancelled: Boolean
)