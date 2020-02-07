---
layout: page
title: "使用 CameraView 处理相机视频"
order: 3
---

基于开源项目 `natario1/CameraView` 定制了一个用于 Android 图像识别领域的相机交互组件，项目文档地址：https://kuloud.github.io/CameraView/ ，结合 AoE 做了简化封装。

# 组件依赖
```groovy
api 'com.noctis:camera-view:1.0.0'
```

# 添加相机视图
```xml
<com.noctis.cameraview.CameraView
    android:id="@+id/camera"
    android:keepScreenOn="true"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

# 绑定生命周期
相机使用可绑定宿主的生命周期，根据视图可见状态自动管理相机使用。

```java
// For activities
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    CameraView camera = findViewById(R.id.camera);
    camera.setLifecycleOwner(this);
}

// For fragments
@Override
public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CameraView camera = findViewById(R.id.camera);
    camera.setLifecycleOwner(getViewLifecycleOwner());
}
```

# 处理相机数据
相机的输出数据被封装为 Frame 对象，他会在 process 方法执行完成后自动释放复用内容供下一帧消费。因为组件适配到 API 15，当 API < 21时，视频数据以字节数组提供，API >= 21 时，直接提供 Image 对象，需根据目标机型做区分。

```java
cameraView.addFrameProcessor(new FrameProcessor() {
    @Override
    @WorkerThread
    public void process(@NonNull Frame frame) {
        int rotation = frame.getRotation();
        long time = frame.getTime();
        Size size = frame.getSize();
        int format = frame.getFormat();
        if (frame.getDataClass() == byte[].class) {
            byte[] data = frame.getData();
            // Process byte array...
        } else if (frame.getDataClass() == Image.class) {
            Image data = frame.getData();
            // Process android.media.Image...
        }
    }
}
```