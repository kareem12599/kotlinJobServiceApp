/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.jobscheduler

import android.app.Service
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.os.Messenger
import android.os.RemoteException
import android.util.Log


class MyJobService : JobService() {

    private var activityMessenger: Messenger? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        activityMessenger = intent.getParcelableExtra(MESSENGER_INTENT_KEY)
        return Service.START_NOT_STICKY
    }

    override fun onStartJob(params: JobParameters): Boolean {

        sendMessage(MSG_COLOR_START, params.jobId)

        val duration = params.extras.getLong(WORK_DURATION_KEY)
        Handler().postDelayed({
            sendMessage(MSG_COLOR_STOP, params.jobId)
            jobFinished(params, false)
        }, duration)
        Log.i(TAG, "on start job: ${params.jobId}")

        return true
    }

    override fun onStopJob(params: JobParameters): Boolean {
        sendMessage(MSG_COLOR_STOP, params.jobId)
        Log.i(TAG, "on stop job: ${params.jobId}")

        return false
    }

    private fun sendMessage(messageID: Int, params: Any?) {
        if (activityMessenger == null) {
            Log.d(TAG, "Service is bound, not started. There's no callback to send a message to.")
            return
        }
        val message = Message.obtain()
        message.run {
            what = messageID
            obj = params
        }
        try {
            activityMessenger?.send(message)
        } catch (e: RemoteException) {
            Log.e(TAG, "Error passing service object back to activity.")
        }
    }

    companion object {
        private val TAG = "MyJobService"
    }
}
