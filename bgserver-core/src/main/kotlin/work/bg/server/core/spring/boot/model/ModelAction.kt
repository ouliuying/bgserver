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

package work.bg.server.core.spring.boot.model

import org.springframework.util.MultiValueMap
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
class  ModelAction(){
    private  var actions = HashMap<String,ActionMethod?>()
    fun invoke(request: HttpServletRequest,
               response:HttpServletResponse,
               session: HttpSession,
               appModel: AppModel,
               appName:String,
               modelName:String,
               action: String): Any?{
        val ac: ActionMethod? = actions[action]
        return ac?.invoke(request,response,session,appModel,appName,modelName)
    }
    fun addMethod(name: String,actionMethod: ActionMethod){
        if (!actions.containsKey(name)){
            actions[name]=actionMethod
        }
    }
}