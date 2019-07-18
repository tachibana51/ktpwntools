from pwn import *
import subprocess
import sys
import time
context.log_level = "CRITICAL"
if(len(sys.argv) < 2):
    print("usage: python3 refsearch.py [filename]")
    sys.exit()
filename = sys.argv[1]

# set rp++ path
path_to_rp = "/bin/rp-lin-x64"
elf = ELF(filename)
pop_dict = {}

def rop_load(filename):
    try:
        p = subprocess.run([path_to_rp, "-f", filename, "-r", "10"],
                           stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    except:
        print("set rp path")
        sys.exit()

    data = p.stdout.decode("utf-8").replace("\x1b[0m\x1b[91m", "").replace(
        "[91m", "").replace("\x1b", "").split("\n")[13:]
    return data


def create_poplist(data_list):
    for data in data_list[:-2]:
        address = re.findall(r"[0-9A-Fa-f]{8}", data)
        pop_dict[int("0x"+address[0], 16)] = data


rplin_stdout = rop_load(filename)
create_poplist(rplin_stdout)
ans = set()
for address in pop_dict.keys():
    address_byte = p64(address)[1:]
    found = elf.search(address_byte)
    for addr in found:
        ans.add((hex(addr)[:-1]+"0"))
for x in list(ans):
    try:
        dest = u64(elf.read(int(x, 16), 8))
        print(x+" contain "+hex(dest))
        print(elf.disasm(dest, 0x10))
        print("")
    except:
        print(x+" doesn't contain executable address")
