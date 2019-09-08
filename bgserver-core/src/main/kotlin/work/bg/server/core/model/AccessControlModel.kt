

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



package work.bg.server.core.model

import com.google.gson.Gson
import org.apache.commons.logging.LogFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.DefaultTransactionDefinition
import work.bg.server.core.cache.PartnerCache
import org.springframework.transaction.TransactionDefinition
import work.bg.server.core.acrule.*
import work.bg.server.core.acrule.bean.*
import work.bg.server.core.acrule.inspector.ModelFieldInspector
import dynamic.model.query.config.ActionType
import dynamic.model.query.constant.ModelReservedKey
import dynamic.model.query.mq.billboard.FieldDefaultValueBillboard
import dynamic.model.query.exception.ModelErrorException
import dynamic.model.query.mq.*
import dynamic.model.query.mq.billboard.FieldValueDependentingRecordBillboard
import dynamic.model.query.mq.billboard.TimestampBillboard
import dynamic.model.query.mq.join.innerJoin
import dynamic.model.query.mq.join.leftJoin
import dynamic.model.query.mq.model.ModelBase
import dynamic.model.query.mq.`in` as selectIn
import dynamic.model.query.mq.specialized.ConstGetRecordRefField
import dynamic.model.query.mq.specialized.ConstRelRegistriesField
import dynamic.model.query.mq.specialized.ConstSetRecordRefField
import work.bg.server.core.model.billboard.*
import java.math.BigInteger
import kotlin.streams.toList


abstract  class AccessControlModel(tableName:String,schemaName:String): ModelBase(tableName,schemaName) {

    private val logger = LogFactory.getLog(javaClass)

    @Value("\${ui.model.page-size}")
    private var pageSize:Int=30
    @Autowired
    protected lateinit var basePartner:BasePartner
    @Autowired
    protected  lateinit var baseCorp:BaseCorp
    @Autowired
    protected lateinit var basePartnerRole:BasePartnerRole

    @Autowired
    protected  lateinit var baseCorpPartnerRel:BaseCorpPartnerRel
    @Autowired
    protected lateinit var txManager:PlatformTransactionManager

    @Autowired
    protected  lateinit var createRecordSetIsolationFields: ModelCreateFieldsSetIsolationFieldsValueBean
    @Autowired
    protected  lateinit var cuFieldsProcessProxyModelField: ModelCUFieldsProcessProxyModelFieldBeanValue
    @Autowired
    protected lateinit var modelCreateFieldsInspectorCheck: ModelCreateFieldsInspectorCheckBean

    @Autowired
    protected lateinit var modelEditFieldsInspectorCheck: ModelEditFieldsInspectorCheckBean

    @Autowired
    protected lateinit var modelEditFieldsBelongToPartnerCheck: ModelEditFieldsBelongToPartnerCheckBean

    @Autowired
    protected lateinit var modelCreateFieldsInStoreInspectorCheck: ModelCreateFieldsInStoreInspectorCheckBean

    @Autowired
    protected lateinit var modelEditFieldsInStoreInspectorCheck: ModelEditFieldsInStoreInspectorCheckBean

    @Autowired
    protected  lateinit var  modelDeleteFieldsBelongToPartnerCheck:ModelDeleteFieldsBelongToPartnerCheckBean
    @Autowired
    protected lateinit var readCorpIsolation:ModelReadCorpIsolationBean
    @Autowired
    protected lateinit var readPartnerIsolation:ModelReadPartnerIsolationBean
    @Autowired
    lateinit var gson: Gson
    /*Corp Isolation Fields Begin*/
    val createTime= dynamic.model.query.mq.ModelField(null, "create_time", dynamic.model.query.mq.FieldType.DATETIME, "添加时间", defaultValue = TimestampBillboard(constant = true))
    val lastModifyTime= dynamic.model.query.mq.ModelField(null, "last_modify_time", dynamic.model.query.mq.FieldType.DATETIME, "最近修改时间", defaultValue = TimestampBillboard())
    val createPartnerID= dynamic.model.query.mq.ModelField(null, "create_partner_id", dynamic.model.query.mq.FieldType.BIGINT, "添加人", defaultValue = CurrPartnerBillboard(true))
    val lastModifyPartnerID= dynamic.model.query.mq.ModelField(null, "last_modify_partner_id", dynamic.model.query.mq.FieldType.BIGINT, "最近修改人", defaultValue = CurrPartnerBillboard())
    val createCorpID= dynamic.model.query.mq.ModelField(null, "create_corp_id", dynamic.model.query.mq.FieldType.BIGINT, "添加公司", defaultValue = CurrCorpBillboard(true))
    val lastModifyCorpID= dynamic.model.query.mq.ModelField(null, "last_modify_corp_id", dynamic.model.query.mq.FieldType.BIGINT, "最近修改公司", defaultValue = CurrCorpBillboard())
    /*Corp Isolation Fields End*/



    init {

    }
    open fun corpIsolationFields():Array<dynamic.model.query.mq.ModelField>?{
        return arrayOf(
                createTime,
                lastModifyTime,
                createPartnerID,
                lastModifyPartnerID,
                createCorpID,
                lastModifyCorpID
        )
    }

    override fun isAssociative():Boolean{
        return false
    }

    open fun maybeCheckACRule():Boolean{

        return true
    }

