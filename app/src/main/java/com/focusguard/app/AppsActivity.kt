package com.focusguard.app

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText

data class AppItem(val label: String, val pkg: String, val icon: Drawable)

class AppsActivity : AppCompatActivity() {

    private val all = mutableListOf<AppItem>()
    private val selected = mutableSetOf<String>()
    private lateinit var adapter: AppAdapter
    private lateinit var tvCount: TextView
    private lateinit var etSearch: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_apps)

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }
        tvCount = findViewById(R.id.tvCount)
        etSearch = findViewById(R.id.etSearch)
        val progress = findViewById<ProgressBar>(R.id.progress)

        selected.addAll(Store.blockedApps(this))
        updateCount()

        adapter = AppAdapter(selected) {
            Store.setBlockedApps(this, selected)
            updateCount()
        }
        val rv = findViewById<RecyclerView>(R.id.rvApps)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        etSearch.doAfterTextChanged { applyFilter() }

        Thread {
            val pm = packageManager
            val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val homePkgs = pm.queryIntentActivities(homeIntent, 0).map { it.activityInfo.packageName }.toSet()
            val excluded = homePkgs + setOf(packageName, "com.android.settings")
            val list = pm.queryIntentActivities(launcherIntent, 0)
                .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm)) }
                .filter { it.pkg !in excluded }
                .distinctBy { it.pkg }
                .sortedBy { it.label.lowercase() }
            runOnUiThread {
                all.clear()
                all.addAll(list)
                progress.visibility = View.GONE
                applyFilter()
            }
        }.start()
    }

    private fun applyFilter() {
        val q = etSearch.text?.toString()?.trim()?.lowercase().orEmpty()
        adapter.submit(if (q.isEmpty()) all.toList() else all.filter { it.label.lowercase().contains(q) })
    }

    private fun updateCount() {
        val n = selected.size
        tvCount.text = when (n) {
            0 -> "Aucune application sélectionnée"
            1 -> "1 application sélectionnée"
            else -> "$n applications sélectionnées"
        }
    }
}

class AppAdapter(
    private val selected: MutableSet<String>,
    private val onChanged: () -> Unit
) : RecyclerView.Adapter<AppAdapter.VH>() {

    private var items: List<AppItem> = emptyList()

    fun submit(list: List<AppItem>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.ivIcon)
        val label: TextView = v.findViewById(R.id.tvLabel)
        val check: CheckBox = v.findViewById(R.id.cbSelected)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_app, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val item = items[position]
        h.icon.setImageDrawable(item.icon)
        h.label.text = item.label
        h.check.setOnCheckedChangeListener(null)
        h.check.isChecked = item.pkg in selected
        val toggle = { on: Boolean ->
            if (on) selected.add(item.pkg) else selected.remove(item.pkg)
            onChanged()
        }
        h.check.setOnCheckedChangeListener { _, on -> toggle(on) }
        h.itemView.setOnClickListener { h.check.isChecked = !h.check.isChecked }
    }
}
