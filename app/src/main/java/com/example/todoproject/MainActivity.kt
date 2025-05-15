package com.example.todoproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1. Set username from intent
        val username = intent.getStringExtra("username")
        findViewById<TextView>(R.id.usernameTextView).text = "Hey, $username"

        // 2. Initialize views
        bottomNav = findViewById(R.id.bottomNavigationView)
        fab = findViewById(R.id.fabAddTask)

        // 3. Setup bottom navigation
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    Toast.makeText(this, "Home selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_add -> {
                    Toast.makeText(this, "Add selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }

        // 4. Setup FAB
        fab.setOnClickListener {
            Toast.makeText(this, "FAB clicked - Add Task", Toast.LENGTH_SHORT).show()
        }

        // 5. Initialize and setup days RecyclerView
        setupDayCards()
    }

    // Data class for day items
    data class DayItem(
        val dayName: String,
        val dayNumber: String,
        val isToday: Boolean = false
    )

    // Adapter for day cards
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

            // Highlight today's card
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
        daysRecyclerView.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        )

        daysRecyclerView.adapter = DaysAdapter(getDaysList())
    }

    private fun getDaysList(): List<DayItem> {
        val calendar = Calendar.getInstance()
        val days = mutableListOf<DayItem>()
        val dateFormatDay = SimpleDateFormat("EEE", Locale.getDefault())
        val dateFormatDate = SimpleDateFormat("d", Locale.getDefault())

        // Yesterday (-1 day)
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        days.add(DayItem(
            dateFormatDay.format(calendar.time),
            dateFormatDate.format(calendar.time)
        ))

        // Today (reset +1 day)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(
            dateFormatDay.format(calendar.time),
            dateFormatDate.format(calendar.time),
            true // Mark as today
        ))

        // Tomorrow (+1 day)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(
            dateFormatDay.format(calendar.time),
            dateFormatDate.format(calendar.time)
        ))

        // Day after tomorrow (+1 day)
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        days.add(DayItem(
            dateFormatDay.format(calendar.time),
            dateFormatDate.format(calendar.time)
        ))

        return days
    }
}