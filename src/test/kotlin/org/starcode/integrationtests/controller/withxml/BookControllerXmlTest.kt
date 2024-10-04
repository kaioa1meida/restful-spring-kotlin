package org.starcode.integrationtests.controller.withxml

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.restassured.RestAssured.given
import io.restassured.builder.RequestSpecBuilder
import io.restassured.filter.log.LogDetail
import io.restassured.filter.log.RequestLoggingFilter
import io.restassured.filter.log.ResponseLoggingFilter
import io.restassured.specification.RequestSpecification
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.springframework.boot.test.context.SpringBootTest
import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest
import org.starcode.integrationtests.vo.AccountCredentialsVO
import org.starcode.integrationtests.vo.BookVO
import org.starcode.integrationtests.vo.TokenVO
import org.starcode.integrationtests.vo.wrappers.WrapperBookVO
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@TestInstance(Lifecycle.PER_CLASS)
class BookControllerXmlTest: AbstractIntegrationTest() {

    private lateinit var bookVO: BookVO
    private lateinit var objectMapper: ObjectMapper
    private lateinit var specification: RequestSpecification

    @BeforeAll
    fun setupTests() {
        bookVO = BookVO()
        objectMapper = ObjectMapper()
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
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
            .contentType(TestConfigs.CONTENT_TYPE_XML)
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

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .body(bookVO)
            .`when`()
            .post()
            .then()
            .assertThat()
            .statusCode(201)
            .extract()
            .body()
            .asString()
        
        val createdBook = objectMapper.readValue(content, BookVO::class.java)
        
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

        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .pathParam("id", bookVO.id)
            .`when`()
            .get("{id}")
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val receivedBook = objectMapper.readValue(content, BookVO::class.java)

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


        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .body(bookToUpdate)
            .`when`()
            .put()
            .then()
            .assertThat()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val updatedBook = objectMapper.readValue(content, BookVO::class.java)

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
    fun findAllBooks() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val wrapper = objectMapper.readValue(content, WrapperBookVO::class.java)
        val books = wrapper.embedded!!.books

        val book1 = books?.get(0)
        val book2 = books?.get(1)
        val book3 = books?.get(3)

        assertEquals("Clean Code", book1!!.title)
        assertEquals("Robert C. Martin", book1.author)
        val localDate2 = LocalDate.of(2009, 1, 10) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date2 = Date.from(localDate2.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date2, book1.launchDate)
        assertEquals(77.00, book1.price)

        assertEquals("Code complete", book2!!.title)
        assertEquals("Steve McConnell", book2.author)
        val localDate3 = LocalDate.of(2017, 11, 7) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date3 = Date.from(localDate3.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date3, book2.launchDate)
        assertEquals(58.00, book2.price)

        assertEquals("Working effectively with legacy code", book3!!.title)
        assertEquals("Michael C. Feathers", book3.author)
        val localDate1 = LocalDate.of(2017, 11, 29) //Gambiarra pois usando Date(yyyy,mm,dd) o Ano nao estava sendo convertido corretamente
        val date1 = Date.from(localDate1.atStartOfDay(ZoneId.systemDefault()).toInstant())
        assertEquals(date1, book3.launchDate)
        assertEquals(49.00, book3.price)
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
            .spec(specificationWithoutToken)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .`when`()
            .get()
            .then()
            .statusCode(403)
            .extract()
            .body()
            .asString()
    }

    @Test
    @Order(7)
    fun findBookByTitle() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .`when`()
            .pathParam("title", "Java")
            .queryParams(
                "page", 0,
                "size",12,
                "direction", "asc")
            .get("/findBookByTitle/{title}")
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        val wrapper = objectMapper.readValue(content, WrapperBookVO::class.java)
        val books = wrapper.embedded!!.books

        val book1 = books?.get(0)

        assertEquals("JavaScript", book1!!.title)
        assertEquals("Crockford", book1.author)
        assertEquals(67.00, book1.price)
    }

    @Test
    @Order(8)
    fun testHATEOAS() {
        val content = given()
            .spec(specification)
            .contentType(TestConfigs.CONTENT_TYPE_XML)
            .queryParams(
                "page", 0,
                "size",12,
                "direction", "asc")
            .`when`()
            .get()
            .then()
            .statusCode(200)
            .extract()
            .body()
            .asString()

        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/book/2"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/book/4"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/book/3"}}}"""))
        assertTrue(content.contains("""_links":{"self":{"href":"http://localhost:8888/api/v1/book/1"}}}"""))

        assertTrue(content.contains(""""self":{"href":"http://localhost:8888/api/v1/book?direction=asc&page=0&size=12&sort=title,asc"}"""))

        assertTrue(content.contains(""""page":{"size":12,"totalElements":4,"totalPages":1,"number":0}}"""))
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