# 安装依赖环境
`pip3 install pycrypto aoe-tool`
# 使用说明
### Samples:
1. encrypt SRC_FILE to ENCODED_FILE
  `atenc -e -f SRC_FILE -t ENCODED_FILE`
2. encrypt SRC_FILE to ENCODED_FILE with specified KEY_VALUE and IV_VALUE
  `atenc -e -f SRC_FILE -t ENCODED_FILE -k 0000000000000000 -i 0101010101010101`
3. decrypt ENCODED_FILE to SRC_FILE
  `atenc -d -f ENCODED_FILE -t SRC_FILE`
4. decrypt ENCODED_FILE to SRC_FILE with specified KEY_VALUE and IV_VALUE
  `atenc -d -f ENCODED_FILE -t SRC_FILE -k 0000000000000000 -i 0101010101010101`
### Options:
```
  -h, --help, show help document.
  -e, --encrypt, encrypt file mode.
  -d, --decrypt, decrypt file mode.
  -f <path>, --from <path>, specify the file path to encrypt/decrypt.
  -t <path>, --to <path>, specify the file path after encrypt/decrypt.
  -k <KEY_VALUE>, --key <KEY_VALUE>, specify the key value to encrypt/decrypt.
  -i <IV_VALUE>, --iv <IV_VALUE>, specify the iv value to encrypt/decrypt.
```
### License
Apache License 2.0
