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

package work.bg.server.sms.bean

import org.quartz.*
import org.springframework.stereotype.Service
import work.bg.server.sms.SmsSender
import java.util.*
import kotlin.collections.ArrayList
import org.springframework.beans.factory.annotation.Autowired
import work.bg.server.core.cache.PartnerCache
import work.bg.server.core.mq.ModelDataObject
import work.bg.server.sms.MobileHelper
import java.util.UUID
import work.bg.server.sms.SmsTimerType
import work.bg.server.sms.job.SmsJob
import work.bg.server.sms.model.SmsSendJobLog
import work.bg.server.sms.model.SmsTimerQueue
import java.nio.file.Files
import java.nio.file.Paths
import java.time.ZonedDateTime




@Service
class DefaultSmsSender: SmsSender {
    @Autowired
    private val scheduler: Scheduler? = null
    private fun buildSendMobilesJob(smsParams: JobDataMap,
                                    name:String,group:String,
                                    description:String):JobDetail{
        smsParams["meta_group"] = group
        smsParams["meta_jobName"] = name
        smsParams["meta_description"] = description
        return JobBuilder.newJob(SmsJob::class.java)
                .withIdentity(name, group)
                .withDescription(description)
                .usingJobData(smsParams)
                .storeDurably()
                .build()
    }

    private fun buildSendMobileTriggerBuilder(jobDetail: JobDetail,
                                              name:String,group:String,
                                              description:String): TriggerBuilder<Trigger> {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail)
                .withIdentity(jobDetail.key.name, group)
                .withDescription(description)
    }

    override fun send(mobiles: ArrayList<String>,
                      message: String,
                      repeatFilter: Boolean,
                      useAccessControl: Boolean,
                      partnerCache:PartnerCache?): Boolean {
        val group = "send sms jobs"
        val description = "send sms job"
        val jobName = UUID.randomUUID().toString()
        var smsParams = JobDataMap()
        smsParams["smsTimerType"]= SmsTimerType.NORMAL.typ
        smsParams["mobiles"]=mobiles
        smsParams["message"]=message
        smsParams["repeatFilter"]=repeatFilter
        smsParams["partnerID"]=partnerCache?.partnerID
        smsParams["corpID"]=partnerCache?.corpID
        if(this.addJobLog(jobName,group,jobName,group,useAccessControl,partnerCache)){
            val jobDetail = buildSendMobilesJob(smsParams,jobName,group,description)
            val tb = buildSendMobileTriggerBuilder(jobDetail,jobName,group,description)
            val trigger = tb.startNow()
                            .withSchedule(SimpleScheduleBuilder
                            .simpleSchedule()
                            .withMisfireHandlingInstructionFireNow())
                            .build()
            this.scheduler?.scheduleJob(jobDetail,trigger)
        }
        return true
    }
    private  fun addJobLog(jobName:String,jobGroup:String,
                           triggerName:String,triggerGroup:String,
                           useAccessControl: Boolean,
                           partnerCache:PartnerCache?):Boolean{
        val tm = SmsSendJobLog.ref
        val mo = ModelDataObject(model=tm)
        mo.setFieldValue(tm.jobName,jobName)
        mo.setFieldValue(tm.jobGroup,jobGroup)
        mo.setFieldValue(tm.triggerName,triggerName)
        mo.setFieldValue(tm.triggerGroup,triggerGroup)
        partnerCache?.let {
            mo.setFieldValue(tm.sendPartner,it.partnerID)
        }
        val ret=tm.rawCreate(mo,useAccessControl,partnerCache)
        if(ret?.first != null && ret.first!!>0){
            return true
        }
        return false
    }
    override fun sendFile(mobileFile: String,
                          message: String,
                          repeatFilter: Boolean,
                          useAccessControl: Boolean,
                          partnerCache:PartnerCache?): Boolean {
        val group = "send sms file jobs/triggers"
        val description = "send sms file job/triggers"
        val jobName = UUID.randomUUID().toString()
        var smsParams = JobDataMap()
        smsParams["smsTimerType"]= SmsTimerType.NORMAL.typ
        smsParams["mobileFile"]=mobileFile
        smsParams["message"]=message
        smsParams["repeatFilter"]=repeatFilter
        smsParams["partnerID"]=partnerCache?.partnerID
        smsParams["corpID"]=partnerCache?.corpID
        if(this.addJobLog(jobName,group,jobName,group,
                        useAccessControl,partnerCache)){
            val jobDetail = buildSendMobilesJob(smsParams,jobName,group,description)
            val tb = buildSendMobileTriggerBuilder(jobDetail,jobName,group,description)
            val trigger = tb.startNow()
                    .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                    .build()
            this.scheduler?.scheduleJob(jobDetail,trigger)
        }
        return true
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
    override fun timingSendFileOnce(mobileFile: String,
                                    message: String,
                                    repeatFilter: Boolean,
                                    timingDate: Date,
                                    useAccessControl: Boolean,
                                    partnerCache:PartnerCache?): Boolean {
        var mobiles = this.readAllMobiles(mobileFile)
        return this.timingSendOnce(mobiles,message,repeatFilter,timingDate,useAccessControl,partnerCache)
    }

    override fun timingSendOnce(mobiles: ArrayList<String>,
                                message: String,
                                repeatFilter: Boolean,
                                timingDate: Date,
                                useAccessControl: Boolean,
                                partnerCache:PartnerCache?): Boolean {
        val qID = this.addSmsTimerQueue(mobiles,
                        message,
                        timingDate,
                        SmsTimerType.RUN_ONCE.typ,
                        useAccessControl,
                        partnerCache)
        qID?.let {
            val group = "send timing sms jobs"
            val description = "send timing sms job"
            val jobName = UUID.randomUUID().toString()
            var smsParams = JobDataMap()
            smsParams["smsTimerType"]= SmsTimerType.RUN_ONCE.typ
            smsParams["mobiles"]=mobiles
            smsParams["message"]=message
            smsParams["timerQueueID"]=it
            smsParams["timingDate"]=timingDate
            smsParams["repeatFilter"]=repeatFilter
            smsParams["partnerID"]=partnerCache?.partnerID
            smsParams["corpID"]=partnerCache?.corpID
            if(this.addJobLog(jobName,group,jobName,group,useAccessControl,partnerCache)){
                val jobDetail = buildSendMobilesJob(smsParams,jobName,group,description)
                val tb = buildSendMobileTriggerBuilder(jobDetail,jobName,group,description)
                val trigger = tb.startAt(timingDate)
                                .withSchedule(SimpleScheduleBuilder
                                .simpleSchedule()
                                .withMisfireHandlingInstructionFireNow())
                                .build()
                this.scheduler?.scheduleJob(jobDetail,trigger)
            }
        }
        return true
    }
    private  fun addSmsTimerQueue(mobiles: ArrayList<String>,
                                  message: String,
                                  timingDate: Date,
                                  smsTimerType:Int,
                                  useAccessControl: Boolean,
                                  partnerCache:PartnerCache?):Long?{
        val refSTQ = SmsTimerQueue.ref
        val mo = ModelDataObject(model=SmsTimerQueue.ref)
        mo.setFieldValue(refSTQ.mobiles,mobiles.joinToString(","))
        mo.setFieldValue(refSTQ.message,message)
        mo.setFieldValue(refSTQ.timerValue,timingDate)
        mo.setFieldValue(refSTQ.timerType,smsTimerType)
        mo.setFieldValue(refSTQ.addTime,Date())
        val ret = refSTQ.safeCreate(mo,useAccessControl,partnerCache)
        return ret.first
    }
}