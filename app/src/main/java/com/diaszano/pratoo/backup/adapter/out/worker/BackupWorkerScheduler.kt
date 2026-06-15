package com.diaszano.pratoo.backup.adapter.out.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupWorkerScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
    ) {
        companion object {
            private const val UNIQUE_WORK_NAME = "drive_recipe_backup"
            private const val PERIODIC_INTERVAL_HOURS = 24L
        }

        fun schedulePeriodicBackup() {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                PeriodicWorkRequestBuilder<DriveBackupWorker>(
                    PERIODIC_INTERVAL_HOURS,
                    TimeUnit.HOURS,
                ).setConstraints(constraints)
                    .setBackoffCriteria(
                        BackoffPolicy.EXPONENTIAL,
                        1,
                        TimeUnit.MINUTES,
                    ).build()

            WorkManager
                .getInstance(context)
                .enqueueUniquePeriodicWork(
                    UNIQUE_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    workRequest,
                )
        }

        fun cancelPeriodicBackup() {
            WorkManager
                .getInstance(context)
                .cancelUniqueWork(UNIQUE_WORK_NAME)
        }

        fun runOneTimeBackup() {
            val constraints =
                Constraints
                    .Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()

            val workRequest =
                OneTimeWorkRequestBuilder<DriveBackupWorker>()
                    .setConstraints(constraints)
                    .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    UNIQUE_WORK_NAME,
                    ExistingWorkPolicy.KEEP,
                    workRequest,
                )
        }
    }
