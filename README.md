## 😁项目简介

开源流媒体服务器ZLMediaKit 的Java Api实现的Java版ZLMediaKit流媒体服务器

感谢 [@夏楚](https://github.com/xia-chu)
提供了这么好的开源流媒体服务器软件[ZLMediaKit](https://github.com/ZLMediaKit/ZLMediaKit)

本项目可以作为[j_zlm_sdk](https://github.com/lidaofu-hub/j_zlm_sdk)使用示例代码。不重复造流媒体服务器轮子，只做代码集成实例。



**注意！！ 本项目修改了ZLMediaKia的源代码实现了拉流代理可以控制更多的协议的开启关闭**

``` java 
 //修改后的多了  int fmp4_enabled, int ts_enabled, int rtmp_enabled, int rtsp_enabled 等参数
 MK_PROXY_PLAYER mk_proxy_player_create(String vhost, String app, String stream, int hls_enabled, int mp4_enabled, int fmp4_enabled, int ts_enabled, int rtmp_enabled, int rtsp_enabled);

```
## 😁项目功能
- **接口**（可以使用knife4j）：
    - 拉流代理接口：/index/api/addStreamProxy
    - 关闭流接口：/index/api/close_stream&/index/api/close_streams
    - 在线流列表接口：/index/api/getMediaList
    - 开始录像接口：/index/api/startRecord
    - 停止录像接口：/index/api/stopRecord
    - 获取录像状态接口：/index/api/isRecording
    - 开发中：😁
- **回调实现**：
    - MKHttpAccessCallBack：http鉴权回调
    - MKHttpBeforeAccessCallBack：http前置鉴权回调
    - MKHttpFlowReportCallBack：码流数据统计回调
    - MKHttpRequestCallBack：http请求回调
    - MKLogCallBack：日志回调
    - MKNoFoundCallBack：未找到流回调
    - MKNoReaderCallBack：无人观看回调
    - MKPlayCallBack：播放回调
    - MKProxyPlayCloseCallBack：流代理关闭回调
    - MKPublishCallBack：推流回调
    - MKRecordMp4CallBack：录制回调
    - MKSourceFindCallBack：找不到流回调
    - MKStreamChangeCallBack：流上下回调
- **流相关（注意rtmp_port、rtsp_port、http_port(非Spring Mvc端口)等参见application.yml，流APP、流名称可自定义）**：
    - RTMP推流：rtmp://127.0.0.1:rtmp_port/流APP/流名称
    - FLV拉流：http://127.0.0.1:http_port/流APP/流名称.live.flv
    - WS-FLV拉流：ws://127.0.0.1:http_port/流APP/流名称.live.flv
    - HLS拉流：http://127.0.0.1:http_port/流APP/流名称/hls.m3u8
    - RTMP拉流：rtmp://127.0.0.1:rtmp_port/流APP/流名称
    - RTSP拉流：rtsp://127.0.0.1:rtsp_port/流APP/流名称

## 😁项目组成
1. 本项目基于Spring Boot 2.7.12版本，使用undertow作为web容器，使用knife4j作为接口文档,
2. 本项目基于2023.11.23拉取ZLMediaKit master分支代码编译开发

## 😁常见问题
1.在 windows 环境运行出现 java.lang.UnsatisfiedLinkError 问题，请安装 openssl 库 参见OpenSSL 下载 或者复制libssl-3-x64.dll&libcrypto-3-x64.dll到系统动态链接库下
## 😁学习探讨
 <p align="center">
  <a >
   <img alt="zlm4j-qun" src="doc/images/qun.jpg" width="350px">
  </a>
</p>
