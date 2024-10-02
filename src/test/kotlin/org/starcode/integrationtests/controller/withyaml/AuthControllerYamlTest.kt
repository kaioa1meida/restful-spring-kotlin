package org.starcode.integrationtests.controller.withyaml

import io.restassured.RestAssured.given
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.http.ContentType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.context.SpringBootTest
import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.controller.withyaml.mapper.YAMLMapper
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.TokenVO

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class AuthControllerYamlTest : AbstractIntegrationTest() {

    private lateinit var tokenVO: TokenVO

    private lateinit var objectMapper: YAMLMapper

    @BeforeAll
    fun setupTests() {
        tokenVO = TokenVO()
        objectMapper = YAMLMapper()
    }

    @Test
    @Order(0)
    fun testLogin() {
        val user = AccountCredentialsVO(
            username = "teste",
            password = "teste"
        )

        tokenVO = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .accept(TestConfigs.CONTENT_TYPE_YAML)
                .contentType(TestConfigs.CONTENT_TYPE_YAML)
                .body(user, objectMapper)
            .`when`()
                .post()
                    .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .`as`(TokenVO::class.java, objectMapper)

        assertNotNull(tokenVO.accessToken)
        assertNotNull(tokenVO.refreshToken)
    }


    @Test
    @Order(1)
    fun testRefresh() {

        tokenVO = given()
            .config(   // Necessário passar o encoder para serializacao
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .basePath("/auth/refresh")
                .port(TestConfigs.SERVER_PORT)
                .accept(TestConfigs.CONTENT_TYPE_YAML)
                .contentType(TestConfigs.CONTENT_TYPE_YAML)
                .pathParam("username", tokenVO.username)
                .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer ${tokenVO.refreshToken}")
            .`when`()
                .put("{username}")
                    .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .`as`(TokenVO::class.java, objectMapper)

        assertNotNull(tokenVO.accessToken)
        assertNotNull(tokenVO.refreshToken)
    }
}