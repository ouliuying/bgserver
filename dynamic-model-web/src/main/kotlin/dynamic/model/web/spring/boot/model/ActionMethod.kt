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
import dynamic.model.query.mq.model.AppModel
import dynamic.model.web.context.ContextType
import dynamic.model.web.errorcode.ErrorCode
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import org.springframework.web.servlet.support.RequestContextUtils
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.javaMethod

class ActionMethod constructor(val method: KFunction<*>){
    var bean: Any? = null
    var name:String?=null
    fun invoke(request:HttpServletRequest,
               response:HttpServletResponse,
               session: HttpSession,
               appModel: AppModel,
               appName:String,
               modelName:String,
               context: ContextType?=null):Any?{
        return if(bean!=null){
            this.doInvoke(request,response,session,appModel,context)
        }else{
            var model=appModel.getModel(appName,modelName)
            if(model!=null){
                this.bean=model
                this.doInvoke(request,response,session,appModel,context)
            }
            else{
                return ActionResult(ErrorCode.UNKNOW)
            }
        }
    }

    private fun doInvoke(request:HttpServletRequest,
                         response:HttpServletResponse,
                         session: HttpSession,
                         appModel: AppModel,
                         context:ContextType?=null): Any?{
        val webRequest = ServletWebRequest(request, response)
        val handlerMethod=HandlerMethod(this.bean,this.method.javaMethod)
        var getDataBinderFactory=RequestMappingHandlerAdapter::class.java!!.getDeclaredMethod("getDataBinderFactory",HandlerMethod::class.java)
        getDataBinderFactory.isAccessible = true
        var binderFactory=getDataBinderFactory.invoke(appModel.requestMappingHandlerAdapter,handlerMethod)
        val invocableMethod = ServletInvocableHandlerMethod(handlerMethod)
        invocableMethod.setDataBinderFactory(binderFactory as WebDataBinderFactory)
        val argumentResolversField = RequestMappingHandlerAdapter::class.java!!.getDeclaredField("argumentResolvers")
        argumentResolversField.isAccessible = true
        val value = argumentResolversField.get(appModel.requestMappingHandlerAdapter)
        if (value != null) {
            invocableMethod.setHandlerMethodArgumentResolvers(value as HandlerMethodArgumentResolverComposite)
        }
        val parameterNameDiscovererField = RequestMappingHandlerAdapter::class.java!!.getDeclaredField("parameterNameDiscoverer")
        parameterNameDiscovererField.isAccessible = true
        val value2 = parameterNameDiscovererField.get(appModel.requestMappingHandlerAdapter)
        if (value2!= null){
            invocableMethod.setParameterNameDiscoverer(value2 as ParameterNameDiscoverer)
        }

        var mavContainer = ModelAndViewContainer()
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request))
        mavContainer.setIgnoreDefaultModelOnRedirect(true)

        var ar= invocableMethod.invokeForRequest(webRequest as NativeWebRequest,mavContainer,session,context)
        return when(ar){
            is ActionResult ->{
                    ar
            }
            else->ar
        }
    }
}