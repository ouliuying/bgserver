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

package work.bg.server.core.mq
import java.util.concurrent.locks.StampedLock


class FieldCollection(vararg fields:FieldBase): Iterable<FieldBase>{
    private var fieldSet= mutableMapOf<String,FieldBase>()
    private var cachedPropertyFieldsLock:StampedLock = StampedLock()
    private lateinit var cachedPropertyFields:MutableMap<String,FieldBase>
    private var cachedPersistFieldsLock: StampedLock = StampedLock()
    private lateinit var cachedPersistFields:MutableMap<String,FieldBase>
    private lateinit var cachedPersistFieldsExcludeVirtualOne2OneField:MutableMap<String,FieldBase>
    private val model by lazy{this.fieldSet.values.first().model!! }
    init {

        fields.forEach {
            fieldSet[it.getFullName()!!]=it
        }
    }
    fun getAllFields():Map<String,FieldBase>{
        return this.fieldSet
    }
    fun getDefaultOrderBy():OrderBy?{
        var idf= getField(FieldConstant.id)
        return if(idf!=null){
            OrderBy(OrderBy.OrderField(idf!!, OrderBy.Companion.OrderType.DESC))
        }
        else{
            null
        }
    }
    fun getFieldByTargetField(field:FieldBase?):FieldBase?{
        return this.fieldSet.values.firstOrNull {
            when(it){
                is RefTargetField->{
                    "${it.targetModelTable}.${it.targetModelFieldName}"==field?.getFullName()}
                else->false
                }
            }
    }
    fun getFieldByPropertyName(propertyName:String):FieldBase?{

        var tamp=cachedPropertyFieldsLock.readLock()
        try {
            if(this::cachedPropertyFields.isInitialized){

                return this.cachedPropertyFields?.get(propertyName)
            }
        }

        finally {
            cachedPropertyFieldsLock.unlockRead(tamp)
        }

        tamp=cachedPropertyFieldsLock.writeLock()
        try {
            this.cachedPropertyFields= mutableMapOf()
            this.fieldSet.forEach{
               this.cachedPropertyFields?.set(it.value.propertyName,it.value)
            }
            return this.cachedPropertyFields?.get(propertyName)
        }
        finally {
            cachedPropertyFieldsLock.unlockWrite(tamp)
        }
    }

    fun getIdField():FieldBase?{
        return getField(FieldConstant.id)
    }
    fun getPartnerField():FieldBase?{
        return getField(FieldConstant.partner)
    }
    fun getCorpField():FieldBase?{
        return getField(FieldConstant.corp)
    }
    fun getPartnerRoleField():FieldBase?{
        return getField(FieldConstant.partner_role)
    }
    fun getPartnerRoleModelField():FieldBase?{
        return getField(FieldConstant.partner_role_model)
    }
    fun getField(name:String?):FieldBase?{
        if(!name?.contains(".")!!){
            return this.fieldSet["${this.model.fullTableName}.$name"]
        }
        return this.fieldSet[name]
    }
    fun clearCache(){
        var tamp=cachedPersistFieldsLock.writeLock()
        try {
            this.cachedPersistFields= mutableMapOf()
            this.cachedPersistFieldsExcludeVirtualOne2OneField= mutableMapOf()
        }
        finally {
            cachedPersistFieldsLock.unlockWrite(tamp)
        }
    }

    override fun iterator(): Iterator<FieldBase> {
        return object :Iterator<FieldBase>{
            val fields=this@FieldCollection.fieldSet.values
            var it=fields.iterator()
            override fun hasNext(): Boolean {
                return it.hasNext()
            }

            override fun next(): FieldBase {
              return it.next()
            }
        }
    }

    fun add(field:FieldBase?):FieldBase?{
        if(!this.fieldSet.containsKey(field?.getFullName()))
        {
            this.fieldSet.put(field?.getFullName()!!,field!!);
        }
        return null
    }

    fun remove(fullName:String):FieldBase?{
        return this.fieldSet.remove(fullName)
    }
    inline fun <reified T>  getTypeFields():Map<String,FieldBase?>?{
        var fs= mutableMapOf<String,FieldBase?>()
        this.getAllFields().forEach { t, u ->
            when(u){
                is T->{
                    fs[t]=u
                }
            }
        }
        return if(fs.count()>0){
            fs
        }
        else{
            null
        }
    }

    fun getAllPersistFields(excludeVirtualOne2OneField:Boolean=false):Map<String,FieldBase>{
        var tamp=cachedPersistFieldsLock.readLock()
        try {
            if(this::cachedPersistFields.isInitialized){
                return if (!excludeVirtualOne2OneField) {
                     this.cachedPersistFields
                } else {
                    this.cachedPersistFieldsExcludeVirtualOne2OneField
                }
            }
        }
        finally {
            cachedPersistFieldsLock.unlockRead(tamp)
        }

        tamp=cachedPersistFieldsLock.writeLock()
        try {
            this.cachedPersistFields= mutableMapOf()
            this.cachedPersistFieldsExcludeVirtualOne2OneField= mutableMapOf()
            this.fieldSet.forEach{
                if((it.value !is FunctionField) && (it.value !is RefRelationField) && it.value !is One2ManyField){
                        this.cachedPersistFields?.set(it.key,it.value)
                    if(it.value !is ModelOne2OneField || !(it.value as ModelOne2OneField).isVirtualField){
                        cachedPersistFieldsExcludeVirtualOne2OneField?.set(it.key,it.value)
                    }
                }
            }
            return this.cachedPersistFields
        }
        finally {
            cachedPersistFieldsLock.unlockWrite(tamp)
        }
    }

    fun sortTypeFields(vararg fields:FieldBase):Triple<Map<String,FieldBase>,Map<String,FieldBase>,Map<String,FieldBase>>?{
        var names=fields.map {
            it.getFullName()
        }
        return sortTypeFields(*names.toTypedArray())
    }
    fun sortTypeFields(vararg fullFieldNames:String?):Triple<Map<String,FieldBase>,Map<String,FieldBase>,Map<String,FieldBase>>?{
        var persistFields= mutableMapOf<String,FieldBase>()
        var funFields= mutableMapOf<String,FieldBase>()
        var relatioinFields=mutableMapOf<String,FieldBase>()
        this.fieldSet.forEach{
            if(fullFieldNames.contains(it.key)){
                when(it){
                    is FunctionField->{
                        funFields[it.key] = it.value
                    }
                    is RefRelationField->{
                        relatioinFields[it.key]=it.value
                    }
                    else->{
                        persistFields[it.key] = it.value
                    }
                }
            }
        }
        return Triple(persistFields,relatioinFields,funFields)
    }
    fun toArray():Array<FieldBase>?{
        return this.fieldSet.values.toTypedArray()
    }
}