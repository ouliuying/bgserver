/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *it under the terms of the GNU Affero General Public License as published by
t *  *  *he Free Software Foundation, either version 3 of the License.

 *  *  *This program is distributed in the hope that it will be useful,
 *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *GNU Affero General Public License for more details.

 *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *  *
  *
  */

package work.bg.server.core

import java.io.PrintWriter
import javax.servlet.ServletOutputStream
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletResponseWrapper
import javax.servlet.WriteListener
import java.io.IOException
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset


class ResetResponseWrapper(response: HttpServletResponse):HttpServletResponseWrapper(response) {
    private val bodyShadow= ByteArrayOutputStream()
    private val feedServletOutputStream=FeedServletOutputStream(response.outputStream,bodyShadow)
    private val feedWriter = PrintWriter(OutputStreamWriter(feedServletOutputStream, this.characterEncoding))
    override fun flushBuffer() {
        super.flushBuffer()
    }
    override fun getBufferSize(): Int {
        return super.getBufferSize()
    }

    override fun getOutputStream(): ServletOutputStream {
        return feedServletOutputStream
    }

    override fun getWriter(): PrintWriter {
        return feedWriter
    }

    override fun resetBuffer() {
        super.resetBuffer()
    }

    override fun reset() {
        super.reset()
    }

    override fun setBufferSize(size: Int) {
        super.setBufferSize(size)
    }

    override fun setResponse(response: ServletResponse?) {
        super.setResponse(response)
    }
    val bodyText: String
        get() = String(bodyShadow.toByteArray(), Charset.defaultCharset())

    class FeedServletOutputStream(private var original:ServletOutputStream,
                                  private var shadow:OutputStream):ServletOutputStream(){


        override fun write(b: Int) {
            original.write(b)
            shadow.write(b)
        }


        override fun write(b: ByteArray, off: Int, len: Int) {
            original.write(b, off, len)
            shadow.write(b, off, len)
        }


        override fun flush() {
            original.flush()
            shadow.flush()
        }


        override fun close() {
            original.close()
            shadow.close()
        }

        override fun isReady(): Boolean {
            return original.isReady
        }

        override fun setWriteListener(listener: WriteListener) {
            original.setWriteListener(listener)
        }
    }

}

