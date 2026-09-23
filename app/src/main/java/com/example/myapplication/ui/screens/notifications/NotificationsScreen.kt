package com.example.myapplication.ui.screens.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.screens.BackNavigationLink
import com.example.myapplication.ui.viewmodel.AppNotification
import com.example.myapplication.ui.viewmodel.NotificationViewModel
import com.example.myapplication.ui.viewmodel.StatusTransition
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@Composable
fun NotificationsScreen(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onOpenOrder: (String) -> Unit,
    onOpenInventory: (Long) -> Unit,
    viewModel: NotificationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadNotifications()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        item {
            BackNavigationLink(onClick = onBack)
        }
        item {
            Text(
                text = "Notificaciones",
                modifier = Modifier.padding(top = 14.dp, bottom = 20.dp),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        when {
            state.isLoading && state.notifications.isEmpty() -> item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null && state.notifications.isEmpty() -> item {
                NotificationsMessage(
                    message = state.errorMessage.orEmpty(),
                    actionLabel = "Reintentar",
                    onAction = viewModel::loadNotifications
                )
            }
            state.notifications.isEmpty() -> item {
                NotificationsMessage(message = "No tienes notificaciones.")
            }
            else -> {
                state.errorMessage?.let { message ->
                    item {
                        NotificationsMessage(
                            message = message,
                            actionLabel = "Reintentar",
                            onAction = viewModel::loadNotifications,
                            compact = true
                        )
                    }
                }
                items(
                    items = state.notifications,
                    key = AppNotification::id
                ) { notification ->
                    NotificationRow(
                        notification = notification,
                        onOpenOrder = onOpenOrder,
                        onOpenInventory = onOpenInventory
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

@Composable
private fun NotificationRow(
    notification: AppNotification,
    onOpenOrder: (String) -> Unit,
    onOpenInventory: (Long) -> Unit
) {
    val orderId = notification.orderId
    val inventoryId = notification.inventoryId
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = orderId != null || inventoryId != null) {
                when {
                    orderId != null -> onOpenOrder(orderId)
                    inventoryId != null -> onOpenInventory(inventoryId)
                }
            }
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = notification.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = notification.description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        notification.statusTransition?.let { transition ->
            StatusTransitionLine(transition)
        }
        Text(
            text = notification.createdAt.asReadableNotificationTime(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusTransitionLine(transition: StatusTransition) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = transition.previous,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.size(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = "Cambio de estado",
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = transition.current,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun NotificationsMessage(
    message: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    compact: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 12.dp else 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

private fun String?.asReadableNotificationTime(): String {
    val date = parseNotificationDate(this) ?: return "Fecha no registrada"
    val locale = Locale("es", "CO")
    val time = SimpleDateFormat("hh:mm a", locale).format(date)
    return "${SimpleDateFormat("dd 'de' MMMM 'de' yyyy", locale).format(date)} · $time"
}

private fun parseNotificationDate(value: String?): Date? {
    val raw = value?.trim()?.takeIf(String::isNotBlank) ?: return null
    val formats = listOf(
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") },
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US),
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    )
    return formats.firstNotNullOfOrNull { format -> runCatching { format.parse(raw) }.getOrNull() }
}
