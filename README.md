## 😁项目简介

开源流媒体服务器ZLMediaKit 的Java Api实现的Java版ZLMediaKit流媒体服务器

本项目可以作为[j_zlm_sdk](https://github.com/lidaofu-hub/j_zlm_sdk)使用示例代码。不重复造流媒体服务器轮子，只做代码集成示例。

本项目接口风格兼容ZLMediaKit REST API

## 😁项目功能
- **接口**（可以使用knife4j）：
    - 拉流代理接口：/index/api/addStreamProxy
    - 关闭流接口：/index/api/close_stream&/index/api/close_streams
    - 在线流列表接口：/index/api/getMediaList
    - 流详情：/index/api/getMediaInfo
    - 流是否在线：/index/api/isMediaOnline
    - 开始录像接口：/index/api/startRecord
    - 停止录像接口：/index/api/stopRecord
    - 获取录像状态接口：/index/api/isRecording
    - 获取内存资源信息：/index/getStatistic
    - 获取服务器配置：/index/getServerConfig
    - 设置服务器配置：/index/setServerConfig
    - 开启rtp服务：/index/openRtpServer
    - 关闭rtp服务：/index/closeRtpServer
    - 获取rtp服务列表：/index/listRtpServer
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
    - RTMP推流：rtmp://ip:rtmp_port/流APP/流名称
    - FLV拉流：http://ip:http_port/流APP/流名称.live.flv
    - WS-FLV拉流：ws://ip:http_port/流APP/流名称.live.flv
    - HLS拉流：http://ip:http_port/流APP/流名称/hls.m3u8
    - RTMP拉流：rtmp://ip:rtmp_port/流APP/流名称
    - RTSP拉流：rtsp://ip:rtsp_port/流APP/流名称

## 😁项目组成
1. 本项目基于Spring Boot 2.7.12版本，使用undertow作为web容器，使用knife4j作为接口文档,
2. 本项目基于最新ZLM4J开发完成
## 😁常见问题

1. 参见[ZLM4J常见问题 ](https://ux5phie02ut.feishu.cn/wiki/SzIAwyxnpilVMlkccS4cfJFGn1g)
## 😁学习探讨
 <p align="center">
  <a >
   <img alt="zlm4j-qun" src="doc/images/qun.jpg" width="350px">
  </a>
</p>
