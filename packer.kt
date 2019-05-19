package douro.pwnpack
import java.lang.ProcessBuilder
import java.io.*
import java.nio.charset.*
import java.net.Socket
import java.nio.*
class Packer(bytewidth : Int){
	var buf = ByteArray(0)
	val bytewidth = bytewidth
	fun fillBytes(bytes : ByteArray) = apply{
		this.buf += bytes
	}
	fun <T  : Number> pack(t : T) = apply{
		when(this.bytewidth) {
			32 -> {
				this.buf += p32(t.toInt())
			}
			64 -> {
				this.buf += p64(t.toLong())
			}
			else -> {
				throw Exception("bytewidth of packer is invalid")
			}
		}
	}
	fun toByteArray(): ByteArray{
		return this.buf
	}
	fun clear() = apply{
		this.buf = ByteArray(0)
	}
	override fun toString() : String{
		return this.buf.toString()
	}
}
