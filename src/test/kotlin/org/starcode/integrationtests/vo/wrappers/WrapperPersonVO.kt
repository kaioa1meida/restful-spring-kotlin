package org.starcode.integrationtests.vo.wrappers

import com.fasterxml.jackson.annotation.JsonProperty

class WrapperPersonVO {

    @JsonProperty("_embedded")
    var embedded: PersonEmbeddedVO? = null
}