package com.facebook.presto.example;

import java.sql.*;

public class TestSaphana {

    private static final String DRIVER = "com.sap.db.jdbc.Driver";
    private static final String URL = "jdbc:sap://192.168.152.171:39015";

    public static void main(String[] args) {
        TestSaphana demo = new TestSaphana();
        try {
            demo.select();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void select() throws Exception {

        Connection con = this.getConnection(DRIVER, URL, "SYSTEM","sicsPoD2020");
        PreparedStatement pstmt = con.prepareStatement("select ID,NAME from Test3");
        System.out.println(pstmt.getClass().getName());
        if(pstmt.isWrapperFor(com.sap.db.jdbc.trace.PreparedStatement.class)){
            System.out.println("true");
        }
        ResultSet rs = pstmt.executeQuery();
        while(rs.next()){
            String id = rs.getString(1);
            String name = rs.getString(2);
            System.out.println(id+","+name);
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
