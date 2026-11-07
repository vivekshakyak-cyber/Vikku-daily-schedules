package com.vikku.dailyschedules

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vikku.dailyschedules.data.LocalDb
import com.vikku.dailyschedules.data.Habit
import com.vikku.dailyschedules.data.ScheduleTask
import com.vikku.dailyschedules.notifications.ReminderScheduler
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val Bg = Color(0xFF10091F)
private val Surface = Color(0xFF211536)
private val Purple = Color(0xFFB56CFF)
private val Cyan = Color(0xFF55E6FF)
private val Muted = Color(0xFFB9A9CC)

class MainActivity : ComponentActivity() {
    private val notificationPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        setContent { VikkuApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VikkuApp() {
    val context = LocalContext.current
    val db = remember { LocalDb(context) }
    var habits by remember { mutableStateOf(db.habits()) }
    var tasks by remember { mutableStateOf(db.tasks()) }
    var tab by remember { mutableStateOf(0) }
    var showHabitDialog by remember { mutableStateOf(false) }
    var showTaskDialog by remember { mutableStateOf(false) }
    fun refresh() { habits = db.habits(); tasks = db.tasks() }
    val completed = habits.count { it.done } + tasks.count { it.done }
    val total = habits.size + tasks.size
    val percent = if (total == 0) 0 else completed * 100 / total

    MaterialTheme(colorScheme = darkColorScheme(primary = Purple, secondary = Cyan, background = Bg, surface = Surface, onSurface = Color.White)) {
        Scaffold(containerColor = Bg, bottomBar = {
            NavigationBar(containerColor = Surface) {
                NavigationBarItem(selected = tab == 0, onClick = { tab = 0 }, icon = { Icon(Icons.Default.EventNote, null) }, label = { Text("Today") })
                NavigationBarItem(selected = tab == 1, onClick = { tab = 1 }, icon = { Icon(Icons.Default.Repeat, null) }, label = { Text("Habits") })
                NavigationBarItem(selected = tab == 2, onClick = { tab = 2 }, icon = { Icon(Icons.Default.CheckCircle, null) }, label = { Text("Schedule") })
            }
        }, floatingActionButton = {
            FloatingActionButton(onClick = { if (tab == 1) showHabitDialog = true else showTaskDialog = true }, containerColor = Purple, contentColor = Bg) { Icon(Icons.Default.Add, "Add") }
        }) { padding ->
            LazyColumn(Modifier.fillMaxSize().padding(padding).padding(horizontal = 18.dp), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(top = 22.dp, bottom = 24.dp)) {
                item {
                    Text("VIKKU", color = Purple, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 4.sp)
                    Text("Daily Schedules", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time), color = Muted, fontSize = 13.sp)
                }
                if (tab == 0) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(22.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(18.dp)) {
                                Text("YOUR DAILY PROGRESS", color = Muted, fontSize = 11.sp, letterSpacing = 1.5.sp)
                                Row(verticalAlignment = Alignment.Bottom) { Text("$percent%", color = Color.White, fontSize = 38.sp, fontWeight = FontWeight.Bold); Spacer(Modifier.width(8.dp)); Text("completed", color = Muted, modifier = Modifier.padding(bottom = 7.dp)) }
                                LinearProgressIndicator(progress = { percent / 100f }, modifier = Modifier.fillMaxWidth().height(9.dp), color = Cyan, trackColor = Bg)
                                Spacer(Modifier.height(8.dp)); Text("$completed of $total activities done", color = Muted, fontSize = 12.sp)
                            }
                        }
                    }
                    item { SectionTitle("Today's habits") }
                    items(habits, key = { "h${it.id}" }) { habit -> HabitRow(habit) { db.toggleHabit(habit.id, !habit.done); refresh() } }
                    item { SectionTitle("Today's schedule") }
                    items(tasks, key = { "t${it.id}" }) { task -> TaskRow(task, onToggle = { db.toggleTask(task.id, !task.done); refresh() }, onDelete = { db.deleteTask(task.id); refresh() }) }
                    if (habits.isEmpty() && tasks.isEmpty()) item { EmptyState() }
                } else if (tab == 1) {
                    item { Text("Build consistency, one day at a time.", color = Muted) }
                    items(habits, key = { "habit${it.id}" }) { habit -> HabitRow(habit) { db.toggleHabit(habit.id, !habit.done); refresh() } }
                    if (habits.isEmpty()) item { EmptyState() }
                } else {
                    item { Text("Plan your day and stay on track.", color = Muted) }
                    items(tasks, key = { "task${it.id}" }) { task -> TaskRow(task, onToggle = { db.toggleTask(task.id, !task.done); refresh() }, onDelete = { db.deleteTask(task.id); refresh() }) }
                    if (tasks.isEmpty()) item { EmptyState() }
                }
                item { Text("Private by design • Data stays on this device", color = Muted, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp)) }
            }
        }
    }
    if (showHabitDialog) AddHabitDialog(onDismiss = { showHabitDialog = false }, onSave = { db.addHabit(it); refresh(); showHabitDialog = false })
    if (showTaskDialog) AddTaskDialog(onDismiss = { showTaskDialog = false }, onSave = { title, time, priority, reminder ->
        db.addTask(title, time, priority, reminder)
        if (reminder != null) ReminderScheduler.schedule(context, (System.currentTimeMillis() % Int.MAX_VALUE).toInt(), title, reminder)
        refresh(); showTaskDialog = false
    })
}

