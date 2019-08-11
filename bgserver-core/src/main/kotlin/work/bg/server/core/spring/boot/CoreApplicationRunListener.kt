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
