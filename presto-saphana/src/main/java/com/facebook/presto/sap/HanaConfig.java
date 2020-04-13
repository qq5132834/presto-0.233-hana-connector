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

import com.facebook.airlift.configuration.Config;
import com.facebook.airlift.log.Logger;
import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;

import javax.validation.constraints.NotNull;

public class HanaConfig
    extends BaseJdbcConfig
{
    private static final Logger log = Logger.get(HanaConfig.class);

    public HanaConfig() {
        super();
    }

    @Override
    public @NotNull String getConnectionUrl() {
        return super.getConnectionUrl();
    }

    @Override
    public String getConnectionUser() {
        return super.getConnectionUser();
    }

    @Override
    public String getConnectionPassword() {
        return super.getConnectionPassword();
    }

    @Config("connection-url")
    @Override
    public BaseJdbcConfig setConnectionUrl(String connectionUrl) {
        log.info("connectionUrl:" + connectionUrl);
        return super.setConnectionUrl(connectionUrl);
    }

    @Config("connection-user")
    @Override
    public BaseJdbcConfig setConnectionUser(String connectionUser) {
        log.info("connectionUser:" + connectionUser);
        return super.setConnectionUser(connectionUser);
    }

    @Config("connection-password")
    @Override
    public BaseJdbcConfig setConnectionPassword(String connectionPassword) {
        log.info("connectionPassword:" + connectionPassword);
        return super.setConnectionPassword(connectionPassword);
    }

}
