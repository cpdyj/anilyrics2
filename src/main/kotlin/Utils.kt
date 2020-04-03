import java.io.File

fun String.wrapWithDirectory() = trim().takeIf { it.endsWith(File.separatorChar) }?:trim()+File.separatorChar
