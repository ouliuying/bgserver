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

package work.bg.server.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
open abstract class SmsClient {
    companion object{
        const val SMS_SUBMIT_DELIVERY_STATUS_QUEUE = "smsreceivesubmitanddeliverstatus"
        const val SMS_REPLY_QUEUE="smsreceivereplymessage"

        const val SUBMIT_SMS_STATUS = "submitsmsstatus"
        const val DELIVERY_SMS_STATUS = "deliverysmsstatus"
        const val REPLY_MESSAGE = "replymessage"
    }
    @Autowired
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>


    @KafkaListener(topics = [SMS_SUBMIT_DELIVERY_STATUS_QUEUE], groupId = "updatesmssendhistorystatus")
    fun popStatus(record: ConsumerRecord<String?, String?>){
        if(record.key()==SUBMIT_SMS_STATUS){
            record.value()?.let {
                this.updateSubmitStatus(it)
            }
        }
        else if(record.key()==DELIVERY_SMS_STATUS){
            record.value()?.let {
                this.updateDeliveryStatus(it)
            }
        }
    }
    abstract fun updateSubmitStatus(data:String)
    abstract fun updateDeliveryStatus(data:String)
    abstract fun addReplyMessage(data:String)
    @KafkaListener(topics = [SMS_REPLY_QUEUE], groupId = "addreplymessage")
    fun popReply(record: ConsumerRecord<String?, String?>){
        record.value()?.let {
            this.addReplyMessage(it)
        }
    }

    fun pushSubmitStatus(data:String){
        this.kafkaTemplate.send(SMS_SUBMIT_DELIVERY_STATUS_QUEUE, SUBMIT_SMS_STATUS,data)
    }

    fun pushDeliveryStatus(data:String){
        this.kafkaTemplate.send(SMS_SUBMIT_DELIVERY_STATUS_QUEUE, DELIVERY_SMS_STATUS,data)
    }

    fun pushReplyMsg(data:String){
        this.kafkaTemplate.send(SMS_REPLY_QUEUE, REPLY_MESSAGE,data)
    }
}