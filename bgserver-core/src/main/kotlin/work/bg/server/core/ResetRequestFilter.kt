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
import org.apache.commons.logging.LogFactory
import org.springframework.web.util.ContentCachingResponseWrapper
import java.nio.charset.Charset
import javax.servlet.Filter
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponseWrapper


class ResetRequestFilter:Filter {
    private val skipUrls:ArrayList<String> = arrayListOf(
           "/ac/chat/chat/loadChannelMeta"
    )
    private  val logger = LogFactory.getLog(javaClass)
    override fun doFilter(request: ServletRequest?, response: ServletResponse?, chain: FilterChain?) {
        val req = ResetRequestWrapper(request as HttpServletRequest)
        val res = ResetResponseWrapper(response as HttpServletResponse)
        chain?.doFilter(req, res)
        val reqBody = req.bodyText
        val resBody = res.bodyText
        if(req.requestURI !in skipUrls){
            logger.info("""

            Logging Request method =  ${req.method}  url =  ${req.requestURI}  body = $reqBody
            ====>Response : content-type = ${res.contentType} ret body = $resBody"
        """)
        }
    }

}