@Composable private fun SectionTitle(title: String) { Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
@Composable private fun HabitRow(habit: Habit, onToggle: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = habit.done, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Cyan, uncheckedColor = Muted))
            Text(habit.name, color = Color.White, modifier = Modifier.weight(1f), fontWeight = FontWeight.Medium)
            if (habit.done) Text("DONE", color = Cyan, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }
    }
}
@Composable private fun TaskRow(task: ScheduleTask, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = Surface), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = task.done, onCheckedChange = { onToggle() }, colors = CheckboxDefaults.colors(checkedColor = Cyan, uncheckedColor = Muted))
            Column(Modifier.weight(1f)) {
                Text(task.title, color = if (task.done) Muted else Color.White, fontWeight = FontWeight.Medium)
                Text("${task.time}  •  ${task.priority} priority", color = Muted, fontSize = 11.sp)
            }
            TextButton(onClick = onDelete) { Text("Delete", color = Color(0xFFFF7B9C), fontSize = 11.sp) }
        }
    }
}
@Composable private fun EmptyState() { Text("Nothing planned here yet. Tap + to add your first item.", color = Muted, modifier = Modifier.padding(vertical = 20.dp)) }

@Composable private fun AddHabitDialog(onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("New habit") }, text = { OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Habit name") }, singleLine = true) }, confirmButton = { TextButton(onClick = { if (name.isNotBlank()) onSave(name.trim()) }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }, containerColor = Surface, titleContentColor = Color.White, textContentColor = Color.White)
}

@Composable private fun AddTaskDialog(onDismiss: () -> Unit, onSave: (String, String, String, Long?) -> Unit) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("09:00") }
    var priority by remember { mutableStateOf("Medium") }
    var reminder by remember { mutableStateOf(true) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add to schedule") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task name") }, singleLine = true)
            OutlinedButton(onClick = {
                val now = Calendar.getInstance()
                TimePickerDialog(context, { _, h, m -> time = String.format(Locale.US, "%02d:%02d", h, m) }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), false).show()
            }) { Text("Time: $time") }
            Text("Priority", color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) { listOf("Low", "Medium", "High").forEach { value -> FilterChip(selected = priority == value, onClick = { priority = value }, label = { Text(value) }) } }
            Row(verticalAlignment = Alignment.CenterVertically) { Checkbox(checked = reminder, onCheckedChange = { reminder = it }); Text("Notify me at this time", color = Color.White, fontSize = 13.sp) }
        }
    }, confirmButton = { TextButton(onClick = {
        if (title.isNotBlank()) {
            val parts = time.split(":"); val cal = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, parts[0].toInt()); set(Calendar.MINUTE, parts[1].toInt()); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0); if (time <= SimpleDateFormat("HH:mm", Locale.US).format(System.currentTimeMillis())) add(Calendar.DAY_OF_YEAR, 1) }
            onSave(title.trim(), time, priority, if (reminder) cal.timeInMillis else null)
        }
    }) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }, containerColor = Surface, titleContentColor = Color.White, textContentColor = Color.White)
}
