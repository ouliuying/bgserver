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

package dynamic.model.web.spring.boot.annotation
import dynamic.model.query.config.AppPackageManifest
import dynamic.model.query.mq.model.AppModel
import dynamic.model.query.mq.model.ModelMetaData
import dynamic.model.web.spring.boot.model.AppModelWeb
import dynamic.model.web.spring.boot.model.ModelPathScan
import org.springframework.beans.factory.support.BeanDefinitionRegistry

import org.springframework.context.ResourceLoaderAware

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import java.util.function.Supplier
class ModelRegister constructor(): ImportBeanDefinitionRegistrar,
        ResourceLoaderAware{
    private var resourceLoader: ResourceLoader? = null
    private  var applicationContext: ApplicationContext?= null
    private  var appModel: AppModelWeb? = null

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resourceLoader = resourceLoader
        this.applicationContext=resourceLoader as ApplicationContext
        appModel?.appContext = applicationContext
    }

    override fun registerBeanDefinitions(importingClassMetadata:
                                         AnnotationMetadata,
                                         registry: BeanDefinitionRegistry) {
        val mapperScanAttrs = AnnotationAttributes.
                fromMap(importingClassMetadata.
                        getAnnotationAttributes(ModelScan::class.java.name))
        if (mapperScanAttrs != null) {
            this.registerBeanDefinitions(mapperScanAttrs, registry)
        }
    }


    private  fun addAppModelBean(modelMetaDatas: List<ModelMetaData>, registry: BeanDefinitionRegistry, appPackageManifests:Map<String, AppPackageManifest>){
        val appModel= AppModelWeb(modelMetaDatas, appPackageManifests, this.applicationContext)
        val concreteModelMetaDatas=appModel.buildModelMetaData()

        val beanDefinition = RootBeanDefinition(
                AppModel::class.java,
                Supplier<AppModel> { appModel }
        )
        val appModelHolder = BeanDefinitionHolder(beanDefinition, "appmodel")

        BeanDefinitionReaderUtils.registerBeanDefinition(appModelHolder, registry)

        concreteModelMetaDatas?.forEach {
            if (it!=null){
                var holder=it.beanDefinitionHolder
                BeanDefinitionReaderUtils.registerBeanDefinition(holder,registry)
            }
        }

    }

    private fun registerBeanDefinitions(annoAttrs: AnnotationAttributes, registry: BeanDefinitionRegistry) {
        val scanner = ModelPathScan(Model::class, registry)
        if (this.resourceLoader != null) {
            scanner.resourceLoader = this.resourceLoader as ResourceLoader
        }
        val packages=annoAttrs.getStringArray("packageNames")
        scanner.registerFilters()
        val modelMetaDatas=scanner.doScanModel(packages)
        this.addAppModelBean(modelMetaDatas, registry,scanner.appPackageManifests)
    }
}
