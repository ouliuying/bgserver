/*
 *
 *  *
 *  *  *
 *  *  *  * Copyright (c) Shanghai Xing Ye, Co. Ltd.
 *  *  *  * https://bg.work
 *  *  *  *
 *  *  *  * GNU Lesser General Public License Usage
 *  *  *  * Alternatively, this file may be used under the terms of the GNU Lesser
 *  *  *  * General Public License version 3 as published by the Free Software
 *  *  *  * Foundation and appearing in the file LICENSE.txt included in the
 *  *  *  * project of this file. Please review the following information to
 *  *  *  * ensure the GNU Lesser General Public License version 3 requirements
 *  *  *  * will be met: https://www.gnu.org/licenses/lgpl-3.0.html.
 *  *  *
 *  *
 *
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