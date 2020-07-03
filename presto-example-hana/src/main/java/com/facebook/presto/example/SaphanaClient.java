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

import com.alibaba.fastjson.JSONObject;
import com.facebook.airlift.log.Logger;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class SaphanaClient
{
    private static final Logger log = Logger.get(SaphanaClient.class);

    public static final String SCHEMA = "zuk_schema";
    public static List<String> tables = new ArrayList<>();
    public static Map<String, List<String>> SCHEMA_TABLES = new HashMap<>();         //schema与table list关系
    public static Map<String, List<SaphanaColumn>> TABLES_TABLES_COLUMNS = new HashMap<>();  //table与column list关系


    static {
        //初始化表
        tables.add("user");
        tables.add("company");
        SCHEMA_TABLES.put(SCHEMA, tables);

        //初始化字段
        List<SaphanaColumn> columns1 = new ArrayList<>();
        columns1.add(new SaphanaColumn("user", "name", "VARCHAR", 20));
        columns1.add(new SaphanaColumn("user", "age", "BIGINT", 10));
        TABLES_TABLES_COLUMNS.put("user", columns1);

        List<SaphanaColumn> columns2 = new ArrayList<>();
        columns2.add(new SaphanaColumn("company", "text", "VARCHAR", 20));
        columns2.add(new SaphanaColumn("company", "value", "BIGINT", 10));
        TABLES_TABLES_COLUMNS.put("company", columns2);

    }

    @Inject
    public SaphanaClient(){

    }

    /***
     *
     *
     * @param schemaName
     * @param tableName
     * @return SaphanaTable
     */
    public SaphanaTable getTable(String schemaName, String tableName)
    {
        log.info("getTable.schemaName:" + schemaName + ",tableName:" + tableName);
        log.info("schemaTables:" + JSONObject.toJSONString(SCHEMA_TABLES));
        //log.info("tableColumns:" + JSONObject.toJSONString(tableColumns));
        requireNonNull(schemaName, "schemaName is null");
        requireNonNull(tableName, "tableName is null");
        List<String> tables = SCHEMA_TABLES.get(schemaName);
        log.info("tabls:" + JSONObject.toJSONString(tables));
        if(tables!=null && tables.contains(tableName)){
            //如果schema与table存在
            SaphanaTable saphanaTable = new SaphanaTable(tableName, TABLES_TABLES_COLUMNS.get(tableName));
            return saphanaTable;
        }
        return null;
    }

    /***
     * 获取某张表下的字段属性
     * @param tableName
     * @return
     * @throws Exception
     */
    public List<SaphanaColumn> getTableColumn(String tableName) throws Exception{

        if(TABLES_TABLES_COLUMNS.get(tableName)!=null){
            return TABLES_TABLES_COLUMNS.get(tableName);
        }

        return new ArrayList<>();
    }


}
