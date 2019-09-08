

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

package dynamic.model.query.mq


import dynamic.model.query.mq.model.ModelBase


class SelectStatement constructor(vararg val selectFields: dynamic.model.query.mq.FieldBase, val fromModel: ModelBase): dynamic.model.query.mq.ModelExpression(){
        var expression: dynamic.model.query.mq.ModelExpression?=null
        var joinModels:MutableList<dynamic.model.query.mq.join.JoinModel>?=null
        var groupBy: dynamic.model.query.mq.GroupBy?=null
        var orderBy: dynamic.model.query.mq.OrderBy?=null
        var offset:Int?=null
        var limit:Int?=null
        var countExpression: dynamic.model.query.mq.aggregation.CountExpression?=null
        var avgExpressions:Array<out dynamic.model.query.mq.aggregation.AvgExpression>?=null
        var maxExpressions:Array<out dynamic.model.query.mq.aggregation.MaxExpression>?=null
        var minExpressions:Array<out dynamic.model.query.mq.aggregation.MinExpression>?=null
        var sumExpressions:Array<out dynamic.model.query.mq.aggregation.SumExpression>?=null
        fun count(countExpression: dynamic.model.query.mq.aggregation.CountExpression?=null): dynamic.model.query.mq.SelectStatement {
            if(countExpression!=null){
                this.countExpression=countExpression
            }
            else{
                this.countExpression= dynamic.model.query.mq.aggregation.CountExpression(null)
            }
            return this
        }
        fun avg(vararg avgExpression: dynamic.model.query.mq.aggregation.AvgExpression): dynamic.model.query.mq.SelectStatement {
            this.avgExpressions=avgExpression
            return this
        }
        fun max(vararg maxExpression: dynamic.model.query.mq.aggregation.MaxExpression): dynamic.model.query.mq.SelectStatement {
            this.maxExpressions=maxExpression
            return this
        }
        fun min(vararg minExpression: dynamic.model.query.mq.aggregation.MinExpression): dynamic.model.query.mq.SelectStatement {
            this.minExpressions=minExpression
            return this
        }
        fun sum(vararg sumExpression: dynamic.model.query.mq.aggregation.SumExpression): dynamic.model.query.mq.SelectStatement {
            this.sumExpressions=sumExpression
            return this
        }
        fun where(expression: dynamic.model.query.mq.ModelExpression?): dynamic.model.query.mq.SelectStatement {
            this.expression=expression
            return this
        }

        fun join(joinModel: dynamic.model.query.mq.join.JoinModel?): dynamic.model.query.mq.SelectStatement {
            if (this.joinModels==null){
              this.joinModels= mutableListOf<dynamic.model.query.mq.join.JoinModel>()
            }
            if(joinModel!=null){
                this.joinModels!!.add(joinModel)
            }
            return this
        }

        fun orderBy(orderBy: dynamic.model.query.mq.OrderBy?): dynamic.model.query.mq.SelectStatement {
            this.orderBy= orderBy
            return this
        }

        fun groupBy(groupBy: dynamic.model.query.mq.GroupBy?): dynamic.model.query.mq.SelectStatement {
            this.groupBy=groupBy;
            return this
        }
        fun offset(offset:Int?): dynamic.model.query.mq.SelectStatement {
            this.offset=offset
            return this
        }
        fun limit(limit:Int?): dynamic.model.query.mq.SelectStatement {
            this.limit=limit
            return this
        }
        override fun render(parent: dynamic.model.query.mq.ModelExpression?):Pair<String,Map<String, dynamic.model.query.mq.FieldValue>>?{
            var render= dynamic.model.query.mq.ModelCriteriaRender()
            this.accept(render,parent)
            return Pair(render.namedSql.toString(),render.namedParameters)
        }

        override fun accept(visitor: dynamic.model.query.mq.ModelExpressionVisitor, parent: dynamic.model.query.mq.ModelExpression?): Boolean {
            // TODO("not implemented")
            // To change body of created functions use File | Settings | File Templates.
            visitor.visit(this,parent)
            return true
        }
}