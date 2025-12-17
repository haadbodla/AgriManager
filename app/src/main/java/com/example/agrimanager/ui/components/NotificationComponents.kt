package com.example.agrimanager.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Red notification dot that appears on dashboard buttons when there's new data.
 * Used for Layer 1 of the notification system.
 */
@Composable
fun NotificationDot(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(10.dp)
            .background(
                color = Color(0xFFE53935), // Red
                shape = CircleShape
            )
    )
}

/**
 * Count badge showing number of new items (e.g., "(2)").
 * Used for Layer 2 of the notification system on machine/location cards.
 */
@Composable
fun CountBadge(
    count: Int,
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Surface(
            modifier = modifier,
            color = Color(0xFFE53935), // Red
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "($count)",
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * "NEW" badge for recently added items.
 * Alternative to CountBadge for individual cards.
 */
@Composable
fun NewBadge(
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = Color(0xFF4CAF50), // Green
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = "NEW",
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            color = Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Returns a highlighted container color for new cards.
 * Used for Layer 3 of the notification system.
 * 
 * @param isNew Whether this card represents new/unseen data
 * @param normalColor The default card background color
 * @param highlightColor The highlight color for new items (defaults to light green)
 */
@Composable
fun newCardColor(
    isNew: Boolean,
    normalColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = Color(0xFFE8F5E9) // Light green
): Color {
    val animatedColor by animateColorAsState(
        targetValue = if (isNew) highlightColor else normalColor,
        animationSpec = tween(durationMillis = 500),
        label = "card_highlight"
    )
    return animatedColor
}
