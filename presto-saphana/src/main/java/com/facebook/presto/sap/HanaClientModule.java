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

import com.facebook.presto.plugin.jdbc.BaseJdbcConfig;
import com.facebook.presto.plugin.jdbc.JdbcClient;
import com.facebook.presto.spi.function.OperatorType;
import com.facebook.presto.spi.type.*;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.FromStringDeserializer;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;

import javax.inject.Inject;

import java.lang.invoke.MethodHandle;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.facebook.airlift.configuration.ConfigBinder.configBinder;
import static com.facebook.airlift.json.JsonBinder.jsonBinder;
import static com.facebook.presto.spi.type.TypeSignature.parseTypeSignature;
import static java.util.Objects.requireNonNull;

public class HanaClientModule
        implements Module
{
    @Override
    public void configure(Binder binder)
    {
        binder.bind(JdbcClient.class).to(HanaClient.class).in(Scopes.SINGLETON);
        configBinder(binder).bindConfig(BaseJdbcConfig.class);
        jsonBinder(binder).addDeserializerBinding(Type.class).to(HanaClientModule.TypeDeserializer.class);
    }

    public static final class TypeDeserializer
            extends FromStringDeserializer<Type>
    {
        private final TypeManager typeManager;

        public TypeDeserializer()
        {
            super(Type.class);
            this.typeManager = new TypeManager() {
                @Override
                public Type getType(TypeSignature signature) {
                    return null;
                }

                @Override
                public Type getParameterizedType(String baseTypeName, List<TypeSignatureParameter> typeParameters) {
                    return null;
                }

                @Override
                public List<Type> getTypes() {
                    return null;
                }

                @Override
                public Collection<ParametricType> getParametricTypes() {
                    return null;
                }

                @Override
                public Optional<Type> getCommonSuperType(Type firstType, Type secondType) {
                    return Optional.empty();
                }

                @Override
                public boolean canCoerce(Type actualType, Type expectedType) {
                    return false;
                }

                @Override
                public boolean isTypeOnlyCoercion(Type actualType, Type expectedType) {
                    return false;
                }

                @Override
                public Optional<Type> coerceTypeBase(Type sourceType, String resultTypeBase) {
                    return Optional.empty();
                }

                @Override
                public MethodHandle resolveOperator(OperatorType operatorType, List<? extends Type> argumentTypes) {
                    return null;
                }
            };
        }

        @Override
        protected Type _deserialize(String value, DeserializationContext context)
        {
            return typeManager.getType(parseTypeSignature(value));
        }
    }
}
