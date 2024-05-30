package org.starcode.controller

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.starcode.data.vo.PersonVO
import org.starcode.services.PersonService

@RestController
@RequestMapping("/person")
class PersonController {

    @Autowired
    private lateinit var personService: PersonService

    @GetMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findAll(): ResponseEntity<List<PersonVO>> {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findAll())
    }


    @GetMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun findById(@PathVariable(value = "id") id: Long): ResponseEntity<PersonVO> {
        return ResponseEntity.status(HttpStatus.OK).body(personService.findById(id))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun create(@RequestBody person: PersonVO): ResponseEntity<PersonVO> {
        val createdPerson = personService.create(person)
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPerson)
    }

    @PutMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun update(@RequestBody person: PersonVO): ResponseEntity<PersonVO> {
        val updatedPerson = personService.update(person)
        return ResponseEntity.status(HttpStatus.CREATED).body(updatedPerson)
    }

    @DeleteMapping(value = ["/{id}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun delete(@PathVariable(value = "id") id: Long): ResponseEntity<String> {
        personService.delete(id)
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body("User deleted!")
    }

}