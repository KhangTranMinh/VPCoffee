package com.vpcoffee.core.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vpcoffee.R
import java.text.NumberFormat
import java.util.Locale

@Composable
fun formatVnd(amount: Long): String = stringResource(
    R.string.currency_vnd,
    NumberFormat.getNumberInstance(Locale.getDefault()).format(amount),
)
