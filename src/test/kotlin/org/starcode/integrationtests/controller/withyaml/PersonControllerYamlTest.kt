package org.starcode.integrationtests.controller.withyaml

import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.controller.withyaml.mapper.YAMLMapper
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.PersonVO
import org.starcode.integrationtests.vo.TokenVO
import org.starcode.integrationtests.vo.wrappers.WrapperPersonVO
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
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PersonControllerYmlTest : AbstractIntegrationTest() {

    private lateinit var specification: RequestSpecification
    private lateinit var objectMapper: YAMLMapper
    private lateinit var person: PersonVO

    @BeforeAll
    fun setupTests(){
        objectMapper = YAMLMapper()
        person = PersonVO()
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
    fun testCreate() {
        mockPerson()

        val item = given()
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
            .body(person, objectMapper)
            .`when`()
            .post()
            .then()
            .statusCode(201)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        person = item

        assertNotNull(item.id)
        assertTrue(item.id > 0)
        assertNotNull(item.firstName)
        assertNotNull(item.lastName)
        assertNotNull(item.address)
        assertNotNull(item.gender)
        assertEquals("Richard", item.firstName)
        assertEquals("Stallman", item.lastName)
        assertEquals("New York City, New York, US", item.address)
        assertEquals("Male", item.gender)
        assertEquals(true, item.enabled)
    }

    @Test
    @Order(2)
    fun testUpdate() {
        person.lastName = "Matthew Stallman"

        val item = given()
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
            .body(person, objectMapper)
            .`when`()
            .put()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        person = item

        assertNotNull(item.id)
        assertNotNull(item.firstName)
        assertNotNull(item.lastName)
        assertNotNull(item.address)
        assertNotNull(item.gender)
        assertEquals(person.id, item.id)
        assertEquals("Richard", item.firstName)
        assertEquals("Matthew Stallman", item.lastName)
        assertEquals("New York City, New York, US", item.address)
        assertEquals("Male", item.gender)
        assertEquals(true, item.enabled)
    }

    @Test
    @Order(3)
    fun testDisablePersonById() {
        val item = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_YAML)
            .pathParam("id", person.id)
            .`when`()
            .patch("{id}")
            .then()
            .statusCode(202)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        person = item

        assertNotNull(item.id)
        assertNotNull(item.firstName)
        assertNotNull(item.lastName)
        assertNotNull(item.address)
        assertNotNull(item.gender)
        assertEquals(person.id, item.id)
        assertEquals("Richard", item.firstName)
        assertEquals("Matthew Stallman", item.lastName)
        assertEquals("New York City, New York, US", item.address)
        assertEquals("Male", item.gender)
        assertEquals(false, item.enabled)
    }

    @Test
    @Order(4)
    fun testFindById() {
        val item = given()
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
            .pathParam("id", person.id)
            .`when`()
            .get("{id}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(PersonVO::class.java, objectMapper)

        person = item

        assertNotNull(item.id)
        assertNotNull(item.firstName)
        assertNotNull(item.lastName)
        assertNotNull(item.address)
        assertNotNull(item.gender)
        assertEquals(person.id, item.id)
        assertEquals("Richard", item.firstName)
        assertEquals("Matthew Stallman", item.lastName)
        assertEquals("New York City, New York, US", item.address)
        assertEquals("Male", item.gender)
        assertEquals(false, item.enabled)
    }

    @Test
    @Order(5)
    fun testDelete() {
        given()
            .spec(specification)
            .pathParam("id", person.id)
            .`when`()
            .delete("{id}")
            .then()
            .statusCode(204)
    }

    @Test
    @Order(6)
    fun testFindAll() {
        val wrapper = given()
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
            .`as`(WrapperPersonVO::class.java, objectMapper)

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
    fun testFindPersonByName() {
        val wrapper = given()
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
            .pathParam("firstName", "Ayr")
            .queryParams(
                "page", 0,
                "size", 12,
                "direction", "asc")
            .`when`()["findPersonByName/{firstName}"]
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(WrapperPersonVO::class.java, objectMapper)

        val people = wrapper.embedded!!.persons

        val item1 = people?.get(0)

        assertNotNull(item1!!.id)
        assertNotNull(item1.firstName)
        assertNotNull(item1.lastName)
        assertNotNull(item1.address)
        assertNotNull(item1.gender)
        assertEquals("Ayrton", item1.firstName)
        assertEquals("Senna", item1.lastName)
        assertEquals("São Paulo", item1.address)
        assertEquals("Male", item1.gender)
        assertEquals(true, item1.enabled)
    }

    @Test
    @Order(8)
    fun testFindAllWithoutToken() {

        val specificationWithoutToken: RequestSpecification = RequestSpecBuilder()
            .setBasePath("/api/person/v1")
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
        person.firstName = "Richard"
        person.lastName = "Stallman"
        person.address = "New York City, New York, US"
        person.gender = "Male"
        person.enabled = true
    }
}