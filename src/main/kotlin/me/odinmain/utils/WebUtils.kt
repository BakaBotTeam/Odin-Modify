package me.odinmain.utils

import com.google.gson.JsonParser
import kotlinx.coroutines.withTimeoutOrNull
import me.odinmain.OdinMain.logger
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.URL

/**
 * Fetches data from a specified URL and returns it as a string.
 *
 * @param url The URL from which to fetch data.
 * @return A string containing the data fetched from the URL, or an empty string in case of an exception.
 */
fun fetchURLData(url: String): String {
    try {
        // Open a connection to the specified URL
        val connection = URL(url).openConnection()

        // Set the user agent to emulate a web browser
        connection.setRequestProperty(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3"
        )

        // Read the content from the input stream and build a string
        val inputStream = connection.getInputStream()
        val reader = BufferedReader(InputStreamReader(inputStream))
        val content = StringBuilder()

        var line: String?
        while (reader.readLine().also { line = it } != null) {
            content.append(line)
        }

        // Close the reader and return the content as a string
        reader.close()
        return content.toString()
    } catch (e: Exception) {
        // Print the stack trace in case of an exception and return an empty string
        logger.error("Error fetching data from URL: $url", e)
        return "Failed to fetch content from URL: $url"
    }
}

fun downloadFile(url: String, outputPath: String) {
    val url = URL(url)
    val connection = url.openConnection()
    connection.connect()

    val inputStream: InputStream = connection.getInputStream()
    val outputFile = File(outputPath)

    outputFile.parentFile?.mkdirs()

    val outputStream = FileOutputStream(outputFile)
    inputStream.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
}

suspend fun hasBonusPaulScore(): Boolean = withTimeoutOrNull(5000) {
    val response: String = URL("https://api.hypixel.net/resources/skyblock/election").readText()
    val jsonObject = JsonParser().parse(response).asJsonObject
    val mayor = jsonObject.getAsJsonObject("mayor") ?: return@withTimeoutOrNull false
    val name = mayor.get("name")?.asString ?: return@withTimeoutOrNull false
    return@withTimeoutOrNull if (name == "Paul") {
        mayor.getAsJsonArray("perks")?.any { it.asJsonObject.get("name")?.asString == "EZPZ" } == true
    } else false
} == true
