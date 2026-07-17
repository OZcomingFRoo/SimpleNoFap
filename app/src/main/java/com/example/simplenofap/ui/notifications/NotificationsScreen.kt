package com.example.simplenofap.ui.notifications

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.simplenofap.data.local.SimpleNoFapDatabase
import com.example.simplenofap.localization.AppStrings
import com.example.simplenofap.localization.LocalAppStrings
import com.example.simplenofap.notifications.AndroidNotificationScheduler
import com.example.simplenofap.notifications.DaysOfWeekMask
import com.example.simplenofap.notifications.NotificationCoordinator
import com.example.simplenofap.notifications.NotificationRepository
import com.example.simplenofap.notifications.NotificationWeekday
import com.example.simplenofap.notifications.ScheduledNotification

@Composable
internal fun NotificationsScreen(
    addRequest: Int,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scheduler = remember { AndroidNotificationScheduler(context.applicationContext) }
    val coordinator = remember {
        NotificationCoordinator(
            NotificationRepository(SimpleNoFapDatabase.getInstance(context).scheduledNotificationDao()),
            scheduler
        )
    }
    val viewModel: NotificationsViewModel = viewModel(
        factory = NotificationsViewModel.Factory(coordinator, scheduler)
    )
    val notifications by viewModel.notifications.collectAsState()
    val draft by viewModel.draft.collectAsState()
    val error by viewModel.error.collectAsState()
    val notificationPermission by viewModel.notificationPermissionGranted.collectAsState()
    val exactPermission by viewModel.exactAlarmsGranted.collectAsState()
    val strings = LocalAppStrings.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { viewModel.refreshPermissions() }
    val exactAlarmLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refreshPermissions() }

    LaunchedEffect(addRequest) { if (addRequest > 0) viewModel.newDraft() }
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                NotificationsEvent.RequestNotificationPermission -> if (Build.VERSION.SDK_INT >= 33) {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else viewModel.refreshPermissions()
                NotificationsEvent.OpenExactAlarmSettings -> if (Build.VERSION.SDK_INT >= 31) {
                    exactAlarmLauncher.launch(
                        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))
                    )
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!notificationPermission && notifications.any { it.active }) item {
            PermissionBanner(strings.notificationPermissionWarning, strings.grantPermission) {
                if (Build.VERSION.SDK_INT >= 33) permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        if (!exactPermission && notifications.any { it.active }) item {
            PermissionBanner(strings.exactAlarmWarning, strings.allowExactAlarms, viewModel::requestExactAlarmAccess)
        }
        error?.let { item {
            PermissionBanner(strings.notificationError, strings.cancel, viewModel::dismissError)
        } }
        if (notifications.isEmpty()) item {
            EmptyState(strings)
        } else items(notifications, key = { it.id }) { notification ->
            NotificationRow(
                notification = notification,
                strings = strings,
                onClick = { viewModel.edit(notification) },
                onToggle = { viewModel.setActive(notification.id, it) }
            )
        }
    }

    draft?.let { EditorDialog(it, strings, viewModel) }
}

@Composable
private fun PermissionBanner(text: String, action: String, onAction: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(text, color = MaterialTheme.colorScheme.onTertiaryContainer)
            TextButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) { Text(action) }
        }
    }
}

