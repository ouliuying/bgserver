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
import dynamic.model.query.mq.AttachedField
import dynamic.model.query.mq.ModelData
import dynamic.model.query.mq.ModelDataArray
import dynamic.model.query.mq.ModelDataObject
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
        if(obj!=null){
            BasePartner.ref?.safeEdit(obj)
        }

    }
    @Test
    fun returnAdminJson(){
        var obj= dynamic.model.query.mq.ModelDataArray()
        var ret=this.gson?.toJson(obj)
    }
    @Test
    fun queryAdmin(){
        var rset= BasePartner.ref?.rawRead(*BasePartner.ref?.fields?.getAllPersistFields()?.values?.toTypedArray()!!,
                model=BasePartner.ref,criteria = null,attachedFields = arrayOf(dynamic.model.query.mq.AttachedField(BasePartner.ref?.corps!!)))
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
        var obj=this.gson?.fromJson(json, dynamic.model.query.mq.ModelData::class.java)
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