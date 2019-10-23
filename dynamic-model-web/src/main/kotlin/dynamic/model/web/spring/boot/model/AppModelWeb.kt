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

package dynamic.model.web.spring.boot.model

import dynamic.model.query.config.AppPackageManifest
import dynamic.model.query.mq.RefSingleton
import dynamic.model.query.mq.model.AppModel
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.query.mq.model.ModelMetaData
import dynamic.model.web.context.ContextType
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.ApplicationContext
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSuperclassOf

class AppModelWeb(modelMetaDatas: List<ModelMetaData>,
                        appPackageManifests:Map<String, AppPackageManifest>,
                        var appContext: ApplicationContext?=null
                  ):AppModel(appPackageManifests) {
    private  val logger = LogFactory.getLog(javaClass)
    private  var appModelRegistries: MutableMap<String, ModelActionRegistry> = HashMap<String, ModelActionRegistry>()
    companion object : RefSingleton<AppModelWeb> {
        override lateinit var ref: AppModelWeb
    }

    init {
        logger.info("AppModel load start at ${Date()}")
        buildAppModelRegistries(modelMetaDatas)
    }
    fun buildAppModelRegistries(modelMetaDatas: List<ModelMetaData>){
        this.appModelRegistries.clear()
        modelMetaDatas.forEach {
            if (this.appModelRegistries.containsKey(it.appName)){
                this.appModelRegistries[it.appName]?.addModel(it)
            }
            else
            {
                val registry = ModelActionRegistry(it.appName, DefaultBuildModelStrategy())
                registry.addModel(it)
                this.appModelRegistries[it.appName]=registry
            }
        }
    }
    override fun filterCandidateModelMetaData(): List<ModelMetaData>?{
        var concreteModelMetaDatas= mutableListOf<ModelMetaData?>()
        this.appModelRegistries.forEach {
            concreteModelMetaDatas.addAll(it.value.refresh())
        }
        return overrideModelMeta(concreteModelMetaDatas)
    }

    override fun getModelByModelMetaData(modelMetaData: ModelMetaData): ModelBase? {
        return this.appContext?.getBean((modelMetaData.beanDefinitionHolder.beanDefinition as GenericBeanDefinition).beanClass) as ModelBase?
    }

    private fun overrideModelMeta(modelMetaDatas:MutableList<ModelMetaData?>):List<ModelMetaData>?{
        var overrideModelMetaDatas= mutableListOf<ModelMetaData?>()
        var sortAppModelMetaDatas = mutableListOf<ModelMetaData?>()
        while (modelMetaDatas.count()>0){
            var fmmd=modelMetaDatas.first()
            var cls=(fmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass
            var fmmds=modelMetaDatas.filter { it->
                var tCls=(it?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass
                tCls?.kotlin?.isSubclassOf(cls?.kotlin!!)!! || cls?.kotlin?.isSubclassOf(tCls?.kotlin!!)!!
            }
            modelMetaDatas.removeAll(fmmds)
            var (minFmmd,maxFmmd)=this.getMaxMinLevelModelMeta(fmmds)
            var mmd= ModelMetaData(minFmmd!!.appName, minFmmd.modelName, minFmmd!!.title, maxFmmd!!.beanDefinitionHolder, minFmmd.index)
            fmmds.forEach {
                it?.let {
                    sortAppModelMetaDatas.add(ModelMetaData(minFmmd.appName, minFmmd.modelName, minFmmd.title, it.beanDefinitionHolder, it.index))
                }
            }
            overrideModelMetaDatas.add(mmd)
        }
        buildAppModelRegistries(sortAppModelMetaDatas as List<ModelMetaData>)
        this.appModelRegistries.forEach {
            it.value.refresh()
        }
        return overrideModelMetaDatas as List<ModelMetaData>
    }
    private fun getMaxMinLevelModelMeta(fmmds:List<ModelMetaData?>):Pair<ModelMetaData?, ModelMetaData?>{
        var fmmd=fmmds.first()
        var maxFmmd=fmmds.first()
        var cnt=fmmds.count()
        if(cnt>1){
            for(i in 1 until cnt){
                var oldCls=(fmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                var maxOldCls=(maxFmmd?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                var iCls=(fmmds[i]?.beanDefinitionHolder?.beanDefinition as GenericBeanDefinition?)?.beanClass!!
                if(oldCls.kotlin.isSubclassOf(iCls.kotlin)){
                    fmmd=fmmds[i]
                }
                if(maxOldCls.kotlin.isSuperclassOf(iCls.kotlin)){
                    maxFmmd=fmmds[i]
                }
            }
        }
        return Pair(fmmd,maxFmmd)
    }
    operator fun invoke(request: HttpServletRequest,
                        response: HttpServletResponse,
                        session: HttpSession,
                        appName: String,
                        modelName: String,
                        action: String,
                        context:ContextType?): Any?{
        try
        {
            return this.appModelRegistries[appName]?.invoke(request,response,session,this,appName,modelName,action,context)
        }
        catch (err:Exception){
            var errMsg=err.toString()
            err.printStackTrace()
            logger.error("$appName - $modelName - $action error: $errMsg")
        }
        finally {
            //
        }
        return null
    }
    fun getBean(typeName:String):Any?{
        return this.appContext?.getBean(Class.forName(typeName))
    }
}