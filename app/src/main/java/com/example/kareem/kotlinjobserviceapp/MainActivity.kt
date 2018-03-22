package com.example.kareem.kotlinjobserviceapp

import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Messenger
import android.os.PersistableBundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Button
import com.example.android.jobscheduler.*
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {



    // Handler for incoming messages from the service.
    lateinit private var handler: IncomingMessageHandler
    lateinit private var serviceComponent: ComponentName
    private var jobId = 0

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        handler = IncomingMessageHandler(this)
        serviceComponent = ComponentName(this, MyJobService::class.java)

        findViewById<Button>(R.id.cancel_button).setOnClickListener { cancelAllJobs() }
        findViewById<Button>(R.id.finished_button).setOnClickListener { finishJob() }
        findViewById<Button>(R.id.schedule_button).setOnClickListener { scheduleJob() }
    }

    override fun onStop() {

        stopService(Intent(this, MyJobService::class.java))
        super.onStop()
    }

    override fun onStart() {
        super.onStart()

        val startServiceIntent = Intent(this, MyJobService::class.java)
        val messengerIncoming = Messenger(handler)
        startServiceIntent.putExtra(MESSENGER_INTENT_KEY, messengerIncoming)
        startService(startServiceIntent)
    }

    private fun scheduleJob() {
        val builder = JobInfo.Builder(jobId++, serviceComponent)

        val delay = delay_time.text.toString()
        if (delay.isNotEmpty()) {
            builder.setMinimumLatency(delay.toLong() * TimeUnit.SECONDS.toMillis(1))
        }

        val deadline = deadline_time.text.toString()
        if (deadline.isNotEmpty()) {
            builder.setOverrideDeadline(deadline.toLong() * TimeUnit.SECONDS.toMillis(1))
        }

        if (checkboxWifi.isChecked) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_UNMETERED)
        } else if (checkboxAny.isChecked) {
            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        }


        val extras = PersistableBundle()
        var workDuration = duration_time.text.toString()
        if (workDuration.isEmpty()) workDuration = "1"
        extras.putLong(WORK_DURATION_KEY, workDuration.toLong() * TimeUnit.SECONDS.toMillis(1))


        builder.run {
            setRequiresDeviceIdle(checkbox_idle.isChecked)
            setRequiresCharging(checkbox_charging.isChecked)
            setExtras(extras)
        }


        Log.d(TAG, "Scheduling job")
        (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(builder.build())
    }


    private fun cancelAllJobs() {
        (getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancelAll()
        showToast(getString(R.string.all_jobs_cancelled))
    }


    private fun finishJob() {
        val jobScheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        val allPendingJobs = jobScheduler.allPendingJobs
        if (allPendingJobs.size > 0) {
            // Finish the last one.
            // Example: If jobs a, b, and c are queued in that order, this method will cancel job c.
            val id = allPendingJobs.first().id
            jobScheduler.cancel(id)
            showToast(getString(R.string.cancelled_job, id))
        } else {
            showToast(getString(R.string.no_jobs_to_cancel))
        }
    }

    companion object {
        private val TAG = "MainActivity"
    }
    fun Context.isNetworkAvailable():Boolean{
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        connectivityManager?.let {
            if(it.isDefaultNetworkActive) return true
        }
        return false
    }
}
