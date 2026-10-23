package com.focusguard.app

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import java.text.Normalizer

sealed class Row {
    data class Header(val title: String) : Row()
    data class Item(val name: String, val custom: Boolean) : Row()
}

class SubjectPickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SUBJECT = "subject"
    }

    private lateinit var adapter: SubjectAdapter
    private lateinit var etSearch: TextInputEditText
    private var custom = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_picker)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        etSearch = findViewById(R.id.etSearch)

        adapter = SubjectAdapter(
            onClick = { choose(it.name) },
            onLong = { if (it.custom) confirmDelete(it.name) }
        )
        val rv = findViewById<RecyclerView>(R.id.rvSubjects)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        etSearch.doAfterTextChanged { rebuild() }
        findViewById<MaterialButton>(R.id.btnAddCustom).setOnClickListener { showAddDialog() }

        custom = Store.customSubjects(this)
        rebuild()
    }

    private fun norm(s: String): String =
        Normalizer.normalize(s, Normalizer.Form.NFD).replace(Regex("\\p{M}"), "").lowercase()

    private fun rebuild() {
        val q = norm(etSearch.text?.toString()?.trim().orEmpty())
        val rows = mutableListOf<Row>()

        val mine = custom.filter { q.isEmpty() || norm(it).contains(q) }
        if (mine.isNotEmpty()) {
            rows.add(Row.Header("Mes matières"))
            mine.forEach { rows.add(Row.Item(it, true)) }
        }
        for ((category, list) in Subjects.catalog) {
            val matches = list.filter { q.isEmpty() || norm(it).contains(q) }
            if (matches.isNotEmpty()) {
                rows.add(Row.Header(category))
                matches.forEach { rows.add(Row.Item(it, false)) }
            }
        }
        adapter.submit(rows)
    }

    private fun choose(name: String) {
        setResult(RESULT_OK, Intent().putExtra(EXTRA_SUBJECT, name))
        finish()
    }

    private fun showAddDialog() {
        val input = EditText(this).apply {
            hint = "Nom de la matière"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
            setSingleLine()
            setText(etSearch.text?.toString()?.trim().orEmpty())
            setSelection(text.length)
        }
        val pad = (20 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(this).apply {
            setPadding(pad, pad / 2, pad, 0)
            addView(input)
        }
        MaterialAlertDialogBuilder(this)
            .setTitle("Nouvelle matière")
            .setView(container)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Ajouter") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotEmpty()) {
                    val known = Subjects.catalog.flatMap { it.second } + custom
                    if (known.none { it.equals(name, ignoreCase = true) }) {
                        custom.add(name)
                        Store.saveCustomSubjects(this, custom)
                    }
                    choose(name)
                }
            }
            .show()
    }

    private fun confirmDelete(name: String) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Supprimer cette matière ?")
            .setMessage(name)
            .setNegativeButton("Annuler", null)
            .setPositiveButton("Supprimer") { _, _ ->
                custom.remove(name)
                Store.saveCustomSubjects(this, custom)
                rebuild()
            }
            .show()
    }
}

class SubjectAdapter(
    private val onClick: (Row.Item) -> Unit,
    private val onLong: (Row.Item) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var rows: List<Row> = emptyList()

    fun submit(list: List<Row>) {
        rows = list
        notifyDataSetChanged()
    }

    class HeaderVH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvHeader)
    }

    class ItemVH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(R.id.tvName)
        val badge: TextView = v.findViewById(R.id.tvBadge)
    }

    override fun getItemViewType(position: Int): Int = if (rows[position] is Row.Header) 0 else 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == 0) {
            HeaderVH(inflater.inflate(R.layout.item_header_subject, parent, false))
        } else {
            ItemVH(inflater.inflate(R.layout.item_subject, parent, false))
        }
    }

    override fun getItemCount(): Int = rows.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val row = rows[position]) {
            is Row.Header -> (holder as HeaderVH).title.text = row.title.uppercase()
            is Row.Item -> {
                val h = holder as ItemVH
                h.name.text = row.name
                h.badge.visibility = if (row.custom) View.VISIBLE else View.GONE
                h.itemView.setOnClickListener { onClick(row) }
                h.itemView.setOnLongClickListener { onLong(row); true }
            }
        }
    }
}
