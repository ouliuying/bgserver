/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  *This program is free software: you can redistribute it and/or modify
 *  *  *  *it under the terms of the GNU Affero General Public License as published by
 * t *  *  *he Free Software Foundation, either version 3 of the License.
 *
 *  *  *  *This program is distributed in the hope that it will be useful,
 *  *  *  *but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  *  *  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  *  *  *GNU Affero General Public License for more details.
 *
 *  *  *  *You should have received a copy of the GNU Affero General Public License
 *  *  *  *along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *   *  *
 *   *
 *
 */

package work.bg.server.core.spring.boot.annotation

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import org.springframework.web.method.HandlerMethod
import work.bg.server.core.constant.SessionTag
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.errorcode.reLogin
import dynamic.model.web.errorcode.reLoginJson
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class ShouldLoginInterceptor constructor(private val unauthRedirectUrl:String?): HandlerInterceptorAdapter() {

    override  fun preHandle(request: HttpServletRequest,
                  response: HttpServletResponse,
                  handler: Any): Boolean {
        if(handler is HandlerMethod){
            val rm = handler.getMethodAnnotation(
                    ShouldLogin::class.java)
            val partnerID=request.session
                    .getAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY)
            if(rm!=null) if(partnerID== null){
                response.contentType="application/json"
                response.characterEncoding="utf-8"
                response.status=200
                response.writer.print(ErrorCode.RELOGIN.reLoginJson(this.unauthRedirectUrl))
                response.writer.close()
                return false
            }
        }
        return true
    }
}