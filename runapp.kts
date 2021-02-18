import java.util.concurrent.TimeUnit
import java.io.File

fun String.runCommand(
    workingDir: File = File("."),
    timeoutAmount: Long = 60,
    timeoutUnit: TimeUnit = TimeUnit.SECONDS
): String? = try {
    println(this)
    ProcessBuilder(split("\\s".toRegex()))
        .directory(workingDir)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .redirectError(ProcessBuilder.Redirect.PIPE)
        .start().apply { waitFor(timeoutAmount, timeoutUnit) }
        .inputStream.bufferedReader().readText()
} catch (e: java.io.IOException) {
    e.printStackTrace()
    null
}

if (args.size > 0) {
    val arg = args.first().split("\\s".toRegex())
    if (System.getProperty("os.name").startsWith("Windows")) {
        "gradlew.bat -q ${arg.first()} \"--args=${arg.drop(1).joinToString(" ")}\""
    } else {
        "./gradlew -q ${arg.first()} \"--args=${arg.drop(1).joinToString(" ")}\""
    }.runCommand()
}