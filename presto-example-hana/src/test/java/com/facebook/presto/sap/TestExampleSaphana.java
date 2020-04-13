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

import com.alibaba.fastjson.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TestExampleSaphana
{

    private static final String DRIVER = "com.sap.db.jdbc.Driver";
    private static final String URL = "jdbc:sap://192.168.152.171:39015?currentschema=sics_pod_schema";

    public TestExampleSaphana() {
    }

    public static void main(String[] args) {
        TestExampleSaphana demo = new TestExampleSaphana();
        try {
            demo.select();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 查询表字段
     * @param con
     * @throws SQLException
     */
    public void showColumn(Connection con) throws SQLException{
        System.out.println("<<<<<<<<<<<<<showColumn>>>>>>>>>>>>>");
        DatabaseMetaData databaseMetaData = con.getMetaData();
//    	ResultSet rs = databaseMetaData.getColumns(null, null, "NUMBERS", null);
        ResultSet rs = databaseMetaData.getColumns(null, "sics_pod_schema".toUpperCase(), "NUMBERS", null);
        int i = 0;
        while(rs.next()){
            String tableName = rs.getString("TABLE_NAME");
            String columnName = rs.getString("COLUMN_NAME");
            int dataType = rs.getInt("DATA_TYPE");
            String dataTypeName = rs.getString("TYPE_NAME");
            int columnSize = rs.getInt("COLUMN_SIZE");
            if(tableName.toLowerCase().contains("numbers")){
                System.out.println(tableName + "," + columnName + "," + dataType + "," + dataTypeName + "," +columnSize );
            }
            else{
                System.out.println(tableName);
            }

        }
        System.out.println(">>>>>>>>>>>>>showColumn<<<<<<<<<<<<<");
    }

    public List<String> getTables(Connection con){

        List<String> list = new ArrayList<>();
        try {
            DatabaseMetaData databaseMetaData = con.getMetaData();
            ResultSet rs = databaseMetaData.getTables(null, "sics_pod_schema".toUpperCase(), null, new String[]{"TABLE"});

            while (rs.next()){
                String tableName = rs.getString("TABLE_NAME");
                list.add(tableName);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        System.out.print(JSONObject.toJSONString(list));
        return list;
    }

    public void select() throws Exception {

        Connection con = this.getConnection(DRIVER, URL, "SYSTEM","sicsPoD2020");

        //查询表字段信息
        this.showColumn(con);
        this.getTables(con);


        PreparedStatement pstmt = con.prepareStatement("select text, value from sics_pod_schema.numbers");
        System.out.println(pstmt.getClass().getName());
        if(pstmt.isWrapperFor(com.sap.db.jdbc.trace.PreparedStatement.class)){
            System.out.println("true");
        }
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            String text = rs.getString(1);
            String value = rs.getString(2);
            System.out.println(text+","+value);
        }

        this.closeConnection(con, pstmt);

    }


    private Connection getConnection(String driver, String url, String user,
                                     String password) throws Exception {
        Class.forName(driver);
        return DriverManager.getConnection(url, user, password);

    }

    private void closeConnection(Connection con, Statement stmt)
            throws Exception {
        if (stmt != null) {
            stmt.close();
        }
        if (con != null) {
            con.close();
        }
    }

}
