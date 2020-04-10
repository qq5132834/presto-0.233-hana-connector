/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.example;

import com.facebook.presto.spi.type.IntegerType;
import com.facebook.presto.spi.type.VarcharType;
import com.facebook.presto.spi.type.Type;

public class SaphanaColumn
{
    private String tableName;       //表名
    private String columnName;      //字段名称
    private Integer dataType;       //
    private String typeName;        //字段数据类型
    private Integer columnSize;     //字段大小

    private Type type;              //类型

    public SaphanaColumn(){}

    public SaphanaColumn(String tableName, String columnName, String typeName, Integer columnSize){
        this.tableName = tableName;
        this.columnName = columnName;
        this.typeName = typeName;
        this.columnSize = columnSize;

        if("VARCHAR".equals(typeName)){
            this.type = VarcharType.createVarcharType(this.columnSize);
        }
        if("INTEGER".equals(typeName)){
            this.type = IntegerType.INTEGER;
        }

    }

    public Type getType() { return type; }

    public void setType(Type type) {
        this.type = type;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public Integer getDataType() {
        return dataType;
    }

    public void setDataType(Integer dataType) {
        this.dataType = dataType;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getColumnSize() {
        return columnSize;
    }

    public void setColumnSize(Integer columnSize) {
        this.columnSize = columnSize;
    }

}
