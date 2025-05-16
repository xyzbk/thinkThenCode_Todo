package com.example.todoproject
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.app.Activity

class AddTask : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descEditText: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.add_task)

        titleEditText = findViewById(R.id.editTaskTitle)
        descEditText = findViewById(R.id.editTaskDescription)
        saveButton = findViewById(R.id.btnSaveTask)

        saveButton.setOnClickListener {
            val title = titleEditText.text.toString().trim()
            val desc = descEditText.text.toString().trim()

            if (title.isEmpty()) {
                Toast.makeText(this, "Please enter a title", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // رجع البيانات إلى MainActivity أو خزّنها بقاعدة البيانات
            val resultIntent = Intent()
            resultIntent.putExtra("task_title", title)
            resultIntent.putExtra("task_desc", desc)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}