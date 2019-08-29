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

import org.apache.commons.logging.LogFactory
import org.springframework.context.ApplicationContext
import work.bg.server.core.spring.boot.model.AppModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContextAware
import org.springframework.http.MediaType
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.*
import work.bg.server.core.model.BasePartner
import work.bg.server.core.spring.boot.annotation.ShouldLogin
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
        return appModel(request,response,session,appName,modelName,actionName)
    }

    @RequestMapping("/login",method = [RequestMethod.POST,RequestMethod.GET],produces = [MediaType.APPLICATION_JSON_UTF8_VALUE])
    fun login(request: HttpServletRequest,
                response: HttpServletResponse,
                session: HttpSession):Any?{
       // logger.info(userName)
        var ret= appModel(request,response,session,"core","partner","login")
        if(ret!=null){
            logger.info(ret.javaClass.canonicalName)
        }
        return ret
    }

}