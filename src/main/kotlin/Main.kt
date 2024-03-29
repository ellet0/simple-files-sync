package net.freshplatform

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileInputStream
import java.net.URL
import java.security.MessageDigest
import kotlin.system.exitProcess


const val URLS_FILE_NAME = "urls.json"

const val IGNORE_FOLDER_NAME = ".ignore"

// Must be the same value in build.gradle.kts when building the jar file
const val EXECUTABLE_JAR_NAME = "simpleFilesSync.jar"

fun main(args: Array<String>): Unit = runBlocking(Dispatchers.IO) {
    val isShouldDeleteOldFiles = args.getOrNull(0)?.toBooleanStrictOrNull() ?: true

    val currentDirectory = System.getProperty("user.dir")
    val urlsFile = File(currentDirectory, URLS_FILE_NAME)
    if (!urlsFile.exists()) {
        println("Please create urls.json first")
        exitProcess(1)
    }

    val excludedFilesInTheCurrentDirectory = setOf(URLS_FILE_NAME, EXECUTABLE_JAR_NAME, IGNORE_FOLDER_NAME)

    val currentDirectoryFiles =
        (File(currentDirectory).listFiles() ?: throw IllegalArgumentException("Can't list current folder")).filter {
            it.name !in excludedFilesInTheCurrentDirectory
        }
    prettyPrintList(currentDirectoryFiles.map { it.name }, "Current files:")

    val urls = Json.decodeFromString<List<DownloadUrl>>(urlsFile.readText())
    prettyPrintList(urls, "Download urls:")

    val fileNames = urls.map { getFileNameFromUrl(it.url) }
    prettyPrintList(fileNames, "File names:")

    for (file in currentDirectoryFiles) {
        // Delete unused files if the user want to
        if (!fileNames.contains(file.name) && isShouldDeleteOldFiles) {
            println(" * Deleting the $file as it's not needed anymore, it's not in the download urls")
            file.delete()
        }
    }

    val httpClient = HttpClient(OkHttp)

    for (downloadUrl in urls) {
        val fileName = getFileNameFromUrl(downloadUrl.url)
        val file = File(currentDirectory, fileName)
        if (file.exists()) {
            val fileHash = calculateSHA256(file)
            if (fileHash == downloadUrl.sha256) {
                println(" - The file: $fileName exists with correct hash (${fileHash}), skip to the next file")
                continue
            }
            println(" - The file $fileName exists but the hash is not matched (the local one is $fileHash and the one from cloud is ${downloadUrl.sha256}), we will delete it and re-download it")
            file.delete()
        }
        println("Downloading $fileName from ${downloadUrl.url}")
        val responseBytes = httpClient.get(downloadUrl.url).bodyAsChannel().toByteArray()
        file.writeBytes(responseBytes)
    }

    println("Done!")
    exitProcess(0)
}


/**
 * Simple print list to log
 * */
fun <T> prettyPrintList(list: List<T>, message: String) {
    println("\n${message}")
    list.forEach {
        println(" - $it")
    }
    println("\n")
}

fun getFileNameFromUrl(url: String): String {
    return URL(url).file.substringAfterLast('/')
}


// From StackOverFlow
suspend fun calculateSHA256(file: File): String = withContext(Dispatchers.IO) {
    val messageDigest = MessageDigest.getInstance("SHA-256")
    val buffer = ByteArray(8192) // 8 KB buffer size

    FileInputStream(file).use { inputStream ->
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } > 0) {
            messageDigest.update(buffer, 0, bytesRead)
        }
    }

    // Convert the byte array to a hexadecimal string
    val hashBytes = messageDigest.digest()
    hashBytes.joinToString("") { "%02x".format(it) }
}
