package com.example.todoproject

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TaskAdapter(private val tasks: List<Task>) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.taskName)
        val status: TextView = view.findViewById(R.id.taskStatus)
        val priority: TextView = view.findViewById(R.id.taskPriority)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.name.text = task.name
        holder.status.text = if (task.completed) "Completed" else "Pending"
        holder.priority.text = task.priority

        // Set priority color
        holder.priority.setTextColor(
            when (task.priority) {
                "High" -> Color.RED
                "Medium" -> Color.parseColor("#FFA500") // Orange
                else -> Color.GREEN
            }
        )
    }

    override fun getItemCount() = tasks.size
}