package com.example.study.presentation.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.study.presentation.components.AddSubjectDialog
import com.example.study.presentation.components.CountCard
import com.example.study.presentation.components.DeleteDialog
import com.example.study.presentation.components.studySessionsList
import com.example.study.presentation.components.tasksList
import com.example.study.presentation.destinations.TaskScreenRouteDestination
import com.example.study.presentation.task.TaskScreenNavArgs
import com.example.study.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

data class SubjectScreenNavArgs(
    val subjectId: Int
)

@Destination(navArgsDelegate = SubjectScreenNavArgs::class)
@Composable
fun SubjectScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel: SubjectViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()

    SubjectScreen(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarEvent = viewModel.snackbarEventFlow,
        onBackButtonClick = { navigator.navigateUp() },
        onAddTaskButtonClick = {
            val navArgs = TaskScreenNavArgs(taskId = null,subjectId=state.currentSubjectId)
            navigator.navigate(TaskScreenRouteDestination(navArgs))

        },
        onTaskCardClick = {taskId ->
            val navArgs = TaskScreenNavArgs(taskId = taskId,subjectId=null)
            navigator.navigate(TaskScreenRouteDestination(navArgs))

        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SubjectScreen(
    state: SubjectState,
    onEvent: (SubjectEvent) -> Unit,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onBackButtonClick: () -> Unit,
    onAddTaskButtonClick: () -> Unit,
    onTaskCardClick: (Int?) -> Unit
) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val listState = rememberLazyListState()
    val isFABExpanded by remember { derivedStateOf { listState.firstVisibleItemIndex == 0 } }
    var isAddSubjectDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var isEditSubjectDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var isDeleteDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var isDeleteSubjectDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true){
        snackbarEvent.collectLatest { event ->
            when(event){
                is SnackbarEvent.ShowSnackBar -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = event.duration
                    )
                }

                SnackbarEvent.NavigateUp -> {
                    onBackButtonClick()
                }
            }
        }
    }

    LaunchedEffect(key1 = state.hoursStudied, key2 = state.studyHoursGoal){
        onEvent(SubjectEvent.UpdateProgress)
    }

    AddSubjectDialog(
        onDismissRequest = { isEditSubjectDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(SubjectEvent.UpdateSubject)
            isEditSubjectDialogOpen = false
                                 },
        isOpen = isEditSubjectDialogOpen,
        selectedColors = state.subjectCardColors,
        onColorChange = { onEvent(SubjectEvent.OnSubjectCardColorChange(it)) },
        subjectName = state.subjectName,
        goalHour = state.studyHoursGoal,
        onSubjectNameChange = { onEvent(SubjectEvent.OnSubjectNameChange(it)) },
        onGoalHourChange =  { onEvent(SubjectEvent.OnStudyHoursGoalChange(it)) }
    )

    DeleteDialog(
        onDismissRequest = { isDeleteDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(SubjectEvent.DeleteSession)
            isDeleteDialogOpen = false
                                 },
        title = "Delete Session",
        isOpen = isDeleteDialogOpen,
        bodyText = "Are you sure you want to delete this session? \n" +
                "This action cannot be undone"
    )

    DeleteDialog(
        onDismissRequest = { isDeleteSubjectDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(SubjectEvent.DeleteSubject)
            isDeleteSubjectDialogOpen = false
                                 },
        title = "Delete Subject",
        isOpen = isDeleteSubjectDialogOpen,
        bodyText = "Are you sure you want to delete this subject? \n" +
                "This action cannot be undone"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SubjectScreenTopBar(
                title = state.subjectName,
                onBackButtonClick = onBackButtonClick,
                onDeleteButtonClick = { isDeleteSubjectDialogOpen = true },
                onEditButtonClick = { isEditSubjectDialogOpen = true},
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddTaskButtonClick,
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add")},
                text = { Text(text = "Add Task")},
                expanded = isFABExpanded
            ) 
        }
    ) { paddingValue ->
        LazyColumn(
            state = listState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValue)
        ){
            item { 
                SubjectOverview(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    hoursStudied = state.hoursStudied.toString(),
                    goal = state.studyHoursGoal,
                    progress = state.progress
                )
            }
            tasksList(
                sectionTitle = "UPCOMING TASKS",
                emptyLisText = "No upcoming tasks found. \n" +
                        "Click the + button in the subject screen to add tasks",
                tasks = state.upcomingTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            tasksList(
                sectionTitle = "COMPLETED TASKS",
                emptyLisText = "No completed tasks found. \n" +
                        "Click the checkbox on completion of tasks",
                tasks = state.completedTasks,
                onCheckBoxClick = { onEvent(SubjectEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                emptyLisText = "No recent study sessions found. \n" +
                        "Start a study session to begin recording your progress",
                sessions = state.recentSessions,
                onDeleteIconClick = {
                    onEvent(SubjectEvent.OnDeleteSessionButtonClick(it))
                    isDeleteDialogOpen = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectScreenTopBar(
    title: String,
    onBackButtonClick: () -> Unit,
    onDeleteButtonClick: () -> Unit,
    onEditButtonClick: () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior
){
    LargeTopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
                         IconButton(onClick = onBackButtonClick) {
                             Icon(
                                 imageVector = Icons.Default.ArrowBack,
                                 contentDescription ="navigate back",
                                 tint = Color(0xFFAC332A)
                             )
                         }
        },
        title = {
            Text(
                text = title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.headlineSmall
            )
        },
        actions = {
            IconButton(onClick = onDeleteButtonClick) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Subject",
                    tint = Color(0xFFAC332A)

                )
            }
            IconButton(onClick = onEditButtonClick) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Subject",
                    tint = Color(0xFFAC332A)

                )
            }
        }
    )
}

@Composable
private fun SubjectOverview(
    modifier: Modifier,
    hoursStudied: String,
    goal: String,
    progress: Float
){
    val percentageProgress = remember(progress) {
        (progress * 100).toInt().coerceIn(0, 100)
    }
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        CountCard(
            modifier = Modifier
                .weight(1f),
            headingText = "Study Hours Goal",
            count = goal
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier
                .weight(1f),
            headingText = "Hours Studied",
            count = hoursStudied
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(
            modifier = Modifier.size(75.dp),
            contentAlignment = Alignment.Center
        ){
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = 1F,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
                color = MaterialTheme.colorScheme.surfaceVariant
            )
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                progress = progress,
                strokeWidth = 4.dp,
                strokeCap = StrokeCap.Round,
            )
            Text(text = "${percentageProgress}%")
        }
    }
}