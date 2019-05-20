# ktpwntools
  
kotlin pwn package for myself  
  
util.kt has some utility for cast

```
String.b()                      // Convert String -> ByteArray
String.hexToByte()              // "x00x0a" -> ByteArray(0x00, 0x0a)
```

```
ByteArray.toHex()              // ByteArray(0x00, 0x11, 0x22) -> "001122"
```

```
p64(Long)      -> ByteArray    //   pack by little endian
u64(ByteArray) -> Long         // unpack by little endian
```
