package yhsb.cjb.app

import picocli.CommandLine
import yhsb.base.cmd.CommandWithHelp

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
    }

    override fun run() {
        CommandLine.usage(Authenticate(), System.out)
    }


}