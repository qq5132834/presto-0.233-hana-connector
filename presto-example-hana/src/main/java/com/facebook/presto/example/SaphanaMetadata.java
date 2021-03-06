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

public class SaphanaMetadata
        implements ConnectorMetadata
{
    private static final Logger log = Logger.get(SaphanaMetadata.class);

    private final String connectorId;
    private final SaphanaClient saphanaClient;

    @Inject
    public SaphanaMetadata(SaphanaConnectorId connectorId, SaphanaClient saphanaClient)
    {
        this.connectorId = requireNonNull(connectorId, "connectorId is null").toString();
        this.saphanaClient = requireNonNull(saphanaClient, "saphana client is null");
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
    public SaphanaTableHandle getTableHandle(ConnectorSession session, SchemaTableName tableName)
    {
        log.info("getTableHandle.schema:" + tableName.getSchemaName() + ",tableName:" + tableName.getTableName());
        log.info("session:" + session.getClass().getName());

        log.info("user:" + session.getUser());
        log.info("extra:" + JSONObject.toJSONString(session.getIdentity().getExtraCredentials()));

        if (!listSchemaNames(session).contains(tableName.getSchemaName())) {
            return null;
        }

        //返回hana的表处理
        SaphanaTable saphanaTable = this.saphanaClient.getTable(tableName.getSchemaName(), tableName.getTableName());
        if (saphanaTable != null) {
            return new SaphanaTableHandle(connectorId,tableName.getSchemaName(), tableName.getTableName());
        }

        return null;

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
        List<String> list = new ArrayList<>();
        list.add(SaphanaClient.SCHEMA);
        log.info("listSchemaNames:" + JSONObject.toJSONString(list));
        return list;
    }


    @Override
    public List<ConnectorTableLayoutResult> getTableLayouts(ConnectorSession session, ConnectorTableHandle table, Constraint<ColumnHandle> constraint, Optional<Set<ColumnHandle>> desiredColumns)
    {
        log.info("getTableLayouts");

        SaphanaTableHandle tableHandle = (SaphanaTableHandle) table;
        ConnectorTableLayout layout = new ConnectorTableLayout(new SaphanaTableLayoutHandle(tableHandle));
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

        SaphanaTableHandle saphanaTableHandle = (SaphanaTableHandle) table;
        checkArgument(saphanaTableHandle.getConnectorId().equals(connectorId), "tableHandle is not for this connector");
        SchemaTableName tableName = saphanaTableHandle.createSchemaTableName();

        return getTableMetadataFromHana(tableName);

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
        log.info("listTables:" + schemaNameOrNull);
        Set<String> schemaNames = null;
        if (schemaNameOrNull != null) {
            schemaNames = ImmutableSet.of(schemaNameOrNull);
        }

        List<SchemaTableName> builder = new ArrayList<>();

        for (String schemaName : schemaNames) {
            //获取hana中schema下的表
            if(SaphanaClient.SCHEMA.equals(schemaName)){
                builder.clear();
                List<String> list = SaphanaClient.SCHEMA_TABLES.get(SaphanaClient.SCHEMA);
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

        SaphanaTableHandle saphanaTableHandle = (SaphanaTableHandle) tableHandle;
        checkArgument(saphanaTableHandle.getConnectorId().equals(connectorId), "tableHandle is not for this connector");
        SaphanaTable saphanaTable = this.saphanaClient.getTable(saphanaTableHandle.getSchemaName(), saphanaTableHandle.getTableName());
        if (saphanaTable == null) {
            throw new TableNotFoundException(saphanaTableHandle.createSchemaTableName());
        }
        Map<String, ColumnHandle> map = new HashMap<>();
        int index = 0;
        for (ColumnMetadata column : saphanaTable.getColumnsMetadata()) {
            map.put(column.getName(), new SaphanaColumnHandle(connectorId, column.getName(), column.getType(), index));
            log.info("column.getName:" + column.getName());
            index++;
        }
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

        ColumnMetadata columnMetadata = ((SaphanaColumnHandle) columnHandle).getColumnMetadata();
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
            ConnectorTableMetadata tableMetadata = getTableMetadataFromHana(tableName);
            // table can disappear during listing operation
            if (tableMetadata != null) {
                columns.put(tableName, tableMetadata.getColumns());
            }
        }
        return columns.build();
    }


    /**
     * 给hana使用
     * @param tableName
     * @return
     */
    private ConnectorTableMetadata getTableMetadataFromHana(SchemaTableName tableName)
    {
        log.info("getTableMetadataFromHana:" + tableName.getSchemaName() + "," + tableName.getTableName());
//        //判断schema是否存在
//        if(SaphanaClient.SCHEMA.equals(tableName.getSchemaName())){
//            return null;
//        }

        SaphanaTable saphanaTable = this.saphanaClient.getTable(tableName.getSchemaName(), tableName.getTableName());
        if (saphanaTable == null) {
            return null;
        }

        log.info("saphanaTable.tableName:" + saphanaTable.getTableName());

        ConnectorTableMetadata connectorTableMetadata = new ConnectorTableMetadata(tableName, saphanaTable.getColumnsMetadata());

        log.info("connectorTableMetadata:");

        return connectorTableMetadata;
    }

    private List<SchemaTableName> listTables(ConnectorSession session, SchemaTablePrefix prefix)
    {
        if (prefix.getSchemaName() == null) {
            return listTables(session, prefix.getSchemaName());
        }
        return ImmutableList.of(new SchemaTableName(prefix.getSchemaName(), prefix.getTableName()));
    }


}
