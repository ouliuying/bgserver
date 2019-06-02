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

package work.bg.server.core.ui

import com.google.gson.Gson
import com.google.gson.JsonObject
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.BeanFactoryAware
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.context.ResourceLoaderAware
import org.springframework.core.io.ResourceLoader
import org.springframework.stereotype.Component
import work.bg.server.core.RefSingleton
import work.bg.server.core.spring.boot.model.AppModel
import org.dom4j.io.SAXReader
import org.dom4j.Element
import work.bg.server.core.constant.ModelReservedKey
import javax.xml.stream.events.EndElement

//@ConditionalOnBean(value = [AppModel::class])
@Component
class UICache:InitializingBean,ApplicationContextAware ,BeanFactoryAware,ResourceLoaderAware{
    private  object ExpressionOpType{
        const val InsertBefore = "insertBefore"
        const val InsertAfter = "insertAfter"
        const val Append = "append"
        const val PreAppend = "preAppend"
        const val Remove = "remove"
        const val Replace = "replace"
    }
    @Autowired
    private lateinit var gson: Gson
    private  val logger = LogFactory.getLog(javaClass)
    private  lateinit var  context:ApplicationContext
    private  lateinit var  registry: BeanFactory
    private lateinit var resLoader:ResourceLoader
    @Autowired
    private  lateinit var appModel:AppModel
    private  var menuTrees = mutableMapOf<String,AppMenuTree>()
    private  var modelViews = mutableMapOf<String,AppModelView>()
    private  var viewActions = mutableMapOf<String,AppModelViewAction>()
    companion object :RefSingleton<UICache> {
        override lateinit var ref: UICache
    }
    override fun afterPropertiesSet() {
        var bean=this.registry.getBean(UICache::class.java) as UICache
        UICache.ref=bean
       // bean.loadUI()
    }

    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.context=applicationContext
    }

    override fun setBeanFactory(beanFactory: BeanFactory) {
        this.registry = beanFactory
    }

    override fun setResourceLoader(resourceLoader: ResourceLoader) {
        this.resLoader=resourceLoader
    }
    private  fun loadUI(){
        var menuUISortFiles=UISortFiles()
        var modelUISortFiles=UISortFiles()
        var viewActionSortFiles=UISortFiles()
        appModel.appPackageManifests.forEach { _, u ->
            try {
                var menuRes=this.resLoader.getResource("classpath:ui/menu/${u.name}.xml")?.inputStream
                if(menuRes!=null){
                    try {
                        var doc=SAXReader().read(menuRes)
                        menuUISortFiles.add(UIFile(doc,u.name))
                    }
                    catch (ex:Exception){
                        logger.error(ex.toString())
                    }
                }
                var modelRes=this.resLoader.getResource("classpath:ui/model/${u.name}.xml")?.inputStream
                if(modelRes!=null){
                    try {
                        var doc=SAXReader().read(modelRes)
                        modelUISortFiles.add(UIFile(doc,u.name))
                    }
                    catch (ex:Exception){
                        logger.error(ex.toString())
                    }
                }

                var actionRes=this.resLoader.getResource("classpath:ui/action/${u.name}.xml")?.inputStream
                if(actionRes!=null){
                    try {
                        var doc=SAXReader().read(actionRes)
                        viewActionSortFiles.add(UIFile(doc,u.name))
                    }
                    catch (ex:Exception){
                        logger.error(ex.toString())
                    }
                }

            }
            catch (ex:Exception){
                logger.error(ex.toString())
            }
        }
        var menuFiles=menuUISortFiles.sort()
        var modelFiles=modelUISortFiles.sort()
        var actionFiles=viewActionSortFiles.sort()

        this.buildMenu(menuFiles)
        this.buildModel(modelFiles)
        this.buildAction(actionFiles)
    }
    fun getMenu(app:String,menu:String):MenuTree?
    {
        return this.menuTrees[app]?.menus?.get(menu)?.createCopy()
    }
    fun getAppModelView(app:String):AppModelView?{
        return this.modelViews[app]
    }
    fun getModelView(app: String,model:String,viewType:String):ModelView?{
        return this.modelViews[app]?.modelViews?.get(model)?.get(viewType)?.createCopy()
    }
    fun getViewAction(app:String,model:String,viewType:String,groupName:String):ViewAction?{
        val appVA = this.viewActions[app]
        if(appVA!=null){
            val modelVA = appVA.modelActions[model]
            if(modelVA!=null){
                val viewTypeVA = modelVA[viewType]
                if(viewTypeVA!=null){
                    val va = viewTypeVA.firstOrNull {
                        it.groups.keys.contains(groupName)
                    }
                    if(va!=null){
                        return va.createCopy()
                    }
                }
            }
        }
        if(app!="*" && model!="*"){
            var va = this.getViewAction("*",model,viewType,groupName)
            if(va!=null){
                return va.createCopy()
            }
        }
        if(app=="*" && model!="*"){
            var va = this.getViewAction("*","*",viewType,groupName)
            if(va!=null){
                return va.createCopy()
            }
        }
        return null
    }


    private fun buildAction(files:List<UIFile>){
        files.forEach {
            var viewActionConfigMap= mutableMapOf<String,UIFile>()
            files.forEach {
                var inheritXPaths=it.doc.selectNodes("ui/inherit/xpath") as List<Element?>?
                if(inheritXPaths!=null && !inheritXPaths.isEmpty()){
                    inheritXPaths.forEach { xPath->
                        var xPathExp=xPath?.attribute("expression")?.value
                        var op=xPath?.attribute("op")?.value
                        var app=xPath?.attribute("app")?.value
                        var targetDoc=viewActionConfigMap[app]
                        if(targetDoc!=null && xPathExp!=null){
                            var sNodes=targetDoc.doc.selectNodes(xPathExp) as List<Element?>?
                            if(op!=null && sNodes!=null && !sNodes.isEmpty()){
                                when(op){
                                    ExpressionOpType.InsertBefore->{
                                        this.insertBefore(sNodes,xPath?.elements() as List<Element?>?)
                                    }
                                    ExpressionOpType.InsertAfter->{
                                        this.insertAfter(sNodes,xPath?.elements() as List<Element?>?)
                                    }
                                    ExpressionOpType.Append->{
                                        this.append(sNodes,xPath?.elements() as List<Element?>?)
                                    }
                                    ExpressionOpType.PreAppend->{
                                        this.preAppend(sNodes,xPath?.elements() as List<Element?>?)
                                    }
                                }
                            }
                        }
                    }
                }
                viewActionConfigMap[it.appName]=it
            }
        }

        files?.forEach {
            var actions=it.doc.selectNodes("/ui/action") as List<Element?>?
            actions?.forEach {ait->
                if(ait!=null){
                    this.buildViewAction(ait)
                }
            }
        }
    }

    private fun addViewAction(va:ViewAction){

        if(this.viewActions.containsKey(va.app)){
            if(this.viewActions[va.app]!!.modelActions?.containsKey(va.model)){
                if(this.viewActions[va.app]!!.modelActions?.get(va.model)?.containsKey(va.viewType)!!){
                    var vaList = this.viewActions[va.app]!!.modelActions?.get(va.model)?.get(va.viewType) as MutableList<ViewAction>?
                    vaList?.add(va)
                }
                else{
                    var vaList = mutableListOf<ViewAction>()
                    vaList.add(va)
                    var mmap = this.viewActions[va.app]!!.modelActions?.get(va.model) as MutableMap?
                    mmap?.put(va.viewType,vaList)
                }
            }
            else{
                //var mmap = mutableMapOf<String,Map<String,List<ViewAction>>>()
                var vmap = mutableMapOf<String,List<ViewAction>>()
                var vaList = mutableListOf<ViewAction>()
                vaList.add(va)
                vmap[va.viewType]=vaList
                //mmap[va.model]=vmap
                var amap= this.viewActions[va.app]?.modelActions as MutableMap
                amap[va.model] = vmap
            }
        }
        else{
            var mmap = mutableMapOf<String,Map<String,List<ViewAction>>>()
            var vmap =  mutableMapOf<String,List<ViewAction>>()
            var vaList = mutableListOf<ViewAction>()
            vaList.add(va)
            vmap[va.viewType]=vaList
            mmap[va.model]=vmap
            var amva = AppModelViewAction(va.app, mmap)
            this.viewActions[va.app]=amva
        }
    }

    private  fun buildViewAction(element:Element?){
        if(element!=null){
            val app=element.attributeValue("app")
            val model=element.attributeValue("model")
            val viewType=element.attributeValue("viewType")
            //var key=ViewActionKey(app,model,viewType)
            var va=ViewAction(app,model,viewType)
           // this.viewActions[key]=va
            this.addViewAction(va)
            var groups=element.selectNodes("group") as List<Element?>?
            groups?.forEach {
                val name=it?.attributeValue("name")
                if(name!=null){
                    var ag=TriggerGroup(name)
                    va.groups[ag.name]=ag
                    var triggers=it?.selectNodes("trigger") as List<Element?>?
                    triggers?.forEach {
                        tit->
                        var tApp=app
                        var tModel=model
                        var tViewType=viewType
                        var name=""
                        var title=""
                        if(tit!=null){
                            var at=tit.attribute("app")
                            if(at!=null){
                                tApp=at.value
                            }
                            at=tit.attribute("model")
                            if(at!=null){
                                tModel=at.value
                            }
                            at=tit.attribute("viewType")
                            if(at!=null){
                                tViewType=at.value
                            }
                            at=tit.attribute("name")
                            if(at!=null){
                                name=at.value
                            }
                            at=tit.attribute("title")
                            if(at!=null){
                                title=at.value
                            }
                            if(!(name.isNullOrEmpty()||title.isNullOrEmpty())){
                                var t=Trigger(name,title,tApp,tModel,tViewType)
                                ag.triggers.add(t)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun buildMenu(files:List<UIFile>){
        var appMenuConfigMap= mutableMapOf<String,UIFile>()
        files.forEach {
            var inheritXPaths=it.doc.selectNodes("ui/inherit/xpath") as List<Element?>?
            if(inheritXPaths!=null && !inheritXPaths.isEmpty()){
                inheritXPaths.forEach { xPath->
                    var xPathExp=xPath?.attribute("expression")?.value
                    var op=xPath?.attribute("op")?.value
                    var app=xPath?.attribute("app")?.value
                    var targetDoc=appMenuConfigMap[app]
                    if(targetDoc!=null && xPathExp!=null){
                        var sNodes=targetDoc.doc.selectNodes(xPathExp) as List<Element?>?
                        if(op!=null && sNodes!=null && !sNodes.isEmpty()){
                            when(op){
                                ExpressionOpType.InsertBefore->{
                                    this.insertBefore(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.InsertAfter->{
                                    this.insertAfter(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.Append->{
                                    this.append(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.PreAppend->{
                                    this.preAppend(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.Remove->{
                                    this.remove(sNodes)
                                }
                                ExpressionOpType.Replace->{
                                    this.replace(sNodes,xPath?.elements() as List<Element?>?)
                                }
                            }
                        }
                    }
                }
            }
            appMenuConfigMap[it.appName]=it
        }
        files.forEach {
            var menus=it.doc.selectNodes("/ui/menu") as List<Element?>?
            menus?.forEach { m->
                var mTree=buildMenuTree(m)
                if(mTree!=null){
                    this.addMenuTree(mTree)
                }
            }
        }
    }

    private  fun addMenuTree(menuTree:MenuTree){
        var amt = AppMenuTree(menuTree.app, mutableMapOf())
        var mmap = amt.menus as MutableMap
        mmap[menuTree.name]=menuTree
        this.menuTrees[amt.appName]=amt
    }

    private  fun buildMenuTree(menu:Element?,parentApp:String?=null):MenuTree?{
        if(menu!=null){
            try {
                var appName=menu?.attributeValue("app")
                if(appName==null || appName.isNullOrEmpty()){
                    appName=parentApp
                }
                var name=menu?.attributeValue("name")
                var title=menu?.attributeValue("title")
                var mT= MenuTree(appName,name,title)
                var elements=menu.elements() as List<Element?>?
                mT.children= arrayListOf()
                elements?.forEach {
                    if(it?.name=="menuItem"){
                        var sMN=this.buildMenuNode(it,appName)
                        if( sMN!=null){
                            mT.children?.add(sMN)
                        }
                    }
                    else if(it?.name=="menu"){
                        var sMT=this.buildMenuTree(it,appName)
                        if( sMT!=null){
                            mT.children?.add(sMT)
                        }
                    }
                }
                return mT

            }
            catch (exp:Exception){

            }
        }
        return null
    }
    private fun buildMenuNode(menuItem:Element?,parentApp:String?=null):MenuNode?{
        if(menuItem!=null){
            try {
                var app=menuItem?.attributeValue("app")
                if(app==null || app.isNullOrEmpty()){
                    app=parentApp
                }
                var title=menuItem?.attributeValue("title")
                var model=menuItem?.attributeValue("model")
                var viewType=menuItem?.attributeValue("viewType")
                return MenuNode(app,title,model,viewType)
            }
            catch (ex:Exception){

            }
        }
        return null
    }
    private  fun insertBefore(anchorNodes:List<Element?>,
                              opNodes:List<Element?>?){
        if(opNodes!=null && !opNodes.isEmpty()){
            anchorNodes.forEach {
                var index=it?.parent?.elements()?.indexOf(it)
                if(index!=null && index>-1){
                    opNodes?.asReversed()?.forEach {opNode->
                        var tcpyNode=opNode?.createCopy()
                        if(tcpyNode!=null){
                            it?.parent?.elements()?.add(index,tcpyNode)
                        }

                    }
                }
            }
        }
    }
    private  fun insertAfter(anchorNodes:List<Element?>,
    opNodes:List<Element?>?){
        if(opNodes!=null && !opNodes.isEmpty()){
            anchorNodes.forEach {
                var index=it?.parent?.elements()?.indexOf(it)
                if(index!=null && index>-1){
                    opNodes?.asReversed()?.forEach {opNode->
                        var cpyNode=opNode?.createCopy()
                        if(cpyNode!=null){
                            it?.parent?.elements()?.add(index+1,cpyNode)
                        }
                    }
                }
            }
        }
    }

    private  fun append(anchorNodes:List<Element?>,
                             opNodes:List<Element?>?){
        if(opNodes!=null && !opNodes.isEmpty()){
            anchorNodes.forEach {anchorNode->
                opNodes?.forEach {opNode->
                    var cpyNode=opNode?.createCopy()
                    if(cpyNode!=null){
                        anchorNode?.elements()?.add(cpyNode)
                    }
                }
            }
        }
    }

    private  fun preAppend(anchorNodes:List<Element?>,
                        opNodes:List<Element?>?){
        if(opNodes!=null && !opNodes.isEmpty()){
            anchorNodes.forEach {anchorNode->
                opNodes?.asReversed()?.forEach {opNode->
                    var cpyNode=opNode?.createCopy()
                    if(cpyNode!=null){
                        anchorNode?.elements()?.add(0,cpyNode)
                    }
                }
            }
        }
    }

    private  fun remove(anchorNodes:List<Element?>){
       anchorNodes.forEach {
           var p=it?.parent
           p?.remove(it)
       }
    }

    private fun replace(anchorNodes: List<Element?>,opNodes:List<Element?>?){
        this.insertAfter(anchorNodes,opNodes)
        this.remove(anchorNodes)
    }
    private fun buildModel(files:List<UIFile>){
        var appModelConfigMap= mutableMapOf<String,UIFile>()
        files.forEach {
            var inheritXPaths=it.doc.selectNodes("ui/inherit/xpath") as List<Element?>?
            if(inheritXPaths!=null && !inheritXPaths.isEmpty()){
                inheritXPaths.forEach { xPath->
                    var xPathExp=xPath?.attribute("expression")?.value
                    var op=xPath?.attribute("op")?.value
                    var app=xPath?.attribute("app")?.value
                    var targetDoc=appModelConfigMap[app]
                    if(targetDoc!=null && xPathExp!=null){
                        var sNodes=targetDoc.doc.selectNodes(xPathExp) as List<Element?>?
                        if(op!=null && sNodes!=null && !sNodes.isEmpty()){
                            when(op){
                                ExpressionOpType.InsertBefore->{
                                    this.insertBefore(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.InsertAfter->{
                                    this.insertAfter(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.Append->{
                                    this.append(sNodes,xPath?.elements() as List<Element?>?)
                                }
                                ExpressionOpType.PreAppend->{
                                    this.preAppend(sNodes,xPath?.elements() as List<Element?>?)
                                }
                            }
                        }
                    }
                }
            }
            appModelConfigMap[it.appName]=it
        }

        files?.forEach {
            var models=it.doc.selectNodes("/ui/model") as List<Element?>?
            models?.forEach {mit->
                if(mit!=null){
                    this.buildModelViews(mit)
                }
            }
        }
    }

    private fun buildModelViews(element:Element?){
        var views=element?.elements() as List<Element?>?
        var model=element?.attributeValue("name")
        var appName=element?.attributeValue("app")
        views?.forEach {vit->
            var mv=buildSingleModelView(appName,model,vit)
            if(mv!=null){
                this.addModelView(mv)
            }
        }
    }
    private  fun addModelView(mv:ModelView){
        if(this.modelViews.containsKey(mv.app)){
            val amv = this.modelViews[mv.app]!!
            if(amv.modelViews.containsKey(mv.model)){
                var mm = amv.modelViews[mv.model] as MutableMap
                if(mm!=null){
                    mm[mv.viewType!!]=mv
                }
                else{
                    mm = mutableMapOf()
                    mm[mv.viewType!!]=mv
                    (amv.modelViews as MutableMap)[mv.model!!]=mm
                }
            }
            else{
                var mm = mutableMapOf<String,ModelView>()
                mm[mv.viewType!!]=mv
                (amv.modelViews as MutableMap)[mv.model!!]=mm
            }
        }
        else{
            var amv = AppModelView(mv.app!!, mutableMapOf<String,Map<String,ModelView>>())
            var mm = mutableMapOf<String,ModelView>()
            mm[mv.viewType!!]=mv
            (amv.modelViews as MutableMap)[mv.model!!]=mm
            this.modelViews[amv.appName]=amv
        }
    }
    private fun buildSingleModelView(app:String?,model:String?,viewNode:Element?):ModelView?{
        try {
            var viewType=viewNode?.attributeValue("type")
            var mv=ModelView(app,model,viewType)
            (viewNode?.elements("field") as List<Element?>)?.forEach {it ->
                if(it!=null){
                    var name=it.attributeValue("name")
                    var style=it.attributeValue("style")
                    if(style.isNullOrEmpty()){
                        style="normal"
                    }
                    var type=it.attributeValue("type")
                    if(type.isNullOrEmpty()){
                        type="singleLineText"
                    }
                    var title=it.attributeValue("title")
                    if(title.isNullOrEmpty()){
                        title=""
                    }
                    var icon=it.attributeValue("icon")
                    if(icon.isNullOrEmpty()){
                        icon=""
                    }
                    var rowSpan=1
                    try {
                        rowSpan = it.attributeValue("rowSpan").toInt(10)
                    }
                    catch (ex:Exception){

                    }
                    var colSpan=1
                    try {
                        colSpan=it.attributeValue("colSpan").toInt(10)
                    }
                    catch (ex:Exception){

                    }
                    var subNodes=it.elements("view")
                    var subNode=if(subNodes.size>0) subNodes[0] as Element? else null
                    val f=mv.addField(name,style,rowSpan,colSpan,type,title,icon)
                    if(subNode!=null){
                        var subModel=viewNode?.attributeValue("name")
                        var subApp=viewNode?.attributeValue("app")?:app
                        f.fieldView=buildSingleModelView(subApp,subModel,subNode)
                    }

                    var subMetaNodes= it.elements("meta")
                    var subMetaNode=if(subMetaNodes.size>0) subMetaNodes[0] as Element? else null
                    if(subMetaNode!=null){
                        f.meta=  gson.fromJson(subMetaNode.textTrim,JsonObject::class.java)
                       // print(f.meta)
                    }

                    var subCtrlProps = it.elements("ctrlProps")
                    if(subCtrlProps.size>0){
                        try {
                            f.ctrlProps = gson.fromJson((subCtrlProps[0] as Element).textTrim,JsonObject::class.java)
                        }
                        catch (ex:java.lang.Exception){

                        }
                    }
                }
            }

            (viewNode?.selectNodes("ref/actions/action") as List<Element>?)?.forEach {
                val aApp = it.attributeValue("app")?:app
                val aModel = it.attributeValue("model")?:model
                val aViewType = it.attributeValue("viewType")?:viewType
                val groupElems = it.selectNodes("group") as List<Element>?
                groupElems?.forEach {
                    val groupName =it.attributeValue("name")
                    val refType =  it.attributeValue("refType")
                    var refTypes = if(refType.isNullOrEmpty())  arrayListOf(ModelViewRefType.Main) else arrayListOf(*refType.split('|').toTypedArray())
                    if(!groupName.isNullOrEmpty()){
                        mv.refActionGroups.add(ModelView.RefActionGroup(aApp!!,
                                aModel!!,aViewType!!,groupName, refTypes))
                    }
                }
            }

            (viewNode?.selectNodes("ref/actions/menu") as List<Element>?)?.forEach {
                val aApp = it.attributeValue("app")?:app
                val name = it.attributeValue("name")
                val refType = it.attributeValue("refType")
                var refTypes = if(refType.isNullOrEmpty()) arrayListOf(ModelViewRefType.Main) else arrayListOf(*refType.split('|').toTypedArray())
                mv.refMenus.add(
                        ModelView.RefMenu(aApp!!,name,refTypes)
                )
            }

            (viewNode?.selectNodes("ref/views/view") as List<Element>?)?.forEach {
                var vApp = it.attributeValue("app")
                var vModel = it.attributeValue("model")
                val viewType = it.attributeValue("type")
                val refType = it.attributeValue("refType")
                var refTypes = if(refType.isNullOrEmpty()) arrayListOf(ModelViewRefType.Main) else arrayListOf(*refType.split('|').toTypedArray())
                var title = it.attributeValue("title")
                var style = it.attributeValue("style")
                val ownerField = it.attributeValue("ownerField")
                if(!ownerField.isNullOrEmpty()){
                    var ownerFieldObj = mv.fields.firstOrNull { fd->
                        fd.name == ownerField
                    }
                    if(ownerFieldObj!=null){
                        vApp=vApp?:ownerFieldObj.app
                        vModel=vModel?:ownerFieldObj.model
                        title=title?:ownerFieldObj.title
                        style = style?:ownerFieldObj.style
                    }
                }
                if(vApp.isNullOrEmpty()){
                    vApp=app
                }
                if(vModel.isNullOrEmpty()){
                    vModel=model
                }
                mv.refViews.add(
                        ModelView.RefView(vApp,vModel,viewType,ownerField?:"",title?:"",style?:"",refTypes)
                )
            }
            return mv
        }
        catch (ex:Exception)
        {
            ex.printStackTrace()
        }
        return null
    }
//    fun getPartnerUICache(corpId:Long,partnerID:Long):Triple<Map<String,MenuTree>,Map<String,ModelView>,Map<String,ViewAction>>{
//        var menuTreeMap= mutableMapOf<String,MenuTree>()
//        var modelViewMap= mutableMapOf<String,ModelView>()
//        var actionMap= mutableMapOf<String,ViewAction>()
//
//        //todo add corp partner role model access control constraint
//        this.menuTrees.forEach{
//            menuTreeMap["${it.key.appName}_${it.key.menuName}"]=it.value.createCopy()
//        }
//        this.modelViews.forEach{
//            modelViewMap["${it.key.appName}_${it.key.modelName}_${it.key.viewType}"]=it.value.createCopy()
//        }
//        this.viewActions.forEach {
//            actionMap["${it.key.appName}_${it.key.modelName}_${it.key.viewType}"]=it.value.createCopy()
//
//        }
//        return Triple(menuTreeMap,modelViewMap,actionMap)
//    }
    class AppMenuTree(val appName:String,val menus:Map<String,MenuTree>)

    class AppModelView(val appName:String, val modelViews:Map<String,Map<String,ModelView>>){
        val modelViewKeys by lazy {
            var keys = mutableMapOf<String,List<String>>()
            modelViews.forEach { t, u ->
                var vLst = keys[t] as MutableList?
                if(vLst!=null){
                    u.forEach {
                        vLst?.add(it.key)
                    }
                }
                else{
                    var vLst = mutableListOf<String>()
                    u.forEach {
                        vLst?.add(it.key)
                    }
                    keys[t]=vLst
                }
            }
            keys
        }
    }

    class AppModelViewAction(val appName: String,val modelActions:Map<String,Map<String,List<ViewAction>>>)
}