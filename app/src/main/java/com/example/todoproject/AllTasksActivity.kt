package com.example.todoproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AllTasksActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var tasksContainer: LinearLayout
    private lateinit var profileContent: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_tasks)

        val username = intent.getStringExtra("username") ?: "User"
        val email = intent.getStringExtra("email") ?: "email@example.com"

        bottomNav = findViewById(R.id.bottomNavigationView)
        tasksContainer = findViewById(R.id.tasksContainer)
        profileContent = findViewById(R.id.profileContent)

        if (intent.getBooleanExtra("showProfile", false)) {
            showProfileView(username, email)
        } else {
            loadAllTasks()
        }

        setupBottomNavigation(username, email)
    }

    private fun setupBottomNavigation(username: String, email: String) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtras(intent.extras ?: Bundle())
                    })
                    finish()
                    true
                }
                R.id.nav_add -> {
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        putExtras(intent.extras ?: Bundle())
                        putExtra("openDialog", true)
                    })
                    finish()
                    true
                }
                R.id.nav_all_tasks -> {
                    tasksContainer.visibility = View.VISIBLE
                    profileContent.visibility = View.GONE
                    true
                }
                R.id.nav_profile -> {
                    showProfileView(username, email)
                    true
                }
                else -> false
            }
        }
        bottomNav.selectedItemId = if (intent.getBooleanExtra("showProfile", false)) {
            R.id.nav_profile
        } else {
            R.id.nav_all_tasks
        }
    }

    private fun showProfileView(username: String, email: String) {
        tasksContainer.visibility = View.GONE
        profileContent.visibility = View.VISIBLE
        profileContent.removeAllViews()

        val view = layoutInflater.inflate(R.layout.profile_info, null)
        profileContent.addView(view)

        view.findViewById<TextView>(R.id.tvUserName).text = username
        view.findViewById<TextView>(R.id.tvUserEmail).text = email

        loadTtlTsksCount(view)

        view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadTtlTsksCount(profileView: View) {
        val username = intent.getStringExtra("username") ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("tasks")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val totalTasks = snapshot.childrenCount
                    profileView.findViewById<TextView>(R.id.tvTotalTasks).text =
                        "Total Tasks: $totalTasks"
                }

                override fun onCancelled(error: DatabaseError) {
                    profileView.findViewById<TextView>(R.id.tvTotalTasks).text =
                        "Total Tasks: Error loading"
                    Toast.makeText(
                        this@AllTasksActivity,
                        "Error loading tasks count: ${error.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    private fun loadAllTasks() {
        val username = intent.getStringExtra("username") ?: return
        val database = FirebaseDatabase.getInstance().reference

        database.child("tasks")
            .orderByChild("username")
            .equalTo(username)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val tasksByCategory = mutableMapOf<String, MutableList<Task>>()

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

                        if (!tasksByCategory.containsKey(task.category)) {
                            tasksByCategory[task.category] = mutableListOf()
                        }
                        tasksByCategory[task.category]?.add(task)
                    }

                    displayTasksByCategory(tasksByCategory)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@AllTasksActivity, "Error loading tasks: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun displayTasksByCategory(tasksByCategory: Map<String, List<Task>>) {
        tasksContainer.removeAllViews()

        val sortedCategories = tasksByCategory.keys.sorted()

        for (category in sortedCategories) {
            val categoryHeader = LayoutInflater.from(this)
                .inflate(R.layout.item_category_header, tasksContainer, false)
            categoryHeader.findViewById<TextView>(R.id.categoryName).text = category
            tasksContainer.addView(categoryHeader)

            val tasks = tasksByCategory[category] ?: continue
            val sortedTasks = tasks.sortedWith(compareBy { MainActivity.PRIORITY_ORDER[it.priority] ?: 3 })

            val recyclerView = RecyclerView(this).apply {
                layoutManager = LinearLayoutManager(this@AllTasksActivity)
                adapter = TaskAdapter(sortedTasks)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setPadding(0, 8.dpToPx(), 0, 32.dpToPx())
                }
                isNestedScrollingEnabled = false
            }
            tasksContainer.addView(recyclerView)
        }
    }

    private fun Int.dpToPx(): Int = (this * resources.displayMetrics.density).toInt()
}