package org.starcode.integrationtests.testcontainers

import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.core.env.MapPropertySource
import org.springframework.test.context.ContextConfiguration
import org.testcontainers.containers.MySQLContainer
import org.testcontainers.lifecycle.Startables
import java.util.stream.Stream

@ContextConfiguration(initializers = [AbstractIntegrationTest.Initializer::class])
open class AbstractIntegrationTest {

    internal class Initializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

        override fun initialize(applicationContext: ConfigurableApplicationContext) {
            startContainers()

            val environment = applicationContext.environment
            val testContainers = MapPropertySource(
                "testcontainers", createConnectionConfiguration()
            )
            environment.propertySources.addFirst(testContainers)
        }

        companion object {

            private lateinit var mysql: MySQLContainer<*>

            private fun startContainers() {
                mysql = MySQLContainer("mysql:8.0.28").apply {
                    withDatabaseName("test")
                    withUsername("user")
                    withPassword("password")
                }
                Startables.deepStart(Stream.of(mysql)).join()
            }

            private fun createConnectionConfiguration(): MutableMap<String, Any> {
                return mutableMapOf(
                    "spring.datasource.url" to mysql.jdbcUrl,
                    "spring.datasource.username" to mysql.username,
                    "spring.datasource.password" to mysql.password
                )
            }
        }
    }
}
