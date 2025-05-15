package com.example.todoproject

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

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
                    view.findViewById<TextView>(R.id.tvCompleted).text = "Completed Tasks: $completed"
                    view.findViewById<TextView>(R.id.tvPending).text = "Pending Tasks: $pending"

                    view.findViewById<TextView>(R.id.tvLogout).setOnClickListener {
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }

                    true
                }
                else -> false
            }
        }

        fab.setOnClickListener {}

        setupDayCards()
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
        daysRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysRecyclerView.adapter = DaysAdapter(getDaysList())
    }

    private fun getDaysList(): List<DayItem> {
        val calendar = java.util.Calendar.getInstance()
        val days = mutableListOf<DayItem>()
        val dateFormatDay = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val dateFormatDate = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())

        calendar.add(java.util.Calendar.DAY_OF_YEAR, -1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time), true))

        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        calendar.add(java.util.Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(dateFormatDay.format(calendar.time), dateFormatDate.format(calendar.time)))

        return days
    }
}
