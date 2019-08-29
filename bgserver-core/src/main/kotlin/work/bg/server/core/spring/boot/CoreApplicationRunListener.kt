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

package work.bg.server.core.spring.boot

import org.apache.commons.logging.LogFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.SpringApplicationRunListener
import org.springframework.context.ApplicationContext
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.ConfigurableEnvironment
import work.bg.server.util.MethodInvocation
import work.bg.server.core.spring.boot.model.AppModel
import work.bg.server.core.ui.UICache
import java.util.*

class CoreApplicationRunListener(val app:SpringApplication,vararg val args:String):SpringApplicationRunListener {
    private val logger = LogFactory.getLog(javaClass)
    init {
        logger.info("CoreApplicationRunListener load at ${Date()}")
    }
    override fun contextLoaded(context: ConfigurableApplicationContext?) {

    }

    override fun contextPrepared(context: ConfigurableApplicationContext?) {

    }

    override fun environmentPrepared(environment: ConfigurableEnvironment?) {

    }

    override fun failed(context: ConfigurableApplicationContext?, exception: Throwable?) {


    }

    override fun running(context: ConfigurableApplicationContext?) {

    }

    override fun started(context: ConfigurableApplicationContext?) {
        var appModel=(context as ApplicationContext).getBean(AppModel::class.java)
        var uiCache =(context as ApplicationContext).getBean(UICache::class.java)
        MethodInvocation(appModel, "initialize")()
        MethodInvocation(uiCache, "loadUI")()
    }

    override fun starting() {

    }
}
