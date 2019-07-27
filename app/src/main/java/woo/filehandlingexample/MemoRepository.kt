package woo.filehandlingexample

import androidx.lifecycle.MutableLiveData
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.DocumentsProvider
import android.security.keystore.KeyGenParameterSpec
import androidx.core.content.MimeTypeFilter
import androidx.documentfile.provider.DocumentFile
import android.util.Log
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * Created by Wooram2 on 2019-07-08.
 */

private const val TAG = "DocumentRepository"
private const val PREFERENCE_SECRET_FILE_KEYS = "woo.filehandlingexample.preference.secretFileKeys"
private const val REFERENCE_SECRET_FILE_KEYS = "secretFileKeys"
private const val VALUE_ENCRYPTION_KEY_ONE = "stringBasedEncryptionKeyOne"
private const val VALUE_ENCRYPTION_KEY_TWO = "stringBasedEncryptionKeyTwo"
private const val FILE_DOC_MEMO = "docMemo"
private const val FILE_FILE_MEMO = "fileMemo"
private const val FILE_ENCRYPTED_MEMO = "encryptedMemo"
private const val FILE_EXTENSION_TXT = "txt"
private const val MIME_TYPE_TEXT_PLAIN = "text/plain"

class MemoRepository {

    private val appExecutors = AppExecutors

    var memoContents = MutableLiveData<Memo<String>>()

    fun writeToDocumentFile(context: Context, contents: String) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            doActionWithDocument(context) {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(            uri, "w")
                val fileOutputStream = ParcelFileDescriptor.AutoCloseOutputStream(parcelFileDescriptor)
                fileOutputStream.apply {
                    write(contents.toByteArray(Charset.forName("UTF-8")))
                    flush()
                    close()
                }
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    fun writeToFile(context: Context, contents: String) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            doActionWithFile(context) {
                val fileOutputStream = FileOutputStream(this@doActionWithFile)
                fileOutputStream.apply {
                    write(contents.toByteArray(Charset.forName("UTF-8")))
                    flush()
                    close()
                }
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    fun writeToEncryptedFile(context: Context, contents: String) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            doActionWithEncryptedFile(context) { file ->
                if (file.exists()) {
                    file.delete()
                }
                file
            }?.apply {
                val fileOutputStream = openFileOutput()
                fileOutputStream.apply {
                    write(contents.toByteArray(Charset.forName("UTF-8")))
                    flush()
                    close()
                }
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    fun readFromDocumentFile(context: Context) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            var contents = ""
            doActionWithDocument(context) {
                val parcelFileDescriptor = context.contentResolver.openFileDescriptor(            uri, "r")
                val fileInputStream = ParcelFileDescriptor.AutoCloseInputStream(parcelFileDescriptor)
                val byteStream = ByteArrayOutputStream()
                var nextByte = fileInputStream.read()
                while (nextByte != -1) {
                    byteStream.write(nextByte)
                    nextByte = fileInputStream.read()
                }
                contents = String(byteStream.toByteArray())
                fileInputStream.close()
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    fun readFromFile(context: Context) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            var contents = ""
            doActionWithFile(context) {
                val fileInputStream = FileInputStream(this@doActionWithFile)
                val byteStream = ByteArrayOutputStream()
                var nextByte = fileInputStream.read()
                while (nextByte != -1) {
                    byteStream.write(nextByte)
                    nextByte = fileInputStream.read()
                }
                contents = String(byteStream.toByteArray())
                fileInputStream.close()
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    fun readFromEncryptedFile(context: Context) {
        memoContents.value = Memo.processing()
        appExecutors.diskIO.execute {
            var contents = ""
            doActionWithEncryptedFile(context) { file ->
                if (file.exists()) {
                    file
                } else {
                    null
                }
            }?.apply {
                val fileInputStream = openFileInput()
                val byteStream = ByteArrayOutputStream()
                var nextByte = fileInputStream.read()
                while (nextByte != -1) {
                    byteStream.write(nextByte)
                    nextByte = fileInputStream.read()
                }
                contents = String(byteStream.toByteArray())
                fileInputStream.close()
            }
            memoContents.postValue(Memo.success(contents))
        }
    }

    private fun doActionWithDocument(context: Context, action: DocumentFile.() -> Unit) {
        context.getExternalFilesDir(null)
            ?.let {
                val docDir = DocumentFile.fromFile(it)
                val fileDir = docDir.findFile("$FILE_DOC_MEMO.$FILE_EXTENSION_TXT")
                if (fileDir == null || !fileDir.exists()) {
                    docDir.createFile(MIME_TYPE_TEXT_PLAIN, FILE_DOC_MEMO)
                } else {
                    fileDir
                }
            }
            ?.run(action)
    }

    private fun doActionWithFile(context: Context, action: File.() -> Unit) {
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            ?.let { dir ->
                val file = File(dir, "$FILE_FILE_MEMO.$FILE_EXTENSION_TXT")
                if (!file.exists()) {
                    file.createNewFile()
                }
                file
            }
            ?.run(action)
    }
    
    private fun doActionWithEncryptedFile(context: Context,
                                          action: (File) -> File?): EncryptedFile? {
        return context.filesDir
            ?.let { dir ->
                val keySet = hashSetOf<String>()
                keySet.add(VALUE_ENCRYPTION_KEY_ONE)
                keySet.add(VALUE_ENCRYPTION_KEY_TWO)
                context.getSharedPreferences(PREFERENCE_SECRET_FILE_KEYS, Context.MODE_PRIVATE)
                    .edit()
                    .putStringSet(REFERENCE_SECRET_FILE_KEYS, keySet)
                    .apply()
                File(dir, "$FILE_ENCRYPTED_MEMO.$FILE_EXTENSION_TXT")
            }
            ?.run(action)
            ?.run {
                EncryptedFile.Builder(this,
                    context,
                    MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
                    EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
                ).setKeysetAlias(REFERENCE_SECRET_FILE_KEYS).build()
            }
    }
}