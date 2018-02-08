/**
 * Copyright 2009-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.ibatis.binding.handler.impl;

import java.lang.reflect.Method;

import org.apache.ibatis.annotations.SqlRef;
import org.apache.ibatis.binding.handler.MapperHandlerContext;
import org.apache.ibatis.binding.handler.ParamResolver;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.Configuration;

/**
 * @author Felix Lin
 * @since 3.4.6
 */
public class DefaultMapperHandlerContext implements MapperHandlerContext {

    final Class<?> mapperInterface;
    final Method method;
    final Configuration configuration;
    ParamResolver paramResolver;
    MappedStatement mappedStatement;
    boolean mappedStatementInitMonitor = false;

    public DefaultMapperHandlerContext(Class<?> mapperInterface, Method method, Configuration configuration) {
        this.mapperInterface = mapperInterface;
        this.method = method;
        this.configuration = configuration;
    }

    @Override
    public Class<?> getMapperInterface() {
        return mapperInterface;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public Configuration getConfiguration() {
        return configuration;
    }

    @Override
    public MappedStatement getMappedStatement() {
        if (!mappedStatementInitMonitor) {
            synchronized (method) {
                if (!mappedStatementInitMonitor) {
                    try {
                        String statement = method.getDeclaringClass().getName() + ".";
                        SqlRef sqlRef = method.getAnnotation(SqlRef.class);
                        if (null != sqlRef && sqlRef.value().length() >= 1) {
                            statement += sqlRef.value().length();
                        } else {
                            statement += method.getName();
                        }
                        this.mappedStatement = configuration.getMappedStatement(statement);
                    } catch (Exception e) {
                        this.mappedStatement = null;
                    } finally {
                        mappedStatementInitMonitor = true;
                    }
                }
            }
        }
        return mappedStatement;
    }

    @Override
    public ParamResolver getParamResolver() {
        if (null == paramResolver) {
            synchronized (method) {
                if (null == paramResolver) {
                    this.paramResolver = configuration.getParamResolverFactory().newParamResolver(configuration, method);
                }
            }
        }
        return paramResolver;
    }
}
