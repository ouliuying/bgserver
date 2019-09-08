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

package work.bg.server.sms.job

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.tomcat.jni.File
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AutowireCapableBeanFactory
import dynamic.model.query.mq.FieldValueArray
import dynamic.model.query.mq.ModelDataArray
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.eq
import dynamic.model.web.spring.boot.model.AppModelWeb
import work.bg.server.sms.bean.C8686SenderBean
import work.bg.server.sms.bean.DefaultSmsSender
import work.bg.server.kafka.SmsClient
import work.bg.server.sms.MobileHelper
import work.bg.server.sms.SmsTimerType
import work.bg.server.sms.model.SmsSendHistory
import work.bg.server.sms.model.SmsSetting
import work.bg.server.sms.model.SmsTimerQueue
import java.io.FileReader
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.ArrayList

class SmsJob:Job {
    @Autowired
    lateinit var smsProvider: C8686SenderBean
    @Autowired
    lateinit var smsClient: SmsClient
    @Autowired
    lateinit var gson:Gson
    override fun execute(context: JobExecutionContext?) {
       val dmap  = context?.jobDetail?.jobDataMap
        AppModelWeb.ref.appContext?.autowireCapableBeanFactory?.autowireBeanProperties(
                this,
                AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE,
                false)

        dmap?.let {
            var msg = dmap["message"] as String?
            val smsCount = if(msg!=null) this.getSmsCount(msg) else 0
            val sendType = if(dmap.containsKey("smsTimerType")) 1 else 0
            var smsTimerType=SmsTimerType.NORMAL.typ
            if(sendType==1){
                smsTimerType=dmap["smsTimerType"] as Int
            }
            val ip = dmap["ip"] as String?
            var userName:String=""
            var password:String=""
            SmsSetting.ref.rawRead(criteria = null)?.firstOrNull()?.let {
                it.getFieldValue(SmsSetting.ref.userName)?.let {uN->
                    userName = uN as String
                }
                it.getFieldValue(SmsSetting.ref.password)?.let {p->
                    password = p as String
                }
            }
            var partnerID = dmap.getLong("partnerID") as Long?
            var corpID = dmap.getLong("corpID") as Long?
            var rmRepeatFilter = dmap["repeatFilter"] as Boolean
            var mobiles = if(dmap.containsKey("mobileFile")){
                readAllMobiles(dmap["mobileFile"] as String)
            }else if(dmap.containsKey("mobiles")){
                dmap["mobiles"] as ArrayList<String>
            }
            else {
                readAllMobilesFromTimerQueue(dmap["timerQueueID"] as Long?)
            }
            if(smsTimerType==SmsTimerType.RUN_ONCE.typ){
                val tid = dmap["timerQueueID"] as Long?
                tid?.let {
                    SmsTimerQueue.ref.rawDelete(criteria = eq(SmsTimerQueue.ref.id,it))
                }
            }
            mobiles = if(rmRepeatFilter) arrayListOf(*mobiles.distinct().toTypedArray()) else mobiles
            mobiles = arrayListOf(*(mobiles.filter {
                MobileHelper.isMobile(it)
            }.toTypedArray()))

            if(mobiles.count()>0 && msg!=null && msg.isNotEmpty()){
                var fromIndex = 0
                val size = 300
                do {
                    var toIndex = fromIndex + size
                    if(toIndex>mobiles.count()){
                        toIndex=mobiles.count()
                    }
                   val packMobiles =  mobiles.subList(fromIndex,toIndex)
                    val localMsgID = UUID.randomUUID().toString()
                    if(this.addSendHistory(packMobiles.toTypedArray(),
                            msg,
                            localMsgID,
                            sendType,
                            smsCount,
                            ip?:"",
                            partnerID,corpID)){
                        var ret= this.smsProvider.doSend(packMobiles.toTypedArray(),msg,userName,password)
                        this.addSubmitStatusToKafka(localMsgID,
                                ret.first,
                                ret.second,
                                (ret.third as String?)?:"")
                    }
                }while (toIndex<mobiles.count())
            }
        }
    }

    private fun getSmsCount(msg:String):Int{
        var len = msg.length
        return (len/70) + if(len%70>0) 1 else 0
    }

    private fun addSendHistory(
            mobiles:Array<String>,
            msg:String,
            msgID:String,
            sendType:Int,
            smsCount:Int,
            ip:String,
            partnerID:Long?,
            corpID:Long?
    ):Boolean{
        val shModel = SmsSendHistory.ref
        var ma= dynamic.model.query.mq.ModelDataArray(model = shModel)
        mobiles.forEach {
            var fvs = dynamic.model.query.mq.FieldValueArray()
            fvs.setValue(shModel.ip,ip)
            fvs.setValue(shModel.mobile,it)
            fvs.setValue(shModel.message,msg)
            fvs.setValue(shModel.msgID,msgID)
            fvs.setValue(shModel.sendType,sendType)
            fvs.setValue(shModel.smsCount,smsCount)
            fvs.setValue(shModel.sendPartner,partnerID)
            fvs.setValue(shModel.createCorpID,corpID)
            fvs.setValue(shModel.createPartnerID,partnerID)
            fvs.setValue(shModel.lastModifyCorpID,corpID)
            fvs.setValue(shModel.lastModifyPartnerID,partnerID)
            ma.add(fvs)
        }
        val ret = shModel.safeCreate(ma)
        return ret.first!=null && ret.first!!>0
    }
    private fun addSubmitStatusToKafka(localMsgID:String,status:Int,statusDesc:String,submitMsgID:String){
        var msg=JsonObject()
        msg.addProperty("localMsgID",localMsgID)
        msg.addProperty("status",status)
        msg.addProperty("statusDesc",statusDesc)
        msg.addProperty("submitMsgID",submitMsgID)
        this.smsClient.pushSubmitStatus(msg.toString())
    }
    private fun readAllMobiles(file:String):ArrayList<String>{
        var mobiles = arrayListOf<String>()
        var txt = String(Files.readAllBytes(Paths.get(file)))
        var lines = txt.split("\r","\n")
        lines.forEach {
            if(MobileHelper.isMobile(it)){
                mobiles.add(it)
            }
        }
        return mobiles
    }
    private  fun readAllMobilesFromTimerQueue(timerQueueID:Long?):ArrayList<String>{
        var mobiles = arrayListOf<String>()
        timerQueueID?.let {
            val tQueue= SmsTimerQueue.ref.rawRead(criteria = eq(SmsTimerQueue.ref.id,it))?.firstOrNull()
            tQueue?.let {
                q->
                val strMobiles = q.getFieldValue(SmsTimerQueue.ref.mobiles) as String?
                if(strMobiles!=null){
                    mobiles.addAll(strMobiles.split(","))
                }
            }
        }
        return mobiles
    }
}