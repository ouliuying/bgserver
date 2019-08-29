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

package work.bg.server.core
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringBootConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.core.annotation.Order
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import work.bg.server.core.spring.boot.annotation.ShouldLoginInterceptor
import javax.servlet.Filter


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
    @Order(HIGHEST_PRECEDENCE)
    fun resetRequestBodyFilter(): Filter {
        return ResetRequestFilter()
    }
}

