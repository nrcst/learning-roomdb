package com.example.tugasenam

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Room
import kotlinx.coroutines.launch

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val text: String,
    val isCompleted: Boolean = false
)

class TodoViewModel(application: Application) : AndroidViewModel(application) {
    private val database = Room.databaseBuilder(
        application,
        TodoDatabase::class.java, "todo_database"
    ).build()

    private val todoDao = database.todoDao()

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState: StateFlow<TodoUiState> = _uiState.asStateFlow()

    init {
        loadTodos()
    }

    private fun loadTodos() {
        viewModelScope.launch {
            val todos = todoDao.getAllTodos()
            _uiState.value = TodoUiState(todos = todos)
        }
    }

    fun addTodo(text: String) {
        viewModelScope.launch {
            val newTodo = TodoItem(text = text)
            todoDao.insertTodo(newTodo)
            loadTodos()
        }
    }

    fun toggleTodo(id: Int) {
        viewModelScope.launch {
            val todo = uiState.value.todos.find { it.id == id }
            todo?.let {
                val updatedTodo = it.copy(isCompleted = !it.isCompleted)
                todoDao.updateTodo(updatedTodo)
                loadTodos()
            }
        }
    }

    fun deleteTodo(id: Int) {
        viewModelScope.launch {
            val todo = uiState.value.todos.find { it.id == id }
            todo?.let {
                todoDao.deleteTodo(it)
                loadTodos()
            }
        }
    }
}

data class TodoUiState(
    val todos: List<TodoItem> = emptyList()
)

@Composable
fun TodoAppScreen(
    modifier: Modifier = Modifier,
    viewModel: TodoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    TodoApp(
        todos = uiState.todos,
        onAddTodo = viewModel::addTodo,
        onToggleTodo = viewModel::toggleTodo,
        onDeleteTodo = viewModel::deleteTodo,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoApp(
    todos: List<TodoItem>,
    onAddTodo: (String) -> Unit,
    onToggleTodo: (Int) -> Unit,
    onDeleteTodo: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(TextFieldValue("")) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "TODO APP",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("New todo") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    if (text.text.isNotBlank()) {
                        onAddTodo(text.text)
                        text = TextFieldValue("")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn {
                items(todos) { todo ->
                    TodoItem(
                        todo = todo,
                        onToggle = { onToggleTodo(todo.id) },
                        onDelete = { onDeleteTodo(todo.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun TodoItem(todo: TodoItem, onToggle: () -> Unit, onDelete: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Checkbox(
            checked = todo.isCompleted,
            onCheckedChange = { onToggle() }
        )
        Text(
            text = todo.text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Delete")
        }
    }
}

@Preview
@Composable
private fun ToDo() {
    TodoAppScreen()
}