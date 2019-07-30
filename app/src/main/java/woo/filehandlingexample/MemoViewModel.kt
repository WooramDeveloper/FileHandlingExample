package woo.filehandlingexample

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by Wooram2 on 2019-07-08.
 */

private const val PREF_KEY_MENU_FILE = "prefKeyMenuFile"

class MemoViewModel(application: Application) : AndroidViewModel(application) {
    enum class FileType {
        DocumentFile,
        File,
        EncryptedFile
    }

    private val memoRepository = MemoRepository()
    var memoContents = memoRepository.memoContents
    var isFileAvailable = MutableLiveData<Boolean>()
    var fileType = MutableLiveData<FileType>()

    init {
        isFileAvailable.value = false
        fileType.value = FileType.values()[
                PreferenceManager
                    .getDefaultSharedPreferences(application)
                    .getInt(PREF_KEY_MENU_FILE, FileType.EncryptedFile.ordinal)]

        fileType.observeForever {
            it?.apply {
                PreferenceManager
                    .getDefaultSharedPreferences(application)
                    .edit()
                    .putInt(PREF_KEY_MENU_FILE, this.ordinal)
                    .apply()
            }
        }
    }

    fun readMemo(context: Context) {
        when (fileType.value) {
            FileType.DocumentFile -> {
                memoRepository.readFromDocumentFile(context)
            }
            FileType.File -> {
                memoRepository.readFromFile(context)
            }
            FileType.EncryptedFile -> {
                memoRepository.readFromEncryptedFile(context)
            }
        }
    }

    fun writeMemo(context: Context, contents: String) {
        when (fileType.value) {
            FileType.DocumentFile -> {
                memoRepository.writeToDocumentFile(context, contents)
            }
            FileType.File -> {
                memoRepository.writeToFile(context, contents)
            }
            FileType.EncryptedFile -> {
                memoRepository.writeToEncryptedFile(context, contents)
            }
        }
    }
}