import subprocess
import re
import sys

path_to_rp = "/bin/rp-lin-x64"

mov_dict = {}
pop_list = []
ans_dict = {}
push_dict = {}
pop_dict = {}


def rop_load(filename):
    p = subprocess.run([path_to_rp, "-f", filename, "-r", "10", "--unique"],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    data = p.stdout.decode("utf-8").split("\n")
    return data


def load_plt_section(filename):
    p = subprocess.run(["/usr/bin/objdump", "-M", "intel", "-D", filename],
                       stdout=subprocess.PIPE, stderr=subprocess.PIPE)
    data = p.stdout.decode("utf-8").split("\n")
    return data


def operand_parser(string):
    return string.replace(";", "").strip().split(",")


def create_movdict(data_list):
    for data in data_list:
        #addressing = re.findall(r"[0-9A-Fa-f]{8}.+mov [^;]+;.*ret", data)
        addressing = re.findall(r"[0-9A-Fa-f]{8}.+mov [^;]+;.*$", data)
        if addressing == []:
            continue
        address = re.findall(r"[0-9A-Fa-f]{8}", str(addressing))[0]
        operands = re.findall(r"(?<=mov )[^;]+;", data)
        for ope in operands:
            ope1, ope2 = operand_parser(ope)
            mov_dict[ope2.strip()] = (
                addressing[0], ope1.strip(), ope2.strip(), address)


def create_poplist(data_list):
    for data in data_list:
        addressing = re.findall(r"[0-9A-Fa-f]{8}.+pop [^;]+;.*$", data)
        if addressing == []:
            continue
        if("rsp" in data or "esp" in data):
             continue
        address = re.findall(r"[0-9A-Fa-f]{8}", addressing[0])[0]
        operands = re.findall(r"(?<=pop )[^;]+;", data)
        popnum = len(operands)
        for i, operand in enumerate(operands):
            ope = operand_parser(operand)[0].strip()
            pop_list.append((addressing[0], ope, popnum, i, address))
            if(ope in pop_dict):
                pop_dict[ope] += [(addressing[0], ope, i, address, popnum)]
            else:
                pop_dict[ope] = [(addressing[0], ope, i, address, popnum)]


def print_can_pop_to_mov():
    for pop in pop_list:
        if(pop[1] in mov_dict):
            mes = "\033[93m " + str(pop[2]) + "." + str(pop[3] + 1) + "X -> " + str((pop[1]) + " -> " + str(
                mov_dict[pop[1]][1]) + "\033[0m : " + "\033[91m0x" + pop[0]) + "\n" + " "*18 + "->  \033[91m0x" + str(mov_dict[pop[1]][0])
            if(mov_dict[pop[1]][3] in ans_dict):
                ans_dict[mov_dict[pop[1]][3]
                         ] += [(mes, pop[2], pop[3], pop[4])]
            else:
                ans_dict[mov_dict[pop[1]][3]] = [(mes, pop[2], pop[3], pop[4])]
    print("\033[92m-------------------------------------\n")
    for address in ans_dict.keys():
        for mes in list(set(ans_dict[address])):
            print(mes[0])
            print("[*]suggestion\n")
            buf = str("buf += p64(0x" + mes[3])
            buf += ")\n"
            popnum = mes[1]
            offset = mes[2]
            for i in range(popnum):
                if i == offset:
                    buf += str("buf += p64(value")
                    buf += ")\n"
                else:
                    buf += str("buf += p64(dummy")
                    buf += ")\n"
            buf += "buf += p64(0x" + address + ")"
            print(buf)
            print("\033[91m")
        if("call" in mes[0]):
            called = re.search(
                r"(?<=call )[^;]+;", mes[0]).group(0).replace(";", "")
            print("[+]to call next rop chain : " +
                  called + " = " + "pop x ret")
            registers = re.findall(r"r[0-9a-z]+", called.replace("word", ""))
            print("so you can use these gadget")
            for reg in registers:
                try:
                    for pop in pop_dict[reg]:
                        print(" "*30+"0x"+pop[0])
                        print("[*]suggestion")
                        print("\033[94m")
                        buf = str("buf += p64(0x" + pop[3])
                        buf += ")\n"
                        popnum = pop[4]
                        offset = pop[2]
                        for i in range(popnum):
                            if i == offset:
                                buf += str("buf += p64(&pop1_ret")
                                buf += ")\n"
                            else:
                                buf += str("buf += p64(dummy")
                                buf += ")\n"
                        buf += "your_gadget"
                        print(buf)
                        print("\033[91m")
                except:
                    print("I have no idea")
                    pass
        print("")
        print("\033[92m-------------------------------------")


def print_can_push_to_pop():
    for push in list(push_dict.keys()):
        print("push {} -> X".format(push))

def genarate_rop_code(code ,popnum, offset,address, operand):
        val = "_".join([i.replace(";", "").strip().replace(" ", "_") for i in re.findall(r"pop [^;]+;", code)])
        print(val + " = 0x" + address)
        print("")
        buf = str("buf += p64(" + val)
        buf += ")\n"
        for i in range(popnum):
            if i == offset:
                buf +=str("buf += p64(value_"+ operand)
                buf += ")\n"
            else:
                buf +=str("buf += p64(dummy")
                buf += ")\n"
        return buf

argc = len(sys.argv)
if(argc <= 1):
    print("invalid argument")
    sys.exit()
filename = str(sys.argv[1])
data_list = rop_load(filename)
create_movdict(data_list)
create_poplist(data_list)
print("[+]poplist")
for pop in pop_list:                #pop code[0], ope, popnum, i, address
    print(pop[0])
    print("")
    buf = genarate_rop_code(pop[0],pop[2],pop[3],pop[4],pop[1])
    print(buf)
print("")
print("pop chain")
print_can_pop_to_mov()
# for pop in pop_list:
#    print(pop)
