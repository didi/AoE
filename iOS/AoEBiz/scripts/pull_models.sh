#!/bin/sh
# 本脚本主要用于在安装时下载模型。
MNIST_MODEL_FILE=dXvj4yfARd1565012051436.zip
FILE_MNIST=mnist

SQUEEZE_MODEL_FILE=fm2gKZ37I11565012061785.zip
FILE_SQUEEZE=squeeze

STATIC_DIDI_URL=https://img0.didiglobal.com/static/starfile/node20190805/895f1e95e30aba5dd56d6f2ccf768b57

function pull_models() {
    rm -rf ${2}
    MODEL_URL=${1}

    echo "Download ${2} from: ${MODEL_URL}"
    download=`curl --fail -s -O $MODEL_URL`
    if [ ! -f ${3} ]; then
    	echo "\033[31m ${3} file not exist, maybe $MODEL_URL not download \033[0m"
    	exit 1
    fi

    unzip -o ${3}
    rm -f ${3}
}

mkdir -p mnist/Models/
mkdir -p squeeze/Models/

cd mnist/Models/
pull_models ${STATIC_DIDI_URL}/$MNIST_MODEL_FILE ${FILE_MNIST} ${MNIST_MODEL_FILE}

cd ../../squeeze/Models/
pull_models ${STATIC_DIDI_URL}/${SQUEEZE_MODEL_FILE} ${FILE_SQUEEZE} ${SQUEEZE_MODEL_FILE}
