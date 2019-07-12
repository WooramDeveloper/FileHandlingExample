package woo.filehandlingexample

import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Created by Wooram2 on 2019-07-08.
 */

object AppExecutors {
    val diskIO: Executor = Executors.newSingleThreadExecutor()
}