    fun acRead(vararg fields: dynamic.model.query.mq.FieldBase,
               model:AccessControlModel?=null,
               criteria: dynamic.model.query.mq.ModelExpression?,
               partnerCache:PartnerCache,
               orderBy: dynamic.model.query.mq.OrderBy?=null,
               pageIndex:Int?=null,
               pageSize:Int?=null,
               attachedFields:Array<dynamic.model.query.mq.AttachedField>?=null,
               relationPaging:Boolean=false): dynamic.model.query.mq.ModelDataArray?{

            if (model == null) return this.rawRead(*fields,
                model = this,
                criteria = criteria,
                orderBy = orderBy,
                pageIndex = pageIndex,
                pageSize = pageSize,
                attachedFields = attachedFields,
                relationPaging = relationPaging,
                useAccessControl = true,
                partnerCache = partnerCache)

            var acCriteria=null as dynamic.model.query.mq.ModelExpression?
            acCriteria=if(acCriteria!=null) {
                if(criteria!=null){
                    and(acCriteria,criteria)
                }
                else{
                    acCriteria
                }
            } else criteria

            return this.rawRead(*fields,
                    model = model,
                    criteria = acCriteria,
                    orderBy = orderBy,
                    pageIndex = pageIndex,
                    pageSize = pageSize,
                    attachedFields = attachedFields,
                    relationPaging = relationPaging,
                    useAccessControl = true,
                    partnerCache = partnerCache)

    }
    //todo rebuild criteria to remove redundant
    open fun smartReconcileCriteria(criteria: dynamic.model.query.mq.ModelExpression?): dynamic.model.query.mq.ModelExpression?{
        return criteria
    }
    open fun beforeRead(vararg queryFields: dynamic.model.query.mq.FieldBase,
                        criteria: dynamic.model.query.mq.ModelExpression?,
                        model:ModelBase,
                        useAccessControl:Boolean,
                        partnerCache: PartnerCache?=null,
                        joinModels:Array<dynamic.model.query.mq.join.JoinModel>?=null):Pair<dynamic.model.query.mq.ModelExpression?,Array<dynamic.model.query.mq.FieldBase>>{
        var ruleCriteria=criteria
        var newQueryFields = arrayListOf<dynamic.model.query.mq.FieldBase>()
        if (useAccessControl && partnerCache!=null){
            var models = arrayListOf<ModelBase>(model)
            joinModels?.let {
                it.forEach { sit->
                    sit.model?.let {
                        models.add(it)
                    }
                }
            }

            models.forEach {
                if(partnerCache.checkReadBelongToPartner(model)){
                    ruleCriteria = this.readCorpIsolation(model,partnerCache,ruleCriteria)
                    ruleCriteria = this.readPartnerIsolation(model,partnerCache,ruleCriteria)
                    val isolationRules = partnerCache.getModelReadAccessControlRules<ModelReadIsolationRule<*>>(this)
                    isolationRules?.forEach {
                        ruleCriteria = it(this,partnerCache,ruleCriteria)
                    }
                    ruleCriteria=smartReconcileCriteria(ruleCriteria)
                }
            }

            queryFields.forEach { fit->
                fit.model?.let {
                    var filters = partnerCache.getModelReadAccessControlRules<ModelReadFieldFilterRule>(it) as MutableList?
                    val modelSelfFieldFilterRule = (it as AccessControlModel).getModelReadAccessFieldFilterRule()
                    modelSelfFieldFilterRule?.let {
                        if(filters!=null){
                            (filters as MutableList).add(modelSelfFieldFilterRule)
                        }
                        else
                        {
                            filters = mutableListOf(modelSelfFieldFilterRule)
                        }
                    }
                    if(filters!=null){
                        for (f in (filters as MutableList)){
                            if(f(fit,partnerCache,null).first){
                                return@forEach
                            }
                        }
                    }
                    newQueryFields.add(fit)
                }
            }
        }
        else if(useAccessControl){
            throw ModelErrorException("权限错误")
        }
        else{
            newQueryFields.addAll(queryFields)
        }
        return Pair(ruleCriteria,newQueryFields.toTypedArray())
    }
    protected  open fun getModelReadAccessFieldFilterRule():ModelReadFieldFilterRule?{
        return null
    }
    protected  open fun getModelEditAccessFieldFilterRule():ModelEditRecordFieldsValueFilterRule<*>?{
        return null
    }
    open fun filterAcModelFields(fields:Array<dynamic.model.query.mq.FieldBase>, model:ModelBase, partnerCache: PartnerCache?):Array<dynamic.model.query.mq.FieldBase>{
        if(partnerCache!=null){
            var rFields = arrayListOf<dynamic.model.query.mq.FieldBase>()
            val rule=partnerCache.getModelRule(model.meta.appName,model.meta.name)
            rule?.let {
                fields.forEach {f->
                    val fr = it.fieldRules[f.propertyName]
                    if(fr!=null){
                        if(fr.readAction.enable!="false"){
                            rFields.add(f)
                        }
                    }
                }
                return rFields.toArray() as Array<dynamic.model.query.mq.FieldBase>
            }
        }
        return fields
    }
    private fun sortFields(model:ModelBase, targetFields:ArrayList<dynamic.model.query.mq.FieldBase>,
                           fs:ArrayList<dynamic.model.query.mq.FieldBase>,
                           o2ofs:ArrayList<dynamic.model.query.mq.FieldBase>,
                           o2mfs:ArrayList<dynamic.model.query.mq.AttachedField>,
                           m2ofs:ArrayList<dynamic.model.query.mq.FieldBase>,
                           m2mfs:ArrayList<dynamic.model.query.mq.AttachedField>,
                           ownerMany2OneFields:ArrayList<dynamic.model.query.mq.ModelMany2OneField>){
        targetFields.forEach {
            when(it){
                is dynamic.model.query.mq.One2OneField ->{
                    o2ofs.add(it)
                }
                is dynamic.model.query.mq.One2ManyField ->{
                    o2mfs.add(dynamic.model.query.mq.AttachedField(it))
                }
                is dynamic.model.query.mq.Many2OneField ->{
                    val tf = this.getTargetModelField(it)
                    val ret = tf?.first?.isSame(model)
                    if(ret==null || !ret){
                        m2ofs.add(it)
                    }
                    else{
                        fs.add(it)
                        ownerMany2OneFields.add(it as dynamic.model.query.mq.ModelMany2OneField)
                    }
                }
                is dynamic.model.query.mq.Many2ManyField ->{
                    m2mfs.add(dynamic.model.query.mq.AttachedField(it))
                }
                else->{
                    if(model.isSame(it.model)){
                        fs.add(it)
                    }
                    else{
                        var rFd= (model as AccessControlModel).getRelationFieldTo(it)
                        rFd?.let{
                            sortFields(model, arrayListOf(rFd),fs,o2ofs,o2mfs,m2ofs,m2mfs,ownerMany2OneFields)
                        }
                    }
                }
            }
        }
    }
    open fun rawRead(vararg fields: dynamic.model.query.mq.FieldBase,
                     model:AccessControlModel?=null,
                     criteria: dynamic.model.query.mq.ModelExpression?,
                     orderBy: dynamic.model.query.mq.OrderBy?=null,
                     pageIndex:Int?=null,
                     pageSize:Int?=null,
                     attachedFields:Array<dynamic.model.query.mq.AttachedField>?=null,
                     relationPaging:Boolean=false,
                     useAccessControl: Boolean=false,
                     partnerCache:PartnerCache?=null): dynamic.model.query.mq.ModelDataArray?{
        if(model==null){
            return this.rawRead(*fields,
                    model=this,
                    criteria = criteria,
                    orderBy = orderBy,
                    pageSize = pageSize,
                    pageIndex = pageIndex,
                    attachedFields = attachedFields,
                    relationPaging = relationPaging,
                    useAccessControl = useAccessControl,
                    partnerCache = partnerCache)
        }
        if(useAccessControl && partnerCache==null){
            return null
        }

        var modelRule = partnerCache?.getModelRule(model.meta.appName,model.meta.name)
        modelRule?.let {
            if(it.readAction.enable=="false"){
                return null
            }
        }
        var fs=ArrayList<dynamic.model.query.mq.FieldBase>()
        var o2ofs=ArrayList<dynamic.model.query.mq.FieldBase>()
        var o2mfs=ArrayList<dynamic.model.query.mq.AttachedField>()
        var m2ofs=ArrayList<dynamic.model.query.mq.FieldBase>()
        var m2mfs=ArrayList<dynamic.model.query.mq.AttachedField>()
        var ownerMany2OneFields = ArrayList<dynamic.model.query.mq.ModelMany2OneField>()
        if(fields.isEmpty()){
            var pFields= model?.fields?.getAllPersistFields()?.values?.toTypedArray()
            if(partnerCache!=null){
                pFields=this.filterAcModelFields(pFields,model=this,partnerCache=partnerCache)
            }
            pFields?.let {
                sortFields(model, arrayListOf(*pFields),fs,o2ofs,o2mfs,m2ofs,m2mfs,ownerMany2OneFields)
            }
        }
        else{
            var pFields: Array<dynamic.model.query.mq.FieldBase>?= fields as Array<dynamic.model.query.mq.FieldBase>?
            pFields = if(useAccessControl){
               // partnerCache?.acFilterReadFields(fields as Array<FieldBase>)
                pFields
            } else fields
            pFields?.let {
                sortFields(model, arrayListOf(*pFields),fs,o2ofs,o2mfs,m2ofs,m2mfs,ownerMany2OneFields)
            }
        }
        fs= arrayListOf(*(fs.distinctBy {
            it.getFullName()
        }.toTypedArray()))
        o2ofs= arrayListOf(*(o2ofs.distinctBy {
            it.getFullName()
        }.toTypedArray()))
        m2ofs= arrayListOf(*(m2ofs.distinctBy {
            it.getFullName()
        }.toTypedArray()))
        o2mfs= arrayListOf(*(o2mfs.distinctBy {
            (it.field as dynamic.model.query.mq.FieldBase).getFullName()
        }.toTypedArray()))
        m2mfs= arrayListOf(*(m2mfs.distinctBy {
            (it.field as dynamic.model.query.mq.FieldBase).getFullName()
        }.toTypedArray()))
        ownerMany2OneFields= arrayListOf(*(ownerMany2OneFields.distinctBy {
            it.getFullName()
        }.toTypedArray()))

        var joinModels= arrayListOf<dynamic.model.query.mq.join.JoinModel>()
        var modelRelationMatcher = ModelRelationMatcher()
        o2ofs.forEach {
            var mf=this.getTargetModelField(it)
            if(mf!=null){
                if(useAccessControl){
                    var o2oFields=mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!//partnerCache?.acFilterReadFields(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(o2oFields!=null){
                        fs.addAll(o2oFields)
                    }
                    else{
                        return@forEach
                    }
                }
                else{
                    fs.addAll(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                }
                var o2oFd= it as dynamic.model.query.mq.ModelOne2OneField
                if(o2oFd.isVirtualField){
                    var idf=o2oFd.model?.fields?.getIdField()
                    modelRelationMatcher.addMatchData(model,o2oFd,mf?.first,mf?.second,idf)
                    joinModels.add(leftJoin(mf?.first,eq(mf?.second!!,idf)!!))
                }
                else{
                    modelRelationMatcher.addMatchData(model,o2oFd,mf?.first,mf?.second)
                    joinModels.add(leftJoin(mf?.first,eq(mf?.second!!,it)!!))
                }
            }
        }

        m2ofs.forEach{
            var mf=this.getTargetModelField(it)
            if(mf!=null){
                if(useAccessControl){
                    var m2oFields=mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!//partnerCache?.acFilterReadFields(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(m2oFields!=null){
                        fs.addAll(m2oFields)
                    }
                    else{
                        return@forEach
                    }
                }
                else{
                    fs.addAll(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                }
                modelRelationMatcher.addMatchData(model,it,mf?.first,mf?.second)
                joinModels.add(leftJoin(mf?.first,eq(mf?.second!!,it)!!))
            }
        }
        var offset=null as Int?
        var limit=null as Int?
        var newOrderBy=orderBy
        if(pageIndex!=null){
            limit= pageSize ?: this.pageSize
            offset=(pageIndex-1)*limit
            if(offset<0){
                offset=0
            }
            if(newOrderBy==null){
                newOrderBy=model?.fields?.getDefaultOrderBy()
            }
        }
        var (readCriteria,postFS) = this.beforeRead(*fs.toTypedArray(),
                criteria=criteria,
                model=this,
                useAccessControl = useAccessControl,
                partnerCache = partnerCache,
                joinModels = joinModels.toTypedArray())

        var mDataArray=this.query(*postFS,
                fromModel = model!!,
                joinModels = joinModels.toTypedArray(),
                criteria = readCriteria,
                orderBy = newOrderBy,
                offset = offset,limit = limit)


        mDataArray?.model=model
        mDataArray=this.reconstructSingleRelationModelRecordSet(mDataArray,modelRelationMatcher)

        var rmfs= mutableMapOf<String,MutableList<dynamic.model.query.mq.AttachedField>>()
        m2mfs.forEach {
            val field=it.field as dynamic.model.query.mq.RefRelationField
            if(field.relationModelTable!=null){
                if(rmfs.containsKey(field.relationModelTable!!)){
                    rmfs[field.relationModelTable!!]?.add(it)
                }
                else{
                    var mlst= mutableListOf<dynamic.model.query.mq.AttachedField>()
                    mlst.add(it)
                    rmfs[field.relationModelTable!!]=mlst
                }
            }
        }

        var to2mfs=ArrayList<dynamic.model.query.mq.AttachedField>()
        to2mfs.addAll(o2mfs)

//        to2mfs.forEach {
//            var rrf=it as RefRelationField
//            if(rrf.relationModelTable!=null){
//                if(rmfs.containsKey(rrf.relationModelTable!!)){
//                    rmfs[rrf.relationModelTable!!]?.add(it)
//                }
//                else{
//                    var mLst= mutableListOf<RefRelationField>()
//                    mLst.add(it)
//                    rmfs[rrf.relationModelTable!!]=mLst
//                }
//                o2mfs.remove(it)
//            }
//        }


        attachedFields?.forEach {
            if(it.field is dynamic.model.query.mq.Many2ManyField){
                var rrf=it.field as dynamic.model.query.mq.RefRelationField
                if(rrf.relationModelTable!=null){
                    if(rmfs.containsKey(rrf.relationModelTable!!)){
                        var fList=rmfs[rrf.relationModelTable!!]
                        if(fList!!.filter {rt-> (rt.field as dynamic.model.query.mq.FieldBase).getFullName()==(rrf as dynamic.model.query.mq.FieldBase).getFullName() }.count()<1){
                            fList.add(it)
                        }
                        else{
                            fList.removeIf { xit ->
                                (xit.field as dynamic.model.query.mq.FieldBase).isSame(rrf as dynamic.model.query.mq.FieldBase)
                            }
                            fList.add(it)
                        }
                    }
                    else{
                        var mlst= mutableListOf<dynamic.model.query.mq.AttachedField>()
                        mlst.add(it)
                        rmfs[rrf.relationModelTable!!]=mlst
                    }
                }
            }
            else if(it.field is dynamic.model.query.mq.One2ManyField){
                var rtf=it.field as dynamic.model.query.mq.RefTargetField
                if(o2mfs.filter { rt-> (rt.field as dynamic.model.query.mq.FieldBase).getFullName()==(it.field as dynamic.model.query.mq.FieldBase).getFullName() }.count()<1)
                {
                    o2mfs.add(it)
                }
                else{
                    o2mfs.removeIf { xit ->
                        (xit.field as dynamic.model.query.mq.FieldBase).isSame(rtf as dynamic.model.query.mq.FieldBase)
                    }
                    o2mfs.add(it)
                }
            }
        }


        rmfs.forEach {
            modelRelationMatcher = ModelRelationMatcher()
            var rmf=model.getRelationModelField(it.value.first().field as FieldBase)
            var idField=model?.fields?.getIdField()
            var rIDField=rmf?.first?.fields?.getFieldByTargetField(idField)
            var subSelect=select(idField!!,fromModel = model!!).where(readCriteria).orderBy(newOrderBy).offset(offset).limit(limit)
            var rtFields=ArrayList<dynamic.model.query.mq.FieldBase>()
            rtFields.addAll(rmf?.first?.fields?.getAllPersistFields(true)?.values!!)
            modelRelationMatcher.addMatchData(model,idField,rmf?.first,rIDField)
            var joinModels=ArrayList<dynamic.model.query.mq.join.JoinModel>()
            it.value.forEach allField@{rrf->
                val relationMF = model.getRelationModelField(rrf.field as FieldBase)?:return@allField
                val targetMF= model.getTargetModelField(rrf.field as FieldBase)?:return@allField
                var jField= relationMF.first?.fields?.getFieldByTargetField(targetMF.second)?:return@allField
                if(useAccessControl){
                    //var rmfFields=partnerCache?.acFilterReadFields(sRmf?.first?.fields?.getAllPersistFields()?.values?.toTypedArray()!!)
                    var targetMFFields=targetMF?.first?.fields?.
                            getAllPersistFields(true)?.
                            values?.toTypedArray()!!
                    if(targetMFFields!=null){
                        //rtFields.addAll(rmfFields)
                        rtFields.addAll(targetMFFields)
                    }
                    else{
                        return@allField
                    }
                }else{
                    rtFields.addAll(targetMF?.first?.fields?.getAllPersistFields(true)?.values!!)
                }
                modelRelationMatcher.addMatchData(rmf?.first,
                        relationMF.second,
                        targetMF?.first,
                        targetMF?.second)
                if(rrf.canBeEmpty){
                    joinModels.add(leftJoin(targetMF?.first, eq(jField!!,targetMF?.second!!)!!))
                }
                else{
                    joinModels.add(innerJoin(targetMF?.first, eq(jField!!,targetMF?.second!!)!!))
                }
            }

            var rOrderBy=null as dynamic.model.query.mq.OrderBy?
            var rOffset=null as Int?
            var rLimit=null as Int?
            //todo add support pagesize every field
//            var selfField=it.value.first() as ModelField
//            if(relationPaging && (selfField is PagingField)){
//                rOrderBy=rmf?.first?.fields?.getDefaultOrderBy()
//                rOffset=0
//                rLimit=selfField.pageSize
//            }
            var attachedCriteriaArr=it.value.filter {
                af->
                af.criteria!=null
            }.stream().map { x->x.criteria }.toList()
            var subCriteria = selectIn(rIDField!!,subSelect)
            if(attachedCriteriaArr.count()>0){
                var mLst= mutableListOf<dynamic.model.query.mq.ModelExpression>()
                attachedCriteriaArr.forEach {mIt->
                    mLst.add(mIt!!)
                }
                mLst.add(subCriteria)
                subCriteria=and(*mLst.toTypedArray())
            }


            var (readCriteria,postFS) = (rmf?.first!! as AccessControlModel).beforeRead(*rtFields.toTypedArray(),
                    criteria=subCriteria,
                    model=rmf?.first!!,
                    useAccessControl = useAccessControl,
                    partnerCache = partnerCache,
                    joinModels = joinModels.toTypedArray())

            var mrDataArray=(rmf?.first!! as AccessControlModel).query(*postFS,
                    fromModel= rmf?.first!!,
                    joinModels=joinModels.toTypedArray(),
                    criteria=readCriteria,
                    orderBy = rOrderBy,
                    offset = rOffset,
                    limit = rLimit)
            var fieldArr=it.value.stream().map { x->x.field }.toList() as List<dynamic.model.query.mq.FieldBase>
            mDataArray=reconstructMultipleRelationModelRecordSet(model,
                    fieldArr.toTypedArray(),
                    mDataArray,rmf.first,
                    postFS,
                    mrDataArray,
                    modelRelationMatcher)
        }


        o2mfs.forEach {
            modelRelationMatcher = ModelRelationMatcher()
            var targetMF=this.getTargetModelField(it.field as dynamic.model.query.mq.FieldBase)
            if(targetMF!=null){
                var subSelect=select(model?.fields?.getIdField()!!,fromModel = model).where(readCriteria).orderBy(newOrderBy).offset(offset).limit(limit)

                var rOrderBy=null as dynamic.model.query.mq.OrderBy?
                var rOffset=null as Int?
                var rLimit=null as Int?
                //todo add support pagesize every field
//                if(relationPaging && (it is PagingField)){
//                    rOrderBy=targetMF?.first?.fields?.getDefaultOrderBy()
//                    rOffset=0
//                    rLimit=it.pageSize
//                }
                if(useAccessControl){
                    var targetMFFields=targetMF?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!//partnerCache?.acFilterReadFields(targetMF?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(targetMFFields!=null){

                        modelRelationMatcher.addMatchData(model,it.field as FieldBase,targetMF?.first,targetMF?.second,model.fields?.getIdField())
                        var subCriteria=selectIn(targetMF?.second!!,subSelect)
                        if(it.criteria!=null){
                            subCriteria=and(subCriteria!!, it.criteria!!)
                        }

                        var (readCriteria,postFS) = (targetMF?.first!! as AccessControlModel).beforeRead(*targetMFFields,
                                criteria=subCriteria,
                                model=targetMF?.first!!,
                                useAccessControl = useAccessControl,
                                partnerCache = partnerCache)

                        var mrDataArray=(targetMF?.first!! as AccessControlModel).query(*postFS,
                                fromModel= targetMF?.first!!,
                                criteria=readCriteria,
                                orderBy = rOrderBy,
                                offset = rOffset,
                                limit = rLimit)

                        mDataArray=reconstructMultipleRelationModelRecordSet(model,
                                arrayOf(it.field as dynamic.model.query.mq.FieldBase),
                                mDataArray,
                                null,
                                postFS,
                                mrDataArray,
                                modelRelationMatcher)
                    }
                    else{
                        return@forEach
                    }
                }
                else{
                    modelRelationMatcher.addMatchData(model,it.field as FieldBase,targetMF?.first,targetMF?.second,model.fields?.getIdField())
                    var subCriteria=selectIn(targetMF?.second!!,subSelect)
                    if(it.criteria!=null){
                        subCriteria=and(subCriteria!!, it.criteria!!)
                    }
                    var targetMFFields=targetMF?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!
                    var (readCriteria,postFS) = (targetMF?.first!! as AccessControlModel).beforeRead(*targetMFFields,
                            criteria=subCriteria,
                            model=targetMF?.first!!,
                            useAccessControl = useAccessControl,
                            partnerCache = partnerCache)

                    var mrDataArray=(targetMF?.first!! as AccessControlModel).query(*postFS,fromModel= targetMF?.first!!,criteria=readCriteria,
                            orderBy = rOrderBy,offset = rOffset,limit = rLimit)

                    mDataArray= reconstructMultipleRelationModelRecordSet(model,
                            arrayOf(it.field as dynamic.model.query.mq.FieldBase),
                            mDataArray,
                            null,
                            postFS,
                            mrDataArray,
                            modelRelationMatcher)
                }
            }
        }

