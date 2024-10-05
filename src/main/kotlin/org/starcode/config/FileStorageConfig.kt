package org.starcode.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "file")
class FileStorageConfig (var uploadDir: String = "")