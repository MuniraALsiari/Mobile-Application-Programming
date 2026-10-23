package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

data class PlannerUiState(
    val greeting: String = "Welcome, Student!",
    val totalTasks: Int = 3,
    val doneTasks: Int = 0
)

@Composable
fun PlannerHomeScreen(
    uiState: PlannerUiState,
    onMarkDone: () -> Unit,
    modifier: Modifier = Modifier
) {
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
            text = "${uiState.doneTasks} of ${uiState.totalTasks} done"
        )

        Button(
            onClick = onMarkDone,
            enabled = uiState.doneTasks < uiState.totalTasks
        ) {
            Text("Mark One Task Done")
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "Nothing Done"
)
@Composable
fun PlannerPreview1() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                totalTasks = 3,
                doneTasks = 0
            ),
            onMarkDone = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    showBackground = true,
    name = "All Tasks Done"
)
@Composable
fun PlannerPreview2() {
    MaterialTheme {
        PlannerHomeScreen(
            uiState = PlannerUiState(
                totalTasks = 3,
                doneTasks = 3
            ),
            onMarkDone = {}
        )
    }
}

@Composable
fun PlannerHomeRoute() {
    var uiState by remember {
        mutableStateOf(
            PlannerUiState(
                greeting = "Welcome, Student!",
                totalTasks = 3,
                doneTasks = 0
            )
        )
    }

    PlannerHomeScreen(
        uiState = uiState,
        onMarkDone = {
            if (uiState.doneTasks < uiState.totalTasks) {
                uiState = uiState.copy(
                    doneTasks = uiState.doneTasks + 1
                )
            }
        }
    )
}
