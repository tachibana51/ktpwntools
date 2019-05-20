package douro.pwnpack
import java.io.*
import java.nio.charset.*
import java.nio.*

fun p64(argLong: Long): ByteArray {
    return ByteBuffer.allocate(8).putLong(argLong).array().toList().reversed().toByteArray()
}

fun u64(argBytes: ByteArray): Long {
    return ByteBuffer.wrap(argBytes.reversed().toByteArray()).getLong()
}

fun p32(argInt: Int): ByteArray {
    return ByteBuffer.allocate(4).putInt(argInt).array().toList().reversed().toByteArray()
}

fun byteArrayToText(argbuf: ByteArray): String {
    var text = ""
    for (b in argbuf) {
        text += "%02x".format(b)
    }
    return text
}

fun byteToText(byte: Byte): String {
    return "%02x".format(byte)
}

fun asciiHexStringToByteArray(argStr: String): ByteArray {
    return argStr.split("x").filter { it != "" }
        .map { Integer.parseInt(it, 16)
        .toByte()
    }.toByteArray()
}

fun String.hexToByte() = let {
    asciiHexStringToByteArray(it)
}

fun String.b() = let {
    it.toByteArray()
}

fun ByteArray.toHex() = let {
    byteArrayToText(it)
}
