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

package work.bg.server.core
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter
import work.bg.server.core.spring.boot.annotation.ShouldLoginInterceptor
import org.springframework.web.filter.ShallowEtagHeaderFilter
import javax.servlet.Filter
import org.zalando.logbook.Logbook
import org.zalando.logbook.servlet.LogbookFilter


inline fun <reified T : Any> runServer(vararg args: String): Unit{
    SpringApplication.run(arrayOf(T::class.java,CoreApplication::class.java),args)
}

//@EnableJdbcHttpSession
@SpringBootConfiguration
class  CoreApplication: WebMvcConfigurer
{
    @Value("\${bg.work.unauth-redirect-url}")
    private val unauthRedirectUrl: String? = null
    override  fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(ShouldLoginInterceptor(this.unauthRedirectUrl))
    }
    @Bean
    fun logBookFilter(): Filter {
        val logbook = Logbook.create()
        var filter= LogbookFilter(logbook)
        return filter
    }
}

