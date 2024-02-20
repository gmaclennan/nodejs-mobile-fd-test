package expo.modules.saffilereader

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import expo.modules.kotlin.Promise
import expo.modules.kotlin.exception.Exceptions
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class SafFileReaderModule : Module() {
  private var pendingPromise: Promise? = null

  // Each module class must implement the definition function. The definition consists of components
  // that describes the module's functionality and behavior.
  // See https://docs.expo.dev/modules/module-api for more details about available components.
  override fun definition() = ModuleDefinition {
    // Sets the name of the module that JavaScript code will use to refer to the module. Takes a string as an argument.
    // Can be inferred from module's class name, but it's recommended to set it explicitly for clarity.
    // The module will be accessible from `requireNativeModule('SafFileReader')` in JavaScript.
    Name("SafFileReader")

    // Defines a JavaScript function that always returns a Promise and whose native code
    // is by default dispatched on the different thread than the JavaScript runtime runs on.
    AsyncFunction("getFile") { promise: Promise ->
      val currentActivity =
              appContext.currentActivity ?: throw Exceptions.MissingActivity()
      if (pendingPromise != null) {
        throw Exceptions.FileSystemModuleNotFound()
      }
      val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        /**
         * It's possible to limit the types of files by mime-type. Since this
         * app displays pages from a PDF file, we'll specify `application/pdf`
         * in `type`.
         * See [Intent.setType] for more details.
         */
        type = "*/*"

        /**
         * Because we'll want to use ContentResolver.openFileDescriptor to read
         * the data of whatever file is picked, we set [Intent.CATEGORY_OPENABLE]
         * to ensure this will succeed.
         */
        addCategory(Intent.CATEGORY_OPENABLE)
      }
      currentActivity.startActivityForResult(intent, OPEN_DOCUMENT_REQUEST_CODE)
      pendingPromise = promise
    }

    OnActivityResult { _, (requestCode, resultCode, data) ->
      if (requestCode == OPEN_DOCUMENT_REQUEST_CODE && pendingPromise != null) {
        val currentActivity =
                appContext.currentActivity ?: throw Exceptions.MissingActivity()
        val result = Bundle()
        val docUri = data?.data
        if (resultCode == Activity.RESULT_OK && docUri !== null) {
          val parcelFileDescriptor = currentActivity.contentResolver.openFileDescriptor(docUri, "r")
          val fd = parcelFileDescriptor?.detachFd()
          result.putBoolean("granted", true)
          if (fd !== null) {
            result.putInt("fd", fd)
          }
        } else {
          result.putBoolean("granted", false)
        }
        pendingPromise?.resolve(result)
        pendingPromise = null
      }
    }
  }
}

private const val OPEN_DOCUMENT_REQUEST_CODE = 0x33
