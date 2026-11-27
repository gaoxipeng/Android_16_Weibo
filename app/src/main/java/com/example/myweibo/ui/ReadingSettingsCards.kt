package com.example.myweibo.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.myweibo.R
import com.example.myweibo.data.ReadingNumber
import com.example.myweibo.ui.liquidglass.SettingsLiquidSwitch

@Composable
fun ReadingFoldSettingsCard(
    enabled: Boolean,
    maxLines: Int,
    onEnabledChange: (Boolean) -> Unit,
    onMaxLinesChange: (Int) -> Unit,
    card: @Composable (content: @Composable () -> Unit) -> Unit,
) {
    val value = ReadingNumber.BodyLines.readSaved(maxLines)
    ReadingSettingsCard(
        title = stringResource(R.string.reading_fold_title),
        description = stringResource(R.string.reading_fold_description),
        label = stringResource(R.string.reading_fold_lines_label),
        valueLabel = stringResource(R.string.reading_fold_lines_value, value),
        footer = null,
        enabled = enabled,
        value = value,
        number = ReadingNumber.BodyLines,
        tag = "reading_fold",
        onEnabledChange = onEnabledChange,
        onValueChange = onMaxLinesChange,
        card = card,
    )
}

@Composable
fun ReverseReadingSettingsCard(
    enabled: Boolean,
    newPostLimit: Int,
    onEnabledChange: (Boolean) -> Unit,
    onNewPostLimitChange: (Int) -> Unit,
    card: @Composable (content: @Composable () -> Unit) -> Unit,
) {
    val value = ReadingNumber.NewPostLimit.readSaved(newPostLimit)
    ReadingSettingsCard(
        title = stringResource(R.string.reading_reverse_title),
        description = stringResource(R.string.reading_reverse_description),
        label = stringResource(R.string.reading_reverse_limit_label),
        valueLabel = stringResource(R.string.reading_reverse_limit_value, value),
        footer = stringResource(R.string.reading_reverse_limit_description, value),
        enabled = enabled,
        value = value,
        number = ReadingNumber.NewPostLimit,
        tag = "reading_reverse",
        onEnabledChange = onEnabledChange,
        onValueChange = onNewPostLimitChange,
        card = card,
    )
}

@Composable
private fun ReadingSettingsCard(
    title: String,
    description: String,
    label: String,
    valueLabel: String,
    footer: String?,
    enabled: Boolean,
    value: Int,
    number: ReadingNumber,
    tag: String,
    onEnabledChange: (Boolean) -> Unit,
    onValueChange: (Int) -> Unit,
    card: @Composable (content: @Composable () -> Unit) -> Unit,
) {
    var showEditor by rememberSaveable(enabled) { mutableStateOf(false) }
    val colors = MaterialTheme.colorScheme
    card {
        Column(Modifier.semantics { testTagsAsResourceId = true }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("${tag}_toggle")
                    .toggleable(value = enabled, role = Role.Switch, onValueChange = onEnabledChange)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.onSurface,
                    )
                    Text(description, style = MaterialTheme.typography.bodyMedium, color = colors.onSurface)
                }
                // Draw the same two-state liquid control without its pointer handler:
                // the enclosing row owns the only switch action.
                SettingsLiquidSwitch(
                    selectedIndex = if (enabled) 1 else 0,
                    onSelected = {},
                    modifier = Modifier.heightIn(min = 48.dp),
                    enabled = false,
                )
            }

            val interaction = if (enabled) {
                Modifier.clickable(role = Role.Button) { showEditor = true }
            } else {
                Modifier.semantics(mergeDescendants = true) { disabled() }
            }
            val rowColor = if (enabled) colors.onSurface else colors.onSurface.copy(alpha = 0.62f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .testTag("${tag}_number")
                    .then(interaction)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(label, Modifier.weight(1f), color = rowColor, style = MaterialTheme.typography.bodyMedium)
                Text(valueLabel, color = rowColor, style = MaterialTheme.typography.bodyMedium)
            }
            if (footer != null) {
                Text(
                    text = footer,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.onSurface,
                )
            }
        }
    }
    if (showEditor && enabled) {
        ReadingNumberDialog(
            label = label,
            value = value,
            number = number,
            tag = tag,
            onDismiss = { showEditor = false },
            onConfirm = {
                onValueChange(it)
                showEditor = false
            },
        )
    }
}

@Composable
private fun ReadingNumberDialog(
    label: String,
    value: Int,
    number: ReadingNumber,
    tag: String,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit,
) {
    var draft by rememberSaveable { mutableStateOf(value.toString()) }
    val parsed = number.parse(draft)
    AlertDialog(
        modifier = Modifier.semantics { testTagsAsResourceId = true }.testTag("${tag}_dialog"),
        onDismissRequest = onDismiss,
        title = { Text(label, color = MaterialTheme.colorScheme.onSurface) },
        text = {
            OutlinedTextField(
                value = draft,
                onValueChange = { draft = it },
                modifier = Modifier.fillMaxWidth().testTag("${tag}_input").semantics {
                    contentDescription = label
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = parsed == null,
                supportingText = if (parsed == null) {
                    {
                        Text(stringResource(R.string.reading_number_invalid, number.range.first, number.range.last))
                    }
                } else null,
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("${tag}_cancel")) {
                Text(stringResource(R.string.reading_dialog_cancel))
            }
        },
        confirmButton = {
            TextButton(
                enabled = parsed != null,
                onClick = { parsed?.let(onConfirm) },
                modifier = Modifier.testTag("${tag}_confirm"),
            ) {
                Text(stringResource(R.string.reading_dialog_confirm))
            }
        },
    )
}
