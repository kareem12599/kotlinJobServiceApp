
package com.example.android.jobscheduler

import android.os.Handler
import android.os.Message
import android.support.v4.content.ContextCompat.getColor
import android.view.View
import android.widget.TextView
import com.example.kareem.kotlinjobserviceapp.MainActivity
import com.example.kareem.kotlinjobserviceapp.R
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit


internal class IncomingMessageHandler(activity: MainActivity) : Handler() {

    private val mainActivity: WeakReference<MainActivity> = WeakReference(activity)

    override fun handleMessage(msg: Message) {
        val mainActivity = mainActivity.get() ?: return
        val showStartView = mainActivity.findViewById<View>(R.id.onstartTextView)
        val showStopView = mainActivity.findViewById<View>(R.id.onStopTextView)
        when (msg.what) {

            MSG_COLOR_START -> {

                showStartView.setBackgroundColor(getColor(mainActivity, R.color.start_received))
                updateParamsTextView(msg.obj, "started")
                sendMessageDelayed(Message.obtain(this, MSG_UNCOLOR_START),
                        TimeUnit.SECONDS.toMillis(1))
            }

            MSG_COLOR_STOP -> {
                showStopView.setBackgroundColor(getColor(mainActivity, R.color.stop_received))
                updateParamsTextView(msg.obj, "stopped")
                sendMessageDelayed(obtainMessage(MSG_UNCOLOR_STOP), TimeUnit.SECONDS.toMillis(1))
            }
            MSG_UNCOLOR_START -> {
                uncolorButtonAndClearText(showStartView, mainActivity)
            }
            MSG_UNCOLOR_STOP -> {
                uncolorButtonAndClearText(showStopView, mainActivity)
            }
        }
    }

    private fun uncolorButtonAndClearText(textView: View, activity: MainActivity) {
        textView.setBackgroundColor(getColor(activity, R.color.none_received))
        updateParamsTextView()
    }

    private fun updateParamsTextView(jobId: Any? = null, action: String = "") {
        val mainActivity = mainActivity.get() ?: return
        val paramsTextView = mainActivity.findViewById<TextView>(R.id.task_params)
        if (jobId == null) {
            paramsTextView.text = ""
            return
        }
        paramsTextView.text = mainActivity.getString(R.string.job_status, jobId.toString(), action)
    }
}
