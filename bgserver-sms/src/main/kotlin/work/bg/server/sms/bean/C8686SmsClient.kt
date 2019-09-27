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

package work.bg.server.sms.bean

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import dynamic.model.query.mq.ModelDataObject
import dynamic.model.query.mq.and
import dynamic.model.query.mq.eq
import dynamic.model.query.mq.gtEq
import org.apache.commons.logging.LogFactory
import org.dom4j.Element
import org.dom4j.io.SAXReader
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import work.bg.server.kafka.SmsClient
import work.bg.server.sms.model.SmsReplyMessageHistory
import java.io.StringReader
import java.lang.Exception
import java.math.BigInteger
import java.util.*
import kotlin.collections.ArrayList
import org.apache.commons.lang3.time.DateUtils
import work.bg.server.sms.model.Sms
import work.bg.server.sms.model.SmsSendHistory
import work.bg.server.util.TypeConvert

@Service
class C8686SmsClient:SmsClient() {
    @Autowired
    lateinit var gson: Gson
    @Autowired
    lateinit var jdbcTemplate: JdbcTemplate
    val logger = LogFactory.getLog(C8686SmsClient::class.java)
    override fun addReplyMessage(data: String) {
        try {
            val reader = StringReader(data)
            val r = SAXReader().read(reader)
            r.selectNodes("/xml/replies/reply")?.forEach {
                try {
                    it?.let {
                        val e = it as Element
                        val mobile = it.selectSingleNode("mobile").text
                        val message = it.selectSingleNode("msg").text
                        val hadSendPartnerIDS = this.getHadSendMobilePartners(mobile)
                        hadSendPartnerIDS.forEach {
                            val rh = SmsReplyMessageHistory.ref
                            var mo = ModelDataObject(model=rh)
                            mo.setFieldValue(rh.receivePartner,it.first)
                            mo.setFieldValue(rh.mobile,mobile)
                            mo.setFieldValue(rh.message,message)
                            mo.setFieldValue(rh.createCorpID,it.second)
                            mo.setFieldValue(rh.lastModifyCorpID,it.second)
                            mo.setFieldValue(rh.createPartnerID,it.first)
                            mo.setFieldValue(rh.lastModifyPartnerID,it.first)
                            mo.setFieldValue(rh.lastModifyTime,Date())
                            mo.setFieldValue(rh.createTime,Date())
                            rh.rawCreate(mo)
                        }
                    }
                }
                catch (ex:Exception){
                    this.logger.error(ex.message)
                }
            }
        }
        catch (ex:Exception){
            this.logger.error(ex.message)
        }
    }
    private  fun getHadSendMobilePartners(mobile:String):ArrayList<Pair<Long,Long>>{
        var ids = arrayListOf<Pair<Long,Long>>()
        var fromDate = DateUtils.addDays(Date(),-3)
        val datas = SmsSendHistory.ref.rawRead(SmsSendHistory.ref.id,
                SmsSendHistory.ref.createPartnerID,
                SmsSendHistory.ref.createCorpID,model=SmsSendHistory.ref,criteria = and(eq(SmsSendHistory.ref.mobile,mobile), gtEq(SmsSendHistory.ref.sendTime,fromDate)))
        datas?.let {
            datas.toModelDataObjectArray().forEach {
                var partnerID = it.getFieldValue(SmsSendHistory.ref.createPartnerID) as Number?
                var corpID = it.getFieldValue(SmsSendHistory.ref.createCorpID) as Number?
                if(partnerID!=null && corpID!=null){
                    ids.add(Pair(
                            TypeConvert.getLong(partnerID)!!,
                            TypeConvert.getLong(corpID)!!
                    ))
                }
            }
        }
        return ids
    }
    override fun updateDeliveryStatus(data: String) {
        try {
            var reader = StringReader(data)
            val r = SAXReader().read(reader)
            val sqls = arrayListOf<String>()
            r.selectNodes("/xml/orders/order")?.forEach {
                it?.let {
                    var e = it as Element
                    val mobile = e.selectSingleNode("mobile").text
                    val orderno = e.selectSingleNode("orderno").text
                    val errorcode = e.selectSingleNode("errorcode").text.toInt()
                    val errormsg = e.selectSingleNode("errormsg").text
                    val sql = "update public.sms_send_history set status=$errorcode,status_desc=$errormsg where msg_id=$orderno and mobile=$mobile"
                    sqls.add(sql)
                }
            }
            this.jdbcTemplate.execute(sqls.joinToString { ";" })
        }
        catch (ex:Exception){
            this.logger.error(ex.message)
        }
    }

    override fun updateSubmitStatus(data: String) {
        try {
            var jobj = this.gson.fromJson(data,JsonObject::class.java)
            jobj?.let {
                val localMsgID = it["localMsgID"].asString
                var status = it["status"].asInt
                status= if(status==0) -1 else status
                val statusDesc = it["statusDesc"].asString
                val submitMsgID = it["submitMsgID"].asString
                val sql = "update public.sms_send_history set status=$status,status_desc=$statusDesc,msg_id=$submitMsgID where msg_id=$localMsgID"
                this.jdbcTemplate.execute(sql)
            }
        }
        catch (ex:Exception){
            this.logger.error(ex.message)
        }
    }
}