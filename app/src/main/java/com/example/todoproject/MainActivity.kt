package com.example.todoproject

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    companion object {
        private val PRIORITY_ORDER = mapOf(
            "High" to 1,
            "Medium" to 2,
            "Low" to 3
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val username = intent.getStringExtra("username") ?: "User"
        val email = intent.getStringExtra("email") ?: "email@example.com"
        val completed = intent.getIntExtra("completedTasks", 0)
        val pending = intent.getIntExtra("pendingTasks", 0)

        findViewById<TextView>(R.id.usernameTextView).text = "Hey, $username"

        bottomNav = findViewById(R.id.bottomNavigationView)
        fab = findViewById(R.id.fabAddTask)

        val homeContent = findViewById<View>(R.id.homeContent)
        val profileContent = findViewById<FrameLayout>(R.id.profileContent)

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    homeContent.visibility = View.VISIBLE
                    profileContent.visibility = View.GONE
                    setupDayCards()
                    findViewById<TextView>(R.id.usernameTextView).text = "Hey, $username"
                    true
                }

                R.id.nav_add -> {
                    showAddTaskDialog()
                    true
                }

                R.id.nav_profile -> {
                    homeContent.visibility = View.GONE
                    profileContent.visibility = View.VISIBLE

                    val view = layoutInflater.inflate(R.layout.profile_info, null)
                    profileContent.removeAllViews()
                    profileContent.addView(view)

                    view.findViewById<TextView>(R.id.tvUserName).text = username
                    view.findViewById<TextView>(R.id.tvUserEmail).text = email
                    view.findViewById<TextView>(R.id.tvCompleted).text =
                        "Completed Tasks: $completed"
                    view.findViewById<TextView>(R.id.tvPending).text = "Pending Tasks: $pending"

                    view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags =
                            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    true
                }

                else -> false
            }
        }

        setupDayCards()
        loadCtgCounts()
        loadTdyTasksCount()
    }


    data class DayItem(
        val dayName: String,
        val dayNumber: String,
        val isToday: Boolean = false
    )

    class DaysAdapter(private val days: List<DayItem>) :
        RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

        class DayViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val dayName: TextView = view.findViewById(R.id.dayNameText)
            val dayNumber: TextView = view.findViewById(R.id.dayNumberText)
            val cardView: LinearLayout = view.findViewById(R.id.dayCard)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_day_card, parent, false)
            return DayViewHolder(view)
        }

        override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
            val day = days[position]
            holder.dayName.text = day.dayName
            holder.dayNumber.text = day.dayNumber
            if (day.isToday) {
                holder.cardView.setBackgroundResource(R.drawable.today_card_bg)
            } else {
                holder.cardView.setBackgroundResource(R.drawable.day_card_bg)
            }
        }

        override fun getItemCount() = days.size
    }

    private fun setupDayCards() {
        val daysRecyclerView = findViewById<RecyclerView>(R.id.daysRecyclerView)
        daysRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysRecyclerView.adapter = DaysAdapter(getDaysList())
    }

    private fun getDaysList(): List<DayItem> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<DayItem>()
        val dateFormatDay = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormatDate = SimpleDateFormat("d", Locale.getDefault())

        calendar.add(Calendar.DAY_OF_YEAR, -1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(
            DayItem(
                dateFormatDay.format(calendar.time),
                dateFormatDate.format(calendar.time),
                true
            )
        )

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        return days
    }

    private fun validateTaskInput(name: String, category: String, date: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter task name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (category.isEmpty()) {
            Toast.makeText(this, "Please select a category", Toast.LENGTH_SHORT).show()
            return false
        }
        if (date.isEmpty()) {
            Toast.makeText(this, "Please select a due date", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun saveTaskToFirestore(
        name: String,
        description: String,
        category: String,
        priority: String,
        date: String
    ) {

        val username = intent.getStringExtra("username") ?: run {
            Toast.makeText(this, "User not authorized", Toast.LENGTH_SHORT).show()
            return
        }

        val database = FirebaseDatabase.getInstance().reference
        val taskRef = database.child("tasks").push()

        val task = mapOf(
            "name" to name,
            "description" to description,
            "category" to category,
            "priority" to priority,
            "date" to date,
            "completed" to false,
            "username" to username,
            "createdAt" to System.currentTimeMillis()
        )

        taskRef.setValue(task)
            .addOnSuccessListener {
                Toast.makeText(this, "New task has been added successfully!", Toast.LENGTH_SHORT)
                    .show()
                loadCtgCounts()
                loadTdyTasksCount()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to add task: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showAddTaskDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_task)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        dialog.setCancelable(true)
        dialog.show()

        val categories = arrayOf("Grocery", "Learning", "Other")
        val categoryAdapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categories)
        val categorySpinner = dialog.findViewById<AppCompatSpinner>(R.id.spinnerCategory)
        categorySpinner.adapter = categoryAdapter

        val etTaskDate = dialog.findViewById<AppCompatEditText>(R.id.etTaskDate)
        etTaskDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    val selectedDate = Calendar.getInstance()
                    selectedDate.set(year, month, day)
                    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                    etTaskDate.setText(dateFormat.format(selectedDate.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        dialog.findViewById<AppCompatButton>(R.id.btnCancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<AppCompatButton>(R.id.btnAdd).setOnClickListener {
            val taskName = dialog.findViewById<AppCompatEditText>(R.id.etTaskName).text.toString()
            val description =
                dialog.findViewById<AppCompatEditText>(R.id.etTaskDescription).text.toString()
            val category =
                dialog.findViewById<AppCompatSpinner>(R.id.spinnerCategory).selectedItem.toString()
            val date = etTaskDate.text.toString()

            val priority =
                when (dialog.findViewById<RadioGroup>(R.id.radioGroupPriority).checkedRadioButtonId) {
                    R.id.radioLow -> "Low"
                    R.id.radioMedium -> "Medium"
                    R.id.radioHigh -> "High"
                    else -> "Medium"
                }

            if (validateTaskInput(taskName, category, date)) {
                saveTaskToFirestore(taskName, description, category, priority, date)
                dialog.dismiss()
            }
        }
    }

    private fun loadCtgCounts() {
        val username = intent.getStringExtra("username") ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("tasks")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val counts = mutableMapOf(
                        "Grocery" to 0,
                        "Learning" to 0,
                        "Other" to 0
                    )

                    for (taskSnapshot in snapshot.children) {
                        val category =
                            taskSnapshot.child("category").getValue(String::class.java) ?: continue
                        counts[category] = counts.getOrDefault(category, 0) + 1
                    }

                    findViewById<TextView>(R.id.groceryTaskCount).text =
                        "${counts["Grocery"]} tasks"
                    findViewById<TextView>(R.id.learningTaskCount).text =
                        "${counts["Learning"]} tasks"
                    findViewById<TextView>(R.id.otherTaskCount).text = "${counts["Other"]} tasks"
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@MainActivity,
                        "Error loading counts: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadTdyTasksCount() {
        val username = intent.getStringExtra("username") ?: return
        val today = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date())
        val database = FirebaseDatabase.getInstance().reference

        database.child("tasks")
            .orderByChild("username")
            .equalTo(username)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasks = mutableListOf<Task>()

                    for (taskSnapshot in snapshot.children) {
                        val task = Task(
                            id = taskSnapshot.key ?: "",
                            name = taskSnapshot.child("name").getValue(String::class.java) ?: "",
                            description = taskSnapshot.child("description").getValue(String::class.java) ?: "",
                            category = taskSnapshot.child("category").getValue(String::class.java) ?: "",
                            priority = taskSnapshot.child("priority").getValue(String::class.java) ?: "Medium",
                            date = taskSnapshot.child("date").getValue(String::class.java) ?: "",
                            completed = taskSnapshot.child("completed").getValue(Boolean::class.java) ?: false,
                            username = taskSnapshot.child("username").getValue(String::class.java) ?: "",
                            createdAt = taskSnapshot.child("createdAt").getValue(Long::class.java) ?: 0
                        )

                        if (task.date == today) {
                            tasks.add(task)
                        }
                    }

                    val sortedTasks = tasks.sortedWith(compareBy { PRIORITY_ORDER[it.priority] ?: 3 })

                    findViewById<TextView>(R.id.todayTasksCount).text = sortedTasks.size.toString()

                    val recyclerView = findViewById<RecyclerView>(R.id.todayTasksRecyclerView)
                    recyclerView.layoutManager = LinearLayoutManager(this@MainActivity)
                    recyclerView.adapter = TaskAdapter(sortedTasks)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@MainActivity, "Error loading tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}