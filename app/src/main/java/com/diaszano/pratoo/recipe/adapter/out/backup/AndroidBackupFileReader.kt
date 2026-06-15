package com.diaszano.pratoo.recipe.adapter.out.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidBackupFileReader
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        fun read(uri: Uri): String {
            val inputStream =
                context.contentResolver.openInputStream(uri)
                    ?: throw IllegalStateException("Could not open file")
            return inputStream.bufferedReader().use { it.readText() }
        }
    }
