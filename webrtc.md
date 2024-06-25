> webrtc支持推流和拉流，推理需要开启https，如果只是拉流http协议就可以支持。
webrtc测试步骤如下：
1. 启动服务
2. 通过rtsp代理拉流添加一个媒体输入源，确保`enable_rtsp=1 app=live stream=test`
3. 在浏览器请求 `http://127.0.0.1:8899/index.html`
4. 直接点击**开始**，既可以看到媒体画面

> **webrtc不支持H265**