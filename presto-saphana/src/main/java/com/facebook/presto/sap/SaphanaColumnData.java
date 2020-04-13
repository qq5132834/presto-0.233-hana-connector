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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaphanaColumnData {

    private final List<List<Object>> datas;

    public List<List<Object>> getDatas() {
        return datas;
    }

    public SaphanaColumnData(){
        this.datas = new ArrayList<>();
    }

    public SaphanaColumnData insert(List<Object> row){
        datas.add(row);
        return this;
    }

    ////////////////////////////////////MOCK数据/////////////////////////////
    public static Map<String, SaphanaColumnData> tableData = new HashMap<>();

    static {

        List<Object> row1 = new ArrayList<>();  row1.add("huangliao"); row1.add(39);
        List<Object> row2 = new ArrayList<>();  row2.add("xubo"); row2.add(49);
        List<Object> row3 = new ArrayList<>();  row3.add("xunjianneng"); row3.add(59);
        tableData.put("user", new SaphanaColumnData().insert(row1).insert(row2).insert(row3));

        List<Object> row4 = new ArrayList<>();  row4.add("pingan"); row4.add(81);
        List<Object> row5 = new ArrayList<>();  row5.add("gree"); row5.add(82);
        List<Object> row6 = new ArrayList<>();  row6.add("sics"); row6.add(83);
        tableData.put("company", new SaphanaColumnData().insert(row4).insert(row5).insert(row5));

    }

    public static List<String>  getColumnDatas(String tableName){
        List<String> list = new ArrayList<>();
        if(tableData.get(tableName) != null){
            List<List<Object>> datas = tableData.get(tableName).getDatas();
            for(List<Object> rows :  datas){
                String type0 = rows.get(0).getClass().getSimpleName();
                String type1 = rows.get(1).getClass().getSimpleName();
                String line = rows.get(0).toString() + ", " + rows.get(1).toString();
                list.add(line);
            }
        }
        return list;
    }

}
