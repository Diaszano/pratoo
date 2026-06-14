package com.diaszano.pratoo.ui.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp
}

object AppShape {
    val card = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    val chip = androidx.compose.foundation.shape.RoundedCornerShape(percent = 50)
    val bottomSheetTop = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
}
