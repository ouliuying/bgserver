

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
    protected  lateinit var deleteCorpIsolationBean: ModelDeleteCorpIsolationBean

    @Autowired
    protected lateinit var editCorpIsolationBean:ModelEditCorpIsolationBean
    @Autowired
    lateinit var gson: Gson
    /*Corp Isolation Fields Begin*/
    val createTime= ModelField(null, "create_time", FieldType.DATETIME, "添加时间", defaultValue = TimestampBillboard(constant = true))
    val lastModifyTime= ModelField(null, "last_modify_time", FieldType.DATETIME, "最近修改时间", defaultValue = TimestampBillboard())
    val createPartnerID= ModelField(null, "create_partner_id", FieldType.BIGINT, "添加人", defaultValue = CurrPartnerBillboard(true))
    val lastModifyPartnerID= ModelField(null, "last_modify_partner_id", FieldType.BIGINT, "最近修改人", defaultValue = CurrPartnerBillboard())
    val createCorpID= ModelField(null, "create_corp_id", FieldType.BIGINT, "添加公司", defaultValue = CurrCorpBillboard(true))
    val lastModifyCorpID= ModelField(null, "last_modify_corp_id", FieldType.BIGINT, "最近修改公司", defaultValue = CurrCorpBillboard())
    /*Corp Isolation Fields End*/



    init {

    }
    open fun corpIsolationFields():Array<ModelField>?{
        return arrayOf(
                createTime,
                lastModifyTime,
                createPartnerID,
                lastModifyPartnerID,
                createCorpID,
                lastModifyCorpID
        )
    }



    open fun maybeCheckACRule():Boolean{

        return true
    }

    fun acRead(vararg fields: FieldBase,
               model:AccessControlModel?=null,
               criteria: ModelExpression?,
               partnerCache:PartnerCache,
               orderBy: OrderBy?=null,
               pageIndex:Int?=null,
               pageSize:Int?=null,
               attachedFields:Array<AttachedField>?=null,
               relationPaging:Boolean=false): ModelDataArray?{

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

            var acCriteria=null as ModelExpression?
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
    open fun smartReconcileCriteria(criteria: ModelExpression?): ModelExpression?{
        return criteria
    }
    open fun beforeRead(vararg queryFields: FieldBase,
                        criteria: ModelExpression?,
                        model:ModelBase,
                        useAccessControl:Boolean,
                        partnerCache: PartnerCache?=null,
                        joinModels:Array<dynamic.model.query.mq.join.JoinModel>?=null):Pair<ModelExpression?,Array<FieldBase>>{
        var ruleCriteria=criteria
        var newQueryFields = arrayListOf<FieldBase>()
        if (useAccessControl && partnerCache!=null){
            var models = arrayListOf<ModelBase>(model)
            joinModels?.let {
                it.forEach { sit->
                    sit.model?.let {
                        models.add(it)
                    }
                }
            }
            ruleCriteria = this.readCorpIsolation(model,partnerCache,ruleCriteria)
            models.forEach {
                if(partnerCache.checkReadBelongToPartner(model)){
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
    protected  open fun getModelEditAccessFieldFilterRule():ModelEditRecordFieldsValueFilterRule<*,*>?{
        return null
    }
    protected  open fun getModelCreateAccessFieldFilterRule():ModelCreateRecordFieldsValueFilterRule<*>?{
        return null
    }
    open fun filterAcModelFields(fields:Array<FieldBase>, model:ModelBase, partnerCache: PartnerCache?):Array<FieldBase>{
        if(partnerCache!=null){
            var rFields = arrayListOf<FieldBase>()
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
                return rFields.toArray() as Array<FieldBase>
            }
        }
        return fields
    }
    private fun sortFields(model:ModelBase, targetFields:ArrayList<FieldBase>,
                           fs:ArrayList<FieldBase>,
                           o2ofs:ArrayList<FieldBase>,
                           o2mfs:ArrayList<AttachedField>,
                           m2ofs:ArrayList<FieldBase>,
                           m2mfs:ArrayList<AttachedField>,
                           ownerMany2OneFields:ArrayList<ModelMany2OneField>){
        targetFields.forEach {
            when(it){
                is One2OneField ->{
                    o2ofs.add(it)
                }
                is One2ManyField ->{
                    o2mfs.add(AttachedField(it))
                }
                is Many2OneField ->{
                    val tf = this.getTargetModelField(it)
                    val ret = tf?.first?.isSame(model)
                    if(ret==null || !ret){
                        m2ofs.add(it)
                    }
                    else{
                        fs.add(it)
                        ownerMany2OneFields.add(it as ModelMany2OneField)
                    }
                }
                is Many2ManyField ->{
                    m2mfs.add(AttachedField(it))
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
    open fun rawRead(vararg fields: FieldBase,
                     model:AccessControlModel?=null,
                     criteria: ModelExpression?,
                     orderBy: OrderBy?=null,
                     pageIndex:Int?=null,
                     pageSize:Int?=null,
                     attachedFields:Array<AttachedField>?=null,
                     relationPaging:Boolean=false,
                     useAccessControl: Boolean=false,
                     partnerCache:PartnerCache?=null): ModelDataArray?{
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
        var fs=ArrayList<FieldBase>()
        var o2ofs=ArrayList<FieldBase>()
        var o2mfs=ArrayList<AttachedField>()
        var m2ofs=ArrayList<FieldBase>()
        var m2mfs=ArrayList<AttachedField>()
        var ownerMany2OneFields = ArrayList<ModelMany2OneField>()
        if(fields.isEmpty()){
            var pFields= model.fields.getAllPersistFields().values.toTypedArray()
            if(partnerCache!=null){
                pFields=this.filterAcModelFields(pFields,model=this,partnerCache=partnerCache)
            }
            pFields.let {
                sortFields(model, arrayListOf(*pFields),fs,o2ofs,o2mfs,m2ofs,m2mfs,ownerMany2OneFields)
            }
        }
        else{
            var pFields: Array<FieldBase>?= fields as Array<FieldBase>?
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
            (it.field as FieldBase).getFullName()
        }.toTypedArray()))
        m2mfs= arrayListOf(*(m2mfs.distinctBy {
            (it.field as FieldBase).getFullName()
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
                    var o2oFields= mf.first.fields.getAllPersistFields(true).values.toTypedArray()//partnerCache?.acFilterReadFields(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(o2oFields!=null){
                        fs.addAll(o2oFields)
                    }
                    else{
                        return@forEach
                    }
                }
                else{
                    fs.addAll(mf.first.fields.getAllPersistFields(true).values.toTypedArray())
                }
                var o2oFd= it as ModelOne2OneField
                if(o2oFd.isVirtualField){
                    var idf=o2oFd.model?.fields?.getIdField()
                    modelRelationMatcher.addMatchData(model,o2oFd, mf.first, mf.second,idf)
                    joinModels.add(leftJoin(mf.first, eq(mf.second, idf)))
                }
                else{
                    modelRelationMatcher.addMatchData(model,o2oFd, mf.first, mf.second)
                    joinModels.add(leftJoin(mf.first, eq(mf.second, it)))
                }
            }
        }

        m2ofs.forEach{
            var mf=this.getTargetModelField(it)
            if(mf!=null){
                if(useAccessControl){
                    var m2oFields= mf.first.fields.getAllPersistFields(true).values.toTypedArray()//partnerCache?.acFilterReadFields(mf?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(m2oFields!=null){
                        fs.addAll(m2oFields)
                    }
                    else{
                        return@forEach
                    }
                }
                else{
                    fs.addAll(mf.first.fields.getAllPersistFields(true).values.toTypedArray())
                }
                modelRelationMatcher.addMatchData(model,it, mf.first, mf.second)
                joinModels.add(leftJoin(mf.first, eq(mf.second, it)))
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
                newOrderBy= model.fields.getDefaultOrderBy()
            }
        }
        var (readCriteria,postFS) = this.beforeRead(*fs.toTypedArray(),
                criteria=criteria,
                model=this,
                useAccessControl = useAccessControl,
                partnerCache = partnerCache,
                joinModels = joinModels.toTypedArray())

        var mDataArray=this.query(*postFS,
                fromModel = model,
                joinModels = joinModels.toTypedArray(),
                criteria = readCriteria,
                orderBy = newOrderBy,
                offset = offset,limit = limit)


        mDataArray?.model=model
        mDataArray=this.reconstructSingleRelationModelRecordSet(mDataArray,modelRelationMatcher)

        var rmfs= mutableMapOf<String,MutableList<AttachedField>>()
        m2mfs.forEach {
            val field=it.field as RefRelationField
            if(field.relationModelTable!=null){
                if(rmfs.containsKey(field.relationModelTable!!)){
                    rmfs[field.relationModelTable!!]?.add(it)
                }
                else{
                    var mlst= mutableListOf<AttachedField>()
                    mlst.add(it)
                    rmfs[field.relationModelTable!!]=mlst
                }
            }
        }

        var to2mfs=ArrayList<AttachedField>()
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
            if(it.field is Many2ManyField){
                var rrf=it.field as RefRelationField
                if(rrf.relationModelTable!=null){
                    if(rmfs.containsKey(rrf.relationModelTable!!)){
                        var fList=rmfs[rrf.relationModelTable!!]
                        if(fList!!.filter {rt-> (rt.field as FieldBase).getFullName()==(rrf as FieldBase).getFullName() }.count()<1){
                            fList.add(it)
                        }
                        else{
                            fList.removeIf { xit ->
                                (xit.field as FieldBase).isSame(rrf as FieldBase)
                            }
                            fList.add(it)
                        }
                    }
                    else{
                        var mlst= mutableListOf<AttachedField>()
                        mlst.add(it)
                        rmfs[rrf.relationModelTable!!]=mlst
                    }
                }
            }
            else if(it.field is One2ManyField){
                var rtf= it.field
                if(o2mfs.filter { rt-> (rt.field as FieldBase).getFullName()==(it.field as FieldBase).getFullName() }.count()<1)
                {
                    o2mfs.add(it)
                }
                else{
                    o2mfs.removeIf { xit ->
                        (xit.field as FieldBase).isSame(rtf as FieldBase)
                    }
                    o2mfs.add(it)
                }
            }
        }


        rmfs.forEach {
            modelRelationMatcher = ModelRelationMatcher()
            var rmf=model.getRelationModelField(it.value.first().field as FieldBase)
            var idField= model.fields.getIdField()
            var rIDField=rmf?.first?.fields?.getFieldByTargetField(idField)
            var subSelect=select(idField!!,fromModel = model).where(readCriteria).orderBy(newOrderBy).offset(offset).limit(limit)
            var rtFields=ArrayList<FieldBase>()
            rtFields.addAll(rmf?.first?.fields?.getAllPersistFields(true)?.values!!)
            modelRelationMatcher.addMatchData(model,idField, rmf.first,rIDField)
            var joinModels=ArrayList<dynamic.model.query.mq.join.JoinModel>()
            it.value.forEach allField@{rrf->
                val relationMF = model.getRelationModelField(rrf.field as FieldBase)?:return@allField
                val targetMF= model.getTargetModelField(rrf.field as FieldBase)?:return@allField
                var jField= relationMF.first.fields.getFieldByTargetField(targetMF.second) ?:return@allField
                if(useAccessControl){
                    //var rmfFields=partnerCache?.acFilterReadFields(sRmf?.first?.fields?.getAllPersistFields()?.values?.toTypedArray()!!)
                    var targetMFFields= targetMF.first.fields.getAllPersistFields(true).values.toTypedArray()
                    if(targetMFFields!=null){
                        //rtFields.addAll(rmfFields)
                        rtFields.addAll(targetMFFields)
                    }
                    else{
                        return@allField
                    }
                }else{
                    rtFields.addAll(targetMF.first.fields.getAllPersistFields(true).values)
                }
                modelRelationMatcher.addMatchData(rmf.first,
                        relationMF.second,
                        targetMF.first,
                        targetMF.second)
                if(rrf.canBeEmpty){
                    joinModels.add(leftJoin(targetMF.first, eq(jField, targetMF.second)))
                }
                else{
                    joinModels.add(innerJoin(targetMF.first, eq(jField, targetMF.second)))
                }
            }

            var rOrderBy=null as OrderBy?
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
                var mLst= mutableListOf<ModelExpression>()
                attachedCriteriaArr.forEach {mIt->
                    mLst.add(mIt!!)
                }
                mLst.add(subCriteria)
                subCriteria=and(*mLst.toTypedArray())
            }


            var (readCriteria,postFS) = (rmf.first as AccessControlModel).beforeRead(*rtFields.toTypedArray(),
                    criteria=subCriteria,
                    model= rmf.first,
                    useAccessControl = useAccessControl,
                    partnerCache = partnerCache,
                    joinModels = joinModels.toTypedArray())

            var mrDataArray=(rmf.first as AccessControlModel).query(*postFS,
                    fromModel= rmf.first,
                    joinModels=joinModels.toTypedArray(),
                    criteria=readCriteria,
                    orderBy = rOrderBy,
                    offset = rOffset,
                    limit = rLimit)
            var fieldArr=it.value.stream().map { x->x.field }.toList() as List<FieldBase>
            mDataArray=reconstructMultipleRelationModelRecordSet(model,
                    fieldArr.toTypedArray(),
                    mDataArray,rmf.first,
                    postFS,
                    mrDataArray,
                    modelRelationMatcher)
        }


        o2mfs.forEach {
            modelRelationMatcher = ModelRelationMatcher()
            var targetMF=this.getTargetModelField(it.field as FieldBase)
            if(targetMF!=null){
                var subSelect=select(model.fields.getIdField()!!,fromModel = model).where(readCriteria).orderBy(newOrderBy).offset(offset).limit(limit)

                var rOrderBy=null as OrderBy?
                var rOffset=null as Int?
                var rLimit=null as Int?
                //todo add support pagesize every field
//                if(relationPaging && (it is PagingField)){
//                    rOrderBy=targetMF?.first?.fields?.getDefaultOrderBy()
//                    rOffset=0
//                    rLimit=it.pageSize
//                }
                if(useAccessControl){
                    var targetMFFields= targetMF.first.fields.getAllPersistFields(true).values.toTypedArray()//partnerCache?.acFilterReadFields(targetMF?.first?.fields?.getAllPersistFields(true)?.values?.toTypedArray()!!)
                    if(targetMFFields!=null){

                        modelRelationMatcher.addMatchData(model,it.field as FieldBase, targetMF.first, targetMF.second, model.fields.getIdField())
                        var subCriteria=selectIn(targetMF.second,subSelect)
                        if(it.criteria!=null){
                            subCriteria=and(subCriteria, it.criteria!!)
                        }

                        var (readCriteria,postFS) = (targetMF.first as AccessControlModel).beforeRead(*targetMFFields,
                                criteria=subCriteria,
                                model= targetMF.first,
                                useAccessControl = useAccessControl,
                                partnerCache = partnerCache)

                        var mrDataArray=(targetMF.first as AccessControlModel).query(*postFS,
                                fromModel= targetMF.first,
                                criteria=readCriteria,
                                orderBy = rOrderBy,
                                offset = rOffset,
                                limit = rLimit)

                        mDataArray=reconstructMultipleRelationModelRecordSet(model,
                                arrayOf(it.field as FieldBase),
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
                    modelRelationMatcher.addMatchData(model,it.field as FieldBase, targetMF.first, targetMF.second, model.fields.getIdField())
                    var subCriteria=selectIn(targetMF.second,subSelect)
                    if(it.criteria!=null){
                        subCriteria=and(subCriteria, it.criteria!!)
                    }
                    var targetMFFields= targetMF.first.fields.getAllPersistFields(true).values.toTypedArray()
                    var (readCriteria,postFS) = (targetMF.first as AccessControlModel).beforeRead(*targetMFFields,
                            criteria=subCriteria,
                            model= targetMF.first,
                            useAccessControl = useAccessControl,
                            partnerCache = partnerCache)

                    var mrDataArray=(targetMF.first as AccessControlModel).query(*postFS,fromModel= targetMF.first,criteria=readCriteria,
                            orderBy = rOrderBy,offset = rOffset,limit = rLimit)

                    mDataArray= reconstructMultipleRelationModelRecordSet(model,
                            arrayOf(it.field as FieldBase),
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
                            var criteria=eq(model.fields.getIdField()!!,v)
                            var (readCriteria,postFS) = this.beforeRead(*fs.toTypedArray(),
                                    criteria=criteria,
                                    model=this,
                                    useAccessControl = useAccessControl,
                                    partnerCache = partnerCache,
                                    joinModels = joinModels.toTypedArray())

                            v=this.query(*postFS,
                                    fromModel = model,
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
    fun getRelationFieldTo(model: FieldBase): FieldBase?{



        return null
    }
    protected open fun doFillModelFunctionFields(modelDataArray: ModelDataArray?, useAccessControl: Boolean, partnerCache: PartnerCache?){
        val model=modelDataArray?.model
        val functions = model?.fields?.getTypeFields<FunctionField<*,*>>()?.values?.toTypedArray()?: arrayOf()
        modelDataArray?.data?.forEach {
            this.doFillFieldValueArrayFunctionFields(*functions,fieldValueArray = it,useAccessControl = useAccessControl,partnerCache = partnerCache)
        }
    }
    protected open fun doFillModelFunctionFields(modelDataObject: ModelDataObject?, useAccessControl: Boolean, partnerCache: PartnerCache?){
        val model=modelDataObject?.model
        val functions = model?.fields?.getTypeFields<FunctionField<*,*>>()?.values?.toTypedArray()?: arrayOf()
        modelDataObject?.let {
            this.doFillFieldValueArrayFunctionFields(*functions,fieldValueArray = modelDataObject.data,
                    useAccessControl = useAccessControl,
                    partnerCache = partnerCache)
        }
    }
    private fun doFillFieldValueArrayFunctionFields(vararg functionFields: FieldBase?, fieldValueArray: FieldValueArray, useAccessControl: Boolean, partnerCache: PartnerCache?){
        functionFields.forEach {
            if(it is FunctionField<*,*>){

                val compValue = (it as FunctionField<*,PartnerCache>).compute(fieldValueArray,partnerCache,null)
                fieldValueArray.setValue(it,compValue)
            }
        }
        fieldValueArray.forEach {
            when {
                it.field is ProxyRelationModelField<*,*> -> (it.field as ProxyRelationModelField<*,PartnerCache>).inverse(fieldValueArray,partnerCache,null,null)
                it.value is ModelDataObject -> this.doFillModelFunctionFields(it.value as ModelDataObject,useAccessControl,partnerCache)
                it.value is ModelDataArray ->  this.doFillModelFunctionFields(it.value as ModelDataArray,useAccessControl,partnerCache)
                it.value is ModelDataSharedObject -> (it.value as ModelDataSharedObject).data.forEach { _, u ->
                    when(u){
                        is ModelDataObject ->{
                            this.doFillModelFunctionFields(u,useAccessControl,partnerCache)
                        }
                        is ModelDataArray ->{
                            this.doFillModelFunctionFields(u,useAccessControl,partnerCache)
                        }
                    }
                }
            }
        }
    }
    protected  open fun reconstructMultipleRelationModelRecordSet(model:ModelBase?,
                                                                  fields:Array<FieldBase>,
                                                                  reqMainArray: ModelDataArray?,
                                                                  relModel:ModelBase?,
                                                                  targetFields:Array<FieldBase>,
                                                                  relDataArray: ModelDataArray?,
                                                                  modelRelationMatcher: ModelRelationMatcher): ModelDataArray?{
        var mainArray = ModelDataArray(model = model, fields = reqMainArray?.fields)
        val idField = model?.fields?.getIdField()?: return reqMainArray
        reqMainArray?.data?.forEach {
            var mId = it.getValue(idField) as Long
            var fvs = FieldValueArray()
            fvs.addAll(it)
            if(relModel!=null){ //m2m
                val field = relModel.fields.getFieldByTargetField(idField)
                val relArray = this.readM2MModelDataArrayFromMultiModelDataArray(relModel,field!!,mId,fields,relDataArray)
                var mds = (fvs.getValue(ConstRelRegistriesField.ref) as ModelDataSharedObject?)?: ModelDataSharedObject()
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
                                                              field: FieldBase,
                                                              fieldValue:Long,
                                                              relFields:Array<FieldBase>,
                                                              dataArray: ModelDataArray?): ModelDataArray?{

        var relReadFields = this.getModelFieldsFromMultiDataArray(relModel,dataArray)
        var relDataArray = ModelDataArray(model = relModel, fields = relReadFields)
        field.let {
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
                        relFieldValue.setValue(rf.second, ModelDataObject(fields = tReadFields, data = tFV, model = tf?.first))
                    }
                }
            }
        }
        return relDataArray
    }
    private fun readOneModelFieldValueFromMultiModelFieldValue(modelFields:ArrayList<FieldBase>, multiModelFieldValue: FieldValueArray): FieldValueArray {
        var n = FieldValueArray()
        modelFields.forEach {
            if(multiModelFieldValue.containFieldKey(it)){
                n.setValue(it,multiModelFieldValue.getValue(it))
            }
        }
        return n
    }
    private  fun readO2MModelDataArrayFromMultiModelDataArray(model:ModelBase?,
                                                              field: FieldBase?,
                                                              fieldValue:Long,
                                                              dataArray: ModelDataArray?): ModelDataArray?{
        var fields = this.getModelFieldsFromMultiDataArray(model,dataArray)
        var modelDataArray= ModelDataArray(model = model, fields = fields)
        field?.let {
            dataArray?.data?.filter {
                (it.getValue(field) as Long)==fieldValue
            }?.forEach { fv->
                var nFv = FieldValueArray()
                fields.forEach {
                    val v =fv.getValue(it)
                    nFv.setValue(it,v)
                }
                modelDataArray.data.add(nFv)
            }
        }
        return modelDataArray
    }

    private  fun getModelFieldsFromMultiDataArray(model:ModelBase?,dataArray: ModelDataArray?):ArrayList<FieldBase>{
        var fields = arrayListOf<FieldBase>()
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

    private fun setOrReplaceFieldValueArrayItem(fVArr: FieldValueArray, field: FieldBase, value:Any?){
        var index=fVArr.indexOfFirst {
            it.field.isSame(field)
        }
        if(index>-1){
            fVArr[index]= FieldValue(field, value)
        }
        else{
            fVArr.add(FieldValue(field, value))
        }
    }
    protected open fun reconstructSingleRelationModelRecordSet(mDataArray: ModelDataArray?,
                                                               modelRelationMatcher: ModelRelationMatcher): ModelDataArray?{
        var mainModel = mDataArray?.model
        var mainModelFields=ArrayList<FieldBase>()
        var subModels= mutableMapOf<ModelBase?, ModelDataObject>()
        mDataArray?.fields?.forEach {
            if(mainModel!=it.model){
               if(subModels.contains(it.model)){
                   subModels[it.model]?.fields?.add(it)
               }
               else
               {
                   var fields=ArrayList<FieldBase>()
                   fields.add(it)
                   var mrDataObject= ModelDataObject(fields = fields, model = it.model)
                   var mfd=modelRelationMatcher.getRelationMatchField(mainModel,it.model)
                   mrDataObject.fromField=mfd?.fromField
                   subModels[it.model]=mrDataObject
               }
            }
            else{
                mainModelFields.add(it)
            }
        }
        var mainModelDataArray = ModelDataArray(fields = mainModelFields, model = mainModel)
        mDataArray?.data?.forEach { fvArr->
            var mainRecord= FieldValueArray()
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
                var subRecord = FieldValueArray()
                it.fields?.forEach {fb->
                    var fv=fvArr.firstOrNull {sf->
                        sf.field.isSame(fb)
                    }
                    if(fv!=null){
                        subRecord.add(fv)
                    }
                }
                mainModelDataArray.fields?.add(it.fromField!!)
                var cloneModelObject= ModelDataObject(data = subRecord, model = it.model)
                mainRecord.add(FieldValue(it.fromField!!, cloneModelObject))
            }
            mainModelDataArray.data.add(mainRecord)
        }
        return mainModelDataArray
    }

    protected open fun getRelationModelField(field: FieldBase):Pair<ModelBase, FieldBase>?{
        if((field is Many2ManyField)){
            var model= this.appModel.getModel((field as RefRelationField).relationModelTable) ?:return null
            var mField= model.fields.getField((field as RefRelationField).relationModelFieldName) ?:return null
            return Pair(model,mField)
        }
        return null
    }
    protected  open fun getTargetModelField(field: FieldBase):Pair<ModelBase, FieldBase>?{
        if(field is RefTargetField){
            var model= this.appModel.getModel(field.targetModelTable) ?:return null
            var mField= model.fields.getField(field.targetModelFieldName) ?:return null
            return Pair(model,mField)
        }
        return null
    }




    fun acCreate(modelData: ModelData,
                 partnerCache:PartnerCache):Pair<Long?,String?>{
            return this.safeCreate(modelData,useAccessControl=true,partnerCache = partnerCache)
    }

    open fun safeCreate(modelData: ModelData,
                        useAccessControl: Boolean=false,
                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        var errorMessage="添加失败" as String?
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager.getTransaction(def)
        try {
           // var dependingModelFieldValueCollection
            if(modelData.context==null){
                modelData.createContext()
            }
            var (id,errMsg)=(modelData.model as AccessControlModel)
                    .rawCreate(modelData,useAccessControl,partnerCache)
            if(id!=null && id>0){
                txManager.commit(status)
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
            txManager.rollback(status)
        }
        catch(ex:Exception)
        {
            errorMessage=ex.message
            logger.error(ex.message)
        }
        return Pair(0,errorMessage)
    }

    open fun getCreateFieldValue(field: FieldBase, value:Any?, partnerCache:PartnerCache?=null, fvs: FieldValueArray?=null): FieldValue?{
            return when (field) {
                is ProxyRelationModelField<*,*> -> null
                else -> when(value){
                    is ModelDataObject ->{
                        return if(value.idFieldValue!=null){
                            FieldValue(field, value.idFieldValue?.value)
                        } else{
                            null//FieldValue(field,null)
                        }
                    }
                    is FieldDefaultValueBillboard ->{
                        return when(value){
                            is CurrCorpBillboard-> FieldValue(field, value.looked(partnerCache))
                            is CurrPartnerBillboard-> FieldValue(field, value.looked(partnerCache))

                            else-> FieldValue(field, value.looked(null))
                        }
                    }
                    is FieldValueDependentingRecordBillboard ->{
                        val ret = value.looked(fvs, ActionType.CREATE)
                        return if(ret.first){
                            FieldValue(field, ret.second)
                        } else{
                            null
                        }
                    }
                    else->{
                        return if(value!=null) FieldValue(field, value) else null
                    }
                }
            }

    }


    open fun getEditFieldValue(field: FieldBase, value:Any?, partnerCache:PartnerCache?=null, fvs: FieldValueArray?=null): FieldValue?{
        return when (field) {
            is ProxyRelationModelField<*,*> -> null
            else -> when(value){
                is ModelDataObject ->{
                    value.idFieldValue?.let {
                        it.value?.let {
                            val lz:Long=0
                            return if((it as BigInteger).toLong()!=lz) FieldValue(field, it) else null
                        }
                    }
                    return null
                }
                is FieldDefaultValueBillboard ->{
                   null
                }
                is FieldValueDependentingRecordBillboard ->{
                    var ret = value.looked(fvs, ActionType.EDIT)
                    return if(ret.first){
                        FieldValue(field, ret.second)
                    }
                    else{
                        null
                    }
                }
                else->{
                    if(value!=null) FieldValue(field, value) else null
                }
            }
        }
    }


    open fun rawCreate(data: ModelData,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{


        when(data){
            is ModelDataObject ->{
                return rawCreateObject(data,useAccessControl,partnerCache)
            }
            is ModelDataArray ->{
                return rawCreateArray(data,useAccessControl,partnerCache)
            }
        }
        return Pair(null,"not support")
    }

    // muse call in safeCreate
    protected open fun rawCreateArray(modelDataArray: ModelDataArray,
                                      useAccessControl: Boolean=false,
                                      partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        for (d in modelDataArray.data){
            var obj= ModelDataObject(d, model = modelDataArray.model, fields = modelDataArray.fields)
            obj.context=modelDataArray.context
            var ret=(modelDataArray.model as AccessControlModel).rawCreateObject(obj,useAccessControl,partnerCache)
            if(ret.first==null|| ret.second!=null){
                return ret
            }
        }
        return Pair(1,null)
    }
    protected  open fun beforeCreateObject(modelDataObject: ModelDataObject,
                                           useAccessControl: Boolean=false,
                                           partnerCache:PartnerCache?=null):Pair<Boolean,String?>
    {
        if(useAccessControl || partnerCache!=null){
            if(partnerCache==null){
                return Pair(false,"权限接口没有提供操作用户信息")
            }

            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {
                if((it is FunctionField<*,*>) || (it is ModelOne2ManyField)
                        ||(it is ModelMany2ManyField)){
                    return@forEach
                }
                val oit = it as ModelField
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

    protected  open fun runCreateFieldsFilterRules(modelDataObject: ModelDataObject, partnerCache: PartnerCache){
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

        var filter = this.getModelCreateAccessFieldFilterRule()
        filter?.let {
            it(modelDataObject,partnerCache,null)
        }
    }

    protected  open fun runEditFieldsFilterRules(modelDataObject: ModelDataObject, partnerCache: PartnerCache){
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
        var filterRules = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueFilterRule<*,*>>(model)

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
    protected open fun runCreateFieldsCheckRules(modelDataObject: ModelDataObject, partnerCache: PartnerCache):Pair<Boolean,String?>{
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
    protected open fun runEditFieldsCheckRules(modelDataObject: ModelDataObject, partnerCache: PartnerCache):Pair<Boolean,String?>{
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

        var modelEditFieldsChecks = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueCheckRule<*,String>>(model)

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

        var modelEditFieldsInStoreChecks = partnerCache.getModelEditAccessControlRules<ModelEditRecordFieldsValueCheckInStoreRule<*,String>>(model)
        modelEditFieldsInStoreChecks?.forEach {
            ret = it(modelDataObject,partnerCache,null)
            if(!ret.first){
                return ret
            }
        }

        return Pair(true,null)
    }

    protected open fun runCreateFieldsInitializeRules(modelDataObject: ModelDataObject, partnerCache: PartnerCache){
        val model = modelDataObject.model?:this
        this.createRecordSetIsolationFields(modelDataObject,partnerCache)
        this.cuFieldsProcessProxyModelField(modelDataObject,partnerCache,null)

        var rules = partnerCache.getModelCreateAccessControlRules<ModelCreateRecordFieldsValueInitializeRule<*>>(model)
        rules?.forEach {
            it(modelDataObject,partnerCache,null)
        }
    }
    protected  open fun rawCreateObject(modelDataObject: ModelDataObject,
                                        useAccessControl: Boolean=false,
                                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{

        var constGetRefField=modelDataObject.data.firstOrNull {
            it.field is ConstGetRecordRefField
        }

        if(constGetRefField!=null){
            return if(modelDataObject.context?.refRecordMap?.containsKey(constGetRefField.value as String)!!){
                var fvc=modelDataObject.context?.refRecordMap?.get(constGetRefField.value as String) as ModelDataObject
                var idValue= fvc.idFieldValue?.value as Long?
                if(idValue!=null) {
                    modelDataObject.data.add(FieldValue(modelDataObject.model?.fields?.getIdField()!!, idValue))
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
                is Many2OneField,is One2OneField ->{
                    if(it.field is One2OneField && (it.field as One2OneField).isVirtualField){
                        return@forEach
                    }
                    if(it.value is ModelDataObject){
                        if((it.value as ModelDataObject).idFieldValue==null){
                            (it.value as ModelDataObject).context=modelDataObject.context
                            var id=((it.value as ModelDataObject).model as AccessControlModel?)?.rawCreate(it.value as ModelDataObject,useAccessControl,partnerCache)
                            if(id==null ||id.second!=null){
                                return id?:Pair(null,"创建失败")
                            }
                            var idField= (it.value as ModelDataObject).model?.fields?.getIdField()
                            (it.value as ModelDataObject).data.add(FieldValue(idField!!, id.first))
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
            var fVCShadow= ModelDataObject(model = modelDataObject.model)
            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {
                if((it is FunctionField<*,*>)
                        || (it is ModelOne2ManyField)
                        ||(it is ModelMany2ManyField)
                        ||(it is One2OneField && (it as One2OneField).isVirtualField)){
                    return@forEach
                }
                val oit = it as ModelField
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
            modelDataObject.data.add(FieldValue(
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
                    is One2ManyField ->{
                        if(fv.value is ModelDataObject && (fv.value as ModelDataObject).idFieldValue==null){
                            var tmf=this.getTargetModelField(fv.field)
                            (fv.value as ModelDataObject).data.add(FieldValue(tmf?.second!!, nID))
                            (fv.value as ModelDataObject).context=modelDataObject.context
                            var o2m=(tmf.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,useAccessControl,partnerCache)
                            if (o2m==null || o2m.second!=null){
                                return  Pair(null,o2m?.second?:"创建失败")
                            }
                        }
                        else if(fv.value is ModelDataArray){
                            var tmf=this.getTargetModelField(fv.field)
                            (fv.value as ModelDataArray).context=modelDataObject.context
                            (fv.value as ModelDataArray).data.forEach {
                                    it.add(FieldValue(tmf?.second!!, nID))
                            }
                            var ret=this.rawCreateArray(fv.value as ModelDataArray,useAccessControl,partnerCache)
                            if(ret.first==null ||ret.second!=null){
                                return ret
                            }
                        }
                    }
                    is One2OneField ->{
                        if((fv.field as One2OneField).isVirtualField){
                            if(fv.value is ModelDataObject && (fv.value as ModelDataObject).idFieldValue==null){
                                var tmf=this.getTargetModelField(fv.field)
                                (fv.value as ModelDataObject).data.add(FieldValue(tmf?.second!!, nID))
                                (fv.value as ModelDataObject).context=modelDataObject.context
                                var o2o=(tmf.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,useAccessControl,partnerCache)
                                if (o2o==null || o2o.second!=null){
                                    return  Pair(null,"创建失败")
                                }
                            }
                        }
                    }
                    is ConstRelRegistriesField->{
                        when(fv.value){
                            is ModelDataSharedObject ->{
                                for( kv in (fv.value as ModelDataSharedObject).data) {
                                    when(kv.value){
                                        is ModelDataObject ->{
                                            var mfvc= kv.value as ModelDataObject
                                            mfvc.context=modelDataObject.context
                                            var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                            if(tField!=null && mfvc.idFieldValue==null){
                                                mfvc.data.add(FieldValue(tField, nID))
                                                var ret=(mfvc.model as AccessControlModel?)?.rawCreate(mfvc,useAccessControl,partnerCache)
                                                if(ret==null || ret.second!=null){
                                                    return Pair(null,"创建失败")
                                                }
                                            }
                                        }
                                        is ModelDataArray ->{
                                            var mmfvc= kv.value as ModelDataArray
                                            mmfvc.context=modelDataObject.context
                                            for(mkv in mmfvc.data){
                                                var tField=mmfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                if(tField!=null){
                                                    mkv.add(FieldValue(tField, nID))
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
            ex.printStackTrace()
            return Pair(null,ex.message)
        }
    }
//    protected open  fun beforeCreateCheck(modelData:ModelDataObject,
//                                          useAccessControl: Boolean,
//                                          partnerCache:PartnerCache?):Pair<Boolean,String?>{
//
//        return Pair(true,null)
//    }
    protected  open fun afterCreateObject(modelDataObject: ModelDataObject,
                                          useAccessControl:Boolean,
                                          pc:PartnerCache?):Pair<Boolean,String?>{
        return Pair(true,"")
    }
    protected open fun addCreateModelLog(modelDataObject: ModelDataObject,
                                         useAccessControl:Boolean,
                                         pc:PartnerCache?){

    }
    protected  open fun beforeEditCheck(modelDataObject: ModelDataObject,
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



    open fun acEdit(modelData: ModelData,
                    criteria: ModelExpression?,
                    partnerCache:PartnerCache):Pair<Long?,String?>{
        if(modelData.isEmpty()){
            return Pair(0,"提交数据为空")
        }
        return this.safeEdit(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }



    open fun safeEdit(modelData: ModelData,
                      criteria: ModelExpression?=null,
                      useAccessControl: Boolean=false,
                      partnerCache:PartnerCache?=null):Pair<Long?,String?>{


        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager.getTransaction(def)
        try {
            // var dependingModelFieldValueCollection
            if(modelData.isObject()){
                var ret= this.rawEdit(modelData.`as`(),
                        criteria=criteria,
                        useAccessControl=useAccessControl,
                        partnerCache = partnerCache)
                val tRet:Long=0
                if(ret.first!=null && ret.first!!>tRet){
                    txManager.commit(status)
                    return ret
                }
                else{
                    txManager.rollback(status)
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
            txManager.rollback(status)
        }
        catch (ex:Exception)
        {
            logger.error(ex.message)
        }
        return Pair(0,"更新失败")
    }
    //todo add model role constraint
    open fun getACEditFieldValue(field: FieldBase, value:Any?, useAccessControl: Boolean, partnerCache: PartnerCache?, useDefault:Boolean=false): FieldValue?{
        return FieldValue(field, value)
    }

    open fun rawEdit(modelDataObject: ModelDataObject,
                     criteria: ModelExpression?=null,
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
                    and(tCriteria, idCriteria)
                } else idCriteria
            }

            if(useAccessControl)
            {
                var acCriteria=null as ModelExpression?//partnerCache?.acGetEditCriteria(modelDataObject.model)
                if(acCriteria!=null){
                    tCriteria= if(tCriteria!=null) {
                        and(tCriteria, acCriteria)
                    } else acCriteria
                }
                tCriteria= this.editCorpIsolationBean(modelDataObject,partnerCache!!,tCriteria)?.second
            }

            modelDataObject.data.forEach {
                when(it.field){
                    is Many2OneField,is One2OneField ->{
                        if(it.field is One2OneField && (it.field as One2OneField).isVirtualField){
                            return@forEach
                        }
                        if(it.value is ModelDataObject){
                            if((it.value as ModelDataObject).idFieldValue==null){
                                (it.value as ModelDataObject).context=modelDataObject.context
                                var id=((it.value as ModelDataObject).model as AccessControlModel?)?.rawCreate(it.value as ModelDataObject,
                                        useAccessControl,
                                        partnerCache)
                                if(id==null ||id.second!=null){
                                    return id?:Pair(null,"创建失败")
                                }
                                (it.value as ModelDataObject).data.add(FieldValue((it.value as ModelDataObject).model?.fields?.getIdField()!!, id.first))
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


            var fVCShadow= ModelDataObject(model = modelDataObject.model, fields = modelDataObject.fields)
            modelDataObject.model?.fields?.getAllFields()?.values?.forEach {

                if((it is FunctionField<*,*>)
                        || (it is ModelOne2ManyField)
                        ||(it is ModelMany2ManyField)
                        ||(it is One2OneField && (it as One2OneField).isVirtualField)){
                    return@forEach
                }
                val oit = it as ModelField
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
                        is One2ManyField ->{
                            if(fv.value is ModelDataObject){
                                var tmf=this.getTargetModelField(fv.field)
                                if((fv.value as ModelDataObject).idFieldValue==null)
                                {
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    (fv.value as ModelDataObject).data.add(FieldValue(tmf?.second!!, mIDFV.value))
                                    var o2m=(tmf.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,
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
                            else if(fv.value is ModelDataArray){
                                var tmf=this.getTargetModelField(fv.field)
                                (fv.value as ModelDataArray).data.forEach {
                                    var tfvc= ModelDataObject(it, (fv.value as ModelDataArray).model)
                                    if(tfvc.idFieldValue==null){
                                        tfvc.context=modelDataObject.context
                                        tfvc.data.add(FieldValue(tmf?.second!!, mIDFV.value))
                                        var o2m=(tmf.first as AccessControlModel?)?.rawCreate(tfvc,
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
                       is One2OneField ->{
                            if((fv.field as One2OneField).isVirtualField && (fv.value is ModelDataObject)){
                                if((fv.value as ModelDataObject).idFieldValue==null){
                                    var tmf=this.getTargetModelField(fv.field)
                                    (fv.value as ModelDataObject).data.add(FieldValue(tmf?.second!!, mIDFV.value))
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    var o2o=(tmf.first as AccessControlModel?)?.rawCreate(fv.value as ModelDataObject,
                                            useAccessControl,
                                            partnerCache)
                                    if (o2o==null || o2o.second!=null){
                                        return  Pair(null,"创建失败")
                                    }
                                }
                                else if((fv.value as ModelDataObject).hasNormalField()){
                                    var tmf=this.getTargetModelField(fv.field)
                                    (fv.value as ModelDataObject).data.add(FieldValue(tmf?.second!!, mIDFV.value))
                                    (fv.value as ModelDataObject).context=modelDataObject.context
                                    var o2o=(tmf.first as AccessControlModel?)?.rawEdit(fv.value as ModelDataObject,
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
                                is ModelDataSharedObject ->{
                                    for( kv in (fv.value as ModelDataSharedObject).data) {
                                        when(kv.value){
                                            is ModelDataObject ->{
                                                var mfvc= kv.value as ModelDataObject
                                                mfvc.context=modelDataObject.context
                                                var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                if(tField!=null && mfvc.idFieldValue==null){
                                                    mfvc.context=modelDataObject.context
                                                    mfvc.data.add(FieldValue(tField, mIDFV.value))
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
                                            is ModelDataArray ->{
                                                var mmfvc= kv.value as ModelDataArray
                                                for(mkv in mmfvc.data){
                                                    var mfvc= ModelDataObject(mkv, mmfvc.model)
                                                    mfvc.context=modelDataObject.context
                                                    var tField=mfvc.model?.fields?.getFieldByTargetField(modelDataObject.model?.fields?.getIdField())
                                                    if(tField!=null && mfvc.idFieldValue==null){
                                                        mfvc.data.add(FieldValue(tField, mIDFV.value))
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
    protected  open fun afterEditObject(modelDataObject: ModelDataObject,
                                        useAccessControl:Boolean,
                                        pc:PartnerCache?):Pair<Boolean,String?>{
        return Pair(true,"")
    }


    protected open fun addEditModelLog(modelDataObject: ModelDataObject,
                                       useAccessControl:Boolean,
                                       pc:PartnerCache?){

    }
    protected  open fun beforeDeleteCheck(modelData: ModelDataObject,
                                          criteria: ModelExpression?,
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
            var ret= modelDeleteFieldsBelongToPartnerCheck(modelData,partnerCache,criteria)
            if(!ret.first){
                return ret
            }
            var ruleTypes = partnerCache.getModelDeleteAccessControlRules<ModelDeleteAccessControlRule<*,String>>(model!!)
            ruleTypes?.forEach {
                ret=it(modelData,partnerCache,null)
                if(!ret.first){
                    return ret
                }
            }
        }
        return Pair(true,null)
    }

    open fun acDelete(modelData: ModelData,
                      criteria: ModelExpression?,
                      partnerCache:PartnerCache?):Pair<Long?,String?>{

        return this.safeDelete(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }

    open fun acDelete(criteria: ModelExpression?,
                      partnerCache:PartnerCache?):Pair<Long?,String?>{
        val modelData = ModelDataObject(model = this)
        return this.safeDelete(modelData,
                criteria = criteria,
                useAccessControl = true,
                partnerCache = partnerCache)
    }

    open fun safeDelete(modelData: ModelData,
                        criteria: ModelExpression?,
                        useAccessControl: Boolean=false,
                        partnerCache:PartnerCache?=null):Pair<Long?,String?>{

        if(useAccessControl && partnerCache==null){
            return Pair(0,"权限接口没有提供操作用户信息")
        }
        val def = DefaultTransactionDefinition()
        def.propagationBehavior = TransactionDefinition.PROPAGATION_REQUIRED
        val status = txManager.getTransaction(def)
        try {
            // var dependingModelFieldValueCollection
            if(modelData.isObject()){
                var ret= this.rawDelete(modelData.`as`(),
                        criteria=criteria,
                        useAccessControl=useAccessControl,
                        partnerCache = partnerCache)
                txManager.commit(status)
                return  ret
            }

        } catch (ex: Exception) {
            ex.printStackTrace()
            logger.error(ex.message)
        }
        try {
            txManager.rollback(status)
        }
        catch (ex:Exception)
        {
            logger.error(ex.message)
        }
        return Pair(0,"更新失败")
    }

    open fun rawDelete(criteria: ModelExpression?,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        return this.rawDelete(ModelDataObject(model = this),criteria,useAccessControl,partnerCache)
    }
    open fun rawDelete(modelDataObject: ModelDataObject,
                       criteria: ModelExpression?,
                       useAccessControl: Boolean=false,
                       partnerCache:PartnerCache?=null):Pair<Long?,String?>{
        if(useAccessControl && partnerCache==null){
            return Pair(null,"must login")
        }
        var (result,errorMsg)=this.beforeDeleteCheck(modelDataObject,criteria = criteria,useAccessControl = useAccessControl,partnerCache = partnerCache)
        if(!result){
            return Pair(null,errorMsg)
        }

        return try {
            var tCriteria=criteria
            var idFV=modelDataObject.idFieldValue
            if(idFV!=null){
                var idCriteria=eq(idFV.field,idFV.value)
                tCriteria= if(tCriteria!=null) {
                    and(tCriteria, idCriteria)
                } else idCriteria
            }

            if(useAccessControl)
            {
                var acCriteria=null as ModelExpression?//partnerCache?.acGetEditCriteria(modelDataObject.model)
                if(acCriteria!=null){
                    tCriteria= if(tCriteria!=null) {
                        and(tCriteria, acCriteria)
                    } else acCriteria
                }

                var corpIsolationCriteria = deleteCorpIsolationBean(modelDataObject,
                        partnerCache = partnerCache!!,
                        criteria=tCriteria)
                if(corpIsolationCriteria.first && corpIsolationCriteria.second!=null){
                    tCriteria = corpIsolationCriteria.second
                }
            }
            Pair(this.delete(modelDataObject,criteria = tCriteria),null)
        } catch (ex:Exception){
            Pair(null,ex.message)
        }
    }

    open fun rawCount(fieldValueArray: FieldValueArray, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Int{
        var expArr = fieldValueArray.map {
            eq(it.field, it.value)
        }.toTypedArray()
        var expressions = and(*expArr)
        var (expressions2,_) = this.beforeRead(criteria = expressions,model=this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).count().where(expressions2)
        return this.queryCount(statement)
    }

    open fun rawCount(criteria: ModelExpression?, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Int{
        val (c,_)=this.beforeRead(criteria = criteria,model = this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).count().where(c)
        return this.queryCount(statement)
    }
    open fun acCount(criteria: ModelExpression?, partnerCache:PartnerCache):Int{
        return this.rawCount(criteria,partnerCache,true)
    }

    open fun rawMax(field: FieldBase, criteria: ModelExpression?=null, partnerCache:PartnerCache?=null, useAccessControl: Boolean=false):Long?{
        val (c,_)=this.beforeRead(field,criteria = criteria,model = this,useAccessControl = useAccessControl,partnerCache = partnerCache)
        var statement = select(fromModel = this).max(dynamic.model.query.mq.aggregation.MaxExpression(field)).where(c)
        return this.queryMax(statement)
    }

    open fun acMax(field: FieldBase, partnerCache:PartnerCache, criteria: ModelExpression?=null):Long?{
        return this.rawMax(field,criteria,partnerCache,true)
    }

}