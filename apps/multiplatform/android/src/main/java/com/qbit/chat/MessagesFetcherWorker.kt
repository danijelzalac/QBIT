package com.qbit.chat

import android.content.Context
import androidx.work.*
import chat.simplex.common.platform.Log
import chat.simplex.common.helpers.getWorkManagerInstance
import chat.simplex.common.model.*
import chat.simplex.common.platform.NtfManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class MessagesFetcherWorker(appContext: Context, workerParams: WorkerParameters): CoroutineWorker(appContext, workerParams) {
  companion object {
    const val INPUT_DATA_INTERVAL = "interval"
    const val INPUT_DATA_DURATION = "duration"
    private const val WAIT_AFTER_LAST_MESSAGE: Long = 10_000
  }

  override suspend fun doWork(): Result {
    // Skip when Simplex service is currently working
    if (SimplexService.getServiceState(SimplexApp.context) == SimplexService.ServiceState.STARTED) {
      reschedule()
      return Result.success()
    }
    val durationSeconds = inputData.getInt(INPUT_DATA_DURATION, 60)
    var shouldReschedule = true
    try {
      // In case of self-destruct is enabled the initialization process will not start in SimplexApp, Let's start it here
      if (DatabaseUtils.ksSelfDestructPassword.get() != null && chatModel.chatDbStatus.value == null) {
        initChatControllerOnStart()
      }
      withTimeout(durationSeconds * 1000L) {
        val chatController = ChatController
        SimplexService.waitDbMigrationEnds(chatController)
        val chatDbStatus = chatController.chatModel.chatDbStatus.value
        if (chatDbStatus != DBMigrationResult.OK) {
          Log.w(TAG, "Worker: problem with the database: $chatDbStatus")
          showPassphraseNotification(chatDbStatus)
          shouldReschedule = false
          return@withTimeout
        }
        Log.w(TAG, "Worker: starting work")
        // Give some time to start receiving messages
        delay(10_000)
        while (!isStopped) {
          if (chatController.lastMsgReceivedTimestamp + WAIT_AFTER_LAST_MESSAGE < System.currentTimeMillis()) {
            Log.d(TAG, "Worker: work is done")
            break
          }
          delay(5000)
        }
      }
    } catch (_: TimeoutCancellationException) { // When timeout happens
      Log.d(TAG, "Worker: work is done (took $durationSeconds sec)")
    } catch (_: CancellationException) { // When user opens the app while the worker is still working
      Log.d(TAG, "Worker: interrupted")
    } catch (e: Exception) {
      Log.d(TAG, "Worker: unexpected exception: ${e.stackTraceToString()}")
    }

    if (shouldReschedule) reschedule()
    return Result.success()
  }

  private fun reschedule() = MessagesFetcherWorker.scheduleWork()
}
