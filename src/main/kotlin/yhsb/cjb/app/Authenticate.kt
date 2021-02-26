package yhsb.cjb.app

import com.google.common.base.Strings
import org.ktorm.dsl.eq
import org.ktorm.entity.*
import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp
import yhsb.base.text.fillRight
import yhsb.cjb.db.AuthDb2021
import yhsb.cjb.db.RawItem
import yhsb.cjb.db.rawData

@CommandLine.Command(
    description = ["城居参保身份谁程序"],
    subcommands = [
    ]
)
class Authenticate : CommandWithHelp() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            CommandLine(Authenticate()).execute(*args)
        }

        fun importRawItem(items: Iterable<RawItem>) {
            AuthDb2021.use {
                items.withIndex().forEach { (index, item) ->
                    println("${index+1} ${item.idCard} ${item.name.fillRight(6)} ${item.type}")

                    if (!Strings.isNullOrEmpty(item.idCard)) {
                        val result = rawData.filter {
                            it.idCard eq item.idCard
                        }
                        if (result.isNotEmpty()) {
                            result.forEach {
                                it.update(item)
                                it.flushChanges()
                            }
                        } else {
                            rawData.add(item)
                            item.flushChanges()
                        }
                    }
                }
            }
        }
    }

    override fun run() {
        CommandLine.usage(Authenticate(), System.out)
    }

}