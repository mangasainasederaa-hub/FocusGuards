package com.focusguard.app

import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import org.json.JSONArray
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import java.util.UUID

const val DEFAULT_MESSAGE = "Reste concentré : ton travail d'abord, le reste attendra."

data class Task(
    val id: String = UUID.randomUUID().toString(),
    var title: String,
    var subject: String,
    var days: Set<Int>,
    var startMin: Int,
    var endMin: Int,
    var message: String,
    var enabled: Boolean = true
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put("id", id)
        put("title", title)
        put("subject", subject)
        put("days", JSONArray(days.toList()))
        put("startMin", startMin)
        put("endMin", endMin)
        put("message", message)
        put("enabled", enabled)
    }

    fun isActiveAt(cal: Calendar): Boolean {
        if (!enabled) return false
        if (cal.get(Calendar.DAY_OF_WEEK) !in days) return false
        val m = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
        return m in startMin until endMin
    }

    companion object {
        fun fromJson(o: JSONObject): Task {
            val arr = o.optJSONArray("days") ?: JSONArray()
            val d = mutableSetOf<Int>()
            for (i in 0 until arr.length()) d.add(arr.getInt(i))
            return Task(
                id = o.optString("id", UUID.randomUUID().toString()),
                title = o.optString("title"),
                subject = o.optString("subject"),
                days = d,
                startMin = o.optInt("startMin", 480),
                endMin = o.optInt("endMin", 600),
                message = o.optString("message"),
                enabled = o.optBoolean("enabled", true)
            )
        }
    }
}

object Fmt {
    val dayOrder = listOf(
        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY,
        Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
    )
    val dayNames = listOf("Lun", "Mar", "Mer", "Jeu", "Ven", "Sam", "Dim")

    fun time(min: Int): String = String.format(Locale.ROOT, "%02d:%02d", min / 60, min % 60)

    fun days(days: Set<Int>): String {
        if (days.size == 7) return "Tous les jours"
        return dayOrder.indices.filter { dayOrder[it] in days }.joinToString(" ") { dayNames[it] }
    }
}

object Store {
    private const val PREFS = "focusguard"
    private const val K_TASKS = "tasks"
    private const val K_APPS = "blocked_apps"
    private const val K_SUBJECTS = "custom_subjects"

    private fun prefs(c: Context) = c.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun tasks(c: Context): MutableList<Task> {
        val out = mutableListOf<Task>()
        try {
            val arr = JSONArray(prefs(c).getString(K_TASKS, "[]"))
            for (i in 0 until arr.length()) out.add(Task.fromJson(arr.getJSONObject(i)))
        } catch (e: Exception) {
            // données illisibles : on repart d'une liste vide
        }
        return out
    }

    fun saveTasks(c: Context, list: List<Task>) {
        val arr = JSONArray()
        list.forEach { arr.put(it.toJson()) }
        prefs(c).edit().putString(K_TASKS, arr.toString()).apply()
    }

    fun blockedApps(c: Context): Set<String> =
        HashSet(prefs(c).getStringSet(K_APPS, emptySet()) ?: emptySet())

    fun setBlockedApps(c: Context, apps: Set<String>) {
        prefs(c).edit().putStringSet(K_APPS, HashSet(apps)).apply()
    }

    fun customSubjects(c: Context): MutableList<String> {
        val out = mutableListOf<String>()
        try {
            val arr = JSONArray(prefs(c).getString(K_SUBJECTS, "[]"))
            for (i in 0 until arr.length()) out.add(arr.getString(i))
        } catch (e: Exception) {
        }
        return out
    }

    fun saveCustomSubjects(c: Context, list: List<String>) {
        val arr = JSONArray()
        list.forEach { arr.put(it) }
        prefs(c).edit().putString(K_SUBJECTS, arr.toString()).apply()
    }

    /** Retourne la tâche en cours si cette appli doit être bloquée maintenant. */
    fun activeTaskFor(c: Context, pkg: String): Task? {
        if (pkg !in blockedApps(c)) return null
        val cal = Calendar.getInstance()
        return tasks(c).firstOrNull { it.isActiveAt(cal) }
    }

    fun isServiceEnabled(c: Context): Boolean {
        val enabled = Settings.Secure.getString(
            c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        val cn = ComponentName(c, BlockerService::class.java)
        val full = cn.flattenToString()
        val short = cn.flattenToShortString()
        return enabled.split(':').any { it.equals(full, true) || it.equals(short, true) }
    }
}
