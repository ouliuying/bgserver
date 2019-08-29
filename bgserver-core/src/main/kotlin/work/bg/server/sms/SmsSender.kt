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

package work.bg.server.sms

import work.bg.server.core.cache.PartnerCache
import java.util.*
import kotlin.collections.ArrayList

interface SmsSender {

    fun send(mobiles:ArrayList<String>,
             message:String,
             repeatFilter:Boolean,
             useAccessControl: Boolean,
             partnerCache: PartnerCache?):Boolean
    fun sendFile(mobileFile:String,
                 message: String,
                 repeatFilter:Boolean,
                 useAccessControl: Boolean,
                 partnerCache: PartnerCache?):Boolean

    fun timingSendOnce(mobiles:ArrayList<String>,
                       message:String,
                       repeatFilter:Boolean,
                       timingDate:Date,
                       useAccessControl: Boolean,
                       partnerCache: PartnerCache?):Boolean
    fun timingSendFileOnce(mobileFile:String,
                           message: String,
                           repeatFilter:Boolean,
                           timingDate:Date,
                           useAccessControl: Boolean,
                           partnerCache: PartnerCache?):Boolean

}