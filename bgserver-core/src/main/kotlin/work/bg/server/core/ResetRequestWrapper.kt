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
import org.apache.commons.logging.LogFactory
import org.springframework.http.MediaType
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.*
import javax.servlet.ServletInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletRequestWrapper

class ResetRequestWrapper(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private  val logger = LogFactory.getLog(javaClass)
    private lateinit  var _body: Array<Byte>
    private var _parameterMap: MutableMap<String,Array<String>> = mutableMapOf()
    private var _parameterNames = Vector<String>()
    init {
        if(this.contentType!=null && this.contentType.contains(MediaType.APPLICATION_FORM_URLENCODED_VALUE,true)){
            this.parseParameters()
        }
        else{
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
                this.parseParameters()
            }
        }
    }
    private fun parseParameters(){
        var pms = request.parameterMap
        val items = arrayListOf<String>()
        pms.forEach { t, u ->
            items.add(arrayListOf(t,u.joinToString()).joinToString("="))
            _parameterMap[t]=u
            _parameterNames.addElement(t)
        }
        _body =  items.joinToString("&").toByteArray(Charset.defaultCharset()).toTypedArray()
    }
    @Throws(IOException::class)
    override fun getInputStream(): ServletInputStream {
        return ResetServletInputStream(_body.toByteArray())
    }

    @Throws(IOException::class)
    override fun getReader(): BufferedReader {
        return BufferedReader(InputStreamReader(this.inputStream))
    }

    override fun getParameter(name: String?): String? {
        return _parameterMap[name]?.get(0)
    }

    override fun getParameterMap(): MutableMap<String, Array<String>> {
        return _parameterMap
    }

    override fun getParameterNames(): Enumeration<String> {
        return _parameterNames.elements()
    }

    override fun getParameterValues(name: String?): Array<String>? {
        return _parameterMap[name]
    }
    val bodyText:String
        get() = if(this._body.count()<1){
               ""
            }
            else{
                String(this._body.toByteArray(), Charset.defaultCharset())
            }

}