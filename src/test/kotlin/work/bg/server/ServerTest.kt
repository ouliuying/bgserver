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

package work.bg.server
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.test.context.junit4.SpringRunner
import org.junit.runner.RunWith
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import work.bg.server.core.model.BasePartner
import work.bg.server.core.model.BasePartnerAppShortcut
import work.bg.server.core.mq.AttachedField
import work.bg.server.core.mq.ModelData
import work.bg.server.core.mq.ModelDataArray
import work.bg.server.core.mq.ModelDataObject
import work.bg.server.core.ui.UICache
import work.bg.server.sms.SmsSender
import work.bg.server.sms.job.SmsJob

@RunWith(SpringRunner::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class HttpRequestTest {

    @LocalServerPort
    private val port: Int = 0

    @Autowired
    private val restTemplate: TestRestTemplate? = null

    @Autowired
    private  val objectMapper:ObjectMapper?=null

    @Autowired
    private  val gson:Gson?=null
    @Autowired
    lateinit var sender: SmsSender
    @Test
    fun greetingShouldReturnDefaultMessage() {
        assertThat(this.restTemplate!!.getForObject<String>("http://localhost:$port/",
                String::class.java)).contains("办公网--每个人的办公系统")
    }
    @Test
    fun loginTest(){
        var header= HttpHeaders()
        header.contentType= MediaType.APPLICATION_FORM_URLENCODED
        var req = HttpEntity("userName=admin&password=admin&devType=0",header)
        //assertThat(
        var ret=this.restTemplate!!.postForObject<Object>("http://localhost:$port/login",req,Object::class.java)
         print(ret)
        //).contains("errorCode")
    }

    @Test
    fun addSuperAdmin() {
        var inputStream=this.javaClass.classLoader.getResourceAsStream("super_admin.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeCreate(obj)
        }
        print(obj)
    }
    @Test
    fun addApp(){
        var inputStream=this.javaClass.classLoader.getResourceAsStream("app.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeCreate(obj)
        }
        print(obj)
    }

    @Test
    fun addDepartment(){
        var inputStream=this.javaClass.classLoader.getResourceAsStream("add_department.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeCreate(obj)
        }
        print(obj)
    }

    @Test
    fun addAppShortcut(){
        var inputStream=this.javaClass.classLoader.getResourceAsStream("app_shortcut.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeCreate(obj)
        }
        print(obj)
    }
    @Test
    fun updateAdmin(){
        var inputStream=this.javaClass.classLoader.getResourceAsStream("update_partner.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeEdit(obj)
        }

    }
    @Test
    fun returnAdminJson(){
        var obj= ModelDataArray()
        var ret=this.gson?.toJson(obj)
    }
    @Test
    fun queryAdmin(){
        var rset= BasePartner.ref?.rawRead(*BasePartner.ref?.fields?.getAllPersistFields()?.values?.toTypedArray()!!,
                model=BasePartner.ref,criteria = null,attachedFields = arrayOf(AttachedField(BasePartner.ref?.corps!!)))
        print(rset?.fromIdValue)
    }
    @Test
    fun queryShortcut(){
        var rset= BasePartnerAppShortcut.ref.getPartnerApps(1)
        print(rset)
    }
    @Test
    fun loadUICache(){
        var modelView = UICache.ref.getModelView("core","partner","list")
        if(modelView?.viewType=="list"){
            var json=gson?.toJson(modelView)
            print(json)
        }
    }
    @Test
    fun addPartnerRelData(){
        var inputStream=this.javaClass.classLoader.getResourceAsStream("test_add_partner_rel_data.json")
        var path=System.getProperty("user.dir")
        print(path)
        var bts=inputStream.readBytes()
        var json=String(bts)
        var obj=this.gson?.fromJson(json,ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeCreate(obj)
        }
        print(obj)
    }

    @Test
    fun testSender(){
        this.sender.send(arrayListOf("18621991588"),"xxx",false,false,null)
    }

    @Test
    fun testAutowireObject(){
        var j= SmsJob()
        j.execute(null)
    }


    
}