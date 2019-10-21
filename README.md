# bg.work办公系统| [bg.work framework](README.EN.md)
bg.work是综合性办公框架，
我们可以使用它完成各种类型办公系统的设计与开发，
当前框架包含：多公司、角色及权限支持，部门管理，
产品，CRM，企业内部通讯IM，企业微信，企业短信，呼叫中心等。


![logo](static/logo/logo.svg)

-------------------------------------------------------

[项目主站 https://www.bg.work ](https://www.bg.work)

-----------------------------------------------------------------------------------------

## 框架说明

----------------------------------------------------------------------

### 数据访问

Model类图
![Model hierarchy](static/image/ModelInherit.png)

```kotlin
 @Model(name = "customer", title="客户")
 class Customer:ContextModel("crm_customer", "public"){
     companion object: RefSingleton<Customer> {
        override lateinit var ref: Customer
      }
    val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
    val name =  ModelField(null, "name", FieldType.STRING, "姓名")
    val comment =  ModelField(null, "comment", FieldType.STRING, "注释")
} 

//读取客户
  val customers = Customer.ref.rawRead()
//读取一个客户
  val customer =  Customer.ref.rawRead(criteria=eq(Customer.ref.id,1)).firstOrNull()
//读取Model内容
  val id = customer.getFieldValue(Customer.ref.id) as BigInteger?
  val name = customer.getFieldValue(Customer.ref.name) as String?
  val comment = customer.getFieldValue(Customer.ref.comment) as String?
```

这个Customer设计的不错，简单明了，没有任何妥妥拉拉的苗头，嗯，看着很棒！

一年过去了，那个很棒的攻城狮离职了，Bob坐在桌前开始增加一个 mobile

嗯，根据Bob对这个框架的理解，Bob知道可以用两种方法完成这个需求：


```kotlin

//直接增加
 @Model(name = "customer", title="客户")
 class Customer:ContextModel("crm_customer", "public"){
     companion object: RefSingleton<Customer> {
        override lateinit var ref: Customer
      }
    val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
    val name =  ModelField(null, "name", FieldType.STRING, "姓名")
    val comment =  ModelField(null, "comment", FieldType.STRING, "注释")
    val mobile = ModelField(null, "mobile",FieldType.STRING,"手机号")
} 

//好了，来测试下
   val customer =  Customer.ref.rawRead(Customer.ref.mobile, criteria=eq(Customer.ref.id,1)).firstOrNull()
   val mobile = customer.getFieldValue(Customer.ref.mobile) as String?
```

```kotlin
    //第二种，来建个单独文件 MobileCustomer.kt
   @Model(name = "customer", title="客户")
   class MobileCustomer : Customer{
       companion object: RefSingleton<MobileCustomer> {
         override lateinit var ref: MobileCustomer
        }
       val mobile = ModelField(null, "mobile",FieldType.STRING,"手机号")
   }
   //好了，来测试下，没错，使用了 Customer, 框架会自动检测到 Customer 添加了 mobile
   val customer =  Customer.ref.rawRead(MobileCustomer.ref.mobile, criteria=eq(Customer.ref.id,1)).firstOrNull()
   val mobile = customer.getFieldValue(MobileCustomer.ref.mobile) as String?
```

好了，在了解了Bob出色的完成了 Customer 后，我们来简单了解下 Model的设计规则：
>什么是Model
>> Model对应业务中实体模型，从数据库角度就是对应一个 table
>
>> Model 中的 ModelField属性 对应数据库table中column

Model field 类型：

* ModelField
* Many2OneField
* Many2ManyField
* One2OneField
* One2ManyField

通过扩展Customer,我们可以很快理解这些Field的意义：

```kotlin

@Model("customer","客户")
class Customer:ContextModel("crm_customer", "public"){
        companion object: RefSingleton<Customer> {
            override lateinit var ref: Customer
        }
        val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
        val name =  ModelField(null, "name", FieldType.STRING, "姓名")
        val comment =  ModelField(null, "comment", FieldType.STRING, "注释")
        val mobile = ModelField(null, "mobile",FieldType.STRING,"手机号")
        val addresses = ModelOne2ManyField(null,"addresses",FeildType.BIGINT,"地址","public.crm_customer_address","customer")
        val threePartyOAuth2Data =  ModelOne2OneField(null, "customer_id",FeildType.BIGINT,"第三方授权","public.crm_three_party_oauth2_data","customer_id", isVirtualField = True)
        val consumedProducts = ModelMany2ManyField(null,"consumed_products",FieldType.BIGINT,"已消费产品","public.crm_customer_product_rel","product_id","public.crm_product","id")
}

//每个 Customer 有多个 address
@Model("customerAddress","客户地址")
class CustomerAddress:ContextModel("crm_customer_address", "public"){
        companion object: RefSingleton<CustomerAddress> {
                override lateinit var ref: CustomerAddress
        }
        val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
        val address = ModelField(null, "address",FieldType.STRING,"地址")
        val customer = ModelMany2OneField(null,"customer_id",FeildType.BIGINT,"客户","public.crm_customer","id")
}

//每个客户对应一个第三方授权数据库，每个第三方数据也对应一个客户
@Model("threePartyOAuth2Data","授权数据")
class ThreePartyOAuth2Data:ContextModel("crm_three_party_oauth2_data","public"){
      companion object: RefSingleton<ThreePartyOAuth2Data> {
          override lateinit var ref: ThreePartyOAuth2Data
      }
      val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
      val openid =  ModelField(null, "open_id",FieldType.STRING,"OpenID")
      val icon =  ModelField(null, "icon", FieldType.STRING, "icon")
      val customer = ModelOne2OneField(null, "customer_id",FeildType.BIGINT,"客户","public.crm_customer","id")
}

@Model("product","公司产品")
class Product:Context("crm_product","public"){
    companion object: RefSingleton<Product> {
              override lateinit var ref: Product
    }
    val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
    val name =  ModelField(null, "name", FieldType.STRING, "名称")
    val price =  ModelField(null, "price", FieldType.NUMBER, "价格")
    val typ =  ModelField(null, "typ", FieldType.STRING, "类型")
    val consumingCustomers = ModelMany2ManyField(null,"consuming_customers",FieldType.BIGINT,"消费客户","public.crm_customer_product_rel","customer_id","public.crm_customer","id")
}

@Model("customerProductRel","客户消费产品关系")
class CustomerProductRel:ContextModel("crm_customer_product_rel","public"){
    companion object: RefSingleton<CustomerProductRel> {
                  override lateinit var ref: CustomerProductRel
    }
    val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
    val customer = ModelMany2OneField(null,"customer_id",FieldType.BIGINT,"客户")
    val product = ModelMany2OneField(null,"product_id",FieldType.BIGINT,"产品")
}

//读取Customer  one2one,many2one field 默认是 读取
val customer = Customer.ref.rawRead(criteria=eq(Customer.ref.id,1)).firstOrNull()
val mobile = customer.getFieldValue(Customer.ref.mobile) as String?
val threePartyOAuth2Data  = customer.getFieldValue(Customer.ref.threePartyOAuth2Data) as ModelDataObject?
val openid = threePartyOAuth2Data?.getFieldValue(ThreePartyOAuth2Data.ref.openid) as String?

// 指定要读取的 one2many field  addresses
val customer =  Customer.ref.rawRead(criteria=eq(Customer.ref.id,1),attachedFields=arrayOf(Customer.ref.addresses)).firstOrNull()
val addresses = customer.getFieldValue(Customer.ref.addresses) as ModelDataArray?
addresses?.toModelDataObjectArray()?.forEach{ address->
    val addressValue = address.getFieldValue(CustomerAddress.ref.address) as String?
}

// 指定要读取的 many2many field  consumedProducts
val customer =  Customer.ref.rawRead(criteria=eq(Customer.ref.id,1),attachedFields=arrayOf(Customer.ref.consumedProducts)).firstOrNull()
val customerProductRels = (customer.getFieldValue(Customer.ref.ConstRelRegistriesField) as ModelDataSharedObject?).data?.get(CustomerProductRel.ref) as ModelDataArray?
customerProductRels?.toModelDataObjectArray()?.forEach{ cpRel->
    val productModelDataObject = cpRel.getFieldValue(CustomerProductRel.ref.product) as ModelDataObject?
    val name = productModelDataObject?.getFieldValue(Product.ref.name) as String?
    val price = productModelDataObject?.getFieldValue(Product.ref.price) as Number?
}

// read customer + addresses + consumedProducts
val customer =  Customer.ref.rawRead(criteria=eq(Customer.ref.id,1),attachedFields=arrayOf(Customer.ref.addresses, Customer.ref.consumedProducts)).firstOrNull()
```

### 权限管理
* 针对 model 设置 crud 权限
* 针对 model field 设置 crud 权限
* [更多](https://www.bg.work)

### model action
>外部与 bg.work办公平台 交互主要调用 model action
```kotlin
class Customer:ContextModel("crm_customer","public"){
    companion object: RefSingleton<Customer> {
            override lateinit var ref: Customer
    }
    val id = ModelField(null, "id", FieldType.BIGINT, "标识", primaryKey = FieldPrimaryKey())
    val name =  ModelField(null, "name", FieldType.STRING, "姓名")
    val comment =  ModelField(null, "comment", FieldType.STRING, "注释")
    val mobile = ModelField(null, "mobile",FieldType.STRING,"手机号")
    @Action("getCustomer")
    fun getCustomer(@RequestBody data:JsonObject?):ActionResult?{
        val name = data?.get("name")
        var customer = this.rawRead(eq(this.name,name)).firstOrNull()
        var ar = ActionResult()
        ar.bag["customer"] = customer
        return ar
    }  
}
```
调用样例
```javascript
 let name = "c_name"
 $.ajax({
    url:"http://bgworkserver/ac/app/customer/getCustomer",
    type:"POST",
    contentType:"application/json",
    data:{name},
    success:function(data){
        
    }
 })
```

### 界面
    
### 业务扩展