@Composable
private fun EmptyState(strings: AppStrings) {
    Column(
        Modifier.fillMaxWidth().padding(vertical = 72.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(strings.noNotifications, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(strings.noNotificationsBody, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun NotificationRow(
    notification: ScheduledNotification,
    strings: AppStrings,
    onClick: () -> Unit,
    onToggle: (Boolean) -> Unit
) {
    val contentColor = if (notification.active) MaterialTheme.colorScheme.onSurface else
        MaterialTheme.colorScheme.onSurface.copy(alpha = .48f)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "%02d:%02d".format(notification.timeMinutesOfDay / 60, notification.timeMinutesOfDay % 60),
                    style = MaterialTheme.typography.headlineMedium,
                    color = contentColor
                )
                Text(
                    NotificationWeekday.entries.filter { DaysOfWeekMask.contains(notification.daysOfWeekMask, it) }
                        .joinToString(" ") { strings.weekdayInitials[it.ordinal] },
                    color = contentColor
                )
                notification.customMessage?.let { Text(it, color = contentColor, maxLines = 2) }
            }
            Switch(checked = notification.active, onCheckedChange = onToggle)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditorDialog(draft: NotificationDraft, strings: AppStrings, viewModel: NotificationsViewModel) {
    val context = LocalContext.current
    var confirmDiscard by remember { mutableStateOf(false) }
    val ringtoneLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val uri: Uri? = if (Build.VERSION.SDK_INT >= 33) {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri::class.java)
            } else @Suppress("DEPRECATION") {
                result.data?.getParcelableExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI)
            }
            val label = uri?.let { runCatching { RingtoneManager.getRingtone(context, it)?.getTitle(context) }.getOrNull() }
            viewModel.updateDraft { it.copy(soundUri = uri?.toString(), soundDisplayName = label) }
        }
    }
    fun attemptClose() { if (draft.changed) confirmDiscard = true else viewModel.closeEditor() }
    BackHandler(onBack = ::attemptClose)

    Dialog(onDismissRequest = ::attemptClose, properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)) {
        AnimatedVisibility(
            visible = true,
            enter = slideInHorizontally(initialOffsetX = { it }),
            exit = slideOutHorizontally(targetOffsetX = { it })
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(if (draft.original == null) strings.newNotification else strings.editNotification) }
                    )
                },
                bottomBar = {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                            .navigationBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(onClick = ::attemptClose, modifier = Modifier.weight(1f), shape = CircleShape) { Text(strings.cancel) }
                        Button(onClick = viewModel::save, modifier = Modifier.weight(1f), shape = CircleShape) { Text(strings.save) }
                    }
                }
            ) { padding ->
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    item { TimeWheel(draft.hour, draft.minute, viewModel) }
                    item { WeekdaySelector(draft, strings, viewModel) }
                    item {
                        OutlinedTextField(
                            value = draft.message,
                            onValueChange = { value -> viewModel.updateDraft { it.copy(message = value) } },
                            label = { Text(strings.message) },
                            placeholder = { Text(strings.messageOptional) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 2
                        )
                    }
                    item {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(strings.sound, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    if (!draft.soundEnabled) strings.silent else draft.soundDisplayName ?: strings.defaultSound,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(draft.soundEnabled, { enabled -> viewModel.updateDraft { it.copy(soundEnabled = enabled) } })
                        }
                    }
                    if (draft.soundEnabled) item {
                        OutlinedButton(onClick = {
                            ringtoneLauncher.launch(Intent(RingtoneManager.ACTION_RINGTONE_PICKER).apply {
                                putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE, RingtoneManager.TYPE_NOTIFICATION)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT, true)
                                putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, draft.soundUri?.let(Uri::parse))
                            })
                        }, modifier = Modifier.fillMaxWidth()) { Text(strings.chooseSound) }
                    }
                }
            }
        }
    }

    if (confirmDiscard) ConfirmDialog(strings.discardChangesTitle, strings.discardChangesBody, strings.discard, strings.keepEditing, {
        confirmDiscard = false; viewModel.closeEditor()
    }, { confirmDiscard = false })
}

@Composable
private fun TimeWheel(hour: Int, minute: Int, viewModel: NotificationsViewModel) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
            NumberWheel(24, hour) { value -> viewModel.updateDraft { it.copy(hour = value) } }
            Text(":", style = MaterialTheme.typography.displaySmall, modifier = Modifier.padding(horizontal = 12.dp))
            NumberWheel(60, minute) { value -> viewModel.updateDraft { it.copy(minute = value) } }
        }
    }
}

@Composable
private fun NumberWheel(count: Int, selected: Int, onSelect: (Int) -> Unit) {
    val state = rememberLazyListState(initialFirstVisibleItemIndex = (selected - 1).coerceAtLeast(0))
    LazyColumn(state = state, modifier = Modifier.size(76.dp, 156.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        items(count) { value ->
            val chosen = value == selected
            Box(
                Modifier.fillMaxWidth().height(52.dp)
                    .background(if (chosen) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, RoundedCornerShape(16.dp))
                    .clickable { onSelect(value) },
                contentAlignment = Alignment.Center
            ) {
                Text("%02d".format(value), style = if (chosen) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleLarge)
            }
        }
    }
}

@Composable
private fun WeekdaySelector(draft: NotificationDraft, strings: AppStrings, viewModel: NotificationsViewModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            NotificationWeekday.entries.forEach { day ->
                val selected = draft.daysMask and day.maskBit != 0
                Box(
                    Modifier.size(40.dp)
                        .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .clickable { viewModel.toggleDay(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(strings.weekdayInitials[day.ordinal], color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        if (draft.showWeekdayError) Text(strings.weekdaysRequired, color = MaterialTheme.colorScheme.error)
    }
}

@Composable
private fun ConfirmDialog(title: String, body: String, confirm: String, dismiss: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(body) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirm) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text(dismiss) } }
    )
}
