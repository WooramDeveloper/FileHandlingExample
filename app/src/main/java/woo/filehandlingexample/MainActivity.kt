package woo.filehandlingexample

import android.Manifest
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

private const val TAG = "MainActivity"
private const val PERMISSION_ALL = 0

class MainActivity : AppCompatActivity() {

    lateinit var memoViewModel: MemoViewModel
    lateinit var editText: EditText
    lateinit var saveButton: Button
    lateinit var loadingBar: View
    private val permissions = listOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        memoViewModel = ViewModelProviders.of(this).get(MemoViewModel::class.java)
        initView()
        subscribeObservers()

        val permissionAllowAll = permissions
            .all { ContextCompat.checkSelfPermission(this, it) ==
                    PackageManager.PERMISSION_GRANTED }

        if (permissionAllowAll) {
            memoViewModel.readMemo(applicationContext)
            memoViewModel.isFileAvailable.value = true
        } else {
            val deniedPermissions = permissions
                .filter { ContextCompat.checkSelfPermission(this, it) ==
                        PackageManager.PERMISSION_DENIED }
            val showRequestPermission = deniedPermissions
                .all { shouldShowRequestPermissionRationale(it) }
            if (showRequestPermission) {
                Toast.makeText(applicationContext,
                    deniedPermissions
                        .map { it.split(".").last() }
                        .joinToString(),
                    Toast.LENGTH_SHORT)
                    .show()
            } else {
                requestPermissions(deniedPermissions.toTypedArray(), PERMISSION_ALL)
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_ALL -> {
                val permissionAllowAll = grantResults
                    .all { it == PackageManager.PERMISSION_GRANTED }
                if (permissionAllowAll) {
                    memoViewModel.readMemo(applicationContext)
                    memoViewModel.isFileAvailable.value = true
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.memo_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.menuDocumentFile -> {
                memoViewModel.fileType.value = MemoViewModel.FileType.DOCUMENT_FILE
                return true
            }
            R.id.menuFile -> {
                memoViewModel.fileType.value = MemoViewModel.FileType.FILE
                return true
            }
            R.id.menuEncryptedFile -> {
                memoViewModel.fileType.value = MemoViewModel.FileType.ENCRYPTED_FILE
                return true
            }
            R.id.menuSharedPreference -> {
                memoViewModel.fileType.value = MemoViewModel.FileType.SHARED_PREFERENCE
                return true
            }
            R.id.menuEncryptedSharedPreference -> {
                memoViewModel.fileType.value = MemoViewModel.FileType.ENCRYPTED_SHARED_PREFERENCE
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initView() {
        editText = findViewById(R.id.editText)
        loadingBar = findViewById(R.id.loadingBar)
        saveButton = findViewById(R.id.saveButton)
        saveButton.setOnClickListener {
            memoViewModel.writeMemo(applicationContext, editText.text.toString())
        }
        saveButton.isEnabled = false
        editText.setOnEditorActionListener { textView, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE && saveButton.isEnabled) {
                memoViewModel.writeMemo(applicationContext, editText.text.toString())
                true
            }
            false
        }
    }

    private fun subscribeObservers() {
        memoViewModel.memoContents.observe(this, Observer { memo ->
            when (memo?.status) {
                Status.SUCCESS -> {
                    editText.isEnabled = true
                    loadingBar.visibility = View.INVISIBLE
                    editText.setText(
                        memo.data ?: "")
                }
                Status.PROCESSING -> {
                    editText.isEnabled = false
                    loadingBar.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    loadingBar.visibility = View.INVISIBLE
                }
            }
        })
        memoViewModel.isFileAvailable.observe(this, Observer {
            if (it == true) {
                saveButton.isEnabled = true
            }
        })
        memoViewModel.fileType.observe(this, Observer {
            memoViewModel.readMemo(applicationContext)
        })
    }
}
