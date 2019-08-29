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
import org.springframework.core.ParameterNameDiscoverer
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.async.WebAsyncUtils
import org.springframework.web.method.HandlerMethod
import org.springframework.web.method.support.HandlerMethodArgumentResolverComposite
import org.springframework.web.method.support.InvocableHandlerMethod
import org.springframework.web.method.support.ModelAndViewContainer
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod
import org.springframework.web.servlet.support.RequestContextUtils
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.cache.PartnerCacheKey
import work.bg.server.core.constant.SessionTag
import work.bg.server.core.model.ContextModel
import work.bg.server.errorcode.ErrorCode
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import kotlin.reflect.KCallable
import kotlin.reflect.KFunction
import kotlin.reflect.KParameter
import kotlin.reflect.jvm.javaMethod
import java.lang.reflect.AccessibleObject.setAccessible
class ActionMethod constructor(val method: KFunction<*>){
    var bean: Any? = null
    var name:String?=null
    fun invoke(request:HttpServletRequest,
               response:HttpServletResponse,
               session: HttpSession,
               appModel: AppModel,
               appName:String,
               modelName:String):Any?{
        return if(bean!=null){
            this.doInvoke(request,response,session,appModel)
        }else{
            var model=appModel.getModel(appName,modelName)
            if(model!=null){
                this.bean=model
                this.doInvoke(request,response,session,appModel)
            }
            else{
                return ActionResult(ErrorCode.UNKNOW)
            }
        }
    }

    private fun doInvoke(request:HttpServletRequest,
                         response:HttpServletResponse,
                         session: HttpSession,
                         appModel: AppModel): Any?{
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
        var partnerCacheKey=session.getAttribute(SessionTag.SESSION_PARTNER_CACHE_KEY)
        var partnerCache=null as PartnerCache?
        if(partnerCacheKey!=null){
            partnerCache= (this.bean as ContextModel).partnerCacheRegistry?.get(partnerCacheKey as PartnerCacheKey)
        }
        else{
            val apiToken= request.getHeader("token")
            if(!apiToken.isNullOrEmpty()){

            }
        }
        var mavContainer = ModelAndViewContainer()
        mavContainer.addAllAttributes(RequestContextUtils.getInputFlashMap(request))
        mavContainer.setIgnoreDefaultModelOnRedirect(true)

        var ar= invocableMethod.invokeForRequest(webRequest as NativeWebRequest,mavContainer,session,partnerCacheKey,partnerCache)
        return when(ar){
            is ActionResult->{

                    ar

            }
            else->ar
        }
    }
}