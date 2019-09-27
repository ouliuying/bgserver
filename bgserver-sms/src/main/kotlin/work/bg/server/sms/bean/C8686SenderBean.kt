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

package work.bg.server.sms.bean

import org.dom4j.DocumentHelper
import org.jaxen.dom4j.Dom4jXPath
import org.springframework.stereotype.Service
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.Charset
import java.security.MessageDigest
import java.time.Instant
import kotlin.experimental.and
import org.dom4j.io.SAXReader
import java.io.StringReader

@Service
class C8686SenderBean {
    private val sender = SmsSender()
    fun doSend(mobiles:Array<String>,message:String,apiKey:String,apiSecret:String):Triple<Int,String,Any?>{
        sender.apikey=apiKey
        sender.apisecret=apiSecret
        var ret = sender.Send(mobile =mobiles.joinToString { "," },msg = message)
        try {
            val reader = StringReader(ret)
            val r= SAXReader().read(reader)
            var retCode = r.selectSingleNode("/response/errorcode").text
            if(retCode=="0"){
                var orderno = r.selectSingleNode("/response/orderno").text
                return Triple(0,"提交成功",orderno)
            }
            else{
                var description =  r.selectSingleNode("/response/errordescription").text
                return Triple(retCode.toInt(),description,null)
            }
        }
        catch (ex:Exception){
            return Triple(9999,ex.message?:"",null)
        }
    }

    class SmsSender {
        private val digits = "0123456789abcdef"
        private val _content_type = "application/x-www-form-urlencoded"
        public var apikey = ""
        public var apisecret = ""

        private fun getUnixTimeStamp(): String {
            val unixTimestamp = Instant.now().epochSecond
            return java.lang.Long.toString(unixTimestamp)
        }

        protected fun toHexString(bts: ByteArray?): String {
            if (bts == null) {
                return ""
            }
            val sb = StringBuilder()
            for (b in bts) {
                val v = (b and 0xff.toByte()).toUByte().toInt()
                val iv = v.toInt()
                sb.append(digits[iv ushr 4])
                sb.append(digits[iv and 0xf])
            }
            return sb.toString()
        }

        protected fun md5(`val`: String): ByteArray? {
            try {
                val btsofval = `val`.toByteArray(charset("UTF-8"))
                val md = MessageDigest.getInstance("MD5")
                return md.digest(btsofval)
            } catch (ex: java.io.UnsupportedEncodingException) {
                return null
            } catch (ex: java.security.NoSuchAlgorithmException) {
                return null
            }

        }

        protected fun signDigest(appsecret: String, data: String, timestamp: String): String {
            val sb = StringBuilder()
            sb.append(appsecret)
            sb.append(data)
            sb.append(timestamp)
            return toHexString(md5(sb.toString()))
        }

        fun Send(mobile: String, msg: String): String {
            val timestamp = getUnixTimeStamp()
            val digest = signDigest(this.apisecret, mobile, timestamp)
            val data = String.format(
                    "mobiles=%s&apikey=%s&digest=%s&timestamp=%s&message=%s",
                    mobile,
                    this.apikey,
                    digest,
                    timestamp,
                    msg
            )
            return _post("https://sms.c8686.com/smsapi/send/utf8", data)
        }

        fun DeleteTimer(orderno: String): String {
            val timestamp = getUnixTimeStamp()
            val digest = signDigest(this.apisecret, orderno, timestamp)
            val data = String.format("apikey=%s&digest=%s&timestamp=%s&orderno=%s", this.apikey, digest, timestamp, orderno)
            return _post("https://sms.c8686.com/smsapi/deletetimer/utf8", data)
        }

        protected fun _post(_url: String, data: String): String {
            print(data)
            try {
                val url = URL(_url)
                val con = url.openConnection()
                val http = con as HttpURLConnection
                http.setRequestMethod("POST")
                http.setDoOutput(true)
                val out = data.toByteArray(charset("UTF-8"))
                val length = out.size
                http.setFixedLengthStreamingMode(length)
                http.setRequestProperty("Content-Type", this._content_type)
                http.setConnectTimeout(1000 * 120)
                http.connect()
                val outstream = http.getOutputStream()
                outstream.write(out)
                val instream = http.getInputStream()
                val bts = ByteArrayOutputStream()
                val buffer = ByteArray(1024)
                var rlen = instream.read(buffer)
                while (rlen!= -1) {
                    bts.write(buffer, 0, rlen)
                    rlen = instream.read(buffer)
                }
                return String(bts.toByteArray(), Charset.forName("utf-8"))
            } catch (ex: java.net.MalformedURLException) {
                return ""
            } catch (ex: java.io.IOException) {
                return ""
            }
        }
    }
}