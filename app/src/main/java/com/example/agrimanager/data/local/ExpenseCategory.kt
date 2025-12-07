package com.example.agrimanager.data.local

import androidx.compose.ui.graphics.Color

data class ExpenseBreakdown(
    val totalExpenses: Double,
    val categories: List<ExpenseCategory>
)

data class ExpenseCategory(
    val name: String,
    val amount: Double,
    val color: Color,
    val percentage: Float
)
