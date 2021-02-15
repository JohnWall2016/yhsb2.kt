package yhsb.base.cmd

import picocli.CommandLine

@CommandLine.Command(mixinStandardHelpOptions = true)
abstract class CommandWithHelp : Runnable
