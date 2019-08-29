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

package work.bg.server.chat

import io.vertx.codegen.annotations.Fluent
import io.vertx.core.AsyncResult
import io.vertx.core.Handler
import io.vertx.core.Vertx
import io.vertx.core.eventbus.Message
import io.vertx.core.json.JsonObject
import org.apache.commons.lang3.time.DateUtils
import java.util.*
import kotlin.collections.ArrayList

class ModelClientChatSession(var redisClient:ModelClientRedis,
                             var model:String,
                             var corpID:Long,
                             var modelID:Long,
                             var chatUUID:String,
                             var deviceSessionIDArray:ArrayList<ModelClientChatDeviceSessionID> = arrayListOf()) {
    init {
        this.redisClient.chatSession = this
    }
    lateinit var vertx: Vertx
    fun refreshTimeout(chatSessionID:String=""){
        if(!chatSessionID.isBlank() && !chatSessionID.isEmpty()){
            this.deviceSessionIDArray.filter {
                it.chatSessionID == chatSessionID
            }.forEach {
                it.timeout = DateUtils.addMinutes(Date(),30)
            }
        }
        else{
            this.deviceSessionIDArray.forEach {
                it.timeout = DateUtils.addMinutes(Date(),30)
            }
        }
    }

    fun removeDeviceSessionID(chatSessionID: String){
        var deviceSessionID = this.deviceSessionIDArray.removeIf {
            it.chatSessionID == chatSessionID
        }
    }

    fun hasDeviceChatSessionID(chatSessionID: String,devType:Int):Boolean{
        return this.deviceSessionIDArray.count {
            it.chatSessionID==chatSessionID && it.devType == devType
        }>0
    }

    fun addChatDeviceSessionID(chatSessionID: String,devType:Int){
        this.deviceSessionIDArray.add(
                ModelClientChatDeviceSessionID(devType,chatSessionID)
        )
    }
    fun dispatchMessage(message: Message<JsonObject>){
        this.redisClient.publish(message,this)
    }
    @Fluent
    fun subscribeToRedis(handler:Handler<AsyncResult<UInt>>):ModelClientChatSession{
        this.redisClient.subscribe(handler)
        return this
    }
}