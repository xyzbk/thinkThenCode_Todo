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
        val subText: TextView = itemView.findViewById(R.id.subText)
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
        holder.subText.text = task.description
        holder.priority.text = task.priority

        if (task.description.isEmpty()) {
            holder.subText.visibility = View.GONE
        } else {
            holder.subText.visibility = View.VISIBLE
        }

        holder.subText.setOnClickListener {
            if (holder.subText.maxLines == 2) {
                holder.subText.maxLines = Integer.MAX_VALUE
            } else {
                holder.subText.maxLines = 2
            }
        }

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