package org.starcode.integrationtests.vo.wrappers

import com.fasterxml.jackson.annotation.JsonProperty
import org.starcode.integrationtests.vo.PersonVO

class PersonEmbeddedVO {

    @JsonProperty("personVOList")
    var persons: List<PersonVO>? = null
}