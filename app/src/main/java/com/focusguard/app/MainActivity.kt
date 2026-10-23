package com.focusguard.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.materialswitch.MaterialSwitch

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: TaskAdapter
    private lateinit var tasks: MutableList<Task>
    private lateinit var tvStatusTitle: TextView
    private lateinit var tvStatusDesc: TextView
    private lateinit var ivStatus: ImageView
    private lateinit var btnActivate: MaterialButton
    private lateinit var btnApps: MaterialButton
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvStatusTitle = findViewById(R.id.tvStatusTitle)
        tvStatusDesc = findViewById(R.id.tvStatusDesc)
        ivStatus = findViewById(R.id.ivStatus)
        btnActivate = findViewById(R.id.btnActivate)
        btnApps = findViewById(R.id.btnApps)
        tvEmpty = findViewById(R.id.tvEmpty)

        val rv = findViewById<RecyclerView>(R.id.rvTasks)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = TaskAdapter(
            onClick = { openEdit(it.id) },
            onToggle = { task, on ->
                task.enabled = on
                Store.saveTasks(this, tasks)
            }
        )
        rv.adapter = adapter

        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener { openEdit(null) }
        btnApps.setOnClickListener { startActivity(Intent(this, AppsActivity::class.java)) }
        btnActivate.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        tasks = Store.tasks(this)
        adapter.submit(tasks)
        tvEmpty.visibility = if (tasks.isEmpty()) View.VISIBLE else View.GONE

        val count = Store.blockedApps(this).size
        btnApps.text = if (count == 0) "Choisir mes applications" else "Mes applications ($count)"

        if (Store.isServiceEnabled(this)) {
            tvStatusTitle.text = "Protection active"
            tvStatusDesc.text = "Les applications choisies sont bloquées pendant tes tâches."
            ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.success))
            btnActivate.visibility = View.GONE
        } else {
            tvStatusTitle.text = "Protection désactivée"
            tvStatusDesc.text = "Active FocusGuard dans les réglages d'accessibilité pour que le blocage fonctionne."
            ivStatus.setColorFilter(ContextCompat.getColor(this, R.color.danger))
            btnActivate.visibility = View.VISIBLE
        }
    }

    private fun openEdit(taskId: String?) {
        val i = Intent(this, TaskEditActivity::class.java)
        if (taskId != null) i.putExtra(TaskEditActivity.EXTRA_TASK_ID, taskId)
        startActivity(i)
    }
}

class TaskAdapter(
    private val onClick: (Task) -> Unit,
    private val onToggle: (Task, Boolean) -> Unit
) : RecyclerView.Adapter<TaskAdapter.VH>() {

    private var items: List<Task> = emptyList()

    fun submit(list: List<Task>) {
        items = list
        notifyDataSetChanged()
    }

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.findViewById(R.id.tvTitle)
        val subject: TextView = v.findViewById(R.id.tvSubject)
        val time: TextView = v.findViewById(R.id.tvTime)
        val sw: MaterialSwitch = v.findViewById(R.id.swEnabled)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false))

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(h: VH, position: Int) {
        val t = items[position]
        h.title.text = t.title
        h.subject.text = if (t.subject.isBlank()) "Sans matière" else t.subject
        h.time.text = "${Fmt.days(t.days)} · ${Fmt.time(t.startMin)} – ${Fmt.time(t.endMin)}"
        h.sw.setOnCheckedChangeListener(null)
        h.sw.isChecked = t.enabled
        h.sw.setOnCheckedChangeListener { _, on -> onToggle(t, on) }
        h.itemView.setOnClickListener { onClick(t) }
    }
}
