package com.tencent.matrix.lifecycle.supervisor

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.android.monitor.trace.lifecycle.IStateObserver
import com.android.monitor.trace.lifecycle.MatrixLifecycleThread
import com.android.monitor.trace.lifecycle.owners.ProcessUILifecycleOwner
import com.android.monitor.util.MatrixLog
import com.android.monitor.util.MatrixUtil
import com.android.monitor.util.safeApply

/**
 * Created by Yves on 2022/2/10
 */
internal object SubordinatePacemaker : BroadcastReceiver() {

    private const val SUPERVISOR_INSTALLED = "SUPERVISOR_INSTALLED"

    private var packageName: String? = null
    private val permission by lazy { "${packageName!!}.matrix.permission.PROCESS_SUPERVISOR" }

    @Volatile
    private var pacemaker: IStateObserver? = null

    private var callback: (() -> Unit)? = null

    fun install(context: Context?, callback: () -> Unit) {
        if (pacemaker != null) {
            MatrixLog.e(ProcessSupervisor.tag, "SubordinatePacemaker: already installed")
            return
        }
        if (ProcessSupervisor.isSupervisor) {
            return
        }
        this.callback = callback
        packageName = MatrixUtil.getPackageName(context)
        val filter = IntentFilter()
        filter.addAction(SUPERVISOR_INSTALLED)
        context?.registerReceiver(this, filter, permission, null)

        pacemaker = object : IStateObserver {
            override fun on() {
                MatrixLifecycleThread.handler.post {
                    MatrixLog.i(ProcessSupervisor.tag, "SubordinatePacemaker: callback when foreground")
                    callback.invoke()
                }
            }

            override fun off() {}
        }
        ProcessUILifecycleOwner.startedStateOwner.observeForever(pacemaker!!)
    }

    fun uninstall(context: Context?) {
        if (pacemaker!= null) {
            ProcessUILifecycleOwner.startedStateOwner.removeObserver(pacemaker!!)
            pacemaker = null
            safeApply(ProcessSupervisor.tag) {
                context?.unregisterReceiver(this)
            }
            MatrixLog.i(ProcessSupervisor.tag, "SubordinatePacemaker: uninstalled")
        }
    }

    fun notifySupervisorInstalled(context: Context?) {
        packageName = MatrixUtil.getPackageName(context)
        Intent(SUPERVISOR_INSTALLED).apply {
            context?.sendBroadcast(this, permission)
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            SUPERVISOR_INSTALLED -> {
                MatrixLifecycleThread.handler.post {
                    MatrixLog.i(ProcessSupervisor.tag, "SubordinatePacemaker: callback when supervisor installed")
                    callback?.invoke()
                }
            }
        }
    }
}