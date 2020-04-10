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

import com.facebook.airlift.log.Logger;
import com.facebook.presto.spi.ColumnHandle;
import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.ConnectorSession;
import com.facebook.presto.spi.ConnectorTableHandle;
import com.facebook.presto.spi.ConnectorTableLayout;
import com.facebook.presto.spi.ConnectorTableLayoutHandle;
import com.facebook.presto.spi.ConnectorTableLayoutResult;
import com.facebook.presto.spi.ConnectorTableMetadata;
import com.facebook.presto.spi.Constraint;
import com.facebook.presto.spi.SchemaTableName;
import com.facebook.presto.spi.SchemaTablePrefix;
import com.facebook.presto.spi.TableNotFoundException;
import com.facebook.presto.spi.connector.ConnectorMetadata;
import com.facebook.presto.spi.type.Type;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import javax.inject.Inject;

import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

public class ExampleMetadata
        implements ConnectorMetadata
{
    private static final Logger log = Logger.get(ExampleMetadata.class);
    private final String connectorId;

    private final ExampleClient exampleClient;

    private final SaphanaClient saphanaClient;

    @Inject
    public ExampleMetadata(ExampleConnectorId connectorId, ExampleClient exampleClient, SaphanaClient saphanaClient)
    {
        this.connectorId = requireNonNull(connectorId, "connectorId is null").toString();
        this.exampleClient = requireNonNull(exampleClient, "client is null");
        this.saphanaClient = requireNonNull(saphanaClient, "saphana client is null");
    }

    /***
     *
     * 第2步调用
     *
     * show schemas from catalogName
     *
     * @param session
     * @return
     */
    @Override
    public List<String> listSchemaNames(ConnectorSession session)
    {
        log.info("listSchemaNames");
        return listSchemaNames();
    }

    public List<String> listSchemaNames()
    {
        //http
        List<String> list = new ArrayList<>();
        list.addAll(ImmutableList.copyOf(exampleClient.getSchemaNames()));

        //添加一个hana的schema信息
        list.add(SaphanaClient.getSCHEMA());

        return list;
    }

    /***
     * 第1步调用
     *
     *
     * 获取SQL中的表实例，ExampleTableHandle中实现了com.facebook.presto.spi.ConnectorTableHandle接口，
     * 在获取分片和表字段的信息时需要用到该实例
     *
     * @param session
     * @param tableName
     * @return ExampleTableHandle
     */
    @Override
    public ExampleTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName)
    {
        log.info("getTableHandle.schema:" + tableName.getSchemaName() + ",tableName:" + tableName.getTableName());

        if (!listSchemaNames(session).contains(tableName.getSchemaName())) {
            return null;
        }

        ExampleTable table = exampleClient.getTable(tableName.getSchemaName(), tableName.getTableName());
        if (table == null) {
            return null;
        }

        return new ExampleTableHandle(connectorId, tableName.getSchemaName(), tableName.getTableName());
    }

    @Override
    public List<ConnectorTableLayoutResult> getTableLayouts(ConnectorSession session, ConnectorTableHandle table, Constraint<ColumnHandle> constraint, Optional<Set<ColumnHandle>> desiredColumns)
    {
        log.info("getTableLayouts");
        ExampleTableHandle tableHandle = (ExampleTableHandle) table;
        ConnectorTableLayout layout = new ConnectorTableLayout(new ExampleTableLayoutHandle(tableHandle));
        return ImmutableList.of(new ConnectorTableLayoutResult(layout, constraint.getSummary()));
    }

    @Override
    public ConnectorTableLayout getTableLayout(ConnectorSession session, ConnectorTableLayoutHandle handle)
    {
        log.info("getTableLayout");
        return new ConnectorTableLayout(handle);
    }

    /***
     * 第3步调用
     * 获取表的元数据，不要包含了表字段信息，所在的schema／owner等
     *
     * @param session
     * @param table
     * @return
     */
    @Override
    public ConnectorTableMetadata getTableMetadata(ConnectorSession session, ConnectorTableHandle table)
    {
        log.info("getTableMetadata");
        ExampleTableHandle exampleTableHandle = (ExampleTableHandle) table;
        checkArgument(exampleTableHandle.getConnectorId().equals(connectorId), "tableHandle is not for this connector");
        SchemaTableName tableName = new SchemaTableName(exampleTableHandle.getSchemaName(), exampleTableHandle.getTableName());

        return getTableMetadata(tableName);
    }

    /***
     * show tables [from catalogName.schemaName]
     *
     * @param session
     * @param schemaNameOrNull
     * @return
     */
    @Override
    public List<SchemaTableName> listTables(ConnectorSession session, String schemaNameOrNull)
    {
        log.info("listTables");
        Set<String> schemaNames;
        if (schemaNameOrNull != null) {
            schemaNames = ImmutableSet.of(schemaNameOrNull);
        }
        else {
            schemaNames = exampleClient.getSchemaNames();
        }

        List<SchemaTableName> builder = new ArrayList<>();
        for (String schemaName : schemaNames) {
            //http
            for (String tableName : exampleClient.getTableNames(schemaName)) {
                log.info("tableName:" + tableName );
                builder.add(new SchemaTableName(schemaName, tableName));
            }
        }

        for (String schemaName : schemaNames) {
            //获取hana中schema下的表
            if(SaphanaClient.getSCHEMA().equals(schemaName)){
                builder.clear();
                List<String> list = this.saphanaClient.getTables("sics_pod_schema");
                for (String tn: list) {
                    log.info("tn:" + tn);
                    builder.add(new SchemaTableName(schemaName, tn));
                }
            }
        }

        return builder;
    }

    /***
     * 第4步调用
     * 获取表的列信息【表中全部的字段信息】
     *
     * @param session
     * @param tableHandle
     * @return
     */
    @Override
    public Map<String, ColumnHandle> getColumnHandles(ConnectorSession session, ConnectorTableHandle tableHandle)
    {
        log.info("getColumnHandles");
        ExampleTableHandle exampleTableHandle = (ExampleTableHandle) tableHandle;
        checkArgument(exampleTableHandle.getConnectorId().equals(connectorId), "tableHandle is not for this connector");

        ExampleTable table = this.exampleClient.getTable(exampleTableHandle.getSchemaName(), exampleTableHandle.getTableName());

        if (table == null) {
            throw new TableNotFoundException(exampleTableHandle.toSchemaTableName());
        }

        Map<String, ColumnHandle> map = new HashMap<>();
        //ImmutableMap.Builder<String, ColumnHandle> columnHandles = ImmutableMap.builder();
        int index = 0;
        for (ColumnMetadata column : table.getColumnsMetadata()) {
            map.put(column.getName(), new ExampleColumnHandle(connectorId, column.getName(), column.getType(), index));
            log.info("column.getName:" + column.getName());
            //columnHandles.put(column.getName(), new ExampleColumnHandle(connectorId, column.getName(), column.getType(), index));
            index++;
        }
        //return columnHandles.build();
        return map;
    }

    /***
     * 获取表的某一列的元数据信息，包含了字段名称，类型等相关信息【select column1, column2】中指定的字段
     * @param session
     * @param tableHandle
     * @param columnHandle
     * @return
     */
    @Override
    public ColumnMetadata getColumnMetadata(ConnectorSession session, ConnectorTableHandle tableHandle, ColumnHandle columnHandle)
    {
        log.info("getColumnMetadata");
        ColumnMetadata columnMetadata = ((ExampleColumnHandle) columnHandle).getColumnMetadata();
        Type type = columnMetadata.getType();
        String columnName = columnMetadata.getName();  //属性名称
        String comment = columnMetadata.getComment();  //字段备注信息
        String displayName = columnMetadata.getType().getDisplayName(); //字段数据属性类型名称
        log.info("columnName:" + columnName + ",comment:" + comment + ",displayName:" + displayName);
        return columnMetadata;
    }

    /***
     * 获取表的字段信息
     *
     * @param session
     * @param prefix
     * @return
     */
    @Override
    public Map<SchemaTableName, List<ColumnMetadata>> listTableColumns(ConnectorSession session, SchemaTablePrefix prefix)
    {
        log.info("listTableColumns");

        requireNonNull(prefix, "prefix is null");
        ImmutableMap.Builder<SchemaTableName, List<ColumnMetadata>> columns = ImmutableMap.builder();
        for (SchemaTableName tableName : listTables(session, prefix)) {
            ConnectorTableMetadata tableMetadata = getTableMetadata(tableName);
            // table can disappear during listing operation
            if (tableMetadata != null) {
                columns.put(tableName, tableMetadata.getColumns());
            }
        }
        return columns.build();
    }

    private ConnectorTableMetadata getTableMetadata(SchemaTableName tableName)
    {
        if (!listSchemaNames().contains(tableName.getSchemaName())) {
            return null;
        }

        ExampleTable table = exampleClient.getTable(tableName.getSchemaName(), tableName.getTableName());
        if (table == null) {
            return null;
        }

        return new ConnectorTableMetadata(tableName, table.getColumnsMetadata());
    }

    private List<SchemaTableName> listTables(ConnectorSession session, SchemaTablePrefix prefix)
    {
        if (prefix.getSchemaName() == null) {
            return listTables(session, prefix.getSchemaName());
        }
        return ImmutableList.of(new SchemaTableName(prefix.getSchemaName(), prefix.getTableName()));
    }


}
