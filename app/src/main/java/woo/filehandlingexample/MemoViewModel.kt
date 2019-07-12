package woo.filehandlingexample

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.preference.PreferenceManager

/**
 * Created by Wooram2 on 2019-07-08.
 */

private const val PREF_KEY_MENU_FILE = "prefKeyMenuFile"

class MemoViewModel(application: Application) : AndroidViewModel(application) {
    enum class MenuFile {
        DOCUMENT_FILE,
        FILE,
        ENCRYPTED_FILE,
        SHARED_PREFERENCE,
        ENCRYPTED_SHARED_PREFERENCE
    }

    private val memoRepository = MemoRepository()
    var memoContents = memoRepository.memoContents
    var isFileAvailable = MutableLiveData<Boolean>()
    var menuFile = MutableLiveData<MenuFile>()

    init {
        isFileAvailable.value = false
        menuFile.value = MenuFile.values()[
                PreferenceManager
                    .getDefaultSharedPreferences(application)
                    .getInt(PREF_KEY_MENU_FILE, MenuFile.DOCUMENT_FILE.ordinal)]
        menuFile.observeForever {
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
        when (menuFile.value) {
            MenuFile.DOCUMENT_FILE -> {
                memoRepository.readFromDocumentFile(context)
            }
            MenuFile.FILE -> {
                memoRepository.readFromFile(context)
            }
            MenuFile.ENCRYPTED_FILE -> {
                memoRepository.readFromEncryptedFile(context)
            }
            MenuFile.SHARED_PREFERENCE -> {
            }
            MenuFile.ENCRYPTED_SHARED_PREFERENCE -> {

            }
        }
    }

    fun writeMemo(context: Context, contents: String) {
        when (menuFile.value) {
            MenuFile.DOCUMENT_FILE -> {
                memoRepository.writeToDocumentFile(context, contents)
            }
            MenuFile.FILE -> {
                memoRepository.writeToFile(context, contents)
            }
            MenuFile.ENCRYPTED_FILE -> {
                memoRepository.writeToEncryptedFile(context, contents)
            }
            MenuFile.SHARED_PREFERENCE -> {

            }
            MenuFile.ENCRYPTED_SHARED_PREFERENCE -> {

            }
        }
    }
}