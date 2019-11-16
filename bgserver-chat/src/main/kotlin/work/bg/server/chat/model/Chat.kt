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

package work.bg.server.chat.model

import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import dynamic.model.query.mq.RefSingleton
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.errorcode.ErrorCode
import dynamic.model.web.spring.boot.model.ActionResult
import java.lang.Exception
import java.net.URI

@Model("chat","企信")
class Chat:ContextModel("chat","public") {
    private val logger = LogFactory.getLog(javaClass)
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
    fun activeChatSession(@RequestParam("sessionID") sessionID:String):ActionResult?{
        this.logger.info("active session $sessionID")
        var ar =ActionResult()
        var redisClient:redis.clients.jedis.Jedis?=null
        try {
            redisClient=redis.clients.jedis.Jedis(URI(this.redisUrl),60)
            var ret = redisClient.expire(sessionID,1800)
            ar.errorCode=ErrorCode.SUCCESS
        }
        catch (ex: Exception){
            ex.printStackTrace()
        }
        finally {
            redisClient?.close()
        }
        return ar
    }

    fun getChannelMetaByChatSessionID(chatSessionID:String):String?{
        var redisClient:redis.clients.jedis.Jedis?=null
        try {
            var redisClient= redis.clients.jedis.Jedis(URI(this.redisUrl),60)
            var ret = redisClient.hgetAll(chatSessionID)
            return ret?.let {
                val corpID = it["corpID"]?.toLong()
                val model = it["model"]
                val modelID = it["modelID"]?.toLong()
                if(model=="partner" && corpID!=null && corpID>0 &&modelID!=null && modelID>0) {
                     ChatPartner.ref.getPartnerChannelMeta(corpID =corpID,partnerID = modelID)
                }
                else null
            }
        }
        catch (ex: Exception){
            ex.printStackTrace()
        }
        finally {
            redisClient?.close()
        }
        return null
    }
}