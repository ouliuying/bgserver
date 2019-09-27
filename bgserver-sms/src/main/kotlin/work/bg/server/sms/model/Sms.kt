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

package work.bg.server.sms.model

import com.google.gson.JsonObject
import dynamic.model.query.mq.RefSingleton
import dynamic.model.web.errorcode.ErrorCode
import work.bg.server.core.model.ContextModel
import dynamic.model.web.spring.boot.annotation.Action
import dynamic.model.web.spring.boot.annotation.Model
import dynamic.model.web.spring.boot.model.ActionResult
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.RequestBody
import work.bg.server.core.cache.PartnerCache
import work.bg.server.kafka.SmsClient
import work.bg.server.sms.bean.DefaultSmsSender
import java.io.StringReader
import java.lang.Exception

@Model("sms")
class Sms:ContextModel("sms","public") {
    @Autowired
    lateinit var  smsSender:DefaultSmsSender
    @Autowired
    lateinit var smsKafkaClient: SmsClient
    companion object : RefSingleton<Sms> {
        override lateinit var ref: Sms
    }
    @Action("sendSms")
    fun sendSms(@RequestBody data:JsonObject, partnerCache: PartnerCache): ActionResult?{
        var ar=ActionResult()
        val mobiles = data["mobiles"].asString
        val message = data["message"].asString
        val rmRepeat = data["rmRepeat"].asBoolean
        val timerValue = data["timerValue"].asString
        val timerType = data["timerType"].asInt
        var mobileList = mobiles.split(",","，").toTypedArray()
        if(mobileList.count()<1 && mobiles.length<11){
            ar.errorCode=ErrorCode.UNKNOW
            ar.description="提交手机号不能为空空"
            return ar
        }
        if(message.isNullOrBlank() || message.isNullOrEmpty()){
            ar.errorCode=ErrorCode.UNKNOW
            ar.description="短信内容不能为空"
            return ar
        }
        if(timerType<0){
            smsSender.send(arrayListOf(*mobileList),
                    message =message,
                    repeatFilter = rmRepeat,
                    useAccessControl = true,
                    partnerCache = partnerCache)
        }
        else if(timerType==0){
            var timeingDate=work.bg.server.util.Time.getDate(timerValue)
            smsSender.timingSendOnce(arrayListOf(*mobileList),message = message,
                    repeatFilter = rmRepeat,
                    timingDate = timeingDate,useAccessControl = true,partnerCache = partnerCache)
        }
        return ar
    }

    @Action("importSendSms")
    fun importSendSms(partnerCache: PartnerCache):ActionResult?{
        var ar=ActionResult()
        return ar
    }

    @Action("receiveStatus")
    fun receiveStatus(@RequestBody xmlData:String?=null):String{
        xmlData?.let {
            if(xmlData.isNullOrEmpty() || xmlData.isNullOrBlank()){
                return "fail"
            }
            this.smsKafkaClient.pushDeliveryStatus(xmlData)
        }
        return "success"
    }

    @Action("receiveReply")
    fun receiveReply(@RequestBody xmlData:String?=null):String{
        xmlData?.let {
            if(xmlData.isNullOrEmpty() || xmlData.isNullOrBlank()){
                return "fail"
            }
            this.smsKafkaClient.pushReplyMsg(xmlData)
        }
        return "success"
    }
}