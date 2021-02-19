package yhsb.base.cmd

import picocli.CommandLine

@CommandLine.Command(mixinStandardHelpOptions = true)
abstract class CommandWithHelp : Runnable

abstract class ExcelBrowseCommand : CommandWithHelp() {
    @CommandLine.Parameters(
        paramLabel = "excel",
        index = "0",
        description = ["excel表格文件路径"]
    )
    protected var excel = ""

    @CommandLine.Parameters(
        paramLabel = "startRow",
        index = "1",
        description = ["开始行(从1开始)"]
    )
    protected var startRow = 0

    @CommandLine.Parameters(
        paramLabel = "endRow",
        index = "2",
        description = ["结束行(包含在内)"]
    )
    protected var endRow = 0
}

abstract class ExcelBrowseWithPersonCommand : ExcelBrowseCommand() {
    @CommandLine.Parameters(
        paramLabel = "nameCol",
        index = "3",
        description = ["姓名所在列, 例如: H"]
    )
    protected var nameCol = ""

    @CommandLine.Parameters(
        paramLabel = "idCardCol",
        index = "4",
        description = ["身份证所在列, 例如: I"]
    )
    protected var idCardCol = ""
}

abstract class ExcelUpdateByPersonCommand : ExcelBrowseWithPersonCommand() {
    @CommandLine.Parameters(
        paramLabel = "updateCol",
        index = "5",
        description = ["更新状态信息所在列, 例如: J"]
    )
    protected var updateCol = ""
}