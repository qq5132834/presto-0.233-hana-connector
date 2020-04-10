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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.spi.ColumnMetadata;

import java.util.ArrayList;
import java.util.List;

public class SaphanaTable
{
    private static final Logger log = Logger.get(SaphanaTable.class);

    //表名
    private String tableName;

    //表字段列表
    private List<SaphanaColumn> saphanaColumns;

    //表字段源数据
    private List<ColumnMetadata> columnsMetadata;

    private SaphanaTable(){}

    public SaphanaTable(String tableName, List<SaphanaColumn> saphanaColumns){
        this.tableName = tableName;
        this.saphanaColumns = saphanaColumns;

        List<ColumnMetadata> columnsMetadata = new ArrayList<>();
        for (SaphanaColumn column : this.saphanaColumns) {
            columnsMetadata.add(new ColumnMetadata(column.getColumnName(), column.getType()));
        }
        this.columnsMetadata = columnsMetadata;
    }

    public static Logger getLog() {
        return log;
    }

    public String getTableName() {
        return tableName;
    }

    public List<SaphanaColumn> getSaphanaColumns() {
        return saphanaColumns;
    }

    public List<ColumnMetadata> getColumnsMetadata() {
        return columnsMetadata;
    }

}
