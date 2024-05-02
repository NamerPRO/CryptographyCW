package ru.namerpro.nchat.commons

import java.io.File

fun getFileType(
    nameOrPath: String
) = nameOrPath.takeLastWhile { it != '.' }

fun getFileName(
    path: String?
) = path?.takeLastWhile { "$it" != File.separator } ?: ""

fun parentPath(
    path: String?
) = path?.dropLastWhile { "$it" != File.separator } ?: ""

fun removePrefix(
    path: String?
) = path?.dropWhile { it != '_' }?.drop(1) ?: ""