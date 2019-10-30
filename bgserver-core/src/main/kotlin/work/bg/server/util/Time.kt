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

package work.bg.server.util

import org.apache.commons.logging.LogFactory
import java.text.SimpleDateFormat
import java.util.*

object  Time{
    private val logger = LogFactory.getLog(javaClass)
    fun now():Date{
        return Date()
    }
    fun getDate(value:String?):Date?{
        var df = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        try {
            return  df.parse(value)
        }
        catch (ex:Exception){
            logger.error(ex.message)
        }
        return null
    }
}