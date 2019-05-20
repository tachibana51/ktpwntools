package douro.pwnpack
import douro.pwnpack.*
import java.io.*
import java.nio.charset.*
class Logger {
    var alignment = 0
    var number_of_bytes = 0
    fun clear(argStr: String) { // must be called in end of function
        println("")
        val hex = this.number_of_bytes.toString(16)
        println("[DEBUG] 0x$hex bytes $argStr")
        println("")
        this.number_of_bytes = 0
        this.alignment = 0
    }
    fun printByteHex(argByte: Byte, block_num: Int) {
        if (this.alignment % block_num == 0) {
            this.alignment = 0
            println("")
        }
        if (this.alignment % block_num == 0) {
            print("0x%04x".format(this.number_of_bytes) + " | ")
        }
        this.number_of_bytes++
        print("%02x".format(argByte) + " ")
        this.alignment++
    }
    fun printByteArrayHex(argBytes: ByteArray, block_num: Int) {
        for (byte in argBytes) {
            this.printByteHex(byte, block_num)
        }
    }
}
