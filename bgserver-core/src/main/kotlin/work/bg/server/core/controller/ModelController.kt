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

package work.bg.server.core.controller

import dynamic.model.query.mq.model.AppModel
import dynamic.model.web.context.ContextType
import dynamic.model.web.spring.boot.model.AppModelWeb
import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.RequestContextHolder
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.cache.PartnerCacheRegistry
import work.bg.server.core.constant.SessionTag
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession

@RestController
class ModelController constructor(val  appModel: AppModel){
//private static final Log logger = LogFactory.getLog(TomcatWebServer.class);
    protected val logger = LogFactory.getLog(javaClass)
    //protected val logger = LogFactory.getLog(javaClass)
    @Autowired
    private var appContext: ApplicationContext? = null
    @Autowired
    var partnerCacheRegistry: PartnerCacheRegistry?=null
    init {
    }
   // @ShouldLogin
    @RequestMapping("/ac/{app}/{name}/{action}",method = [RequestMethod.POST,RequestMethod.GET,RequestMethod.HEAD])
    fun call(request: HttpServletRequest,
             response: HttpServletResponse,
             session: HttpSession,
             @PathVariable("app") appName: String,
             @PathVariable("name") modelName: String,
             @PathVariable("action") actionName: String): Any? {
                var partnerCacheKey=session.getAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY)
                var partnerCache=null as PartnerCache?
                if(partnerCacheKey!=null){
                    partnerCache= this.partnerCacheRegistry?.get(partnerCacheKey as PartnerCacheKey)
                }
                else{
                    val apiToken= request.getHeader("token")
                    if(!apiToken.isNullOrEmpty()){

                    }
                }
                return (appModel as AppModelWeb)(request,response,session,appName,modelName,actionName,partnerCache)
    }
    @RequestMapping("/app/dynamic/**","/login","/login/**",method = [RequestMethod.GET])
    fun redirect(resp:HttpServletResponse){
        resp.status=HttpServletResponse.SC_TEMPORARY_REDIRECT
        resp.sendRedirect("/")
    }
    @RequestMapping("/login",method = [RequestMethod.POST],produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun login(request: HttpServletRequest,
                response: HttpServletResponse,
                session: HttpSession):Any?{
       // logger.info(userName)
        var ret= (appModel as AppModelWeb)(request,response,session,"core","partner","login",null as PartnerCache?)
        if(ret!=null){
            logger.info(ret.javaClass.canonicalName)
        }
        return ret
    }


}