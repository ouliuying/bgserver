/*
 *
 *  *
 *  *  * copy from https://stackoverflow.com/questions/10210645/http-servlet-request-lose-params-from-post-body-after-read-it-once
 *  *  *
 *  *
 *
 *
 */

package work.bg.server.core

import org.apache.catalina.connector.CoyoteInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class ResetRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {

    private  var _body: Array<Byte>
    init {
        val bytes = arrayListOf<Byte>()
        val stream = request.inputStream
        var bt=stream.read()
        if(bt>-1){
            while (bt>-1){
                bytes.add(bt.toByte())
                bt=stream.read()
            }
            _body = bytes.toTypedArray()
        }
        else{
             var pms = request.parameterMap
             val items = arrayListOf<String>()
             pms.forEach { t, u ->
                items.add(arrayListOf(t,u.joinToString()).joinToString("="))
             }
             _body =  items.joinToString("&").toByteArray(Charset.defaultCharset()).toTypedArray()
        }
    }

    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream {
        return ResetServletInputStream(_body.toByteArray())
    }

    @Throws(IOException::class)
    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(this.inputStream))
    }

    val bodyText:String
        get() = if(this._body.count()<1){
               ""
            }
            else{
                String(this._body.toByteArray(), Charset.defaultCharset())
            }

}