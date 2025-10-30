/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.yourorg.fineract.plugins.overdraftproduct.config;

import javax.sql.DataSource;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibase;
import org.apache.fineract.infrastructure.core.service.migration.ExtendedSpringLiquibaseFactory;
import org.apache.fineract.infrastructure.core.service.migration.TenantDatabaseUpgradeService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@AutoConfiguration
@ComponentScan(basePackages = "com.yourorg.fineract.plugins.overdraftproduct")
@EntityScan(basePackages = "com.yourorg.fineract.plugins.overdraftproduct.domain")
@EnableJpaRepositories(basePackages = "com.yourorg.fineract.plugins.overdraftproduct.domain.repositories")
@EnableConfigurationProperties(OverdraftPluginProperties.class)
@ConditionalOnProperty(prefix = "plugin.overdraft", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OverdraftAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "plugin.overdraft", name = "liquibase-enabled", havingValue = "true", matchIfMissing = true)
    public ExtendedSpringLiquibase overdraftLiquibase(@Qualifier("hikariTenantDataSource") DataSource dataSource,
            ExtendedSpringLiquibaseFactory liquibaseFactory) {
        ExtendedSpringLiquibase liquibase = liquibaseFactory.create(dataSource, TenantDatabaseUpgradeService.TENANT_DB_CONTEXT,
                TenantDatabaseUpgradeService.CUSTOM_CHANGELOG_CONTEXT);
        liquibase.setChangeLog("classpath:db/changelog/db.changelog-overdraft.xml");
        liquibase.setShouldRun(true);
        return liquibase;
    }
}
