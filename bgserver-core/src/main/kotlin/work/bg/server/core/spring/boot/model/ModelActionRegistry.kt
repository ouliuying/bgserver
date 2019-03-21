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