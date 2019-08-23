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

package work.bg.server.chat.model

import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.RefSingleton
import work.bg.server.core.model.ContextModel
import work.bg.server.core.spring.boot.annotation.Action
import work.bg.server.core.spring.boot.annotation.Model
import work.bg.server.core.spring.boot.model.ActionResult
import work.bg.server.errorcode.ErrorCode
import java.lang.Exception
import java.net.URI
import java.util.*

@Model("chat")
class Chat:ContextModel("chat","public") {
    @Value("\${bg.chat.redis.url}")
    private lateinit var redisUrl:String

    companion object: RefSingleton<Chat> {
        override lateinit var ref: Chat
    }

    @Action("loadChannelMeta")
    fun loadChannelMeta(@RequestBody chatData: com.google.gson.JsonObject):ActionResult?{
        val chatSessionID = chatData.get("chatSessionID")?.asString
        return chatSessionID?.let {
             val ar = ActionResult(ErrorCode.UNKNOW,"continue")
             val channelMeta = this.getChannelMetaByChatSessionID(chatSessionID)
             return if(channelMeta!=null) {
                 ar.errorCode = ErrorCode.SUCCESS
                 ar.bag["channelMeta"] = this.gson.fromJson(channelMeta,com.google.gson.JsonArray::class.java)
                 return ar
             } else ar
        }?:ActionResult(ErrorCode.UNKNOW,"continue")
    }

    @Action("activeChatSession")
    fun activeChatSession(sessionID:String):ActionResult?{
        return null
    }

    private fun getChannelMetaByChatSessionID(chatSessionID:String):String?{
        try {
            var pool= redis.clients.jedis.JedisPool(URI(this.redisUrl))
            var ret = pool.resource.hgetAll(chatSessionID)
            return ret?.let {
                return it["channelMeta"]
            }
        }
        catch (ex: Exception){
            ex.printStackTrace()
        }
        return null
    }
}