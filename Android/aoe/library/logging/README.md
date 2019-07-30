# AoE logging 组件 #

此模组实现了 AoE 默认的日志实现，用户可实现Logger接口后调用LoggerFactory静态方法进行注入构造器，进行托管。

```
LoggerFactory.setLoggerBinder(@Nullable LoggerBinder binder)
```

