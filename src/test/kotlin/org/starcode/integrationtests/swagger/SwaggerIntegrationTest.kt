package org.starcode.integrationtests.swagger

import io.restassured.RestAssured
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.starcode.integrationtests.TestConfigs
import org.starcode.integrationtests.testcontainers.AbstractIntegrationTest

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
class SwaggerIntegrationTest() : AbstractIntegrationTest() {

	@Test
	fun shouldDisplaySwaggerUiPage() {
		val content = RestAssured.given()
			.basePath("/swagger-ui/index.html")
			.port(TestConfigs.SERVER_PORT)
				.`when`()
			.get()
			.then()
				.statusCode(200)
			.extract()
			.body()
			.asString()


		Assertions.assertTrue(content.contains("Swagger UI"))
	}

}
