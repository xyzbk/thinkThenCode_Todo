package com.example.todoproject

data class Task(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val category: String = "",
    val priority: String = "",
    val date: String = "",
    val completed: Boolean = false,
    val username: String = "",
    val createdAt: Long = 0
)