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
import org.testng.annotations.Test;
import com.facebook.presto.example.*;
import java.util.*;

/***
 * mock:
 * select name, age from hana.sics_pod_schema.user union all select text , value from hana.sics_pod_schema.company
 */
public class TestSaphanaColumnData
{
    @Test
    public void testColumnData(){
        Map<String, SaphanaColumnData> tableData = SaphanaColumnData.tableData;
        //System.out.println(JSONObject.toJSONString(tableData));
        for (Map.Entry<String, SaphanaColumnData> map : tableData.entrySet()) {

            String key = map.getKey();

            System.out.println(key);
            List<String> list = SaphanaColumnData.getColumnDatas(key);
            for (String s: list) {
                System.out.println(s);
            }
        }
    }
}
