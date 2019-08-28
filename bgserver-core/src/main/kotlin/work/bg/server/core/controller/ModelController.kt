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