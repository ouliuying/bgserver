/*
 *
 *  *
 *  *  * //copy from https://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.core


import javax.servlet.ReadListener
import javax.servlet.ServletInputStream
import java.io.IOException

class ResetServletInputStream : ServletInputStream {

    private var myBytes: ByteArray? = null

    private var lastIndexRetrieved = -1
    private var readListener: ReadListener? = null

    constructor(inputBytes: ByteArray) {
        this.myBytes = inputBytes
    }

    override fun isFinished(): Boolean {
        return lastIndexRetrieved == myBytes!!.size - 1
    }

    override fun isReady(): Boolean {
        // This implementation will never block
        // We also never need to call the readListener from this method, as this method will never return false
        return isFinished
    }

    override fun setReadListener(readListener: ReadListener) {
        this.readListener = readListener
        if (!isFinished) {
            try {
                readListener.onDataAvailable()
            } catch (e: IOException) {
                readListener.onError(e)
            }

        } else {
            try {
                readListener.onAllDataRead()
            } catch (e: IOException) {
                readListener.onError(e)
            }

        }
    }

    @Throws(IOException::class)
    override fun read(): Int {
        val i: Int
        if (!isFinished) {
            i = myBytes!![lastIndexRetrieved + 1].toInt()
            lastIndexRetrieved++
            if (isFinished && readListener != null) {
                try {
                    readListener!!.onAllDataRead()
                } catch (ex: IOException) {
                    readListener!!.onError(ex)
                    throw ex
                }

            }
            return i
        } else {
            return -1
        }
    }
}