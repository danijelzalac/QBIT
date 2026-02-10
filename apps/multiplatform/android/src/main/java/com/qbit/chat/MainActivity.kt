package com.qbit.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.View
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.platform.ClipboardManager
import androidx.fragment.app.FragmentActivity
import chat.simplex.app.SimplexApp
import chat.simplex.app.SimplexService
import chat.simplex.app.CallService
import chat.simplex.app.model.NtfManager
import chat.simplex.app.model.NtfManager.getUserIdFromIntent
import chat.simplex.common.*
import chat.simplex.common.helpers.*
import chat.simplex.common.model.*
import chat.simplex.common.ui.theme.*
import chat.simplex.common.views.chatlist.*
import chat.simplex.common.views.helpers.*
import chat.simplex.common.views.onboarding.*
import chat.simplex.common.platform.*
import chat.simplex.res.MR
import kotlinx.coroutines.*
import java.lang.ref.WeakReference
import androidx.compose.runtime.*
import com.qbit.chat.views.CalendarCoverScreen
import androidx.compose.material.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import dev.icerock.moko.resources.compose.painterResource

object QbitLock {
    var isUnlocked by mutableStateOf(false)
}

class MainActivity: FragmentActivity() {
  companion object {
    const val OLD_ANDROID_UI_FLAGS = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    mainActivity = WeakReference(this)
    platform.androidSetNightModeIfSupported()
    val c = CurrentColors.value.colors
    platform.androidSetStatusAndNavigationBarAppearance(c.isLight, c.isLight)
    applyAppLocale(ChatModel.controller.appPrefs.appLanguage)
    // This flag makes status bar and navigation bar fully transparent. But on API level < 30 it breaks insets entirely
    // https://issuetracker.google.com/issues/236862874
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }
    super.onCreate(savedInstanceState)
    // testJson()
    // When call ended and orientation changes, it re-process old intent, it's unneeded.
    // Only needed to be processed on first creation of activity
    if (savedInstanceState == null) {
      processNotificationIntent(intent)
      processIntent(intent)
      processExternalIntent(intent)
    }
    if (ChatController.appPrefs.privacyProtectScreen.get()) {
      if (BuildConfig.DEBUG) Log.d(TAG, "onCreate: set FLAG_SECURE")
      window.setFlags(
        WindowManager.LayoutParams.FLAG_SECURE,
        WindowManager.LayoutParams.FLAG_SECURE
      )
    }
    // QBIT: Force FLAG_SECURE always to protect against app switcher snapshots
    window.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
    )
    
    enableEdgeToEdge()
    setContent {
      // SECURITY: Root Entrypoint is ALWAYS Calendar Cover.
      // Chat UI is only rendered if explicitly unlocked.
      // This prevents UI flickering during startup.
      if (QbitLock.isUnlocked) {
        Box(modifier = Modifier.fillMaxSize()) {
            AppScreen()
            // QBIT: Quick Exit Button (Always visible when unlocked)
            FloatingActionButton(
                onClick = { QbitLock.isUnlocked = false },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 24.dp)
                    .size(48.dp),
                backgroundColor = Color(0xFFF5B301) // Warning Yellow
            ) {
                Icon(
                    painter = painterResource(MR.images.ic_lock), 
                    contentDescription = "Quick Exit",
                    tint = Color.Black
                )
            }
        }
      } else {
        CalendarCoverScreen(onUnlock = { QbitLock.isUnlocked = true })
      }
    }
    SimplexApp.context.schedulePeriodicServiceRestartWorker()
    SimplexApp.context.schedulePeriodicWakeUp()
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    processIntent(intent)
    processExternalIntent(intent)
  }

  override fun onResume() {
    super.onResume()
    AppLock.recheckAuthState()
    withApi {
      delay(1000)
      if (!isAppOnForeground) return@withApi
      /**
       * When the app calls [ClipboardManager.shareText] and a user copies text in clipboard, Android denies
       * access to clipboard because the app considered in background.
       * This will ensure that the app will get the event on resume
       * */
      val service = getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
      chatModel.clipboardHasText.value = service.hasPrimaryClip()
    }
  }

  override fun onPause() {
    super.onPause()
    /**
     * SECURITY: App Switcher Protection
     * When the app loses focus or is paused, immediately lock to Cover.
     * This ensures the Recent Apps snapshot shows the Calendar, not the Chat.
     */
    AppLock.appWasHidden()
    QbitLock.isUnlocked = false
  }

  override fun onStop() {
    super.onStop()
    /**
     * SECURITY: Background Protection
     * Stop all media and lock the UI state.
     */
    VideoPlayerHolder.stopAll()
    AppLock.appWasHidden()
    QbitLock.isUnlocked = false
  }

  override fun onBackPressed() {
    val canFinishActivity = (
        onBackPressedDispatcher.hasEnabledCallbacks() // Has something to do in a backstack
            || Build.VERSION.SDK_INT >= Build.VERSION_CODES.R // Android 11 or above
            || isTaskRoot // there are still other tasks after we reach the main (home) activity
        ) && SimplexApp.context.chatModel.sharedContent.value !is SharedContent.Forward
    if (canFinishActivity) {
      // https://medium.com/mobile-app-development-publication/the-risk-of-android-strandhogg-security-issue-and-how-it-can-be-mitigated-80d2ddb4af06
      super.onBackPressed()
    }

    if (!onBackPressedDispatcher.hasEnabledCallbacks() && ChatController.appPrefs.performLA.get()) {
      // When pressed Back and there is no one wants to process the back event, clear auth state to force re-auth on launch
      AppLock.clearAuthState()
      AppLock.laFailed.value = true
    }
    if (!onBackPressedDispatcher.hasEnabledCallbacks()) {
      val sharedContent = chatModel.sharedContent.value
      // Drop shared content
      chatModel.sharedContent.value = null
      if (sharedContent is SharedContent.Forward) {
        chatModel.chatId.value = sharedContent.fromChatInfo.id
      }
      if (canFinishActivity) {
        finish()
      }
    }
  }
}

