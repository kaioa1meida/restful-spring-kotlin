package org.starcode.services

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PagedResourcesAssembler
import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.PagedModel
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo
import org.springframework.stereotype.Service
import org.starcode.controller.PersonController
import org.starcode.data.vo.PersonVO
import org.starcode.exceptions.RequiredObjectIsNullException
import org.starcode.exceptions.ResourceNotFoundException
import org.starcode.mapper.DozerMapper
import org.starcode.model.Person
import org.starcode.repository.PersonRepository
import java.util.logging.Logger


@Service
class PersonService {

    @Autowired
    private lateinit var repository: PersonRepository
    @Autowired
    private lateinit var assembler: PagedResourcesAssembler<PersonVO>

    private val logger = Logger.getLogger(PersonService::class.java.name)

    fun findAll(pageable: Pageable): PagedModel<EntityModel<PersonVO>> {
        logger.info("Finding all persons!")

        val persons = repository.findAll(pageable)

        val vos = persons.map { p -> DozerMapper.parseObject(p, PersonVO::class.java) }

        vos.map { p -> p.add(linkTo(PersonController::class.java).slash(p.key).withSelfRel()) }

        return assembler.toModel(vos)
    }

    fun findPersonByName(firstName: String, pageable: Pageable): PagedModel<EntityModel<PersonVO>> {
        logger.info("Finding persons by First Name!")

        val persons = repository.findPersonByName(firstName, pageable)

        val vos = persons.map { p -> DozerMapper.parseObject(p, PersonVO::class.java) }

        vos.map { p -> p.add(linkTo(PersonController::class.java).slash(p.key).withSelfRel()) }

        return assembler.toModel(vos)
    }

    fun findById(id: Long): PersonVO{
        logger.info("Finding one person with ID: $id!")

        val person = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("No records found for this ID!") }
        val personVO: PersonVO = DozerMapper.parseObject(person, PersonVO::class.java)
        val withSelfRel = linkTo(PersonController::class.java).slash(personVO.key).withSelfRel()
        personVO.add(withSelfRel)
        return personVO
    }

    fun create(person: PersonVO?): PersonVO {
        if (person == null) throw RequiredObjectIsNullException()
        logger.info("Creating a one person with name: ${person.firstName}!")

        var entity: Person = DozerMapper.parseObject(person, Person::class.java)
        val personVO: PersonVO = DozerMapper.parseObject(repository.save(entity), PersonVO::class.java)
        val withSelfRel = linkTo(PersonController::class.java).slash(personVO.key).withSelfRel()
        personVO.add(withSelfRel)
        return personVO
    }

    fun update(person: PersonVO?): PersonVO {
        if (person == null) throw RequiredObjectIsNullException()
        logger.info("Updating a one person with ID: ${person.key}!")

        val entity = repository.findById(person.key)
            .orElseThrow { ResourceNotFoundException("No records found for this ID!") }

        entity.firstName = person.firstName
        entity.lastName = person.lastName
        entity.address = person.address
        entity.gender = person.gender

        val personVO: PersonVO = DozerMapper.parseObject(repository.save(entity), PersonVO::class.java)
        val withSelfRel = linkTo(PersonController::class.java).slash(personVO.key).withSelfRel()
        personVO.add(withSelfRel)
        return personVO
    }

    @Transactional
    fun disablePerson(id: Long): PersonVO{
        logger.info("Disabling one person with ID: $id!")

        repository.disablePerson(id)

        val person = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("No records found for this ID!") }
        val personVO: PersonVO = DozerMapper.parseObject(person, PersonVO::class.java)
        val withSelfRel = linkTo(PersonController::class.java).slash(personVO.key).withSelfRel()
        personVO.add(withSelfRel)
        return personVO
    }

    fun delete(id: Long){
        logger.info("Deleting a one person with ID: ${id}!")

        val entity = repository.findById(id)
            .orElseThrow { ResourceNotFoundException("No records found for this ID!") }

        repository.delete(entity)
    }

}