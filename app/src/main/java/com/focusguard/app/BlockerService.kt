package com.focusguard.app

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent

/**
 * Surveille l'appli qui passe au premier plan. Si elle fait partie des applis
 * choisies et qu'une tâche est en cours, affiche l'écran de blocage.
 */
class BlockerService : AccessibilityService() {

    private var lastBlock = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) return

        val task = Store.activeTaskFor(this, pkg) ?: return

        val now = SystemClock.elapsedRealtime()
        if (now - lastBlock < 700) return
        lastBlock = now

        val intent = Intent(this, BlockActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra(BlockActivity.EXTRA_TASK_ID, task.id)
            putExtra(BlockActivity.EXTRA_PACKAGE, pkg)
        }
        startActivity(intent)
    }

    override fun onInterrupt() {}
}
