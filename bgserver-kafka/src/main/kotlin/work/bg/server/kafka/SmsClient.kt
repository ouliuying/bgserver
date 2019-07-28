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

package work.bg.server.kafka

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service

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

@Service
class SmsClient {
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

    }

    @KafkaListener(topics = [SMS_REPLY_QUEUE], groupId = "addreplymessage")
    fun popReply(record: ConsumerRecord<String?, String?>){

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