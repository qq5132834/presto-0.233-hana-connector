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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorSplit;
import com.facebook.presto.spi.RecordSet;
import com.facebook.presto.spi.connector.ConnectorRecordSetProvider;
import com.facebook.presto.spi.connector.ConnectorTransactionHandle;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;

import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class SaphanaRecordSetProvider
        implements ConnectorRecordSetProvider
{
    private static final Logger logger = Logger.get(SaphanaRecordSetProvider.class);
    private final String connectorId;

    @Inject
    public SaphanaRecordSetProvider(SaphanaConnectorId connectorId)
    {
        this.connectorId = requireNonNull(connectorId, "connectorId is null").toString();
    }

    @Override
    public RecordSet getRecordSet(ConnectorTransactionHandle transactionHandle,
                                  ConnectorSession session,
                                  ConnectorSplit split,
                                  List<? extends ColumnHandle> columns)
    {
        logger.info("getRecordSet");

        {
            requireNonNull(split, "partitionChunk is null");
            SaphanaSplit saphanaSplit = (SaphanaSplit) split;
            checkArgument(saphanaSplit.getConnectorId().equals(connectorId), "split is not for this connector");

            ImmutableList.Builder<SaphanaColumnHandle> handles = ImmutableList.builder();
            for (ColumnHandle handle : columns) {
                handles.add((SaphanaColumnHandle) handle);
            }
            return new SaphanaRecordSet(saphanaSplit, new SaphanaTableHandle(saphanaSplit.getConnectorId(), saphanaSplit.getSchemaName(), saphanaSplit.getTableName()) ,handles.build());
        }

    }
}
