package woo.filehandlingexample

/**
 * Created by Wooram2 on 2019-07-08.
 */

class Memo<T>(var status: Status, var data: T?, var message: String?) {

    companion object {
        fun <T> success(data: T?) : Memo<T> {
            return Memo(Status.SUCCESS, data, null)
        }
        fun <T> error(msg: String?) : Memo<T> {
            return Memo(Status.ERROR, null, msg)
        }
        fun <T> processing() : Memo<T> {
            return Memo(Status.PROCESSING, null, null)
        }
    }
}

enum class Status {SUCCESS, ERROR, PROCESSING}