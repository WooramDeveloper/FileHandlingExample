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
        DOCUMENT_FILE,
        FILE,
        ENCRYPTED_FILE,
        SHARED_PREFERENCE,
        ENCRYPTED_SHARED_PREFERENCE
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
                    .getInt(PREF_KEY_MENU_FILE, FileType.ENCRYPTED_FILE.ordinal)]

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
            FileType.DOCUMENT_FILE -> {
                memoRepository.readFromDocumentFile(context)
            }
            FileType.FILE -> {
                memoRepository.readFromFile(context)
            }
            FileType.ENCRYPTED_FILE -> {
                memoRepository.readFromEncryptedFile(context)
            }
            FileType.SHARED_PREFERENCE -> {
            }
            FileType.ENCRYPTED_SHARED_PREFERENCE -> {

            }
        }
    }

    fun writeMemo(context: Context, contents: String) {
        when (fileType.value) {
            FileType.DOCUMENT_FILE -> {
                memoRepository.writeToDocumentFile(context, contents)
            }
            FileType.FILE -> {
                memoRepository.writeToFile(context, contents)
            }
            FileType.ENCRYPTED_FILE -> {
                memoRepository.writeToEncryptedFile(context, contents)
            }
            FileType.SHARED_PREFERENCE -> {

            }
            FileType.ENCRYPTED_SHARED_PREFERENCE -> {

            }
        }
    }
}