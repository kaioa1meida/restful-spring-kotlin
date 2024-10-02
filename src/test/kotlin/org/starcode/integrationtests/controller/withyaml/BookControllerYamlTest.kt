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
import org.starcode.integrationtests.vo.BookVO
import org.starcode.integrationtests.vo.TokenVO
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class BookControllerYamlTest: AbstractIntegrationTest() {

    private lateinit var bookVO: BookVO
    private lateinit var objectMapper: YAMLMapper
    private lateinit var specification: RequestSpecification

    @BeforeAll
    fun setupTests() {
        bookVO = BookVO()
        objectMapper = YAMLMapper()
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
                .setBasePath("/api/v1/book")
            .setPort(TestConfigs.SERVER_PORT)
                .addFilter(RequestLoggingFilter(LogDetail.ALL))
                .addFilter(ResponseLoggingFilter(LogDetail.ALL))
            .build()
    }

    @Test
    @Order(1)
    fun testCreateBook() {
        mockBook()

        val createdBook = given()
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
            .body(bookVO, objectMapper)
            .`when`()
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .body()
            .`as`(BookVO::class.java, objectMapper)

        bookVO = createdBook

        assertNotNull(createdBook.id)
        assertNotNull(createdBook.title)
        assertNotNull(createdBook.author)
        assertNotNull(createdBook.launchDate)
        assertNotNull(createdBook.price)

        assertTrue(createdBook.id > 0)

        assertEquals("Think in Python", createdBook.title)
        assertEquals("Allen B. Downey", createdBook.author)
        val localDate = LocalDate.of(2002, 4, 1)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date, createdBook.launchDate)
        assertEquals(54.00, createdBook.price)

    }

    @Test
    @Order(2)
    fun testFindOneBook() {

        val receivedBook = given()
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
            .pathParam("id", bookVO.id)
            .`when`()
            .get("{id}")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .`as`(BookVO::class.java, objectMapper)

        assertNotNull(receivedBook.id)
        assertNotNull(receivedBook.title)
        assertNotNull(receivedBook.author)
        assertNotNull(receivedBook.launchDate)
        assertNotNull(receivedBook.price)

        assertTrue(receivedBook.id > 0)

        assertEquals("Think in Python", receivedBook.title)
        assertEquals("Allen B. Downey", receivedBook.author)
        val localDate = LocalDate.of(2002, 4, 1)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date, receivedBook.launchDate)
        assertEquals(54.00, receivedBook.price)
    }

    @Test
    @Order(3)
    fun testUpdateBook() {

        val bookToUpdate = bookVO
        bookToUpdate.title = "Think In Python"
        bookToUpdate.author = "Allen B. Downey"
        val localDate = LocalDate.of(2002, 4, 2)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        bookToUpdate.launchDate = date
        bookToUpdate.price = 60.00


        val updatedBook = given()
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
            .body(bookToUpdate, objectMapper)
            .`when`()
            .put()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .`as`(BookVO::class.java, objectMapper)

        assertNotNull(updatedBook.id)
        assertNotNull(updatedBook.title)
        assertNotNull(updatedBook.author)
        assertNotNull(updatedBook.launchDate)
        assertNotNull(updatedBook.price)

        assertTrue(updatedBook.id > 0)

        assertEquals("Think In Python", updatedBook.title)
        assertEquals("Allen B. Downey", updatedBook.author)
        assertEquals(date, updatedBook.launchDate)
        assertEquals(60.00, updatedBook.price)
    }

    @Test
    @Order(4)
    fun testDeleteBook() {

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
            .pathParam("id", bookVO.id)
            .`when`()
            .delete("{id}")
            .then()
            .assertThat()
            .statusCode(204)
    }

    @Test
    @Order(5)
    fun findAllPersons() {

        val books = given()
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
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .`as`(Array<BookVO>::class.java, objectMapper)


        val book1 = books[0]
        val book2 = books[1]
        val book3 = books[3]


        assertEquals("Working effectively with legacy code", book1.title)
        assertEquals("Michael C. Feathers", book1.author)
        val localDate1 = LocalDate.of(2017, 11, 29) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date1 = Date.from(localDate1.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date1, book1.launchDate)
        assertEquals(49.00, book1.price)

        assertEquals("Clean Code", book2.title)
        assertEquals("Robert C. Martin", book2.author)
        val localDate2 = LocalDate.of(2009, 1, 10) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date2 = Date.from(localDate2.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date2, book2.launchDate)
        assertEquals(77.00, book2.price)

        assertEquals("Code complete", book3.title)
        assertEquals("Steve McConnell", book3.author)
        val localDate3 = LocalDate.of(2017, 11, 7) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date3 = Date.from(localDate3.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date3, book3.launchDate)
        assertEquals(58.00, book3.price)
    }

    @Test
    @Order(6)
    fun findAllBooksWithoutToken() {

        val specificationWithoutToken = RequestSpecBuilder()
            .setBasePath("/api/v1/book")
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
    }



    private fun mockBook() {
        bookVO.title = "Think in Python"
        bookVO.author = "Allen B. Downey"
        val localDate = LocalDate.of(2002, 4, 1)
        val date = Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        bookVO.launchDate = date
        bookVO.price = 54.00
    }


}