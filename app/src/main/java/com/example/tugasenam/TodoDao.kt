package com.example.tugasenam

import androidx.room.*

@Dao
interface TodoDao {
    @Query("SELECT * FROM todo_items")
    suspend fun getAllTodos(): List<TodoItem>

    @Insert
    suspend fun insertTodo(todo: TodoItem)

    @Update
    suspend fun updateTodo(todo: TodoItem)

    @Delete
    suspend fun deleteTodo(todo: TodoItem)
}
