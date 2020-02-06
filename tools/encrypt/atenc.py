#!/usr/bin/env python3

# -*- coding: utf-8 -*-
import getopt
import hashlib
import os
import sys

from Crypto.Cipher import AES

AES_SECRET_KEY = '0000000000000000'  # 此处16个字符
IV = "0101010101010101"

# padding算法
BS = len(AES_SECRET_KEY)
pad = lambda s: s + (BS - len(s) % BS) * chr(BS - len(s) % BS)
pad_bytes = lambda bs: bs + (BS - len(bs) % BS) * b'\0'


class AesEncrypt(object):
    def __init__(self):
        self.key = AES_SECRET_KEY
        self.iv = IV
        self.mode = AES.MODE_CBC

    # 加密函数
    def encrypt_file(self, source_file, encrypt_file):
        cryptor = AES.new(self.key.encode("utf8"), self.mode, self.iv.encode("utf8"))
        fo = open(source_file, "rb")
        file_size = os.path.getsize(source_file)
        fw = open(encrypt_file, "wb")

        # aes encode
        file_size = os.path.getsize(source_file)
        # print("source file size:", file_size)
        source_file_bytes = pad_bytes(fo.read(file_size))
        source_cipher_text = cryptor.encrypt(source_file_bytes)

        # md5 source file
        md5_tool = hashlib.md5()
        md5_tool.update(source_file_bytes)
        source_file_md5 = md5_tool.hexdigest()[8:-8]
        # print("source file md5 16:", source_file_md5)

        # exchange 16 bytes
        len_source_cipher_bytes = len(source_cipher_text)
        len_cipher = len_source_cipher_bytes if len_source_cipher_bytes < 1024 * 16 else 1024 * 16
        mixCount = int(len_cipher / 1024)
        listb = bytearray(source_cipher_text)
        listmd5 = bytearray(source_file_md5.encode("utf-8"))
        for idx in range(mixCount):
            tmp = listb[idx * 1024]
            listb[idx * 1024] = listmd5[idx]
            listmd5[idx] = tmp
        fw.write(bytes([1]))
        fw.write(file_size.to_bytes(4, byteorder="big"))
        fw.write(listmd5)
        fw.write(listb)
        fw.flush()
        fw.close()

    # 解密函数
    def decrypt_file(self, source_file, decrypt_file):
        cryptor = AES.new(self.key.encode("utf8"), self.mode, self.iv.encode("utf8"))
        fo = open(source_file, "rb")
        file_size = os.path.getsize(source_file)
        left_size = file_size

        b1 = fo.read(1)
        version = int.from_bytes(b1, byteorder='big', signed=False)

        left_size -= 1
        if version > 1:
            raise Exception("model version is too high: ", version)

        fsize_bytes = fo.read(4)
        orgFileSize = int.from_bytes(fsize_bytes, byteorder="big", signed=False)
        left_size -= 4

        fw = open(decrypt_file, "wb")

        md5 = fo.read(16)
        left_size -= 16

        len = left_size if left_size < 1024 * 16 else 1024 * 16
        b = fo.read(len)
        left_size -= len
        mixCount = int(len / 1024)
        listb = bytearray(b)
        listmd5 = bytearray(md5)
        for idx in range(mixCount):
            tmp = listb[idx * 1024]
            listb[idx * 1024] = listmd5[idx]
            listmd5[idx] = tmp

        rest = fo.read(left_size)
        allbytes = listb + rest
        plain_text = cryptor.decrypt(bytes(allbytes))
        unpad_text = plain_text[0:orgFileSize]
        fw.write(unpad_text)
        fw.close()
        fo.close()


def usage():
    print("Help document!")
    print("Samples:")
    print("1. encrypt SRC_FILE to ENCODED_FILE")
    print("  atenc -e -f SRC_FILE -t ENCODED_FILE")
    print("2. encrypt SRC_FILE to ENCODED_FILE with specified KEY_VALUE and IV_VALUE")
    print("  atenc -e -f SRC_FILE -t ENCODED_FILE -k 0000000000000000 -i 0101010101010101")
    print("3. decrypt ENCODED_FILE to SRC_FILE")
    print("  atenc -d -f ENCODED_FILE -t SRC_FILE")
    print("4. decrypt ENCODED_FILE to SRC_FILE with specified KEY_VALUE and IV_VALUE")
    print("  atenc -d -f ENCODED_FILE -t SRC_FILE -k 0000000000000000 -i 0101010101010101")
    print("Options:")
    print("  -h, --help,", "show help document.")
    print("  -e, --encrypt,", "encrypt file mode.")
    print("  -d, --decrypt,", "decrypt file mode.")
    print("  -f <path>, --from <path>,", "specify the file path to encrypt/decrypt.")
    print("  -t <path>, --to <path>,", "specify the file path after encrypt/decrypt.")
    print("  -k <KEY_VALUE>, --key <KEY_VALUE>,", "specify the key value to encrypt/decrypt.")
    print("  -i <IV_VALUE>, --iv <IV_VALUE>,", "specify the iv value to encrypt/decrypt.")
    pass


def args_analysis():
    from_file = ""
    to_file = ""
    encrypt_mode = False
    decrypt_mode = False
    try:
        options, args = getopt.getopt(sys.argv[1:], "edhf:t:k:i:",
                                      ["help", "mode=", "from=", "to=", "encrypt=", "decrypt=", "key=", "iv="])
    except getopt.GetoptError:
        print("Error: Input arguments is not support, please see the help document by \"atenc -h\"")
        sys.exit()
    if options.__len__() == 0:
        print("Error: No arguments found, please see the help document by \"atenc -h\"")
        sys.exit()
    for name, value in options:
        if name in ("-h", "--help"):
            usage()
            sys.exit()
        elif name in ("-f", "--from"):
            if from_file:
                print("Error: from file is not empty: ", from_file)
                sys.exit()
            from_file = value
        elif name in ("-t", "--to"):
            if to_file:
                print("Error: to file is not empty:", to_file)
                sys.exit()
            to_file = value
        elif name in ("-e", "--encrypt"):
            encrypt_mode = True
        elif name in ("-d", "--decrypt"):
            decrypt_mode = True
        elif name in ("-k", "--key"):
            if len(value) != 16:
                print("Error: len of secret key is", len(value), ", should be 16.")
                sys.exit()
            aes_encrypt.key = value
        elif name in ("-i", "--iv"):
            if len(value) != 16:
                print("Error: len of iv is", len(value), ", should be 16.")
                sys.exit()
            aes_encrypt.iv = value
        else:
            print("Error: Input arguments is not support, please see the help document by \"./atenc.py -h\"")
            sys.exit()
    if encrypt_mode and decrypt_mode:
        print("Error: encrypt and decrypt can not be set the same time.")
        sys.exit()
    if not encrypt_mode and not decrypt_mode:
        print("Error: no encrypt/decrypt mode to be set.")
        sys.exit()
    if not from_file or not to_file:
        print("Error: from file and to file must to be set the same time")
        sys.exit()
    if encrypt_mode:
        aes_encrypt.encrypt_file(from_file, to_file)
    elif decrypt_mode:
        aes_encrypt.decrypt_file(from_file, to_file)
    print("Success:", "encrypt" if encrypt_mode else "decrypt", "file", from_file, "to file", to_file)


aes_encrypt = AesEncrypt()


def main():
    # aes_encrypt.encrypt_file("logo.png",
    #                          "logo.png.aoe")
    #
    # aes_encrypt.decrypt_file("logo.png.aoe",
    #                          "logo.png.aoe.decrypt.png")
    args_analysis()


if __name__ == '__main__':
    main()
