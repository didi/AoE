# AoE Demo #

此应用主要通过手写识别功能的演示和物体识别功能的演示，向大家分享了如何能快速接入AoE SDK。

所有关于AoE组件使用以及扩展的部分都在AoEBiz目录下关于这部分内容可以阅读[ AoEBiz ](./../AoEBiz/docs/README.md)

## MNIST ##

Domo 的 MNIST 目录中包括了手写响应以及SDK调用的方法。


## Squeeze #

SqueezeDemoViewController 类中viewdidload方法中下列代码讲述了如何通过SDK 初始化基于Squeezenet实现物体识别功能。

```
    AoEClientOption *clientOption = [AoEClientOption new];
    clientOption.interpreterClassName = @"ABSqueezeInterceptor";
    clientOption.modelOptionLoaderClassName = @"ABSqueezeModelManager";
    
    NSString *dir = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"AoEBiz-squeeze.bundle/squeeze"];
    AoEClient *client = [[AoEClient alloc] initWithClientOption:clientOption ModelDir:dir subDir:nil];
    __weak typeof(self) weak_self = self;
    [client setupModel:^(AoEClientStatusCode statusCode) {
        __strong typeof(weak_self) strong_self = weak_self;
        strong_self.resultLabel.text = [@"status message is " stringByAppendingString:@(statusCode).stringValue];
    }];
    self.client = client;

```

当选择图片完成后，我们只要将输入数据交给`AoEclient`实例的process方法即可得到结果。

```

self.drawView.image = info[UIImagePickerControllerOriginalImage];
    NSString *result = (NSString *)[self.client process:((id<AoEInputModelProtocol>)self.drawView.image)];
    self.resultLabel.text = result;

```
