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
import com.facebook.airlift.json.JsonCodec;
import com.facebook.airlift.log.Logger;
import com.facebook.presto.spi.ColumnMetadata;
import com.facebook.presto.spi.type.Type;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Resources;

import javax.inject.Inject;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Maps.transformValues;
import static com.google.common.collect.Maps.uniqueIndex;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.requireNonNull;

public class ExampleClient
{
    /**
     * SchemaName -> (TableName -> TableMetadata)
     */
    private final Supplier<Map<String, Map<String, ExampleTable>>> schemas;
    private static final Logger log = Logger.get(ExampleClient.class);

    @Inject
    public ExampleClient(ExampleConfig config, JsonCodec<Map<String, List<ExampleTable>>> catalogCodec)
    {
        requireNonNull(config, "config is null");
        requireNonNull(catalogCodec, "catalogCodec is null");

        schemas = Suppliers.memoize(schemasSupplier(catalogCodec, config.getMetadata()));
        log.info("schemas:" + JSONObject.toJSONString(schemas));
    }

    public Set<String> getSchemaNames()
    {
        Set<String> sets = schemas.get().keySet();
        log.info("getSchemaNames:" + JSONObject.toJSONString(sets));
        return sets;
    }

    public Set<String> getTableNames(String schema)
    {
        requireNonNull(schema, "schema is null");
        Map<String, ExampleTable> tables = schemas.get().get(schema);
        if (tables == null) {
            return ImmutableSet.of();
        }
        Set<String> sets = tables.keySet();
        log.info("getTableNames:" + JSONObject.toJSONString(sets));
        return sets;
    }

    public ExampleTable getTable(String schema, String tableName)
    {
        log.info("schema:" + schema + ",tableName:" + tableName);
        requireNonNull(schema, "schema is null");
        requireNonNull(tableName, "tableName is null");
        Map<String, ExampleTable> tables = schemas.get().get(schema);
        if (tables == null) {
            return null;
        }
        return tables.get(tableName);
    }

    private static Supplier<Map<String, Map<String, ExampleTable>>> schemasSupplier(final JsonCodec<Map<String, List<ExampleTable>>> catalogCodec, final URI metadataUri)
    {
        log.info("schemasSupplier");
        return () -> {
            try {
                return lookupSchemas(metadataUri, catalogCodec);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static Map<String, Map<String, ExampleTable>> lookupSchemas(URI metadataUri, JsonCodec<Map<String, List<ExampleTable>>> catalogCodec)
            throws IOException
    {
        log.info("metadataUri:" + metadataUri.toString() + ",catalogcodec:" + JSONObject.toJSONString(catalogCodec));
        URL result = metadataUri.toURL();
        String json = Resources.toString(result, UTF_8);
        log.info("json:" + json);
        Map<String, List<ExampleTable>> catalog = catalogCodec.fromJson(json);
        //log.info("catalog:" + JSONObject.toJSONString(catalog));
        for (Map.Entry<String, List<ExampleTable>> entry : catalog.entrySet()) {
            String key = entry.getKey();
            List<ExampleTable> list = entry.getValue();
            log.info("key:" + key);
            if (list != null) {
                for (ExampleTable exampleTable : list) {
                    String name = exampleTable.getName();
                    log.info("name:" + name);
                    //
                    List<ExampleColumn> columns = exampleTable.getColumns();
                    if (columns != null) {
                        for (ExampleColumn col : columns) {
                            log.info("columnName:" + col.getName());
                            Type type = col.getType();
                            log.info("type.toString:" + type.toString());
                        }
                    }
                    //
                    List<ColumnMetadata> columnMetadatas = exampleTable.getColumnsMetadata();
                    if (columnMetadatas != null) {
                        for (ColumnMetadata data : columnMetadatas) {
                            log.info("data:" + data.toString());
                        }
                    }
                }
            }
        }
        return ImmutableMap.copyOf(transformValues(catalog, resolveAndIndexTables(metadataUri)));
    }

    private static Function<List<ExampleTable>, Map<String, ExampleTable>> resolveAndIndexTables(final URI metadataUri)
    {
        log.info("resolveAndIndexTables");
        return tables -> {
            Iterable<ExampleTable> resolvedTables = transform(tables, tableUriResolver(metadataUri));
            return ImmutableMap.copyOf(uniqueIndex(resolvedTables, ExampleTable::getName));
        };
    }

    private static Function<ExampleTable, ExampleTable> tableUriResolver(final URI baseUri)
    {
        log.info("tableUriResolver");
        return table -> {
            List<URI> sources = ImmutableList.copyOf(transform(table.getSources(), baseUri::resolve));
            return new ExampleTable(table.getName(), table.getColumns(), sources);
        };
    }
}