fun processNotificationIntent(intent: Intent?) {
  val userId = getUserIdFromIntent(intent)
  when (intent?.action) {
    NtfManager.OpenChatAction -> {
      val chatId = intent.getStringExtra("chatId")
      if (BuildConfig.DEBUG) Log.d(TAG, "processNotificationIntent: OpenChatAction $chatId")
      if (chatId != null) {
        ntfManager.openChatAction(userId, chatId)
      }
    }
    NtfManager.ShowChatsAction -> {
      if (BuildConfig.DEBUG) Log.d(TAG, "processNotificationIntent: ShowChatsAction")
      ntfManager.showChatsAction(userId)
    }
    NtfManager.AcceptCallAction -> {
      val chatId = intent.getStringExtra("chatId")
      if (chatId == null || chatId == "") return
      if (BuildConfig.DEBUG) Log.d(TAG, "processNotificationIntent: AcceptCallAction $chatId")
      ntfManager.acceptCallAction(chatId)
    }
  }
}

fun processIntent(intent: Intent?) {
  when (intent?.action) {
    "android.intent.action.VIEW" -> {
      val uri = intent.data
      if (uri != null) {
        chatModel.appOpenUrl.value = null to uri.toString()
      } else {
        AlertManager.shared.showAlertMsg(generalGetString(MR.strings.error_parsing_uri_title), generalGetString(MR.strings.error_parsing_uri_desc))
      }
    }
  }
}

fun processExternalIntent(intent: Intent?) {
  when (intent?.action) {
    Intent.ACTION_SEND -> {
      // Close active chat and show a list of chats
      chatModel.chatId.value = null
      chatModel.clearOverlays.value = true
      when {
        intent.type == "text/plain" -> {
          val text = intent.getStringExtra(Intent.EXTRA_TEXT)
          val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
          if (uri != null) {
            if (uri.scheme != "content") return showWrongUriAlert()
            // Shared file that contains plain text, like `*.log` file
            chatModel.sharedContent.value = SharedContent.File(text ?: "", uri.toURI())
          } else if (text != null) {
            // Shared just a text
            chatModel.sharedContent.value = SharedContent.Text(text)
          }
        }
        isMediaIntent(intent) -> {
          val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
          if (uri != null) {
            if (uri.scheme != "content") return showWrongUriAlert()
            chatModel.sharedContent.value = SharedContent.Media(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "", listOf(uri.toURI()))
          } // All other mime types
        }
        else -> {
          val uri = intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
          if (uri != null) {
            if (uri.scheme != "content") return showWrongUriAlert()
            chatModel.sharedContent.value = SharedContent.File(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "", uri.toURI())
          }
        }
      }
    }
    Intent.ACTION_SEND_MULTIPLE -> {
      // Close active chat and show a list of chats
      chatModel.chatId.value = null
      chatModel.clearOverlays.value = true
      if (BuildConfig.DEBUG) Log.e(TAG, "ACTION_SEND_MULTIPLE ${intent.type}")
      when {
        isMediaIntent(intent) -> {
          val uris = intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM) as? List<Uri>
          if (uris != null) {
            if (uris.any { it.scheme != "content" }) return showWrongUriAlert()
            chatModel.sharedContent.value = SharedContent.Media(intent.getStringExtra(Intent.EXTRA_TEXT) ?: "", uris.map { it.toURI() })
          } // All other mime types
        }
        else -> {}
      }
    }
  }
}

fun isMediaIntent(intent: Intent): Boolean =
  intent.type?.startsWith("image/") == true || intent.type?.startsWith("video/") == true

//fun testJson() {
//  val str: String = """
//  """.trimIndent()
//
//  println(json.decodeFromString<APIResult>(str))
//}
