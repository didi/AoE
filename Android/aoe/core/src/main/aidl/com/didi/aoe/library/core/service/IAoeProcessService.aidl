// IAoeProcessService.aidl
package com.didi.aoe.library.core.service;

import com.didi.aoe.library.core.pojos.Message;

/**
* AoE 远程处理服务
*
*/
interface IAoeProcessService {
    /**
     * 初始化模型翻译器
     *
     */
    int init(String clientId, in Message options);

    /**
    * 处理模型输入的切片数据
    */
    Message process(String clientId, in Message input);

    /**
    * 释放模型资源文件
    */
    void release(String clientId);

}
