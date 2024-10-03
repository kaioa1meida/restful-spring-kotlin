package org.starcode.integrationtests.controller.withyaml

import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.config.EncoderConfig
import io.restassured.config.RestAssuredConfig
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.http.ContentType
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.context.SpringBootTest
import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.controller.withyaml.mapper.YAMLMapper
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.PersonVO
import org.starcode.integrationtests.vo.TokenVO

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class PersonControllerYamlTest : AbstractIntegrationTest() {

    private lateinit var specification: RequestSpecification
    private lateinit var objectMapper: YAMLMapper
    private lateinit var personVO: PersonVO

    @BeforeAll
    fun setupTests() {
        objectMapper = YAMLMapper() // Converte as Responses(String) para Objetos(PersonVO) Java
        personVO = PersonVO()
    }

    @Test
    @Order(0)
    fun testLogin() {
        val user = AccountCredentialsVO(
            username = "teste",
            password = "teste"
        )

        val token = given()
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
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .body(user, objectMapper)
            .`when`()
            .post()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(TokenVO::class.java, objectMapper)
            .accessToken

        specification = RequestSpecBuilder()
            .addHeader(TestConfigs.HEADER_PARAM_AUTHORIZATION, "Bearer $token")
                .setBasePath("/api/v1/person")
            .setPort(TestConfigs.SERVER_PORT)
                .addFilter(RequestLoggingFilter(LogDetail.ALL))
                .addFilter(ResponseLoggingFilter(LogDetail.ALL))
            .build()
    }

    @Test
    @Order(1)
    fun testCreatePerson() {
        mockPerson()

        val createdPerson = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .accept(TestConfigs.CONTENT_TYPE_YAML)
            .body(personVO, objectMapper)
            .`when`()
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        personVO = createdPerson

        assertNotNull(createdPerson.id)
        assertNotNull(createdPerson.firstName)
        assertNotNull(createdPerson.lastName)
        assertNotNull(createdPerson.address)
        assertNotNull(createdPerson.gender)
        assertNotNull(createdPerson.enabled)

        assertTrue(createdPerson.id > 0)

        assertEquals("James", createdPerson.firstName)
        assertEquals("Gosling", createdPerson.lastName)
        assertEquals("Calgary, Canadá", createdPerson.address)
        assertEquals("Male", createdPerson.gender)
        assertEquals(true, createdPerson.enabled)
    }

    @Test
    @Order(2)
    fun testDisablePerson() {

        val receivedPerson = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .pathParam("id", personVO.id)
            .`when`()
            .patch("{id}")
            .then()
            .statusCode(202)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        assertNotNull(receivedPerson.id)
        assertNotNull(receivedPerson.firstName)
        assertNotNull(receivedPerson.lastName)
        assertNotNull(receivedPerson.address)
        assertNotNull(receivedPerson.gender)
        assertNotNull(receivedPerson.enabled)

        assertTrue(receivedPerson.id > 0)

        assertEquals("James", receivedPerson.firstName)
        assertEquals("Gosling", receivedPerson.lastName)
        assertEquals("Calgary, Canadá", receivedPerson.address)
        assertEquals("Male", receivedPerson.gender)
        assertEquals(false, receivedPerson.enabled)
    }

    @Test
    @Order(3)
    fun testFindOnePerson() {

        val receivedPerson = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .accept(TestConfigs.CONTENT_TYPE_YAML)
            .pathParam("id", personVO.id)
            .`when`()
            .get("{id}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        assertNotNull(receivedPerson.id)
        assertNotNull(receivedPerson.firstName)
        assertNotNull(receivedPerson.lastName)
        assertNotNull(receivedPerson.address)
        assertNotNull(receivedPerson.gender)
        assertNotNull(receivedPerson.enabled)

        assertTrue(receivedPerson.id > 0)

        assertEquals("James", receivedPerson.firstName)
        assertEquals("Gosling", receivedPerson.lastName)
        assertEquals("Calgary, Canadá", receivedPerson.address)
        assertEquals("Male", receivedPerson.gender)
        assertEquals(false, receivedPerson.enabled)

    }

    @Test
    @Order(4)
    fun testUpdatePerson() {

        val dataToUpdate = personVO

        dataToUpdate.firstName = "James"
        dataToUpdate.lastName = "Gosling 'Java Father'"
        dataToUpdate.address = "Calgary, CA, Canadá"
        dataToUpdate.gender = "Male"

        val updatedPerson = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .accept(TestConfigs.CONTENT_TYPE_YAML)
            .body(dataToUpdate, objectMapper)
            .`when`()
            .put()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        assertNotNull(updatedPerson.id)
        assertNotNull(updatedPerson.firstName)
        assertNotNull(updatedPerson.lastName)
        assertNotNull(updatedPerson.address)
        assertNotNull(updatedPerson.gender)
        assertNotNull(updatedPerson.enabled)

        assertTrue(updatedPerson.id > 0)

        assertEquals(personVO.id, updatedPerson.id)
        assertEquals("James", updatedPerson.firstName)
        assertEquals("Gosling 'Java Father'", updatedPerson.lastName)
        assertEquals("Calgary, CA, Canadá", updatedPerson.address)
        assertEquals("Male", updatedPerson.gender)
        assertEquals(false, updatedPerson.enabled)

    }

    @Test
    @Order(5)
    fun testDeletePerson() {

        given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specification)
            .pathParam("id", personVO.id)
            .`when`()
            .delete("{id}")
            .then()
            .statusCode(204)
    }
    @Test
    @Order(6)
    fun findAllPersons() {
        val persons = given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .accept(TestConfigs.CONTENT_TYPE_YAML)
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(Array<PersonVO>::class.java, objectMapper)

        val person1 = persons[0]
        val person2 = persons[3]
        val person3 = persons[5]

        assertEquals("Ayrton", person1.firstName)
        assertEquals("Senna", person1.lastName)
        assertEquals("São Paulo", person1.address)
        assertEquals("Male", person1.gender)
        assertEquals(true, person1.enabled)


        assertEquals("Mahatma", person2.firstName)
        assertEquals("Gandhi", person2.lastName)
        assertEquals("Porbandar - India", person2.address)
        assertEquals("Male", person2.gender)
        assertEquals(true, person2.enabled)


        assertEquals("Nelson", person3.firstName)
        assertEquals("Mandela", person3.lastName)
        assertEquals("Mvezo - South Africa", person3.address)
        assertEquals("Male", person3.gender)
        assertEquals(true, person3.enabled)
    }


    @Test
    @Order(7)
    fun findAllPersonsWithoutToken() {

        val specificationWithoutToken = RequestSpecBuilder()
            .setBasePath("/api/v1/person")
            .setPort(TestConfigs.SERVER_PORT)
            .addFilter(RequestLoggingFilter(LogDetail.ALL))
            .addFilter(ResponseLoggingFilter(LogDetail.ALL))
            .build()

        given()
            .config(
                RestAssuredConfig
                    .config()
                    .encoderConfig(
                        EncoderConfig.encoderConfig()
                            .encodeContentTypeAs(TestConfigs.CONTENT_TYPE_YAML, ContentType.TEXT)
                    )
            )
            .spec(specificationWithoutToken)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .accept(TestConfigs.CONTENT_TYPE_YAML)
            .`when`()
            .get()
            .then()
            .statusCode(403)
    }

    private fun mockPerson() {
        personVO.firstName = "James"
        personVO.lastName = "Gosling"
        personVO.address = "Calgary, Canadá"
        personVO.gender = "Male"
    }
}