package com.example.personaldashboard.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DragIndicator
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.personaldashboard.model.DashboardState
import com.example.personaldashboard.model.DashboardWidget
import com.example.personaldashboard.model.WidgetKind
import com.example.personaldashboard.ui.theme.ThemePack
import com.example.personaldashboard.ui.theme.AppFontFamily
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun DashboardWidgetCard(
    widget: DashboardWidget,
    state: DashboardState,
    pack: ThemePack,
    editMode: Boolean,
    onRemove: () -> Unit,
    onToggleTodo: (String) -> Unit,
    onAddTodo: (String) -> Unit,
    onNoteChange: (String) -> Unit,
    onAdvanceHabit: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tint = when (widget.kind) {
        WidgetKind.CLOCK, WidgetKind.HABIT -> pack.surface
        WidgetKind.WEATHER -> mix(pack.surface, Color(0xFFBCEBFF), 0.28f)
        WidgetKind.OVERVIEW -> mix(pack.surface, pack.primary, 0.10f)
        WidgetKind.TODO, WidgetKind.FOCUS -> pack.surfaceAlt
        WidgetKind.NOTE -> mix(pack.surface, pack.sparkle, 0.11f)
        WidgetKind.COUNTDOWN -> mix(pack.surface, pack.secondary, 0.10f)
    }
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(if (widget.kind == WidgetKind.OVERVIEW) 208.dp else 232.dp)
            .shadow(if (editMode) 11.dp else 5.dp, shape)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(mix(tint, Color.White, 0.28f).copy(alpha = 0.97f), tint.copy(alpha = 0.96f)),
                ),
            )
            .border(
                if (editMode) 2.5.dp else 1.5.dp,
                pack.border.copy(alpha = if (editMode) 1f else 0.72f),
                shape,
            ),
    ) {
        Box {
            Column(Modifier.padding(13.dp)) {
                WidgetTitle(widget.kind, pack, editMode)
                Spacer(Modifier.height(8.dp))
                when (widget.kind) {
                    WidgetKind.CLOCK -> ClockWidget(pack)
                    WidgetKind.WEATHER -> WeatherWidget(pack)
                    WidgetKind.OVERVIEW -> OverviewWidget(state, pack)
                    WidgetKind.TODO -> TodoWidget(state, pack, onToggleTodo, onAddTodo)
                    WidgetKind.NOTE -> NoteWidget(state.note, pack, onNoteChange)
                    WidgetKind.FOCUS -> FocusWidget(pack)
                    WidgetKind.HABIT -> HabitWidget(state, pack, onAdvanceHabit)
                    WidgetKind.COUNTDOWN -> CountdownWidget(pack)
                }
            }
            AnimatedVisibility(editMode, modifier = Modifier.align(Alignment.TopEnd)) {
                Row(modifier = Modifier.padding(6.dp).clip(RoundedCornerShape(14.dp))) {
                    IconButton(onClick = onRemove, modifier = Modifier.size(34.dp)) {
                        Icon(Icons.Rounded.Close, "Remove", Modifier.size(18.dp), tint = pack.secondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetTitle(kind: WidgetKind, pack: ThemePack, editMode: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(kind.emoji, fontSize = 18.sp)
        Spacer(Modifier.width(7.dp))
        Text(kind.title, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = pack.ink)
        Spacer(Modifier.weight(1f))
        if (editMode) Icon(Icons.Rounded.DragIndicator, "Drag", tint = pack.mutedInk)
    }
}

@Composable
private fun ClockWidget(pack: ThemePack) {
    var now by remember { mutableStateOf(LocalDateTime.now()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalDateTime.now()
            delay(1_000)
        }
    }
    Text(
        now.format(DateTimeFormatter.ofPattern("HH:mm")),
        fontFamily = AppFontFamily,
        fontSize = 40.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.Bold,
        color = pack.primary,
    )
    Text(now.format(DateTimeFormatter.ofPattern("EEEE, MMM d")), color = pack.mutedInk, fontSize = 13.sp)
    Spacer(Modifier.height(10.dp))
    Text("☀  A fresh little page", fontSize = 12.sp, color = pack.secondary)
}

@Composable
private fun WeatherWidget(pack: ThemePack) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "22°",
                fontFamily = AppFontFamily,
                fontSize = 41.sp,
                lineHeight = 41.sp,
                fontWeight = FontWeight.Bold,
                color = pack.primary,
            )
            Text("Partly cloudy", color = pack.ink, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text("🌤️", fontSize = 45.sp)
    }
    Spacer(Modifier.height(14.dp))
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text("H 24°", color = pack.mutedInk, fontSize = 11.sp)
        Text("L 16°", color = pack.mutedInk, fontSize = 11.sp)
        Text("☂ 20%", color = pack.primary, fontSize = 11.sp)
    }
}

@Composable
private fun OverviewWidget(state: DashboardState, pack: ThemePack) {
    val doneTasks = state.todos.count { it.done }
    val totalTasks = state.todos.size.coerceAtLeast(1)
    val habitPoints = state.habitProgress.values.sum()
    val progress = ((doneTasks.toFloat() / totalTasks) * 0.55f + (habitPoints / 12f) * 0.45f).coerceIn(0f, 1f)

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Your gentle progress", color = pack.mutedInk, fontSize = 12.sp)
            Text(
                "${(progress * 100).toInt()}%",
                fontFamily = AppFontFamily,
                fontSize = 35.sp,
                lineHeight = 36.sp,
                fontWeight = FontWeight.Bold,
                color = pack.primary,
            )
        }
        Text("🏰", fontSize = 48.sp)
    }
    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp).height(8.dp).clip(CircleShape),
        color = pack.secondary,
        trackColor = pack.border.copy(alpha = 0.18f),
    )
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        QuestStat("$doneTasks/$totalTasks", "Tasks", pack, Modifier.weight(1f))
        QuestStat("$habitPoints", "Habit stars", pack, Modifier.weight(1f))
        QuestStat("25m", "Next focus", pack, Modifier.weight(1f))
    }
}