        if( ownerMany2OneFields.count()>0){
            mDataArray?.let {
                for(fvs in it.data){
                    ownerMany2OneFields.forEach {m2o->
                        var v = fvs.getValue(m2o)
                        if(v!=null && m2o.targetModelFieldName== ModelReservedKey.idFieldName){
                            var criteria=eq(model!!.fields.getIdField()!!,v)
                            var (readCriteria,postFS) = this.beforeRead(*fs.toTypedArray(),
                                    criteria=criteria,
                                    model=this,
                                    useAccessControl = useAccessControl,
                                    partnerCache = partnerCache,
                                    joinModels = joinModels.toTypedArray())

                            v=this.query(*postFS,
                                    fromModel = model!!,
                                    joinModels = joinModels.toTypedArray(),criteria =readCriteria )?.firstOrNull()
                            if(v!=null){
                                v.model=model
                                fvs.setValue(m2o,v)
                            }
                        }
                    }

                }
            }
        }
        this.doFillModelFunctionFields(mDataArray,useAccessControl,partnerCache)
        return mDataArray
    }
    fun getRelationFieldTo(model: dynamic.model.query.mq.FieldBase): dynamic.model.query.mq.FieldBase?{



        return null
    }
    protected open fun doFillModelFunctionFields(modelDataArray: dynamic.model.query.mq.ModelDataArray?, useAccessControl: Boolean, partnerCache: PartnerCache?){
        val model=modelDataArray?.model
        val functions = model?.fields?.getTypeFields<dynamic.model.query.mq.FunctionField<*,*>>()?.values?.toTypedArray()?: arrayOf()
        modelDataArray?.data?.forEach {
            this.doFillFieldValueArrayFunctionFields(*functions,fieldValueArray = it,useAccessControl = useAccessControl,partnerCache = partnerCache)
        }
    }
    protected open fun doFillModelFunctionFields(modelDataObject: dynamic.model.query.mq.ModelDataObject?, useAccessControl: Boolean, partnerCache: PartnerCache?){
        val model=modelDataObject?.model
        val functions = model?.fields?.getTypeFields<dynamic.model.query.mq.FunctionField<*,*>>()?.values?.toTypedArray()?: arrayOf()
        modelDataObject?.let {
            this.doFillFieldValueArrayFunctionFields(*functions,fieldValueArray = modelDataObject.data,
                    useAccessControl = useAccessControl,
                    partnerCache = partnerCache)
        }
    }
    private fun doFillFieldValueArrayFunctionFields(vararg functionFields: dynamic.model.query.mq.FieldBase?, fieldValueArray: dynamic.model.query.mq.FieldValueArray, useAccessControl: Boolean, partnerCache: PartnerCache?){
        functionFields.forEach {
            if(it is dynamic.model.query.mq.FunctionField<*,*>){

                val compValue = (it as dynamic.model.query.mq.FunctionField<*,PartnerCache>).compute(fieldValueArray,partnerCache,null)
                fieldValueArray.setValue(it,compValue)
            }
        }
        fieldValueArray.forEach {
            when {
                it.field is dynamic.model.query.mq.ProxyRelationModelField<*,*> -> (it.field as dynamic.model.query.mq.ProxyRelationModelField<*,PartnerCache>).inverse(fieldValueArray,partnerCache,null,null)
                it.value is dynamic.model.query.mq.ModelDataObject -> this.doFillModelFunctionFields(it.value as ModelDataObject,useAccessControl,partnerCache)
                it.value is dynamic.model.query.mq.ModelDataArray ->  this.doFillModelFunctionFields(it.value as ModelDataArray,useAccessControl,partnerCache)
                it.value is dynamic.model.query.mq.ModelDataSharedObject -> (it.value as ModelDataSharedObject).data.forEach { _, u ->
                    when(u){
                        is dynamic.model.query.mq.ModelDataObject ->{
                            this.doFillModelFunctionFields(u,useAccessControl,partnerCache)
                        }
                        is dynamic.model.query.mq.ModelDataArray ->{
                            this.doFillModelFunctionFields(u,useAccessControl,partnerCache)
                        }
                    }
                }
            }
        }
    }
    protected  open fun reconstructMultipleRelationModelRecordSet(model:ModelBase?,
                                                                  fields:Array<dynamic.model.query.mq.FieldBase>,
                                                                  reqMainArray: dynamic.model.query.mq.ModelDataArray?,
                                                                  relModel:ModelBase?,
                                                                  targetFields:Array<dynamic.model.query.mq.FieldBase>,
                                                                  relDataArray: dynamic.model.query.mq.ModelDataArray?,
                                                                  modelRelationMatcher: ModelRelationMatcher): dynamic.model.query.mq.ModelDataArray?{
        var mainArray = dynamic.model.query.mq.ModelDataArray(model = model, fields = reqMainArray?.fields)
        val idField = model?.fields?.getIdField()?: return reqMainArray
        reqMainArray?.data?.forEach {
            var mId = it.getValue(idField) as Long
            var fvs = dynamic.model.query.mq.FieldValueArray()
            fvs.addAll(it)
            if(relModel!=null){ //m2m
                val field = relModel.fields?.getFieldByTargetField(idField)
                val relArray = this.readM2MModelDataArrayFromMultiModelDataArray(relModel,field!!,mId,fields,relDataArray)
                var mds = (fvs.getValue(ConstRelRegistriesField.ref) as dynamic.model.query.mq.ModelDataSharedObject?)?: dynamic.model.query.mq.ModelDataSharedObject()
                relArray?.let {
                    mds.data[relModel]=relArray
                    fvs.setValue(ConstRelRegistriesField.ref,mds)
                }
            }
            else{//o2m
                fields.forEach { o2mField->
                    val tf = this.getTargetModelField(o2mField)
                    val targetModelDataArray = this.readO2MModelDataArrayFromMultiModelDataArray(tf?.first,tf?.second,mId,relDataArray)
                    mainArray.fields?.add(o2mField)
                    fvs.setValue(o2mField,targetModelDataArray)
                }
            }
            mainArray.data.add(fvs)
        }
        return mainArray
    }

    private  fun readM2MModelDataArrayFromMultiModelDataArray(relModel:ModelBase,
                                                              field: dynamic.model.query.mq.FieldBase,
                                                              fieldValue:Long,
                                                              relFields:Array<dynamic.model.query.mq.FieldBase>,
                                                              dataArray: dynamic.model.query.mq.ModelDataArray?): dynamic.model.query.mq.ModelDataArray?{

        var relReadFields = this.getModelFieldsFromMultiDataArray(relModel,dataArray)
        var relDataArray = dynamic.model.query.mq.ModelDataArray(model = relModel, fields = relReadFields)
        field?.let {
            dataArray?.data?.filter {
                (it.getValue(field) as Long)==fieldValue
            }?.forEach {fv->
                var relFieldValue= this.readOneModelFieldValueFromMultiModelFieldValue(relReadFields,fv)
                relDataArray.data.add(relFieldValue)
                relFields.forEach {
                    var tf = this.getTargetModelField(it)
                    var tReadFields = this.getModelFieldsFromMultiDataArray(tf?.first,dataArray)
                    var tFV = this.readOneModelFieldValueFromMultiModelFieldValue(tReadFields,fv)
                    var rf = this.getRelationModelField(it)
                    rf?.let {
                        relFieldValue.setValue(rf.second!!, dynamic.model.query.mq.ModelDataObject(fields = tReadFields, data = tFV, model = tf?.first))
                    }
                }
            }
        }
        return relDataArray
    }
    private fun readOneModelFieldValueFromMultiModelFieldValue(modelFields:ArrayList<dynamic.model.query.mq.FieldBase>, multiModelFieldValue: dynamic.model.query.mq.FieldValueArray): dynamic.model.query.mq.FieldValueArray {
        var n = dynamic.model.query.mq.FieldValueArray()
        modelFields.forEach {
            if(multiModelFieldValue.containFieldKey(it)){
                n.setValue(it,multiModelFieldValue.getValue(it))
            }
        }
        return n
    }
    private  fun readO2MModelDataArrayFromMultiModelDataArray(model:ModelBase?,
                                                              field: dynamic.model.query.mq.FieldBase?,
                                                              fieldValue:Long,
                                                              dataArray: dynamic.model.query.mq.ModelDataArray?): dynamic.model.query.mq.ModelDataArray?{
        var fields = this.getModelFieldsFromMultiDataArray(model,dataArray)
        var modelDataArray= dynamic.model.query.mq.ModelDataArray(model = model, fields = fields)
        field?.let {
            dataArray?.data?.filter {
                (it.getValue(field) as Long)==fieldValue
            }?.forEach { fv->
                var nFv = dynamic.model.query.mq.FieldValueArray()
                fields.forEach {
                    val v =fv.getValue(it)
                    nFv.setValue(it,v)
                }
                modelDataArray.data.add(nFv)
            }
        }
        return modelDataArray
    }

    private  fun getModelFieldsFromMultiDataArray(model:ModelBase?,dataArray: dynamic.model.query.mq.ModelDataArray?):ArrayList<dynamic.model.query.mq.FieldBase>{
        var fields = arrayListOf<dynamic.model.query.mq.FieldBase>()
         model?.let {
             var d = dataArray?.data?.firstOrNull()
             d?.forEach {
                 if(it.field.model!!.isSame(model)){
                     fields.add(it.field)
                 }
             }
         }
        return fields
    }

    private fun setOrReplaceFieldValueArrayItem(fVArr: dynamic.model.query.mq.FieldValueArray, field: dynamic.model.query.mq.FieldBase, value:Any?){
        var index=fVArr.indexOfFirst {
            it.field.isSame(field)
        }
        if(index>-1){
            fVArr[index]= dynamic.model.query.mq.FieldValue(field, value)
        }
        else{
            fVArr.add(dynamic.model.query.mq.FieldValue(field, value))
        }
    }
    protected open fun reconstructSingleRelationModelRecordSet(mDataArray: dynamic.model.query.mq.ModelDataArray?,
                                                               modelRelationMatcher: ModelRelationMatcher): dynamic.model.query.mq.ModelDataArray?{
        var mainModel = mDataArray?.model
        var mainModelFields=ArrayList<dynamic.model.query.mq.FieldBase>()
        var subModels= mutableMapOf<ModelBase?, dynamic.model.query.mq.ModelDataObject>()
        mDataArray?.fields?.forEach {
            if(mainModel!=it.model){
               if(subModels.contains(it.model)){
                   subModels[it.model]?.fields?.add(it)
               }
               else
               {
                   var fields=ArrayList<dynamic.model.query.mq.FieldBase>()
                   fields.add(it)
                   var mrDataObject= dynamic.model.query.mq.ModelDataObject(fields = fields, model = it.model)
                   var mfd=modelRelationMatcher.getRelationMatchField(mainModel,it.model)
                   mrDataObject.fromField=mfd?.fromField
                   subModels[it.model]=mrDataObject
               }
            }
            else{
                mainModelFields.add(it)
            }
        }
        var mainModelDataArray = dynamic.model.query.mq.ModelDataArray(fields = mainModelFields, model = mainModel)
        mDataArray?.data?.forEach { fvArr->
            var mainRecord= dynamic.model.query.mq.FieldValueArray()
            mainModelDataArray.fields?.forEach {mf->
                //var key= it.getFullName()
                //mainRecord[it.propertyName]= mit[key]
                //todo performance
                var fv=fvArr.firstOrNull {
                    it.field.isSame(mf)
                }
                if(fv!=null){
                    mainRecord.add(fv)
                }
            }
            subModels.values.forEach {
                var subRecord = dynamic.model.query.mq.FieldValueArray()
                it.fields?.forEach {fb->
                    var fv=fvArr.firstOrNull {sf->
                        sf.field.isSame(fb)
                    }
                    if(fv!=null){
                        subRecord.add(fv)
                    }
                }
                mainModelDataArray.fields?.add(it.fromField!!)
                var cloneModelObject= dynamic.model.query.mq.ModelDataObject(data = subRecord, model = it.model)
                mainRecord.add(dynamic.model.query.mq.FieldValue(it.fromField!!, cloneModelObject))
            }
            mainModelDataArray.data.add(mainRecord)
        }
        return mainModelDataArray
    }

    protected open fun getRelationModelField(field: FieldBase):Pair<ModelBase, FieldBase>?{
        if((field is Many2ManyField)){
            var model=this.appModel?.getModel((field as RefRelationField).relationModelTable)?:return null
            var mField=model?.fields?.getField((field as RefRelationField).relationModelFieldName)?:return null
            return Pair(model,mField)
        }
        return null
    }
    protected  open fun getTargetModelField(field: dynamic.model.query.mq.FieldBase):Pair<ModelBase, dynamic.model.query.mq.FieldBase>?{
        if(field is dynamic.model.query.mq.RefTargetField){
            var model=this.appModel?.getModel(field.targetModelTable)?:return null
            var mField=model?.fields?.getField(field.targetModelFieldName)?:return null
            return Pair(model,mField)
        }
        return null
    }




    fun acCreate(modelData: dynamic.model.query.mq.ModelData,
                 partnerCache:PartnerCache):Pair<Long?,String?>{
            return this.safeCreate(modelData,useAccessControl=true,partnerCache = partnerCache)
    }

    open fun safeCreate(modelData: dynamic.model.query.mq.ModelData,
                        useAccessControl: Boolean=false,
                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        var errorMessage="添加失败" as String?
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager?.getTransaction(def)
        try {
           // var dependingModelFieldValueCollection
            if(modelData.context==null){
                modelData.createContext()
            }
            var (id,errMsg)=(modelData.model as AccessControlModel)
                    .rawCreate(modelData,useAccessControl,partnerCache)
            if(id!=null && id>0){
                txManager?.commit(status)
                return Pair(id,null)
            }
            errorMessage=errMsg
        } catch (ex: Exception) {
            logger.error(ex.message)
            ex.printStackTrace()
            errorMessage=ex.message
        }
        try
        {
            //Hikaricp discard database connection when some error occur
            txManager?.rollback(status)
        }
        catch(ex:Exception)
        {
            errorMessage=ex.message
            logger.error(ex.message)
        }
        return Pair(0,errorMessage)
    }

    open fun getCreateFieldValue(field: dynamic.model.query.mq.FieldBase, value:Any?, partnerCache:PartnerCache?=null, fvs: dynamic.model.query.mq.FieldValueArray?=null): dynamic.model.query.mq.FieldValue?{
            return when (field) {
                is dynamic.model.query.mq.ProxyRelationModelField<*,*> -> null
                else -> when(value){
                    is dynamic.model.query.mq.ModelDataObject ->{
                        return if(value.idFieldValue!=null){
                            dynamic.model.query.mq.FieldValue(field, value.idFieldValue?.value)
                        } else{
                            null//FieldValue(field,null)
                        }
                    }
                    is FieldDefaultValueBillboard ->{
                        return when(value){
                            is CurrCorpBillboard-> dynamic.model.query.mq.FieldValue(field, value.looked(partnerCache))
                            is CurrPartnerBillboard-> dynamic.model.query.mq.FieldValue(field, value.looked(partnerCache))

                            else-> dynamic.model.query.mq.FieldValue(field, value.looked(null))
                        }
                    }
                    is FieldValueDependentingRecordBillboard ->{
                        val ret = value.looked(fvs, ActionType.CREATE)
                        return if(ret.first){
                            dynamic.model.query.mq.FieldValue(field, ret.second)
                        } else{
                            null
                        }
                    }
                    else->{
                        return if(value!=null) dynamic.model.query.mq.FieldValue(field, value) else null
                    }
                }
            }

    }


    open fun getEditFieldValue(field: dynamic.model.query.mq.FieldBase, value:Any?, partnerCache:PartnerCache?=null, fvs: dynamic.model.query.mq.FieldValueArray?=null): dynamic.model.query.mq.FieldValue?{
        return when (field) {
            is dynamic.model.query.mq.ProxyRelationModelField<*,*> -> null
            else -> when(value){
                is dynamic.model.query.mq.ModelDataObject ->{
                    value.idFieldValue?.let {
                        it.value?.let {
                            val lz:Long=0
                            return if((it as BigInteger).toLong()!=lz) dynamic.model.query.mq.FieldValue(field, it) else null
                        }
                    }
                    return null
                }
                is FieldDefaultValueBillboard ->{
                   null
                }
                is FieldValueDependentingRecordBillboard ->{
                    var ret = value?.looked(fvs, ActionType.EDIT)
                    return if(ret.first){
                        dynamic.model.query.mq.FieldValue(field, ret.second)
                    }
                    else{
                        null
                    }
                }
                else->{
                    if(value!=null) dynamic.model.query.mq.FieldValue(field, value) else null
                }
            }
        }
    }


    open fun rawCreate(data: dynamic.model.query.mq.ModelData,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{


        when(data){
            is dynamic.model.query.mq.ModelDataObject ->{
                return rawCreateObject(data,useAccessControl,partnerCache)
            }
            is dynamic.model.query.mq.ModelDataArray ->{
                return rawCreateArray(data,useAccessControl,partnerCache)
            }
        }
        return Pair(null,"not support")
    }

    // muse call in safeCreate
    protected open fun rawCreateArray(modelDataArray: dynamic.model.query.mq.ModelDataArray,
                                      useAccessControl: Boolean=false,
                                      partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        for (d in modelDataArray.data){
            var obj= dynamic.model.query.mq.ModelDataObject(d, model = modelDataArray.model, fields = modelDataArray.fields)
            obj.context=modelDataArray.context
            var ret=(modelDataArray.model as AccessControlModel).rawCreateObject(obj,useAccessControl,partnerCache)
            if(ret.first==null|| ret.second!=null){
                return ret
            }
        }
        return Pair(1,null)
    }
    protected  open fun beforeCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                           useAccessControl: Boolean=false,
                                           partnerCache:PartnerCache?=null):Pair<Boolean,String?>
    {
        if(useAccessControl || partnerCache!=null){
            if(partnerCache==null){
                return Pair(false,"权限接口没有提供操作用户信息")
            }

            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {
                if((it is dynamic.model.query.mq.FunctionField<*,*>) || (it is dynamic.model.query.mq.ModelOne2ManyField)
                        ||(it is dynamic.model.query.mq.ModelMany2ManyField)){
                    return@forEach
                }
                val oit = it as dynamic.model.query.mq.ModelField
                var fv = modelDataObject.data.firstOrNull { fv->
                    fv.field.getFullName() == oit.getFullName()
                }
                if (fv == null) {
                    if (oit.defaultValue != null) {
                        var acFV=this.getCreateFieldValue(oit, oit.defaultValue,partnerCache,modelDataObject.data)
                        acFV?.let {
                            modelDataObject.setFieldValue(acFV.field,acFV.value)
                        }
                    }
                }
                else{
                    var tFV=this.getCreateFieldValue(fv.field,fv.value,partnerCache,modelDataObject.data)
                    tFV?.let {
                        modelDataObject.setFieldValue(tFV.field,tFV.value)
                    }
                }
            }

            this.runCreateFieldsInitializeRules(modelDataObject,partnerCache)
            var ret = this.runCreateFieldsCheckRules(modelDataObject,partnerCache)
            if(!ret.first){
                return ret
            }
            this.runCreateFieldsFilterRules(modelDataObject,partnerCache)
        }
        return Pair(true,null)
    }

    protected open fun getModelCreateFieldsInspectors():Array<ModelFieldInspector>?{
        return null
    }
    protected  open fun getModelCreateFieldsInStoreInspectors():Array<ModelFieldInspector>?{
        return null
    }

    protected  open fun runCreateFieldsFilterRules(modelDataObject: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache){
        val model = modelDataObject.model?:this

        var modelRule = partnerCache.getModelRule(model.meta.appName,model.meta.name)
        modelRule?.let {
            if(it.createAction.enable=="false"){

            }
        }
        modelRule?.fieldRules?.forEach { _, u ->
            if(u.createAction.enable!="false") {
                if(u.createAction.setValue!=null){
                    modelDataObject.setFieldValue(u.field,this.getValueFromPartnerContextConstKey(u.createAction.setValue,partnerCache))
                } else if(u.createAction.default!=null && !modelDataObject.hasFieldValue(u.field)){
                    modelDataObject.setFieldValue(u.field,this.getValueFromPartnerContextConstKey(u.createAction.default,partnerCache))
                }
            } else{
                modelDataObject.removeFieldValue(u.field)
            }
        }

        var filterRules = partnerCache.getModelCreateAccessControlRules<ModelCreateRecordFieldsValueFilterRule<*>>(model)

        filterRules?.forEach {
            it(modelDataObject,partnerCache,null)
        }

    }

    protected  open fun runEditFieldsFilterRules(modelDataObject: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache){
        val model = modelDataObject.model?:this

        var modelRule = partnerCache.getModelRule(model.meta.appName,model.meta.name)
        modelRule?.fieldRules?.forEach { _, u ->
            if(u.editAction.enable!="false") {
                if(u.editAction.setValue!=null){
                    modelDataObject.setFieldValue(u.field,this.getValueFromPartnerContextConstKey(u.editAction.setValue,partnerCache))
                } else if(u.editAction.default!=null && !modelDataObject.hasFieldValue(u.field)){
                    modelDataObject.setFieldValue(u.field,this.getValueFromPartnerContextConstKey(u.editAction.default,partnerCache))
                }
            } else{
                modelDataObject.removeFieldValue(u.field)
            }
        }
        var fitler = this.getModelEditAccessFieldFilterRule()
        fitler?.let {
            it(modelDataObject,partnerCache,null)
        }
        var filterRules = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueFilterRule<*>>(model)

        filterRules?.forEach {
            it(modelDataObject,partnerCache,null)
        }

    }

    protected open  fun getValueFromPartnerContextConstKey(value:String?,partnerCache:PartnerCache):String? {
        if(value.isNullOrEmpty()){
            return value
        }
        var ret= partnerCache.getContextValue(value)
        return if(ret.first){
            if(ret.second!=null){
                ret.second.toString()
            }
            else
            {
                null
            }
        }
        else{
            value
        }
    }
    protected open fun runCreateFieldsCheckRules(modelDataObject: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache):Pair<Boolean,String?>{
        val model = modelDataObject.model?:this

        var modelRule = partnerCache.getModelRule(model.meta.appName,model.meta.name)
        modelRule?.let {
            if(it.createAction.enable=="false"){
                return Pair(false,"没有添加权限")
            }
        }

        var inspectors=this.getModelCreateFieldsInspectors()
        var ret = modelCreateFieldsInspectorCheck(modelDataObject,partnerCache,inspectors)
        if(!ret.first){
            return ret
        }

        var modelCreateFieldsChecks = partnerCache.getModelCreateAccessControlRules<ModelCreateRecordFieldsValueCheckRule<*>>(model)

        modelCreateFieldsChecks?.forEach {
            ret = it(modelDataObject,partnerCache,null)
            if(!ret.first){
                return ret
            }
        }


        inspectors = this.getModelCreateFieldsInStoreInspectors()
        ret =modelCreateFieldsInStoreInspectorCheck(modelDataObject,partnerCache,inspectors)
        if(!ret.first){
            return ret
        }

        var modelCreateFieldsInStoreChecks = partnerCache.getModelCreateAccessControlRules<ModelCreateRecordFieldsValueCheckInStoreRule<*>>(model)
        modelCreateFieldsInStoreChecks?.forEach {
            ret = it(modelDataObject,partnerCache,null)
            if(!ret.first){
                return ret
            }
        }

        return Pair(true,null)
    }

    protected  open fun getModelEditFieldsInspectors():Array<ModelFieldInspector>?{
        return null
    }
    protected  open fun getModelEditFieldsInStoreInspectors():Array<ModelFieldInspector>?{
        return null
    }
    protected open fun runEditFieldsCheckRules(modelDataObject: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache):Pair<Boolean,String?>{
        val model = modelDataObject.model?:this

        var modelRule = partnerCache.getModelRule(model.meta.appName,model.meta.name)
        modelRule?.let {
            if(it.editAction.enable=="false"){
                return Pair(false,"没有更新权限")
            }
        }
        var inspectors=this.getModelEditFieldsInspectors()
        var ret = modelEditFieldsInspectorCheck(modelDataObject,partnerCache,inspectors)
        if(!ret.first){
            return ret
        }

        var modelEditFieldsChecks = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueCheckRule<*>>(model)

        modelEditFieldsChecks?.forEach {
            ret = it(modelDataObject,partnerCache,null)
            if(!ret.first){
                return ret
            }
        }

        ret = modelEditFieldsBelongToPartnerCheck(modelDataObject,partnerCache,null)
        if(!ret.first){
            return ret
        }

        inspectors = this.getModelEditFieldsInStoreInspectors()
        ret =modelEditFieldsInStoreInspectorCheck(modelDataObject,partnerCache,inspectors)
        if(!ret.first){
            return ret
        }

        var modelEditFieldsInStoreChecks = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueCheckInStoreRule<*>>(model)
        modelEditFieldsInStoreChecks?.forEach {
            ret = it(modelDataObject,partnerCache,null)
            if(!ret.first){
                return ret
            }
        }

        return Pair(true,null)
    }

    protected open fun runCreateFieldsInitializeRules(modelDataObject: dynamic.model.query.mq.ModelDataObject, partnerCache: PartnerCache){
        val model = modelDataObject.model?:this
        this.createRecordSetIsolationFields(modelDataObject,partnerCache)
        this.cuFieldsProcessProxyModelField(modelDataObject,partnerCache,null)

        var rules = partnerCache.getModelCreateAccessControlRules<ModelCreateRecordFieldsValueInitializeRule<*>>(model)
        rules?.forEach {
            it(modelDataObject,partnerCache,null)
        }
    }
    protected  open fun rawCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                        useAccessControl: Boolean=false,
                                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{

        var constGetRefField=modelDataObject.data.firstOrNull {
            it.field is ConstGetRecordRefField
        }

        if(constGetRefField!=null){
            return if(modelDataObject.context?.refRecordMap?.containsKey(constGetRefField.value as String)!!){
                var fvc=modelDataObject.context?.refRecordMap?.get(constGetRefField.value as String) as dynamic.model.query.mq.ModelDataObject
                var idValue=fvc?.idFieldValue?.value as Long?
                if(idValue!=null) {
                    modelDataObject.data.add(dynamic.model.query.mq.FieldValue(modelDataObject.model?.fields?.getIdField()!!, idValue))
                    Pair(idValue,null)
                } else{
                    Pair(null,"cant find the ref record")
                }

            } else{
                Pair(null,"cant find the ref record")
            }
        }

        var ret= this.beforeCreateObject(modelDataObject,useAccessControl,partnerCache)
        if(!ret.first){
            return Pair(null,ret.second)
        }

//        var (result,errorMsg)=this.beforeCreateCheck(modelDataObject,useAccessControl = useAccessControl,partnerCache = partnerCache)
//        if(!result){
//            return Pair(null,errorMsg)
//        }

        modelDataObject.data.forEach {
            when(it.field){
                is dynamic.model.query.mq.Many2OneField,is dynamic.model.query.mq.One2OneField ->{
                    if(it.field is dynamic.model.query.mq.One2OneField && (it.field as One2OneField).isVirtualField){
                        return@forEach
                    }
                    if(it.value is dynamic.model.query.mq.ModelDataObject){
                        if((it.value as ModelDataObject).idFieldValue==null){
                            (it.value as ModelDataObject).context=modelDataObject.context
                            var id=((it.value as ModelDataObject).model as AccessControlModel?)?.rawCreate(it.value as ModelDataObject,useAccessControl,partnerCache)
                            if(id==null ||id.second!=null){
                                return id?:Pair(null,"创建失败")
                            }
                            var idField= (it.value as ModelDataObject)?.model?.fields?.getIdField()
                            (it.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(idField!!, id?.first))
                        }
                        else if((it.value as ModelDataObject).hasNormalField()){
                            (it.value as ModelDataObject).context=modelDataObject.context
                            var id=((it.value as ModelDataObject).model as AccessControlModel?)?.rawEdit(it.value as ModelDataObject,null,useAccessControl,partnerCache)
                            if(id==null ||id.second!=null){
                                return id?:Pair(null,"创建失败")
                            }
                        }
                    }
                }
            }
        }

//        if(useAccessControl)
//        {
//            var rules=partnerCache?.acGetCreateRules(modelDataObject.model)
//            rules?.forEach {
//                var (ok,errorMsg)=it.check(modelDataObject,context = partnerCache?.modelExpressionContext!!)
//                if(!ok){
//                    return Pair(null,errorMsg)
//                }
//            }
//        }

        return try {
            var fVCShadow= dynamic.model.query.mq.ModelDataObject(model = modelDataObject.model)
            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {
                if((it is dynamic.model.query.mq.FunctionField<*,*>) || (it is dynamic.model.query.mq.ModelOne2ManyField)
                        ||(it is dynamic.model.query.mq.ModelMany2ManyField)){
                    return@forEach
                }
                val oit = it as dynamic.model.query.mq.ModelField
                var fv = modelDataObject.data.firstOrNull { fv->
                    fv.field.getFullName() == oit.getFullName()
                }
                if (fv == null) {
                    if (oit.defaultValue != null) {
                        var acFV=this.getCreateFieldValue(oit, oit.defaultValue,partnerCache, modelDataObject.data)
                            acFV?.let { fVCShadow.data.add(acFV) }
                    }
                }
                else{
                    var tFV=this.getCreateFieldValue(fv.field,fv.value,partnerCache, modelDataObject.data)
                    tFV?.let { fVCShadow.data.add(tFV) }
                }
            }
            var nID=this.create(fVCShadow)
            if(nID==null || nID<1){
                return Pair(null,"创建失败")
            }
            modelDataObject.data.add(dynamic.model.query.mq.FieldValue(
                    modelDataObject.model?.fields?.getIdField()!!,
                    nID
            ))
            var refField=modelDataObject.data.firstOrNull{
                it.field is ConstSetRecordRefField
            }
            if(refField!=null){
                modelDataObject.context?.refRecordMap?.set(refField.value as String,modelDataObject)
            }
            modelDataObject.data.forEach {
                fv->
                when(fv.field){
                    is dynamic.model.query.mq.One2ManyField ->{
                        if(fv.value is dynamic.model.query.mq.ModelDataObject && (fv.value as ModelDataObject).idFieldValue==null){
                            var tmf=this.getTargetModelField(fv.field)
                            (fv.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, nID))
                            (fv.value as ModelDataObject).context=modelDataObject.context
                            var o2m=(tmf?.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,useAccessControl,partnerCache)
                            if (o2m==null || o2m.second!=null){
                                return  Pair(null,o2m?.second?:"创建失败")
                            }
                        }
                        else if(fv.value is dynamic.model.query.mq.ModelDataArray){
                            var tmf=this.getTargetModelField(fv.field)
                            (fv.value as ModelDataArray).context=modelDataObject.context
                            (fv.value as ModelDataArray).data.forEach {
                                    it.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, nID))
                            }
                            var ret=this.rawCreateArray(fv.value as ModelDataArray,useAccessControl,partnerCache)
                            if(ret.first==null ||ret.second!=null){
                                return ret
                            }
                        }
                    }
                    is dynamic.model.query.mq.One2OneField ->{
                        if((fv.field as One2OneField).isVirtualField){
                            if(fv.value is dynamic.model.query.mq.ModelDataObject && (fv.value as ModelDataObject).idFieldValue==null){
                                var tmf=this.getTargetModelField(fv.field)
                                (fv.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, nID))
                                (fv.value as ModelDataObject).context=modelDataObject.context
                                var o2o=(tmf?.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,useAccessControl,partnerCache)
                                if (o2o==null || o2o.second!=null){
                                    return  Pair(null,"创建失败")
                                }
                            }
                        }
                    }
                    is ConstRelRegistriesField->{
                        when(fv.value){
                            is dynamic.model.query.mq.ModelDataSharedObject ->{
                                for( kv in (fv.value as ModelDataSharedObject).data) {
                                    when(kv.value){
                                        is dynamic.model.query.mq.ModelDataObject ->{
                                            var mfvc= kv.value as dynamic.model.query.mq.ModelDataObject
                                            mfvc.context=modelDataObject.context
                                            var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                            if(tField!=null && mfvc.idFieldValue==null){
                                                mfvc.data.add(dynamic.model.query.mq.FieldValue(tField, nID))
                                                var ret=(mfvc.model as AccessControlModel?)?.rawCreate(mfvc,useAccessControl,partnerCache)
                                                if(ret==null || ret.second!=null){
                                                    return Pair(null,"创建失败")
                                                }
                                            }
                                        }
                                        is dynamic.model.query.mq.ModelDataArray ->{
                                            var mmfvc= kv.value as dynamic.model.query.mq.ModelDataArray
                                            mmfvc.context=modelDataObject.context
                                            for(mkv in mmfvc.data){
                                                var tField=mmfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                if(tField!=null){
                                                    mkv.add(dynamic.model.query.mq.FieldValue(tField, nID))
                                                }
                                            }
                                            var ret=rawCreateArray(mmfvc,useAccessControl,partnerCache)
                                            if(ret.first==null ||ret.second!=null){
                                                return ret
                                            }
                                        }
                                        else->{

                                        }
                                    }
                                }
                            }
                        }
                        // return Pair(null,null)
                    }
                }
            }
            val ret = this.afterCreateObject(modelDataObject,useAccessControl,partnerCache)
            if(!ret.first){
                return Pair(null,"添加失败")
            }
            this.addCreateModelLog(modelDataObject,useAccessControl,partnerCache)
            return Pair(nID,null)
        } catch (ex:Exception){
            return Pair(null,ex.message)
        }
    }
