
package sa.edu.iau.cshj.studyplanner

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable


// Lab 4 & Lab 7: Data and State


data class StudyTask(
    val id: Int,
    val title: String,
    val course: String,
    val isDone: Boolean = false
)

data class PlannerUiState(
    val greeting: String = "Welcome, Student!",
    val tasks: List<StudyTask> = emptyList()
)

private val sampleTasks = listOf(
    StudyTask(1, "Review Kotlin", "CSC 402"),
    StudyTask(2, "Finish Lab Report", "CSC 402"),
    StudyTask(3, "Prepare for Quiz", "CSC 403"),
    StudyTask(4, "Read Chapter 5", "CSC 311"),
    StudyTask(5, "Complete Assignment", "CSC 231")
)


// Lab 6: Save State on Rotation


private val plannerStateSaver = listSaver<PlannerUiState, Any>(
    save = { state ->
        buildList<Any> {
            add(state.greeting)
            state.tasks.forEach { task ->
                add(task.id)
                add(task.title)
                add(task.course)
                add(task.isDone)
            }
        }
    },
    restore = { saved ->
        PlannerUiState(
            greeting = saved[0] as String,
            tasks = saved.drop(1).chunked(4).map { item ->
                StudyTask(
                    id = item[0] as Int,
                    title = item[1] as String,
                    course = item[2] as String,
                    isDone = item[3] as Boolean
                )
            }
        )
    }
)

// Lab 7: Reusable Task Item


@Composable
fun TaskItem(
    task: StudyTask,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onTaskClick(task.id) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = { checked ->
                onDoneChange(task.id, checked)
            }
        )

        Column {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = task.course,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


// Lab 4 & Lab 7: Home Screen


@Composable
fun PlannerHomeScreen(
    uiState: PlannerUiState,
    onMarkDone: () -> Unit,
    onTaskClick: (Int) -> Unit,
    onDoneChange: (Int, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalTasks = uiState.tasks.size
    val doneTasks = uiState.tasks.count { it.isDone }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Study Planner",
            style = MaterialTheme.typography.headlineMedium
        )

        Text(uiState.greeting)

        Text(
            text = "$doneTasks of $totalTasks done",
            style = MaterialTheme.typography.titleMedium
        )

        if (totalTasks > 0) {
            LinearProgressIndicator(
                progress = {
                    doneTasks.toFloat() / totalTasks
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = onMarkDone,
            enabled = doneTasks < totalTasks
        ) {
            Text("Mark One Task Done")
        }

        Text(
            text = "Study Tasks",
            style = MaterialTheme.typography.titleLarge
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(
                uiState.tasks,
                key = { it.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = onTaskClick,
                    onDoneChange = onDoneChange
                )
            }
        }
    }
}


// Lab 4 & Lab 7: Home Route


@Composable
fun PlannerHomeRoute(
    onTaskClick: (Int) -> Unit
) {
    var uiState by rememberSaveable(
        stateSaver = plannerStateSaver
    ) {
        mutableStateOf(
            PlannerUiState(tasks = sampleTasks)
        )
    }

    PlannerHomeScreen(
        uiState = uiState,

        onMarkDone = {
            val firstUndone = uiState.tasks
                .firstOrNull { !it.isDone }

            if (firstUndone != null) {
                uiState = uiState.copy(
                    tasks = uiState.tasks.map { task ->
                        if (task.id == firstUndone.id) {
                            task.copy(isDone = true)
                        } else {
                            task
                        }
                    }
                )
            }
        },

        onTaskClick = onTaskClick,

        onDoneChange = { taskId, checked ->
            uiState = uiState.copy(
                tasks = uiState.tasks.map { task ->
                    if (task.id == taskId) {
                        task.copy(isDone = checked)
                    } else {
                        task
                    }
                }
            )
        }
    )
}

// Lab 5: Task Detail Screen


data class TaskDetailUiState(
    val title: String = "",
    val course: String = ""
)

@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onShareClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Task Detail",
            style = MaterialTheme.typography.headlineMedium
        )

        Text("Task: ${uiState.title}")
        Text("Course: ${uiState.course}")

        Button(onClick = onShareClick) {
            Text("Share")
        }

        OutlinedButton(onClick = onBackClick) {
            Text("Back")
        }
    }
}


// Lab 5: Share Intent

@Composable
fun TaskDetailRoute(
    taskId: Int,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current

    val task = sampleTasks.firstOrNull {
        it.id == taskId
    }

    val title = task?.title ?: "Unknown Task"
    val course = task?.course ?: ""

    TaskDetailScreen(
        uiState = TaskDetailUiState(
            title = title,
            course = course
        ),

        onShareClick = {
            val send = Intent(Intent.ACTION_SEND).apply {
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Today I'm studying: $title"
                )
                type = "text/plain"
            }

            context.startActivity(
                Intent.createChooser(send, "Share via")
            )
        },

        onBackClick = onBackClick
    )
}

// Lab 5: Navigation Routes

@Serializable
object PlannerHome

@Serializable
data class TaskDetail(
    val taskId: Int
)

// Lab 5: NavHost

@Composable
fun StudyPlannerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PlannerHome
    ) {
        composable<PlannerHome> {
            PlannerHomeRoute(
                onTaskClick = { taskId ->
                    navController.navigate(
                        TaskDetail(taskId)
                    )
                }
            )
        }

        composable<TaskDetail> { entry ->
            val detail = entry.toRoute<TaskDetail>()

            TaskDetailRoute(
                taskId = detail.taskId,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Main Activity

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                StudyPlannerApp()
            }
        }
    }
}

// Lab 7: Preview 1 - Not Done

@Preview(
    showBackground = true,
    name = "Task Not Done"
)
@Composable
fun TaskNotDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Review Kotlin", "CSC 402", false
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> }        )
    }
}

// Lab 7: Preview 2 - Done

@Preview(
    showBackground = true,
    name = "Task Done"
)
@Composable
fun TaskDonePreview() {
    MaterialTheme {
        TaskItem(
            task = StudyTask(
                1, "Review Kotlin", "CSC 402", true
            ),
            onTaskClick = {},
            onDoneChange = { _, _ -> }        )
    }
}

// Lab 7: Preview 3 - Full List

@Preview(
    showBackground = true,
    name = "Five Study Tasks"
)
@Composable
fun FiveTasksPreview() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                tasks = sampleTasks
            ),
            onMarkDone = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> }        )
    }
}


// Lab 4: Original Home Previews

@Preview(
    showBackground = true,
    name = "Nothing Completed"
)
@Composable
fun NothingCompletedPreview() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                tasks = sampleTasks
            ),
            onMarkDone = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> }        )
    }
}

@Preview(
    showBackground = true,
    name = "Everything Completed"
)
@Composable
fun EverythingCompletedPreview() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                tasks = sampleTasks.map {
                    it.copy(isDone = true)
                }
            ),
            onMarkDone = {},
            onTaskClick = {},
            onDoneChange = { _, _ -> }        )
    }
}

// Lab 5: Detail Previews

@Preview(
    showBackground = true,
    name = "Kotlin Detail"
)
@Composable
fun KotlinDetailPreview() {
    MaterialTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                "Review Kotlin", "CSC 402"
            ),
            onShareClick = {},
            onBackClick = {}
        )
    }
}

@Preview(
    showBackground = true,
    name = "Report Detail"
)
@Composable
fun ReportDetailPreview() {
    MaterialTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                "Finish Lab Report", "CSC 402"
            ),
            onShareClick = {},
            onBackClick = {}
        )
    }
}
