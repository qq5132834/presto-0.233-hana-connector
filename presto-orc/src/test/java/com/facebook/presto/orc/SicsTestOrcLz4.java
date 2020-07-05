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
package com.facebook.presto.orc;

import com.facebook.presto.orc.cache.StorageOrcFileTailSource;
import com.facebook.presto.spi.block.Block;
import com.facebook.presto.spi.type.Type;
import com.facebook.presto.spi.type.VarcharType;
import com.google.common.collect.ImmutableMap;
import com.sun.org.apache.xpath.internal.operations.String;
import io.airlift.slice.Slice;
import io.airlift.units.DataSize;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import java.util.Map;

import static com.facebook.presto.hive.HiveFileContext.DEFAULT_HIVE_FILE_CONTEXT;
import static com.facebook.presto.memory.context.AggregatedMemoryContext.newSimpleAggregatedMemoryContext;
import static com.facebook.presto.orc.OrcEncoding.ORC;
import static com.facebook.presto.orc.OrcReader.INITIAL_BATCH_SIZE;
import static com.facebook.presto.orc.metadata.CompressionKind.LZ4;
import static com.facebook.presto.spi.type.BigintType.BIGINT;
import static com.facebook.presto.spi.type.VarcharType.VARCHAR;
import static com.facebook.presto.spi.type.IntegerType.INTEGER;
import static com.google.common.io.Resources.getResource;
import static com.google.common.io.Resources.toByteArray;
import static io.airlift.units.DataSize.Unit.MEGABYTE;
import static java.lang.Math.toIntExact;
import static org.testng.Assert.assertEquals;

public class SicsTestOrcLz4
{
    private static final DataSize SIZE = new DataSize(1, MEGABYTE);

    @Test
    public void testReadLz4()
            throws Exception
    {
        // this file was written with Apache ORC
        // TODO: use Apache ORC library in OrcTester
        byte[] data = toByteArray(getResource("sics.orc"));

        OrcReader orcReader = new OrcReader(
                new InMemoryOrcDataSource(data),
                ORC,
                new StorageOrcFileTailSource(),
                new StorageStripeMetadataSource(),
                new OrcReaderOptions(
                        SIZE,
                        SIZE,
                        SIZE,
                        false),
                DEFAULT_HIVE_FILE_CONTEXT);

//        assertEquals(orcReader.getCompressionKind(), LZ4);
//        assertEquals(orcReader.getFooter().getNumberOfRows(), 10_000);

        Map<Integer, Type> includedColumns = ImmutableMap.<Integer, Type>builder()
                .put(0, VarcharType.VARCHAR)
                .put(1, INTEGER)
                .build();

        OrcBatchRecordReader reader = orcReader.createBatchRecordReader(
                includedColumns,
                OrcPredicate.TRUE,
                DateTimeZone.UTC,
                newSimpleAggregatedMemoryContext(),
                INITIAL_BATCH_SIZE,
                DEFAULT_HIVE_FILE_CONTEXT);

        int rows = 0;
        while (true) {
            int batchSize = reader.nextBatch();
            if (batchSize <= 0) {
                break;
            }
            rows += batchSize;

            Block xBlock = reader.readBlock(0);
            Block yBlock = reader.readBlock(1);
//            Block zBlock = reader.readBlock(2);

            for (int position = 0; position < batchSize; position++) {

               // Long long1 = VARCHAR.getLong(yBlock, position);
                //Object obj = VARCHAR.getObject(xBlock, position);
                Slice slice = VARCHAR.getSlice(xBlock,position);
                Long age =  INTEGER.getLong(yBlock, position);
                byte[] bs = slice.getBytes();
                java.lang.String str = new java.lang.String(bs);
                System.out.println(str);
                System.out.println(age);
            }
        }
        System.out.println("rows:" + rows);
        assertEquals(rows, reader.getFileRowCount());
    }

    private static class InMemoryOrcDataSource
            extends AbstractOrcDataSource
    {
        private final byte[] data;

        public InMemoryOrcDataSource(byte[] data)
        {
            super(new OrcDataSourceId("memory"), data.length, SIZE, SIZE, SIZE, false);
            this.data = data;
        }

        @Override
        protected void readInternal(long position, byte[] buffer, int bufferOffset, int bufferLength)
        {
            System.arraycopy(data, toIntExact(position), buffer, bufferOffset, bufferLength);
        }
    }
}