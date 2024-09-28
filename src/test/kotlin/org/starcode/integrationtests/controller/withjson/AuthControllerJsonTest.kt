package org.starcode.integrationtests.controller.withjson

import io.restassured.RestAssured.given
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.context.SpringBootTest
import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.TokenVO

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class AuthControllerJsonTest : AbstractIntegrationTest() {

    private lateinit var tokenVO: TokenVO

    @BeforeAll
    fun setupTests() {
        tokenVO = TokenVO()
    }

    @Test
    @Order(0)
    fun testLogin() {
        val user = AccountCredentialsVO(
            username = "teste",
            password = "teste"
        )

        tokenVO = given()
            .basePath("/auth/signin")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .body(user)
            .`when`()
                .post()
                    .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .`as`(TokenVO::class.java)

        assertNotNull(tokenVO.accessToken)
        assertNotNull(tokenVO.refreshToken)
    }


    @Test
    @Order(1)
    fun testRefresh() {

        tokenVO = given()
            .basePath("/auth/refresh")
                .port(TestConfigs.SERVER_PORT)
                .contentType(TestConfigs.CONTENT_TYPE_JSON)
                .pathParam("username", tokenVO.username)
                .header(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer ${tokenVO.refreshToken}")
            .`when`()
                .put("{username}")
                    .then()
                        .statusCode(200)
                        .extract()
                        .body()
                        .`as`(TokenVO::class.java)

        assertNotNull(tokenVO.accessToken)
        assertNotNull(tokenVO.refreshToken)
    }
}