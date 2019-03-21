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
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanDefinitionHolder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.GenericBeanDefinition
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner
import org.springframework.context.annotation.ScannedGenericBeanDefinition
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.core.type.filter.AnnotationTypeFilter
import work.bg.server.core.config.AppNamePackage
import work.bg.server.core.config.AppPackageManifest
import work.bg.server.core.spring.boot.annotation.ModelRegister
import kotlin.reflect.KClass
class  ModelPathScan constructor(private val annotation: KClass<out Annotation>, registry: BeanDefinitionRegistry): ClassPathBeanDefinitionScanner(registry, false) {
    private val LOGGER = LoggerFactory.getLogger(ModelRegister::class.java)
    var appPackageManifests = mutableMapOf<String,AppPackageManifest>()
    init {

    }

    fun registerFilters() {
        this.addIncludeFilter(AnnotationTypeFilter(this.annotation.java))
        this.addExcludeFilter { metadataReader, _ ->
            val className = metadataReader.getClassMetadata().getClassName()

            className.endsWith("package-info") ||
                    className.endsWith("AppName")
        }
    }

    protected override fun isCandidateComponent(beanDefinition: AnnotatedBeanDefinition): Boolean {
        return beanDefinition.metadata.isConcrete && beanDefinition.metadata.isIndependent
    }

    protected override fun checkCandidate(beanName: String, beanDefinition: BeanDefinition): Boolean {
        return when (super.checkCandidate(beanName, beanDefinition) && beanName.isNotEmpty()) {
            true -> true
            false -> {
                LOGGER.warn( "Bean already defined with the same name!" )
                false
            }
        }
    }

    private  fun getAppPackageManifest(cls: Class<*>): AppPackageManifest {
       val app = cls.newInstance() as AppNamePackage
        return app.get()
    }


    override  fun registerBeanDefinition(definitionHolder: BeanDefinitionHolder?, registry: BeanDefinitionRegistry?) {
        //super.registerBeanDefinition(definitionHolder, registry)
    }
    private  fun getModelNameByModelCls(cls: Class<*>): String{
       var ano = AnnotationUtils.findAnnotation(cls,work.bg.server.core.spring.boot.annotation.Model::class.java)
        if (ano!=null){
            return ano.name
        }
        return ""
    }
    fun doScanModel(packageNames: Array<String>): List<ModelMetaData>{
        val modelMetaDatas: MutableList<ModelMetaData> = arrayListOf<ModelMetaData>()
        packageNames.forEach {
            try {
                //print(App_Name::class.java.canonicalName)
                val appCls=Class.forName(it+".AppName",false,this.resourceLoader.classLoader)
                var appPackageManifest=this.getAppPackageManifest(appCls)
                this.appPackageManifests[appPackageManifest.name]=appPackageManifest
                val appName = appPackageManifest.name //this.getAppName(appCls)
                this.setBeanNameGenerator { definition, registry ->
                    var beanName = definition.beanClassName
                    val sd=definition as ScannedGenericBeanDefinition
                    val beanClass=Class.forName(beanName)
                    val name= this.getModelNameByModelCls(beanClass)
                    //val ano = sd.metadata.getAnnotationAttributes(Model::class.java.name) as Map<String, Object>
                    if(name!=""){
                        beanName=appName+"#"+name+"#"+beanName
                    }
                    when(registry.isBeanNameInUse(beanName)){
                        true -> ""
                        false -> beanName
                    }
                }
                val definitions=this.doScan(it)
                val iter=definitions.iterator()
                while (iter.hasNext()){
                    val holder = iter.next()
                    val definition=holder.beanDefinition as GenericBeanDefinition
                    //definition.beanClass
                    definition.beanClass=Class.forName(definition.beanClassName)
                    var ano=AnnotationUtils.findAnnotation(definition.beanClass,work.bg.server.core.spring.boot.annotation.Model::class.java)
                    val items=holder.beanName.split("#")
                    if(items.count()>2){
                        modelMetaDatas.add(ModelMetaData(items[0],items[1],ano.title,holder))
                    }
                }
            }
            catch (err: Exception)
            {
                print(err.toString())
            }

        }
        return modelMetaDatas
    }
}

