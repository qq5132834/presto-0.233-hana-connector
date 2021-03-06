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
import com.facebook.presto.plugin.jdbc.*;
import com.facebook.presto.spi.*;
import com.facebook.presto.spi.type.Type;
import com.sap.db.jdbc.Driver;

import javax.inject.Inject;
import java.sql.*;
import java.util.Optional;
import java.util.Set;

import static com.facebook.presto.plugin.jdbc.JdbcErrorCode.JDBC_ERROR;
import static com.facebook.presto.spi.StandardErrorCode.ALREADY_EXISTS;
import static com.facebook.presto.spi.StandardErrorCode.NOT_SUPPORTED;
import static com.facebook.presto.spi.type.VarbinaryType.VARBINARY;
import static com.facebook.presto.spi.type.IntegerType.INTEGER;
import static com.facebook.presto.spi.type.VarcharType.UNBOUNDED_LENGTH;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static java.util.Locale.ENGLISH;

public class HanaClient
        extends BaseJdbcClient
{

    private static final Logger log = Logger.get(HanaClient.class);

    @Inject
    public HanaClient(JdbcConnectorId connectorId, BaseJdbcConfig config)
    {
        super(connectorId, config, "\"", new DriverConnectionFactory(new Driver(), config));
    }

    @Override
    public PreparedStatement getPreparedStatement(Connection connection, String sql)
            throws SQLException
    {
        log.info("getPreparedStatement.sql:" + sql);
        connection.setAutoCommit(false);
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setFetchSize(1000);
        return statement;
    }

    @Override
    protected ResultSet getTables(Connection connection, Optional<String> schemaName, Optional<String> tableName)
            throws SQLException
    {
        DatabaseMetaData metadata = connection.getMetaData();
        Optional<String> escape = Optional.ofNullable(metadata.getSearchStringEscape());
        return metadata.getTables(
                connection.getCatalog(),
                escapeNamePattern(schemaName, escape).orElse(null),
                escapeNamePattern(tableName, escape).orElse(null),
                new String[] {"TABLE"});
    }

    @Override
    public void createTable(ConnectorSession session, ConnectorTableMetadata tableMetadata) {
        throw new PrestoException(NOT_SUPPORTED, "createTable");
    }

    @Override
    protected JdbcOutputTableHandle createTable(ConnectorTableMetadata tableMetadata, ConnectorSession session, String tableName) throws SQLException {
        throw new PrestoException(NOT_SUPPORTED, "createTable.");
    }

    @Override
    public void dropTable(JdbcIdentity identity, JdbcTableHandle handle) {
        throw new PrestoException(NOT_SUPPORTED, "dropTable");
    }


    @Override
    public void renameTable(JdbcIdentity identity, JdbcTableHandle handle, SchemaTableName newTable) {
        throw new PrestoException(NOT_SUPPORTED, "renameTable");
    }

    @Override
    protected void renameTable(JdbcIdentity identity, String catalogName, SchemaTableName oldTable, SchemaTableName newTable) {
        throw new PrestoException(NOT_SUPPORTED, "renameTable.");
    }

    @Override
    public void addColumn(JdbcIdentity identity, JdbcTableHandle handle, ColumnMetadata column) {
        throw new PrestoException(NOT_SUPPORTED, "addColumn");
    }

    @Override
    public void renameColumn(JdbcIdentity identity, JdbcTableHandle handle, JdbcColumnHandle jdbcColumn, String newColumnName) {
        throw new PrestoException(NOT_SUPPORTED, "renameColumn");
    }

    @Override
    public void dropColumn(JdbcIdentity identity, JdbcTableHandle handle, JdbcColumnHandle column) {
        throw new PrestoException(NOT_SUPPORTED, "dropColumn");
    }



}
