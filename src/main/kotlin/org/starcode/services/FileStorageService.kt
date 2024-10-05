package org.starcode.services

import org.starcode.config.FileStorageConfig
import org.starcode.exceptions.FileStorageException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import org.starcode.exceptions.InternalFileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.logging.Logger

@Service
class FileStorageService @Autowired constructor(fileStorageConfig: FileStorageConfig) {

    private val logger = Logger.getLogger(FileStorageService::class.java.name)

    private val fileStorageLocation: Path = run {
        val path = Paths.get(fileStorageConfig.uploadDir).toAbsolutePath().normalize()
        try {
            Files.createDirectories(path)
            path
        } catch (e: Exception) {
            throw FileStorageException("Could not create the directory where the uploaded files will be stored", e)
        }
    }


    fun storeFile(file: MultipartFile): String {
        logger.info("Storing a new file: ${file.originalFilename}")

        val fileName = StringUtils.cleanPath(file.originalFilename!!)
        return try {
            if (fileName.contains(".."))
                throw FileStorageException("Sorry! Filename contains invalid path sequence $fileName")
            val targetLocation = fileStorageLocation.resolve(fileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
            fileName
        } catch (e: Exception) {
            throw FileStorageException("Could not store file $fileName. Please try again!", e)
        }
    }

    fun loadAsResource (fileName: String): Resource {
        logger.info("Trying load resource: $fileName")

        return try {
            val filePath = fileStorageLocation.resolve(fileName).normalize()
            val resource: Resource = UrlResource(filePath.toUri())
            if (resource.exists()) {
                resource
            } else throw InternalFileNotFoundException("File not found $fileName")
        } catch (e: Exception) {
            throw InternalFileNotFoundException("File not found $fileName", e)
        }
    }
}