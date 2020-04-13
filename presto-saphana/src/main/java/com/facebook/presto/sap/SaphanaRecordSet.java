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

import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.RecordSet;
import com.facebook.presto.spi.type.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.io.ByteSource;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class SaphanaRecordSet
        implements RecordSet
{
    private final SaphanaTableHandle tableHandle;
    private final List<SaphanaColumnHandle> columnHandles;
    private final List<Type> columnTypes;

    private final List<String> list;
    private final ByteSource byteSource = null;

    public SaphanaRecordSet(SaphanaSplit split, SaphanaTableHandle tableHandle,  List<SaphanaColumnHandle> columnHandles)
    {
        requireNonNull(split, "split is null");

        this.tableHandle = requireNonNull(tableHandle, "table is null");
        this.columnHandles = requireNonNull(columnHandles, "column handles is null");
        ImmutableList.Builder<Type> types = ImmutableList.builder();
        for (SaphanaColumnHandle column : columnHandles) {
            types.add(column.getColumnType());
        }

        this.columnTypes = types.build();


        //TODO 这里根据sql查询不同的数据。byteSource就是用来存储数据用的，然后将bytesource传递给 RecordCursor 通过游标遍历
        list = SaphanaColumnData.getColumnDatas(tableHandle.getTableName());
//        try {
//            byteSource = Resources.asByteSource(split.getUri().toURL());
//        }
//        catch (MalformedURLException e) {
//            throw new RuntimeException(e);
//        }
    }


    @Override
    public List<Type> getColumnTypes()
    {
        return columnTypes;
    }

    @Override
    public RecordCursor cursor()
    {
        return new SaphanaRecordCursor(columnHandles, list);
    }
}
