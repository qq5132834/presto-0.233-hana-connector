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
import com.facebook.presto.spi.RecordCursor;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.CountingInputStream;
import io.airlift.slice.Slice;
import io.airlift.slice.Slices;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.BooleanType.BOOLEAN;
import static com.facebook.presto.spi.type.DoubleType.DOUBLE;
import static com.facebook.presto.spi.type.VarcharType.createUnboundedVarcharType;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import static java.nio.charset.StandardCharsets.UTF_8;

public class SaphanaRecordCursor
        implements RecordCursor
{
    private static final Logger log = Logger.get(SaphanaRecordCursor.class);
    private static final Splitter LINE_SPLITTER = Splitter.on(",").trimResults();

    private final List<SaphanaColumnHandle> columnHandles;
    private final int[] fieldToColumnIndex;

    private final Iterator<String> lines;
    private final long totalBytes;

    private List<String> fields;

    public SaphanaRecordCursor(List<SaphanaColumnHandle> columnHandles)
    {
        this.columnHandles = columnHandles;

        fieldToColumnIndex = new int[columnHandles.size()];
        for (int i = 0; i < columnHandles.size(); i++) {
            SaphanaColumnHandle columnHandle = columnHandles.get(i);
            fieldToColumnIndex[i] = columnHandle.getOrdinalPosition();
        }

        String str1 = "xixi, 1";
        String str2 = "haha, 2";
        totalBytes = str1.getBytes().length + str2.getBytes().length;
        List<String> list = new ArrayList<>();
        list.add(str1);
        list.add(str2);

        lines = list.iterator();

//        try (CountingInputStream input = new CountingInputStream(byteSource.openStream())) {
//            // lines = byteSource.asCharSource(UTF_8).readLines().iterator();
//            totalBytes = input.getCount();
//
//            List<String> lineList = byteSource.asCharSource(UTF_8).readLines().asList();
//            log.info("lineList-01:" + JSONObject.toJSONString(lineList));
//            if(lineList!=null){
//                List<String> ls = new ArrayList<>();
//                for ( String str : lineList ) {
//                    ls.add(str);
//                }
//                log.info("lineList-02:" + JSONObject.toJSONString(ls));
//                lines = ls.iterator();
//            }
//            else{
//                lines = null;
//            }
//
//        }
//        catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
    }

    @Override
    public long getCompletedBytes()
    {
        return totalBytes;
    }

    @Override
    public long getReadTimeNanos()
    {
        return 0;
    }

    @Override
    public Type getType(int field)
    {
        checkArgument(field < columnHandles.size(), "Invalid field index");
        return columnHandles.get(field).getColumnType();
    }

    @Override
    public boolean advanceNextPosition()
    {
        if (!lines.hasNext()) {
            return false;
        }
        String line = lines.next();
        log.info("advanceNextPosition.line:" + line);
        List<String> ls = LINE_SPLITTER.splitToList(line);
        fields = ls;
        log.info("fields:" + JSONObject.toJSONString(ls));
        return true;
    }

    private String getFieldValue(int field)
    {
        checkState(fields != null, "Cursor has not been advanced yet");

        int columnIndex = fieldToColumnIndex[field];
        return fields.get(columnIndex);
    }

    @Override
    public boolean getBoolean(int field)
    {
        checkFieldType(field, BOOLEAN);
        return Boolean.parseBoolean(getFieldValue(field));
    }

    @Override
    public long getLong(int field)
    {
        checkFieldType(field, BIGINT);
        return Long.parseLong(getFieldValue(field));
    }

    @Override
    public double getDouble(int field)
    {
        checkFieldType(field, DOUBLE);
        return Double.parseDouble(getFieldValue(field));
    }

    @Override
    public Slice getSlice(int field)
    {
        checkFieldType(field, createUnboundedVarcharType());
        return Slices.utf8Slice(getFieldValue(field));
    }

    @Override
    public Object getObject(int field)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isNull(int field)
    {
        checkArgument(field < columnHandles.size(), "Invalid field index");
        return Strings.isNullOrEmpty(getFieldValue(field));
    }

    private void checkFieldType(int field, Type expected)
    {
        Type actual = getType(field);
        checkArgument(actual.equals(expected), "Expected field %s to be type %s but is %s", field, expected, actual);
    }

    @Override
    public void close()
    {
    }
}
