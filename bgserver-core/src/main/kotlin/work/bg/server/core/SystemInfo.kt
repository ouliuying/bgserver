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

package work.bg.server.core

import org.apache.commons.logging.LogFactory
import java.util.concurrent.locks.Lock

object SystemInfo{
    @JvmStatic
    var logger = LogFactory.getLog(CoreApplication::class.java)
    @JvmStatic
    fun print(){
        logger.info("""
     .______     _______           ____    __    ____  ______   .______       __  ___ 
     |   _  \   /  _____|          \   \  /  \  /   / /  __  \  |   _  \     |  |/  / 
     |  |_)  | |  |  __             \   \/    \/   / |  |  |  | |  |_)  |    |  '  /  
     |   _  <  |  | |_ |             \            /  |  |  |  | |      /     |    <   
     |  |_)  | |  |__| |     __       \    /\    /   |  `--'  | |  |\  \----.|  .  \  
     |______/   \______|    (__)       \__/  \__/     \______/  | _| `._____||__|\__\ 
               
     === bg.work === $Version
         """)
    }
    @JvmStatic
    val Version = "1.0.0 Release"
}