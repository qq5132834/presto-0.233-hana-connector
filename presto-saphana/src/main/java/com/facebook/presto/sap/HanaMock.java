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
package com.facebook.presto.sap;

import java.sql.*;
import java.util.*;

import static java.util.Objects.requireNonNull;

public class HanaMock
{
    private static final String SCHEMA = "sics_pod_schema";
    private static final String DRIVER = "com.sap.db.jdbc.Driver";
    private static final String URL = "jdbc:sap://192.168.152.171:39015?currentschema="+SCHEMA;
    private static final String USER = "SYSTEM";
    private static final String PASSWORD = "sicsPoD2020";

    private static Map<String, List<String>> schemaTables = new HashMap<>();
    private static Map<String, List<HanaColumn>> tableColumns = new HashMap<>();

    public static String getSCHEMA(){
        return SCHEMA;
    }

    static {

        List<String> tables = new ArrayList<>();
        tables.add("user");
        tables.add("company");
        schemaTables.put("sics_pod_schema", tables);

        List<HanaColumn> columns1 = new ArrayList<>();
        columns1.add(new HanaColumn("user", "name", "VARCHAR", 20));
        columns1.add(new HanaColumn("user", "age", "BIGINT", 10));
        tableColumns.put("user", columns1);

        List<HanaColumn> columns2 = new ArrayList<>();
        columns2.add(new HanaColumn("company", "text", "VARCHAR", 20));
        columns2.add(new HanaColumn("company", "value", "BIGINT", 10));
        tableColumns.put("company", columns2);
    }

    public HanaTable getTable(String schemaName, String tableName)
    {
        requireNonNull(schemaName, "schemaName is null");
        requireNonNull(tableName, "tableName is null");
        List<String> tables = schemaTables.get(schemaName);
        if(tables!=null && tables.contains(tableName)){
            HanaTable saphanaTable = new HanaTable(tableName, tableColumns.get(tableName));
            return saphanaTable;
        }
        return null;
    }


    public List<HanaColumn> getTableColumn(String tableName) throws Exception{

        if(tableColumns.get(tableName)!=null){
            return tableColumns.get(tableName);
        }

        Connection con = this.getConnection();
        DatabaseMetaData databaseMetaData = con.getMetaData();
        ResultSet rs = databaseMetaData.getColumns(null, null, tableName.toUpperCase(), null);
        List<HanaColumn> columns = null;
        while(rs.next()) {
            if(columns==null){
                columns = new ArrayList<>();
            }
            String columnName = rs.getString("COLUMN_NAME");
            int dataType = rs.getInt("DATA_TYPE");
            String typeName = rs.getString("TYPE_NAME");
            int columnSize = rs.getInt("COLUMN_SIZE");

            HanaColumn hanaColumn = new HanaColumn();
            hanaColumn.setTableName(tableName);
            hanaColumn.setColumnName(columnName);
            hanaColumn.setTypeName(typeName);
            hanaColumn.setColumnSize(columnSize);

            columns.add(hanaColumn);
        }
        closeConnection(con, null);

        tableColumns.put(tableName.toUpperCase(), columns);

        return columns;
    }

    public List<String> getTables(String schema){

        if(schemaTables.get(schema) != null){
            return schemaTables.get(schema);
        }

        List<String> list = new ArrayList<>();
        try {
            Connection con = this.getConnection();
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet rs = databaseMetaData.getTables(null, schema.toUpperCase(), null, new String[]{"TABLE"});
            while (rs.next()){
                String tableName = rs.getString("TABLE_NAME");
                list.add(tableName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        schemaTables.put(schema.toUpperCase(),list);

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
