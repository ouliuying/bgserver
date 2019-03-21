/*
 *
 *  *
 *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  * https://bg.work
 *  *  *
 *  *  * GNU Lesser General Public License Usage
 *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  * General Public License version 3 as published by the Free Software
 *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  * project of this file. Please review the following information to
 *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *
 *
 */

package work.bg.server.core.spring.boot.annotation

import org.springframework.http.HttpStatus
import org.springframework.web.servlet.HandlerInterceptor
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.method.HandlerMethod
import work.bg.server.core.constant.ServletRequestAttributeTag
import work.bg.server.core.constant.SessionTag
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import work.bg.server.errorcode.ErrorCode
import work.bg.server.errorcode.jsonFormat
import work.bg.server.errorcode.reLogin

class ShouldLoginInterceptor constructor(private val unauthRedirectUrl:String?): HandlerInterceptorAdapter() {

    override  fun preHandle(request: HttpServletRequest,
                  response: HttpServletResponse,
                  handler: Any): Boolean {
        if(handler is HandlerMethod){
            val rm = (handler as HandlerMethod).getMethodAnnotation(
                    ShouldLogin::class.java)
            val partnerID=request.session
                    .getAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY)
           // request.setAttribute(ServletRequestAttributeTag.PARTNER_CONTEXT_TAG,partner_ctx)
            if(rm!=null) if(partnerID== null){
                //  ErrorCode::
                response.contentType="application/json"
                response.characterEncoding="utf-8"
                response.status=200
                response.writer.print(ErrorCode.RELOGIN.reLogin(this.unauthRedirectUrl))
                response.writer.close()
                return false
            }
        }
        return true
    }
}