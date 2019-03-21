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

package work.bg.server.core.spring.boot.annotation
import org.springframework.beans.factory.support.BeanDefinitionRegistry

import org.springframework.context.ResourceLoaderAware

import org.springframework.context.annotation.ImportBeanDefinitionRegistrar
import org.springframework.core.annotation.AnnotationAttributes
import org.springframework.core.io.ResourceLoader
import org.springframework.core.type.AnnotationMetadata
import org.springframework.util.StringUtils
import work.bg.server.core.spring.boot.model.AppModel
import work.bg.server.core.spring.boot.model.ModelMetaData
import work.bg.server.core.spring.boot.model.ModelPathScan
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import work.bg.server.core.config.AppPackageManifest
import java.util.function.Supplier
class ModelRegister constructor(): ImportBeanDefinitionRegistrar,
        ResourceLoaderAware{
    private var resourceLoader: ResourceLoader? = null
    private  var applicationContext: ApplicationContext?= null
    private  var appModel: AppModel? = null

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


    private  fun addAppModelBean(modelMetaDatas: List<ModelMetaData>,registry: BeanDefinitionRegistry,appPackageManifests:Map<String,AppPackageManifest>){
        val appModel=AppModel(modelMetaDatas,appPackageManifests,this.applicationContext)
        val concreteModelMetaDatas=appModel.refresh()

        val beanDefinition = RootBeanDefinition(
                AppModel::class.java,
                Supplier<AppModel> { appModel }
        )
        val appModelHolder = BeanDefinitionHolder(beanDefinition, "appmodel")

        BeanDefinitionReaderUtils.registerBeanDefinition(appModelHolder, registry)

        concreteModelMetaDatas.forEach {
            if (it!=null){
                var holder=it.beanDefinitionHolder
                BeanDefinitionReaderUtils.registerBeanDefinition(holder,registry)
            }
        }

    }

    private fun registerBeanDefinitions(annoAttrs: AnnotationAttributes, registry: BeanDefinitionRegistry) {
        val scanner = ModelPathScan(Model::class,registry)
        if (this.resourceLoader != null) {
            scanner.resourceLoader = this.resourceLoader as ResourceLoader
        }
        val packages=annoAttrs.getStringArray("packageNames")
        scanner.registerFilters()
        val modelMetaDatas=scanner.doScanModel(packages)
        this.addAppModelBean(modelMetaDatas, registry,scanner.appPackageManifests)
    }
}
