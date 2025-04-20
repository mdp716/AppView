package com.mikasys.appview

import java.io.File

object SizeCalculator {
    fun calculateDirSize(dir: File): Long {
        var size = 0L
        if (!dir.exists()) return size

        dir.listFiles()?.forEach { file ->
            size += if (file.isFile) {
                file.length()
            } else {
                calculateDirSize(file)
            }
        }
        return size
    }
}