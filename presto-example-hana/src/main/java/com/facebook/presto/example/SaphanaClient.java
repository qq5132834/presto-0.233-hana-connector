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
    private static final String SCHEMA = "sics_pod_schema";
    private static final String DRIVER = "com.sap.db.jdbc.Driver";
    private static final String URL = "jdbc:sap://192.168.152.171:39015?currentschema="+SCHEMA;
    private static final String USER = "SYSTEM";
    private static final String PASSWORD = "sicsPoD2020";

    private static Map<String, List<String>> schemaTables = new HashMap<>();         //schema与table list关系
    private static Map<String, List<SaphanaColumn>> tableColumns = new HashMap<>();  //table与column list关系


    public static String getSCHEMA(){
        return SCHEMA;
    }

    static {
        //初始化表
        List<String> tables = new ArrayList<>();
        tables.add("user");
        tables.add("company");
        schemaTables.put("sics_pod_schema", tables);

        //初始化字段
        List<SaphanaColumn> columns1 = new ArrayList<>();
        columns1.add(new SaphanaColumn("user", "name", "VARCHAR", 20));
        columns1.add(new SaphanaColumn("user", "age", "BIGINT", 10));
        tableColumns.put("user", columns1);

        List<SaphanaColumn> columns2 = new ArrayList<>();
        columns2.add(new SaphanaColumn("company", "text", "VARCHAR", 20));
        columns2.add(new SaphanaColumn("company", "value", "BIGINT", 10));
        tableColumns.put("company", columns2);
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
        log.info("schemaTables:" + JSONObject.toJSONString(schemaTables));
        //log.info("tableColumns:" + JSONObject.toJSONString(tableColumns));
        requireNonNull(schemaName, "schemaName is null");
        requireNonNull(tableName, "tableName is null");
        List<String> tables = schemaTables.get(schemaName);
        log.info("tabls:" + JSONObject.toJSONString(tables));
        if(tables!=null && tables.contains(tableName)){
            //如果schema与table存在
            SaphanaTable saphanaTable = new SaphanaTable(tableName, tableColumns.get(tableName));
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

        if(tableColumns.get(tableName)!=null){
            return tableColumns.get(tableName);
        }

        Connection con = this.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet rs = databaseMetaData.getColumns(null, null, tableName.toUpperCase(), null);
        List<SaphanaColumn> columns = null;
        while(rs.next()) {
            if(columns==null){
                columns = new ArrayList<>();
            }
            String columnName = rs.getString("COLUMN_NAME");
            int dataType = rs.getInt("DATA_TYPE");
            String typeName = rs.getString("TYPE_NAME");
            int columnSize = rs.getInt("COLUMN_SIZE");

            SaphanaColumn saphanaColumn = new SaphanaColumn();
            saphanaColumn.setTableName(tableName);
            saphanaColumn.setColumnName(columnName);
            saphanaColumn.setTypeName(typeName);
            saphanaColumn.setColumnSize(columnSize);

            columns.add(saphanaColumn);
        }
        closeConnection(con, null);

        tableColumns.put(tableName.toUpperCase(), columns);

        log.info("getTableColumn.tableName:" + tableName + ",columns:" + JSONObject.toJSONString(columns));
        return columns;
    }

    /***
     * 获取hana中schema下的表
     * @param schema
     * @return
     */
    public List<String> getTables(String schema){

        //判断缓存中是否存在
        if(schemaTables.get(schema) != null){
            return schemaTables.get(schema);
        }

        List<String> list = new ArrayList<>();
        try {
            Connection con = this.getConnection();
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet rs = databaseMetaData.getTables(null, schema.toUpperCase(), null, new String[]{"TABLE"});
            log.info("schema:" + schema.toUpperCase());
            while (rs.next()){

                String tableName = rs.getString("TABLE_NAME");
                log.info(tableName);
                list.add(tableName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        //缓存起来
        schemaTables.put(schema.toUpperCase(),list);

        log.info("getTables:" + JSONObject.toJSONString(list));
        return list;
    }

    private Connection getConnection() throws Exception {
        Class.forName(DRIVER);
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private void closeConnection(Connection con, Statement stmt) throws Exception {
        if (stmt != null) {
            stmt.close();
        }
        if (con != null) {
            con.close();
        }
    }

}
