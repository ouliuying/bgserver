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

package work.bg.server

import org.apache.commons.logging.LogFactory
import java.util.Date
import org.springframework.boot.autoconfigure.SpringBootApplication
import work.bg.server.core.runServer
import work.bg.server.core.spring.boot.annotation.ModelScan


@SpringBootApplication
@ModelScan(packageNames = ["work.bg.server.core.model",
    "work.bg.server.corp.model",
    "work.bg.server.product.model",
    "work.bg.server.crm.model",
    "work.bg.server.account.model",
    "work.bg.server.worktable.model",
    "work.bg.server.setting.model"])
class ServerApplication{
    private val logger = LogFactory.getLog(javaClass)
    init {
        logger.info("bg.work server start ${Date()}")
    }
}

fun main(args: Array<String>){
    runServer<ServerApplication>(*args)
}