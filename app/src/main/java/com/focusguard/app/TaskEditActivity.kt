package com.focusguard.app

import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText

class TaskEditActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_TASK_ID = "task_id"
    }

    private var editing: Task? = null
    private var subject: String = ""
    private var startMin = 8 * 60
    private var endMin = 10 * 60
    private val chips = mutableListOf<Chip>()

    private lateinit var etTitle: TextInputEditText
    private lateinit var etMessage: TextInputEditText
    private lateinit var tvSubject: TextView
    private lateinit var btnStart: MaterialButton
    private lateinit var btnEnd: MaterialButton

    private val pickSubject =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
            if (r.resultCode == RESULT_OK) {
                subject = r.data?.getStringExtra(SubjectPickerActivity.EXTRA_SUBJECT) ?: subject
                renderSubject()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_task_edit)

        etTitle = findViewById(R.id.etTitle)
        etMessage = findViewById(R.id.etMessage)
        tvSubject = findViewById(R.id.tvSubject)
        btnStart = findViewById(R.id.btnStart)
        btnEnd = findViewById(R.id.btnEnd)
        val chipGroup = findViewById<ChipGroup>(R.id.chipDays)
        val btnDelete = findViewById<MaterialButton>(R.id.btnDelete)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Chips des jours
        for (name in Fmt.dayNames) {
            val chip = LayoutInflater.from(this).inflate(R.layout.item_chip_day, chipGroup, false) as Chip
            chip.text = name
            chipGroup.addView(chip)
            chips.add(chip)
        }

        // Chargement d'une tâche existante
        val id = intent.getStringExtra(EXTRA_TASK_ID)
        editing = Store.tasks(this).firstOrNull { it.id == id }
        val t = editing
        if (t != null) {
            findViewById<TextView>(R.id.tvScreenTitle).text = "Modifier la tâche"
            etTitle.setText(t.title)
            etMessage.setText(t.message)
            subject = t.subject
            startMin = t.startMin
            endMin = t.endMin
            Fmt.dayOrder.forEachIndexed { i, d -> chips[i].isChecked = d in t.days }
            btnDelete.visibility = android.view.View.VISIBLE
        } else {
            etMessage.setText(DEFAULT_MESSAGE)
            // Lundi à vendredi par défaut
            for (i in 0..4) chips[i].isChecked = true
        }
        renderSubject()
        renderTimes()

        findViewById<MaterialCardView>(R.id.cardSubject).setOnClickListener {
            pickSubject.launch(Intent(this, SubjectPickerActivity::class.java))
        }
        btnStart.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> startMin = h * 60 + m; renderTimes() },
                startMin / 60, startMin % 60, true).show()
        }
        btnEnd.setOnClickListener {
            TimePickerDialog(this, { _, h, m -> endMin = h * 60 + m; renderTimes() },
                endMin / 60, endMin % 60, true).show()
        }
        findViewById<MaterialButton>(R.id.btnSave).setOnClickListener { save() }
        btnDelete.setOnClickListener { confirmDelete() }
    }

    private fun renderSubject() {
        if (subject.isBlank()) {
            tvSubject.text = "Choisir une matière"
            tvSubject.setTextColor(getColor(R.color.text_secondary))
        } else {
            tvSubject.text = subject
            tvSubject.setTextColor(getColor(R.color.text_primary))
        }
    }

    private fun renderTimes() {
        btnStart.text = "Début  ${Fmt.time(startMin)}"
        btnEnd.text = "Fin  ${Fmt.time(endMin)}"
    }

    private fun save() {
        var title = etTitle.text?.toString()?.trim().orEmpty()
        if (title.isEmpty()) title = subject
        if (title.isEmpty()) {
            Toast.makeText(this, "Donne un titre ou choisis une matière.", Toast.LENGTH_SHORT).show()
            return
        }
        val days = Fmt.dayOrder.filterIndexed { i, _ -> chips[i].isChecked }.toSet()
        if (days.isEmpty()) {
            Toast.makeText(this, "Choisis au moins un jour.", Toast.LENGTH_SHORT).show()
            return
        }
        if (endMin <= startMin) {
            Toast.makeText(this, "L'heure de fin doit être après le début.", Toast.LENGTH_SHORT).show()
            return
        }
        val message = etMessage.text?.toString()?.trim().orEmpty().ifEmpty { DEFAULT_MESSAGE }

        val list = Store.tasks(this)
        val current = editing
        if (current != null) {
            val idx = list.indexOfFirst { it.id == current.id }
            val updated = current.copy(
                title = title, subject = subject, days = days,
                startMin = startMin, endMin = endMin, message = message
            )
            if (idx >= 0) list[idx] = updated else list.add(updated)
        } else {
            list.add(
                Task(
                    title = title, subject = subject, days = days,
                    startMin = startMin, endMin = endMin, message = message
                )
            )
        }
        Store.saveTasks(this, list)
        finish()
    }

    private fun confirmDelete() {
        val current = editing ?: return
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer cette tâche ?")
            .setMessage(current.title)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Supprimer") { _, _ ->
                val list = Store.tasks(this)
                list.removeAll { it.id == current.id }
                Store.saveTasks(this, list)
                finish()
            }
            .show()
    }
}
