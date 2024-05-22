package com.example.study.presentation.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.study.R
import com.example.study.domain.model.Session
import com.example.study.domain.model.Subject
import com.example.study.domain.model.Task
import com.example.study.presentation.components.AddSubjectDialog
import com.example.study.presentation.components.CountCard
import com.example.study.presentation.components.DeleteDialog
import com.example.study.presentation.components.SubjectCard
import com.example.study.presentation.components.studySessionsList
import com.example.study.presentation.components.tasksList
import com.example.study.presentation.destinations.SessionScreenRouteDestination
import com.example.study.presentation.destinations.SubjectScreenRouteDestination
import com.example.study.presentation.destinations.TaskScreenRouteDestination
import com.example.study.presentation.subject.SubjectScreenNavArgs
import com.example.study.presentation.task.TaskScreenNavArgs
import com.example.study.util.SnackbarEvent
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootNavGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest

@RootNavGraph(start = true)
@Destination()
@Composable
fun DashboardScreenRoute(
    navigator: DestinationsNavigator
) {
    val viewModel: DashboardViewModel = hiltViewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val recentSessions by viewModel.recentSession.collectAsStateWithLifecycle()

    DashboardScreen(
        state = state,
        tasks = tasks,
        recentSessions = recentSessions,
        snackbarEvent = viewModel.snackbarEventFlow,
        onEvent = viewModel::onEvent,
        onSubjectCardClick = {subjectId ->
            subjectId?.let {
                val navArgs = SubjectScreenNavArgs(subjectId)
                navigator.navigate(SubjectScreenRouteDestination(navArgs))
            }

        },
        onTaskCardClick = {taskId ->
            val navArgs = TaskScreenNavArgs(taskId = taskId,subjectId=null)
            navigator.navigate(TaskScreenRouteDestination(navArgs))

        },
        onStartSessionButtonClick = {
            navigator.navigate(SessionScreenRouteDestination())
        }
    )
}

@Composable
private fun DashboardScreen(
    state: DashboardState,
    tasks: List<Task>,
    recentSessions: List<Session>,
    snackbarEvent: SharedFlow<SnackbarEvent>,
    onEvent: (DashboardEvent) -> Unit,
    onSubjectCardClick: (Int?) -> Unit,
    onTaskCardClick: (Int?) -> Unit,
    onStartSessionButtonClick: () -> Unit,

){
    var isAddSubjectDialogOpen by rememberSaveable {
        mutableStateOf(false)
    }

    var isDeleteDialogOpen by rememberSaveable {
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

                SnackbarEvent.NavigateUp -> {}
            }
        }
    }

    AddSubjectDialog(
        onDismissRequest = { isAddSubjectDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(DashboardEvent.SaveSubject)
            isAddSubjectDialogOpen = false
        },
        isOpen = isAddSubjectDialogOpen,
        selectedColors = state.subjectCardColors,
        onColorChange = { onEvent(DashboardEvent.OnSubjectCardColorChange(it)) },
        subjectName = state.subjectName,
        goalHour = state.studyHoursGoal,
        onSubjectNameChange = { onEvent(DashboardEvent.OnSubjectNameChange(it)) },
        onGoalHourChange =  { onEvent(DashboardEvent.OnStudyHoursGoalChange(it)) }
    )

    DeleteDialog(
        onDismissRequest = { isDeleteDialogOpen = false },
        onConfirmButtonClicked = {
            onEvent(DashboardEvent.DeleteSession)
            isDeleteDialogOpen = false
        },
        title = "Delete Session",
        isOpen = isDeleteDialogOpen,
        bodyText = "Are you sure you want to delete this session? \n" +
                "This action cannot be undone"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = { DashboardTopBar() }
    ) {paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                CountCardSection(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    subjectCount = state.totalSubjectCount,
                    hoursStudied = state.totalHoursStudied.toString(),
                    goalHours = state.totalStudyHoursGoal.toString()
                )
            }
            item {
                SubjectCardsSection(
                    modifier = Modifier.fillMaxWidth(),
                    subjectList = state.subjects,
                    onAddIconClicked = { isAddSubjectDialogOpen = true },
                    onSubjectCardClick = onSubjectCardClick
                )
            }

            item {
                Button(
                    onClick = onStartSessionButtonClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 48.dp, vertical = 20.dp)
                ) {
                    Text(
                        text = "Start Study Session"
                    )
                }
            }
            tasksList(
                sectionTitle = "UPCOMING TASKS",
                emptyLisText = "No upcoming tasks found. \n" +
                        "Click the + button in the subject screen to add tasks",
                tasks = tasks,
                onCheckBoxClick = { onEvent(DashboardEvent.OnTaskIsCompleteChange(it)) },
                onTaskCardClick = onTaskCardClick
            )
            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
            studySessionsList(
                sectionTitle = "RECENT STUDY SESSIONS",
                emptyLisText = "No recent study sessions found. \n" +
                        "Start a study session to begin recording your progress",
                sessions = recentSessions,
                onDeleteIconClick = {
                    onEvent(DashboardEvent.OnDeleteSessionButtonClick(it))
                    isDeleteDialogOpen = true
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DashboardTopBar(){
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = "StudyBelle",
                style = MaterialTheme.typography.headlineMedium
            )
        }
    )
}

@Composable
private fun CountCardSection(
    modifier: Modifier,
    subjectCount: Int,
    hoursStudied: String,
    goalHours: String
){
    Row(
        modifier = modifier
    ) {
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "Subject Count",
            count = "$subjectCount"
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "Hours Studied",
            count = hoursStudied
        )
        Spacer(modifier = Modifier.width(10.dp))
        CountCard(
            modifier = Modifier.weight(1f),
            headingText = "Study Goal Hours",
            count = goalHours
        )
    }
}

@Composable
private fun SubjectCardsSection(
    modifier: Modifier,
    subjectList: List<Subject>,
    emptyLisText: String = "No subjects found. \n Click the + button to add one.",
    onAddIconClicked: () -> Unit,
    onSubjectCardClick: (Int?) -> Unit
){
    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "SUBJECTS",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 12.dp)
            )
            IconButton(onClick = onAddIconClicked) {
                Icon(
                    imageVector = Icons.TwoTone.Add,
                    contentDescription = "Add Subject"
                )
            }
        }
        if (subjectList.isEmpty()){
            Image(
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally),
                painter = painterResource(id = R.drawable.img_book_removebg_preview),
                contentDescription = emptyLisText
            )
            Text(
                modifier=Modifier.fillMaxWidth(),
                text = emptyLisText,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(start=12.dp, end=12.dp)
        ){
            items(subjectList){subject ->
                SubjectCard(
                    subjectName = subject.name,
                    gradientColor = subject.colors.map { Color(it) },
                    onClick = { onSubjectCardClick(subject.subjectId) }
                )

            }
        }
    }
}