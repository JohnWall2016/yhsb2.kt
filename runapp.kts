import java.io.BufferedInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

fun String.execute(): Process {
    //println(this)
    return Runtime.getRuntime().exec(this)
}

private class ByteDumper(`in`: InputStream, out: OutputStream) : Runnable {
    val `in`: InputStream
    val out: OutputStream?
    override fun run() {
        val buf = ByteArray(8192)
        var next: Int
        try {
            while (`in`.read(buf).also { next = it } != -1) {
                out?.write(buf, 0, next)
            }
        } catch (e: IOException) {
            throw Exception("exception while dumping process stream", e)
        }
    }

    init {
        this.`in` = BufferedInputStream(`in`)
        this.out = out
    }
}

fun Process.waitForProcessOutput(output: OutputStream, error: OutputStream) {
    val tout: Thread = consumeProcessOutputStream(output)
    val terr: Thread = consumeProcessErrorStream(error)
    var interrupted = false
    try {
        try {
            tout.join()
        } catch (ignore: InterruptedException) {
            interrupted = true
        }
        try {
            terr.join()
        } catch (ignore: InterruptedException) {
            interrupted = true
        }
        try {
            waitFor()
        } catch (ignore: InterruptedException) {
            interrupted = true
        }
        closeStreams()
    } finally {
        if (interrupted) Thread.currentThread().interrupt()
    }
}

fun Process.closeStreams() {
    try {
        errorStream.close()
    } catch (ignore: IOException) {
    }
    try {
        inputStream.close()
    } catch (ignore: IOException) {
    }
    try {
        outputStream.close()
    } catch (ignore: IOException) {
    }
}

fun Process.consumeProcessOutputStream(output: OutputStream): Thread {
    val thread = Thread(ByteDumper(this.inputStream, output))
    thread.start()
    return thread
}

fun Process.consumeProcessErrorStream(err: OutputStream): Thread {
    val thread = Thread(ByteDumper(this.errorStream, err))
    thread.start()
    return thread
}

if (args.isNotEmpty()) {
    val task = args.first()
    val argument =
        if (args.size > 1) {
            "\"--args=${args.drop(1).joinToString(" ")}\""
        } else ""

    if (System.getProperty("os.name").startsWith("Windows")) {
        "gradlew.bat -q $task $argument"
    } else {
        "./gradlew -q $task $argument"
    }.execute().waitForProcessOutput(System.out, System.err)
}