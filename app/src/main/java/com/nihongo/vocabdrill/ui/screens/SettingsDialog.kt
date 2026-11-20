package com.nihongo.vocabdrill.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nihongo.vocabdrill.model.DrillDirection
import com.nihongo.vocabdrill.model.ThemeMode
import com.nihongo.vocabdrill.ui.theme.IndigoBlueDark
import com.nihongo.vocabdrill.ui.theme.IndigoBlueLight
import com.nihongo.vocabdrill.ui.theme.IndigoBluePrimary
import com.nihongo.vocabdrill.ui.theme.IndigoBlueVivid
import com.nihongo.vocabdrill.ui.theme.WarningAmber
import com.nihongo.vocabdrill.viewmodel.NihongoViewModel

@Composable
fun SettingsDialog(
    viewModel: NihongoViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.appSettings.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .widthIn(max = 520.dp).fillMaxWidth().padding(horizontal = 16.dp)
                .padding(vertical = 16.dp)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header (Title + Subtitle + Close 'X')
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Quiz Settings",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Configure quiz direction & audio",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Theme toggle & Close X Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("btn_close_settings")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close settings",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // PRACTICE DIRECTION Section
                Text(
                    text = "PRACTICE DIRECTION",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 3 Practice Direction Cards
                val directionList = listOf(
                    DrillDirection.HIRAGANA_TO_ENGLISH,
                    DrillDirection.ENGLISH_TO_HIRAGANA,
                    DrillDirection.MIX_BOTH
                )

                directionList.forEach { dir ->
                    val isSelected = settings.drillDirection == dir
                    DirectionSelectionCard(
                        direction = dir,
                        isSelected = isSelected,
                        onClick = { viewModel.updateDrillDirection(dir) }
                    )
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // DRILL OPTIONS (Switches)
                Text(
                    text = "DRILL PREFERENCES",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Option 1: Show Romaji Hints
                    SettingsToggleRow(
                        icon = Icons.Default.Translate,
                        title = "Show Romaji Hints",
                        subtitle = "Display pronunciation letters under Japanese",
                        checked = settings.showRomaji,
                        onCheckedChange = { viewModel.toggleRomaji(it) },
                        testTag = "switch_romaji"
                    )

                    // Option 2: Auto-pronounce Audio
                    SettingsToggleRow(
                        icon = Icons.Default.VolumeUp,
                        title = "Auto-pronounce Audio",
                        subtitle = "Automatically speak word in Japanese on new question",
                        checked = settings.autoPlaySpeech,
                        onCheckedChange = { viewModel.toggleAutoPlaySpeech(it) },
                        testTag = "switch_auto_speech"
                    )

                    // Option 3: Hide MCQ Options + [Mind Recall] Badge
                    SettingsToggleRow(
                        icon = Icons.Default.VisibilityOff,
                        title = "Hide MCQ Options",
                        
                        badgeColor = WarningAmber,
                        subtitle = "Think of the meaning in your mind, then click button to check",
                        checked = settings.hideMcqOptions,
                        onCheckedChange = { viewModel.toggleHideMcqOptions(it) },
                        testTag = "switch_hide_mcq"
                    )

                    // Option 4: Feedback Chimes
                    SettingsToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Feedback Chimes",
                        subtitle = "Play a soft chime for correct answers",
                        checked = settings.soundEffects,
                        onCheckedChange = { viewModel.toggleSoundEffects(it) },
                        testTag = "switch_feedback_chimes"
                    )
                    SettingsToggleRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Encouragement",
                        subtitle = "A little motivation, up to 3 times per drill",
                        checked = settings.encouragement,
                        onCheckedChange = viewModel::toggleEncouragement,
                        testTag = "switch_encouragement"
                    )

                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = IndigoBlueVivid
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth().heightIn(min = 48.dp)
                            .testTag("btn_save_and_continue")
                    ) {
                        Text(
                            text = "Save & Continue",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

            }
        }
    }
}

@Composable
private fun DirectionSelectionCard(
    direction: DrillDirection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) IndigoBlueVivid else MaterialTheme.colorScheme.outlineVariant
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                IndigoBlueVivid.copy(alpha = 0.08f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("direction_card_${direction.name.lowercase()}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = direction.label,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = direction.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right checkmark circle when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(IndigoBlueVivid),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    icon: ImageVector,
    title: String,
    badge: String? = null,
    badgeColor: Color = IndigoBluePrimary,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                }
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = if (badge != null) badgeColor else IndigoBlueVivid
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
