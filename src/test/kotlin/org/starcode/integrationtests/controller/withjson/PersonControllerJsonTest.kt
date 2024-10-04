package org.starcode.integrationtests.controller.withjson

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
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
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.PersonVO
import org.starcode.integrationtests.vo.TokenVO
import org.starcode.integrationtests.vo.wrappers.WrapperPersonVO

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class PersonControllerJsonTest : AbstractIntegrationTest() {

    private lateinit var specification: RequestSpecification
    private lateinit var objectMapper: ObjectMapper
    private lateinit var personVO: PersonVO

    @BeforeAll
    fun setupTests() {
        objectMapper = ObjectMapper() // Converte as Responses(String) para Objetos(PersonVO) Java
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES) // Ignora a propriedade _links do HATEOAS vindos na response
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

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .body(personVO)
            .`when`()
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .body()
            .asString()

        val createdPerson = objectMapper.readValue(content, PersonVO::class.java)

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

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .pathParam("id", personVO.id)
            .`when`()
            .patch("{id}")
            .then()
            .statusCode(202)
            .extract()
            .body()
            .asString()

        val updatedPerson = objectMapper.readValue(content, PersonVO::class.java)

        assertNotNull(updatedPerson.id)
        assertNotNull(updatedPerson.firstName)
        assertNotNull(updatedPerson.lastName)
        assertNotNull(updatedPerson.address)
        assertNotNull(updatedPerson.gender)
        assertNotNull(updatedPerson.enabled)

        assertTrue(updatedPerson.id > 0)

        assertEquals("James", updatedPerson.firstName)
        assertEquals("Gosling", updatedPerson.lastName)
        assertEquals("Calgary, Canadá", updatedPerson.address)
        assertEquals("Male", updatedPerson.gender)
        assertEquals(false, updatedPerson.enabled)
    }

    @Test
    @Order(3)
    fun testFindOnePerson() {

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .pathParam("id", personVO.id)
            .`when`()
            .get("{id}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val receivedPerson = objectMapper.readValue(content, PersonVO::class.java)

        assertNotNull(receivedPerson.id)
        assertNotNull(receivedPerson.firstName)
        assertNotNull(receivedPerson.lastName)
        assertNotNull(receivedPerson.address)
        assertNotNull(receivedPerson.gender)

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

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .body(dataToUpdate)
            .`when`()
            .put()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val updatedPerson = objectMapper.readValue(content, PersonVO::class.java)

        assertNotNull(updatedPerson.id)
        assertNotNull(updatedPerson.firstName)
        assertNotNull(updatedPerson.lastName)
        assertNotNull(updatedPerson.address)
        assertNotNull(updatedPerson.gender)

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
            .spec(specification)
            .pathParam("id", personVO.id)
            .`when`()
            .delete("{id}")
            .then()
            .statusCode(204)
            .extract()
            .body()
            .asString()
    }

    @Test
    @Order(6)
    fun findAllPersons() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .`when`()
            .queryParams(
                "page", 3,
                "size",12,
                "direction", "asc")
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val wrapper = objectMapper.readValue(content, WrapperPersonVO::class.java)
        val peoples = wrapper.embedded!!.persons

        val person1 = peoples?.get(0)
        val person2 = peoples?.get(3)
        val person3 = peoples?.get(5)


        assertEquals("Allin", person1!!.firstName)
        assertEquals("Emmot", person1.lastName)
        assertEquals("7913 Lindbergh Way", person1.address)
        assertEquals("Male", person1.gender)
        assertEquals(false, person1.enabled)

        assertEquals("Almeria", person2!!.firstName)
        assertEquals("Curm", person2.lastName)
        assertEquals("34 Burrows Point", person2.address)
        assertEquals("Female", person2.gender)
        assertEquals(false, person2.enabled)


        assertEquals("Alphonso", person3!!.firstName)
        assertEquals("Eddisforth", person3.lastName)
        assertEquals("485 Dayton Avenue", person3.address)
        assertEquals("Male", person3.gender)
        assertEquals(true, person3.enabled)

    }

    @Test
    @Order(7)
    fun findFindPersonByName() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .`when`()
            .pathParam("firstName", "Ayr")
            .queryParams(
                "page", 0,
                "size",12,
                "direction", "asc")
            .get("/findPersonByName/{firstName}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val wrapper = objectMapper.readValue(content, WrapperPersonVO::class.java)
        val peoples = wrapper.embedded!!.persons

        val person1 = peoples?.get(0)

        assertEquals("Ayrton", person1!!.firstName)
        assertEquals("Senna", person1.lastName)
        assertEquals("São Paulo", person1.address)
        assertEquals("Male", person1.gender)
        assertEquals(true, person1.enabled)

    }

    @Test
    @Order(8)
    fun findAllPersonsWithoutToken() {

        val specificationWithoutToken = RequestSpecBuilder()
            .setBasePath("/api/v1/person")
            .setPort(TestConfigs.SERVER_PORT)
            .addFilter(RequestLoggingFilter(LogDetail.ALL))
            .addFilter(ResponseLoggingFilter(LogDetail.ALL))
            .build()

        given()
            .spec(specificationWithoutToken)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .`when`()
            .get()
            .then()
            .statusCode(403)
            .extract()
            .body()
            .asString()
    }

    @Test
    @Order(9)
    fun testHATEOAS() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_JSON)
            .queryParams(
                "page", 3,
                "size",12,
                "direction", "asc")
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/person/797"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/person/199"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/person/687"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/person/209"}}}"""))

        assertTrue(content.contains("""{"first":{"href":"http://localhost:8888/api/v1/person?direction=asc&page=0&size=12&sort=firstName,asc"}"""))
        assertTrue(content.contains(""","prev":{"href":"http://localhost:8888/api/v1/person?direction=asc&page=2&size=12&sort=firstName,asc"}"""))
        assertTrue(content.contains(""","self":{"href":"http://localhost:8888/api/v1/person?direction=asc&page=3&size=12&sort=firstName,asc"}"""))
        assertTrue(content.contains(""","next":{"href":"http://localhost:8888/api/v1/person?direction=asc&page=4&size=12&sort=firstName,asc"}"""))
        assertTrue(content.contains(""","last":{"href":"http://localhost:8888/api/v1/person?direction=asc&page=83&size=12&sort=firstName,asc"}"""))

        assertTrue(content.contains(""""page":{"size":12,"totalElements":1007,"totalPages":84,"number":3}}"""))
    }

    private fun mockPerson() {
        personVO.firstName = "James"
        personVO.lastName = "Gosling"
        personVO.address = "Calgary, Canadá"
        personVO.gender = "Male"
    }
}