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