//    protected open  fun beforeCreateCheck(modelData:ModelDataObject,
//                                          useAccessControl: Boolean,
//                                          partnerCache:PartnerCache?):Pair<Boolean,String?>{
//
//        return Pair(true,null)
//    }
    protected  open fun afterCreateObject(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                          useAccessControl:Boolean,
                                          pc:PartnerCache?):Pair<Boolean,String?>{
        return Pair(true,"")
    }
    protected open fun addCreateModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                         useAccessControl:Boolean,
                                         pc:PartnerCache?){

    }
    protected  open fun beforeEditCheck(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                        useAccessControl: Boolean,
                                        partnerCache:PartnerCache?):Pair<Boolean,String?>{

        if(useAccessControl || partnerCache!=null){
            if(partnerCache==null){
                return Pair(false,"权限接口没有提供操作用户信息")
            }
            this.cuFieldsProcessProxyModelField(modelDataObject,partnerCache,null)
            var ret = this.runEditFieldsCheckRules(modelDataObject,partnerCache)
            if(!ret.first){
                return ret
            }
            this.runEditFieldsFilterRules(modelDataObject,partnerCache)
        }
        return Pair(true,null)
    }



    open fun acEdit(modelData: dynamic.model.query.mq.ModelData,
                    criteria: dynamic.model.query.mq.ModelExpression?,
                    partnerCache:PartnerCache):Pair<Long?,String?>{
        if(modelData.isEmpty()){
            return Pair(0,"提交数据为空")
        }
        return this.safeEdit(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }



    open fun safeEdit(modelData: dynamic.model.query.mq.ModelData,
                      criteria: dynamic.model.query.mq.ModelExpression?=null,
                      useAccessControl: Boolean=false,
                      partnerCache:PartnerCache?=null):Pair<Long?,String?>{


        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager?.getTransaction(def)
        try {
            // var dependingModelFieldValueCollection
            if(modelData.isObject()){
                var ret= this.rawEdit(modelData.`as`(),
                        criteria=criteria,
                        useAccessControl=useAccessControl,
                        partnerCache = partnerCache)
                val tRet:Long=0
                if(ret.first!=null && ret.first!!>tRet){
                    txManager?.commit(status)
                    return ret
                }
                else{
                    txManager?.rollback(status)
                }
                return  Pair(0,ret.second)
            }
            else{

            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            logger.error(ex.message)
        }
        try {
            txManager?.rollback(status)
        }
        catch (ex:Exception)
        {
            logger.error(ex.message)
        }
        return Pair(0,"更新失败")
    }
    //todo add model role constraint
    open fun getACEditFieldValue(field: dynamic.model.query.mq.FieldBase, value:Any?, useAccessControl: Boolean, partnerCache: PartnerCache?, useDefault:Boolean=false): dynamic.model.query.mq.FieldValue?{
        return dynamic.model.query.mq.FieldValue(field, value)
    }

    open fun rawEdit(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                     criteria: dynamic.model.query.mq.ModelExpression?,
                     useAccessControl: Boolean=false,
                     partnerCache:PartnerCache?=null):Pair<Long?,String?>{


        var (result,errorMsg)=this.beforeEditCheck(modelDataObject,useAccessControl = useAccessControl,partnerCache = partnerCache)
        if(!result){
            return Pair(null,errorMsg)
        }
        return try {
            var tCriteria=criteria
            var idFV=modelDataObject.idFieldValue
            if(idFV!=null){
                var idCriteria=eq(idFV.field,idFV.value)
                tCriteria= if(tCriteria!=null) {
                    and(tCriteria,idCriteria!!)!!
                } else idCriteria
            }

            if(useAccessControl)
            {
                var acCriteria=null as dynamic.model.query.mq.ModelExpression?//partnerCache?.acGetEditCriteria(modelDataObject.model)
                if(acCriteria!=null){
                    tCriteria= if(tCriteria!=null) {
                        and(tCriteria, acCriteria)
                    } else acCriteria
                }
            }

            modelDataObject.data.forEach {
                when(it.field){
                    is dynamic.model.query.mq.Many2OneField,is dynamic.model.query.mq.One2OneField ->{
                        if(it.field is dynamic.model.query.mq.One2OneField && (it.field as One2OneField).isVirtualField){
                            return@forEach
                        }
                        if(it.value is dynamic.model.query.mq.ModelDataObject){
                            if((it.value as ModelDataObject).idFieldValue==null){
                                (it.value as ModelDataObject).context=modelDataObject.context
                                var id=((it.value as ModelDataObject).model as AccessControlModel?)?.rawCreate(it.value as ModelDataObject,
                                        useAccessControl,
                                        partnerCache)
                                if(id==null ||id.second!=null){
                                    return id?:Pair(null,"创建失败")
                                }
                                (it.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue((it.value as ModelDataObject)?.model?.fields?.getIdField()!!, id?.first))
                            }
                            else if((it.value as ModelDataObject).hasNormalField()){
                                (it.value as ModelDataObject).context=modelDataObject.context
                                var ret=((it.value as ModelDataObject).model as AccessControlModel?)?.rawEdit(it.value as ModelDataObject,null,useAccessControl,partnerCache)
                                if(ret==null ||ret.second!=null){
                                    return ret?:Pair(null,"更新失败")
                                }
                            }
                        }
                    }
                }
            }


            var fVCShadow= dynamic.model.query.mq.ModelDataObject(model = modelDataObject.model, fields = modelDataObject.fields)
            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {

                if((it is dynamic.model.query.mq.FunctionField<*,*>) || (it is dynamic.model.query.mq.ModelOne2ManyField)
                        ||(it is dynamic.model.query.mq.ModelMany2ManyField)){
                    return@forEach
                }
                val oit = it as dynamic.model.query.mq.ModelField
                if(oit.isIdField()){
                    return@forEach
                }
                var fv = modelDataObject.data.firstOrNull { fv->
                    fv.field.getFullName() == oit.getFullName()
                }
                if (fv != null) {
                    var acFV=this.getEditFieldValue(fv.field, fv.value,partnerCache,modelDataObject.data)
                    if(acFV!=null){
                        fVCShadow.setFieldValue(acFV.field,acFV.value)
                    }
                }
                else{
                    if(oit.defaultValue is FieldValueDependentingRecordBillboard){
                        var ret = (oit.defaultValue as FieldValueDependentingRecordBillboard).looked(modelDataObject.data, ActionType.EDIT)
                        if(ret.first){
                            fVCShadow.setFieldValue(oit,ret.second)
                        }
                    }
                }
            }

            var ret=this.update(fVCShadow,criteria = tCriteria)
            if(ret==null ||ret<1){
               return  Pair(null,"更新失败")
            }
            var mIDFV=modelDataObject.idFieldValue
            if(mIDFV!=null){
                modelDataObject.data.forEach {
                    fv->
                    when(fv.field){
                        is dynamic.model.query.mq.One2ManyField ->{
                            if(fv.value is dynamic.model.query.mq.ModelDataObject){
                                var tmf=this.getTargetModelField(fv.field)
                                if((fv.value as ModelDataObject).idFieldValue==null)
                                {
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    (fv.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, mIDFV.value))
                                    var o2m=(tmf?.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,
                                            useAccessControl,
                                            partnerCache)
                                    if (o2m==null || o2m.second!=null){
                                        return  Pair(null,"创建失败")
                                    }
                                }
                                else if((fv.value as ModelDataObject).hasNormalField()){
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    var ret=(tmf?.first as AccessControlModel?)?.rawEdit(fv.value as ModelDataObject,
                                            null,
                                            useAccessControl,
                                            partnerCache)
                                    if (ret==null || ret.second!=null){
                                        return  Pair(null,"更新失败")
                                    }
                                }
                            }
                            else if(fv.value is dynamic.model.query.mq.ModelDataArray){
                                var tmf=this.getTargetModelField(fv.field)
                                (fv.value as ModelDataArray).data.forEach {
                                    var tfvc= dynamic.model.query.mq.ModelDataObject(it, (fv.value as ModelDataArray).model)
                                    if(tfvc.idFieldValue==null){
                                        tfvc.context=modelDataObject.context
                                        tfvc.data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, mIDFV.value))
                                        var o2m=(tmf?.first as AccessControlModel?)?.rawCreate(tfvc,
                                                useAccessControl,
                                                partnerCache)
                                        if (o2m==null || o2m.second!=null){
                                            return  Pair(null,"创建失败")
                                        }
                                    }
                                    else if(tfvc.hasNormalField())
                                    {
                                        tfvc.context=modelDataObject.context
                                        var ret=(tmf?.first as AccessControlModel?)?.rawEdit(tfvc,
                                                null,
                                                useAccessControl,
                                                partnerCache)
                                        if (ret==null || ret.second!=null){
                                            return  Pair(null,"更新失败")
                                        }
                                    }
                                }
                            }
                        }
                       is dynamic.model.query.mq.One2OneField ->{
                            if((fv.field as One2OneField).isVirtualField && (fv.value is dynamic.model.query.mq.ModelDataObject)){
                                if((fv.value as ModelDataObject).idFieldValue==null){
                                    var tmf=this.getTargetModelField(fv.field)
                                    (fv.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, mIDFV.value))
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    var o2o=(tmf?.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,
                                            useAccessControl,
                                            partnerCache)
                                    if (o2o==null || o2o.second!=null){
                                        return  Pair(null,"创建失败")
                                    }
                                }
                                else if((fv.value as ModelDataObject).hasNormalField()){
                                    var tmf=this.getTargetModelField(fv.field)
                                    (fv.value as ModelDataObject).data.add(dynamic.model.query.mq.FieldValue(tmf?.second!!, mIDFV.value))
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    var o2o=(tmf?.first as AccessControlModel?)?.rawEdit(fv.value as ModelDataObject,
                                            criteria=null,
                                            useAccessControl = useAccessControl,
                                            partnerCache = partnerCache)
                                    if (o2o==null || o2o.second!=null){
                                        return  Pair(null,"更新失败")
                                    }
                                }
                            }
                        }
                        is ConstRelRegistriesField->{
                            when(fv.value){
                                is dynamic.model.query.mq.ModelDataSharedObject ->{
                                    for( kv in (fv.value as ModelDataSharedObject).data) {
                                        when(kv.value){
                                            is dynamic.model.query.mq.ModelDataObject ->{
                                                var mfvc= kv.value as dynamic.model.query.mq.ModelDataObject
                                                mfvc.context=modelDataObject.context
                                                var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                if(tField!=null && mfvc.idFieldValue==null){
                                                    mfvc.context=modelDataObject.context
                                                    mfvc.data.add(dynamic.model.query.mq.FieldValue(tField, mIDFV.value))
                                                    var ret=(mfvc.model as AccessControlModel?)?.rawCreate(mfvc,useAccessControl,partnerCache)
                                                    if(ret==null || ret.second!=null){
                                                        return Pair(null,"创建失败")
                                                    }
                                                }
                                                else if(mfvc.idFieldValue!=null && mfvc.hasNormalField()){
                                                    mfvc.context=modelDataObject.context
                                                    var ret=(mfvc.model as AccessControlModel?)?.rawEdit(mfvc,null,useAccessControl,partnerCache)
                                                    if(ret==null || ret.second!=null){
                                                        return Pair(null,"更新失败")
                                                    }
                                                }
                                            }
                                            is dynamic.model.query.mq.ModelDataArray ->{
                                                var mmfvc= kv.value as dynamic.model.query.mq.ModelDataArray
                                                for(mkv in mmfvc.data){
                                                    var mfvc= dynamic.model.query.mq.ModelDataObject(mkv, mmfvc.model)
                                                    mfvc.context=modelDataObject.context
                                                    var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                    if(tField!=null && mfvc.idFieldValue==null){
                                                        mfvc.data.add(dynamic.model.query.mq.FieldValue(tField, mIDFV.value))
                                                        var ret=(mfvc.model as AccessControlModel?)?.rawCreate(mfvc,useAccessControl,partnerCache)
                                                        if(ret==null || ret.second!=null){
                                                            return Pair(null,ret?.second)
                                                        }
                                                    }
                                                    else if(mfvc.idFieldValue!=null && mfvc.hasNormalField()){
                                                        mfvc.context=modelDataObject.context
                                                        var ret=(mfvc.model as AccessControlModel?)?.rawEdit(mfvc,null,useAccessControl,partnerCache)
                                                        if(ret==null || ret.second!=null){
                                                            return Pair(null,"更新失败")
                                                        }
                                                    }
                                                }
                                            }
                                            else->{

                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            var afterRet=this.afterEditObject(modelDataObject,useAccessControl,partnerCache)
            if(!afterRet.first){
                return Pair(0,afterRet.second)
            }
            this.addEditModelLog(modelDataObject,useAccessControl,partnerCache)
            Pair(ret,null)
        } catch (ex:Exception){
            ex.printStackTrace()
            Pair(null,ex.message)
        }
    }
    protected  open fun afterEditObject(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                        useAccessControl:Boolean,
                                        pc:PartnerCache?):Pair<Boolean,String?>{
        return Pair(true,"")
    }


    protected open fun addEditModelLog(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                                       useAccessControl:Boolean,
                                       pc:PartnerCache?){

    }
    protected  open fun beforeDeleteCheck(modelData: dynamic.model.query.mq.ModelDataObject,
                                          criteria: dynamic.model.query.mq.ModelExpression?,
                                          useAccessControl: Boolean,
                                          partnerCache:PartnerCache?):Pair<Boolean,String?>{
        val model = modelData.model
        model?.let {
            var modelRule = partnerCache?.getModelRule(model.meta.appName,model.meta.name)
            modelRule?.let {
                if(it.deleteAction.enable=="false"){
                    return Pair(false,"无删除权限")
                }
            }
        }

        if(partnerCache!=null){
            var ret= modelDeleteFieldsBelongToPartnerCheck(modelData,partnerCache,null)
            if(!ret.first){
                return ret
            }
            var ruleTypes = partnerCache.getModelDeleteAccessControlRules<ModelDeleteAccessControlRule<*>>(model!!)
            ruleTypes?.forEach {
                ret=it(modelData,partnerCache,null)
                if(!ret.first){
                    return ret
                }
            }
        }
        return Pair(true,null)
    }

    open fun acDelete(modelData: dynamic.model.query.mq.ModelData,
                      criteria: dynamic.model.query.mq.ModelExpression?,
                      partnerCache:PartnerCache?):Pair<Long?,String?>{

        return this.safeDelete(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }

    open fun acDelete(criteria: dynamic.model.query.mq.ModelExpression?,
                      partnerCache:PartnerCache?):Pair<Long?,String?>{
        val modelData = dynamic.model.query.mq.ModelDataObject(model = this)
        return this.safeDelete(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }

    open fun safeDelete(modelData: dynamic.model.query.mq.ModelData,
                        criteria: dynamic.model.query.mq.ModelExpression?,
                        useAccessControl: Boolean=false,
                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{

        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager?.getTransaction(def)
        try {
            // var dependingModelFieldValueCollection
            if(modelData.isObject()){
                var ret= this.rawDelete(modelData.`as`(),
                        criteria=criteria,
                        useAccessControl=useAccessControl,
                        partnerCache = partnerCache)
                txManager?.commit(status)
                return  ret
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            logger.error(ex.message)
        }
        try {
            txManager?.rollback(status)
        }
        catch (ex:Exception)
        {
            logger.error(ex.message)
        }
        return Pair(0,"更新失败")
    }

    open fun rawDelete(criteria: dynamic.model.query.mq.ModelExpression?,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        return this.rawDelete(dynamic.model.query.mq.ModelDataObject(model = this),criteria,useAccessControl,partnerCache)
    }
    open fun rawDelete(modelDataObject: dynamic.model.query.mq.ModelDataObject,
                       criteria: dynamic.model.query.mq.ModelExpression?,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        if(useAccessControl && partnerCache==null){
            return Pair(null,"must login")
        }
        var (result,errorMsg)=this.beforeDeleteCheck(modelDataObject,criteria = criteria,useAccessControl = useAccessControl,partnerCache = partnerCache)
        if(!result){
            return Pair(null,errorMsg)
        }

//        if(useAccessControl){
//            var rules=partnerCache?.acGetDeleteRules(modelDataObject.model)
//            rules?.forEach {
//                var (ok,errorMsg)=it.check(modelDataObject,context = partnerCache?.modelExpressionContext!!)
//                if(!ok){
//                    return Pair(null,errorMsg)
//                }
//            }
//        }


        return try {
            var tCriteria=criteria
            var idFV=modelDataObject.idFieldValue
            if(idFV!=null){
                var idCriteria=eq(idFV.field,idFV.value)
                tCriteria= if(tCriteria!=null) {
                    and(tCriteria,idCriteria!!)!!
                } else idCriteria
            }

            if(useAccessControl)
            {
                var acCriteria=null as dynamic.model.query.mq.ModelExpression?//partnerCache?.acGetEditCriteria(modelDataObject.model)
                if(acCriteria!=null){
                    tCriteria= if(tCriteria!=null) {
                        and(tCriteria,acCriteria)!!
                    } else acCriteria
                }
            }

            Pair(this.delete(modelDataObject,criteria = tCriteria),null)
        } catch (ex:Exception){
            Pair(null,ex.message)
        }
    }

    open fun rawCount(fieldValueArray: dynamic.model.query.mq.FieldValueArray, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Int{
        var expArr = fieldValueArray.map {
            eq(it.field,it.value)!!
        }.toTypedArray()
        var expressions = and(*expArr)
        var (expressions2,_) = this.beforeRead(criteria = expressions,model=this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).count().where(expressions2)
        return this.queryCount(statement)
    }

    open fun rawCount(criteria: dynamic.model.query.mq.ModelExpression?, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Int{
        val (c,_)=this.beforeRead(criteria = criteria,model = this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).count().where(c)
        return this.queryCount(statement)
    }
    open fun acCount(criteria: dynamic.model.query.mq.ModelExpression?, partnerCache:PartnerCache):Int{
        return this.rawCount(criteria,partnerCache,true)
    }

    open fun rawMax(field: dynamic.model.query.mq.FieldBase, criteria: dynamic.model.query.mq.ModelExpression?=null, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Long?{
        val (c,_)=this.beforeRead(field,criteria = criteria,model = this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).max(dynamic.model.query.mq.aggregation.MaxExpression(field)).where(c)
        return this.queryMax(statement)
    }

    open fun acMax(field: dynamic.model.query.mq.FieldBase, partnerCache:PartnerCache, criteria: dynamic.model.query.mq.ModelExpression?=null):Long?{
        return this.rawMax(field,criteria,partnerCache,true)
    }

}