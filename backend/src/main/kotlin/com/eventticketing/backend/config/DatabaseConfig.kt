package com.eventticketing.backend.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import javax.sql.DataSource

@Configuration
class DatabaseConfig {

    @Value("\${spring.datasource.url}")
    private lateinit var jdbcUrl: String

    @Value("\${spring.datasource.username}")
    private lateinit var username: String

    @Value("\${spring.datasource.password}")
    private lateinit var password: String

    @Value("\${spring.datasource.driver-class-name}")
    private lateinit var driverClassName: String

    @Value("\${spring.datasource.hikari.connection-timeout:30000}")
    private var connectionTimeout: Long = 30000

    @Value("\${spring.datasource.hikari.idle-timeout:600000}")
    private var idleTimeout: Long = 600000

    @Value("\${spring.datasource.hikari.max-lifetime:1800000}")
    private var maxLifetime: Long = 1800000

    @Value("\${spring.datasource.hikari.maximum-pool-size:10}")
    private var maximumPoolSize: Int = 10

    @Value("\${spring.datasource.hikari.minimum-idle:5}")
    private var minimumIdle: Int = 5

    @Bean
    @Primary
    fun dataSource(): DataSource {
        val config = HikariConfig()
        config.jdbcUrl = jdbcUrl
        config.username = username
        config.password = password
        config.driverClassName = driverClassName
        config.connectionTimeout = connectionTimeout
        config.idleTimeout = idleTimeout
        config.maxLifetime = maxLifetime
        config.maximumPoolSize = maximumPoolSize
        config.minimumIdle = minimumIdle
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
        config.addDataSourceProperty("useServerPrepStmts", "true")
        config.addDataSourceProperty("useLocalSessionState", "true")
        config.addDataSourceProperty("rewriteBatchedStatements", "true")
        config.addDataSourceProperty("cacheResultSetMetadata", "true")
        config.addDataSourceProperty("cacheServerConfiguration", "true")
        config.addDataSourceProperty("elideSetAutoCommits", "true")
        config.addDataSourceProperty("maintainTimeStats", "false")
        
        return HikariDataSource(config)
    }
} 