@Composable
private fun QuestStat(value: String, label: String, pack: ThemePack, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = pack.surface.copy(alpha = 0.62f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, pack.border.copy(alpha = 0.28f)),
    ) {
        Column(Modifier.padding(horizontal = 9.dp, vertical = 7.dp)) {
            Text(value, fontFamily = AppFontFamily, fontWeight = FontWeight.Bold, color = pack.ink, fontSize = 15.sp)
            Text(label, color = pack.mutedInk, fontSize = 9.sp, maxLines = 1)
        }
    }
}

@Composable
private fun TodoWidget(
    state: DashboardState,
    pack: ThemePack,
    onToggle: (String) -> Unit,
    onAdd: (String) -> Unit,
) {
    var draft by remember { mutableStateOf("") }
    state.todos.take(4).forEach { item ->
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Checkbox(
                checked = item.done,
                onCheckedChange = { onToggle(item.id) },
                colors = CheckboxDefaults.colors(checkedColor = pack.secondary),
                modifier = Modifier.size(36.dp),
            )
            Text(
                item.label,
                modifier = Modifier.weight(1f),
                fontSize = 13.sp,
                color = if (item.done) pack.mutedInk else pack.ink,
                textDecoration = if (item.done) TextDecoration.LineThrough else null,
            )
        }
    }
    OutlinedTextField(
        value = draft,
        onValueChange = { draft = it },
        placeholder = { Text("New task…", fontSize = 12.sp) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
        shape = RoundedCornerShape(14.dp),
        trailingIcon = {
            IconButton(onClick = {
                onAdd(draft)
                draft = ""
            }) {
                Icon(Icons.Rounded.Add, "Add task", tint = pack.secondary)
            }
        },
    )
}

@Composable
private fun NoteWidget(note: String, pack: ThemePack, onNoteChange: (String) -> Unit) {
    OutlinedTextField(
        value = note,
        onValueChange = onNoteChange,
        modifier = Modifier.fillMaxWidth().heightIn(min = 115.dp),
        shape = RoundedCornerShape(15.dp),
        placeholder = { Text("Write something lovely…") },
        maxLines = 5,
    )
    Text("Saved automatically", fontSize = 10.sp, color = pack.mutedInk, modifier = Modifier.padding(top = 5.dp))
}

@Composable
private fun FocusWidget(pack: ThemePack) {
    var seconds by remember { mutableIntStateOf(25 * 60) }
    var running by remember { mutableStateOf(false) }
    LaunchedEffect(running, seconds) {
        if (running && seconds > 0) {
            delay(1_000)
            seconds--
        } else if (seconds == 0) running = false
    }
    val minutes = seconds / 60
    val remainder = seconds % 60
    Box(Modifier.fillMaxWidth().height(94.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { 1f - seconds / (25f * 60f) },
            modifier = Modifier.size(88.dp),
            color = pack.secondary,
            trackColor = pack.border.copy(alpha = 0.16f),
            strokeWidth = 7.dp,
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🍅", fontSize = 22.sp)
            Text(
                "%02d:%02d".format(minutes, remainder),
                fontFamily = AppFontFamily,
                fontSize = 22.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = pack.ink,
            )
        }
    }
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = { running = !running },
            colors = ButtonDefaults.buttonColors(containerColor = pack.secondary),
        ) { Text(if (running) "Pause" else "Start") }
        TextButton(onClick = { running = false; seconds = 25 * 60 }) { Text("Reset", color = pack.primary) }
    }
}

@Composable
private fun HabitWidget(state: DashboardState, pack: ThemePack, onAdvance: (String) -> Unit) {
    val icons = mapOf("Water" to "💧", "Read" to "📖", "Move" to "🌿")
    icons.forEach { (name, icon) ->
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(icon, fontSize = 20.sp)
            Text(name, Modifier.weight(1f).padding(horizontal = 9.dp), fontWeight = FontWeight.SemiBold, color = pack.ink)
            val progress = state.habitProgress[name] ?: 0
            Text(
                (0 until 3).joinToString("") { if (it < progress) "♥" else "♡" },
                color = pack.secondary,
                fontSize = 18.sp,
                modifier = Modifier.clickable { onAdvance(name) },
            )
        }
    }
}

@Composable
private fun CountdownWidget(pack: ThemePack) {
    val now = LocalDate.now()
    var target = LocalDate.of(now.year, Month.DECEMBER, 31)
    if (!target.isAfter(now)) target = target.plusYears(1)
    val days = ChronoUnit.DAYS.between(now, target)
    Text(days.toString(), fontFamily = AppFontFamily, fontSize = 42.sp, fontWeight = FontWeight.Bold, color = pack.primary)
    Text("days until a new chapter", color = pack.mutedInk, fontSize = 12.sp)
    Spacer(Modifier.height(12.dp))
    Text("🌙  Keep a wish close", fontSize = 12.sp, color = pack.secondary)
}

private fun mix(first: Color, second: Color, amount: Float): Color = Color(
    red = first.red * (1f - amount) + second.red * amount,
    green = first.green * (1f - amount) + second.green * amount,
    blue = first.blue * (1f - amount) + second.blue * amount,
    alpha = first.alpha,
)
