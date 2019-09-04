# 安装依赖环境
`pip3 install -r requirements.txt`
# 使用说明
###Samples:
1. encrypt logo.png to logo.png.aoe
  `./encrypt_helper.py -e -f logo.png -t logo.png.aoe`
2. decrypt logo.png.aoe to logo.png.aoe.png
  `./encrypt_helper.py -d -f logo.png.aoe -t logo.png.aoe.png`
###Options:
```
  -h, --help, show help document.
  -e, --encrypt, encrypt file mode.
  -d, --decrypt, decrypt file mode.
  -f <path>, --from <path>, specify the file path to encrypt/decrypt.
  -t <path>, --to <path>, specify the file path after encrypt/decrypt.
```
