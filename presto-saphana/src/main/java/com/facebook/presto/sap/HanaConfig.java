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
        String connectionUrl = super.getConnectionUrl();
        log.info("connectionUrl:" + connectionUrl);
        return connectionUrl;
    }

    @Override
    public String getConnectionUser() {
        String user = super.getConnectionUser();
        log.info("connectionUser:" + user);
        return user;
    }

    @Override
    public String getConnectionPassword() {
        String pwd =  super.getConnectionPassword();
        return pwd;
    }



}
