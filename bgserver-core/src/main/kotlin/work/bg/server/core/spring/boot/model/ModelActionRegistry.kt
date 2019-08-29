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
import kotlin.reflect.KClass
import kotlin.collections.List
class  ModelActionRegistry(val name: String,val buildModelStrategy: BuildModelStrategy){
    private var modelActions: Map<String,ModelAction?> = mutableMapOf<String,ModelAction>()
    private var models: MutableList<ModelMetaData> = arrayListOf<ModelMetaData>()
    operator  fun invoke(request:HttpServletRequest,
                         respone:HttpServletResponse,
                         session: HttpSession,
                         appModel: AppModel,
                         appName: String,
                         modName: String,
                         action: String): Any?{
        val actions: ModelAction? =modelActions?.get(modName)
        return actions?.invoke(request,respone,session,appModel,appName,modName, action)
    }

    fun addModel(model: ModelMetaData){
        this.models.add(model)
    }

    fun  refresh(): List<ModelMetaData?>{
        val (modelActions,concreteModeMetaDatas)=this.buildModelStrategy.buildModel(this.models)
        this.modelActions=modelActions
        this.models.clear()
        return concreteModeMetaDatas
    }
}