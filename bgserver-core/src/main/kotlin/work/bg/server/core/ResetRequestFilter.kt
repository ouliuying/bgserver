/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
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