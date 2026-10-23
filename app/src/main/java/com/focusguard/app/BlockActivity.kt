package com.focusguard.app

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class BlockActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_PACKAGE = "package"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_block)

        findViewById<MaterialButton>(R.id.btnHome).setOnClickListener { goHome() }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })
        bind(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        if (intent != null) {
            setIntent(intent)
            bind(intent)
        }
    }

    private fun bind(i: Intent) {
        val taskId = i.getStringExtra(EXTRA_TASK_ID)
        val pkg = i.getStringExtra(EXTRA_PACKAGE).orEmpty()
        val task = Store.tasks(this).firstOrNull { it.id == taskId }

        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
        } catch (e: Exception) {
            if (pkg.isBlank()) "Cette application" else pkg
        }

        findViewById<TextView>(R.id.tvAppName).text = "$appName est bloquée"

        val tvTask = findViewById<TextView>(R.id.tvTask)
        val tvSubject = findViewById<TextView>(R.id.tvSubject)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val tvUntil = findViewById<TextView>(R.id.tvUntil)

        if (task != null) {
            tvTask.text = "Tâche en cours : ${task.title}"
            if (task.subject.isBlank()) {
                tvSubject.text = ""
                tvSubject.visibility = android.view.View.GONE
            } else {
                tvSubject.text = task.subject
                tvSubject.visibility = android.view.View.VISIBLE
            }
            tvMessage.text = task.message.ifBlank { DEFAULT_MESSAGE }
            tvUntil.text = "Accessible à partir de ${Fmt.time(task.endMin)}"
        } else {
            tvTask.text = "Tâche en cours"
            tvSubject.visibility = android.view.View.GONE
            tvMessage.text = DEFAULT_MESSAGE
            tvUntil.text = ""
        }
    }

    private fun goHome() {
        val home = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        startActivity(home)
        finish()
    }
}
