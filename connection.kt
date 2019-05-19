package douro.pwnpack
import java.lang.ProcessBuilder
import java.io.*
import java.nio.charset.*
import java.net.Socket
import java.nio.*
import java.util.concurrent.TimeUnit
interface LinkIO {
	var inputStream : InputStream
	val outputStream : OutputStream
	val charset : Charset
	val logger : Logger
	fun send(message : ByteArray){
		this.outputStream.write(message)
		this.outputStream.flush()
		logger.printByteArrayHex(message, 8)
		logger.clear("sent")
	}

	fun sendLine(message : ByteArray) {
		val messageline = message + "\n".b()
		this.outputStream.write(messageline)
		this.outputStream.flush()
		logger.printByteArrayHex(messageline, 8)
		logger.clear("sent")
	}

	fun recv() : ByteArray{
		val buf : ByteArray = ByteArray(10000)
		while(true){
			var result = this.inputStream.read(buf, 0, this.inputStream.available())
			logger.printByteHex(buf.last(),8)
			if(result <= 0)
				break
			}
		logger.clear("received")
		return buf
	}

	fun recvUntil(exceptedBytes : ByteArray) : ByteArray{
		val buf : ByteArray = ByteArray(1)
		var result = ByteArray(0)
		while(this.inputStream.read(buf,0,1) != -1){
			result = result +  buf
			logger.printByteHex(buf.last(),8)
			if(byteArrayToText(result).contains(byteArrayToText(exceptedBytes)) ){
				break
			}
		}
		logger.clear("received")
		return result
	}
	fun recvAll() : ByteArray{
		val buf : ByteArray = ByteArray(1)
		do{
			val result = this.inputStream.read(buf, 0, 1)
			logger.printByteHex(buf.last(),8)
		}while(this.inputStream.available() > 0)
		logger.clear("received")
		return buf
	}
	fun isAlive() : Boolean
	fun interactive(){
		val reader = BufferedReader(InputStreamReader(System.`in`),4028)
		var len:Int = 0
		var buf = ""
		val reloadBuf = ByteArray(1024)
		while(this.isAlive()){
			if(this.inputStream.available() <= 0){
				TimeUnit.SECONDS.sleep(1)
			}
			val mes = this.recv().toString(charset)
			println(mes)
			System.out.flush()
			print("\u001B[32m$\u001B[0m")
			buf = reader.readLine().toString()
			this.sendLine(buf.b())
		}
	}
}

class Process(processPath : String) : LinkIO {
	override val charset = Charsets.UTF_8
	override var inputStream :InputStream = System.`in`
	override var outputStream : OutputStream = System.out
	override val logger = Logger()
	private var process : java.lang.Process
	init{
		this.process = ProcessBuilder(processPath).start()
		this.inputStream = this.process.getInputStream()
		this.outputStream = this.process.getOutputStream()
	}
	override fun isAlive() : Boolean{
		return this.process.isAlive()
	}
}
class Remote(url : String , port : Int) : LinkIO {
	override val charset = Charsets.UTF_8
	override var inputStream : InputStream = System.`in`
	override val outputStream : OutputStream
	override val logger = Logger()
	private var connection : Socket
	init{
		this.connection = Socket(url, port)
		this.inputStream = this.connection.getInputStream()
		this.outputStream = this.connection.getOutputStream()
	}
	override fun isAlive() : Boolean{
		return !this.connection.isInputShutdown()
	}
}
