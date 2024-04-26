package ru.namerpro.nchat.commons

import android.net.Uri

fun toUri(
    serializedUri: String?
) = if (serializedUri == null) {
    Uri.EMPTY
} else {
    Uri.parse(serializedUri)
}