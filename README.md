## 😁项目简介

开源流媒体服务器ZLMediaKit 的Java Api实现的Java版ZLMediaKit流媒体服务器

本项目可以作为[ZLM4J](https://gitee.com/aizuda/zlm4j)使用示例代码。

本项目接口风格部分兼容ZLMediaKit REST API

## 😁项目功能

- **接口**（可以使用knife4j）：
  - 拉流代理接口：/index/api/addStreamProxy
  - 关闭拉流代理接口：/index/api/delStreamProxy
  - 推流代理接口：/index/api/addStreamPusherProxy
  - 关闭推流代理接口：/index/api/delStreamPusherProxy
  - 关闭流接口：/index/api/close_stream&/index/api/close_streams
  - 在线流列表接口：/index/api/getMediaList
  - 流详情：/index/api/getMediaInfo
  - 流是否在线：/index/api/isMediaOnline
  - 开始录像接口：/index/api/startRecord
  - 停止录像接口：/index/api/stopRecord
  - 获取录像状态接口：/index/api/isRecording
  - 获取内存资源信息：/index/api/getStatistic
  - 获取服务器配置：/index/api/getServerConfig
  - 设置服务器配置：/index/api/setServerConfig
  - 开启rtp服务：/index/api/openRtpServer
  - 关闭rtp服务：/index/api/closeRtpServer
  - 获取rtp服务列表：/index/api/listRtpServer
  - 截图：/index/api/getSnap
  - 转码（beta） ：/index/api/transcode
  - 开始拼接屏任务（beta） ：/index/api/stack/start
  - 重设拼接屏任务（beta） ：/index/api/stack/rest
  - 停止拼接屏任务（beta） ：/index/api/stack/stop
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

## 😁拼接屏使用说明
###  参数说明

```
以row：4 col：4 拼接16宫格为例
视频被分为下面的块

| ----1--- | -----2---- | ----3--- | ----4---- |
| ----5--- | -----6---- | ----7--- | ----8---- |
| ----9--- | ----10---- | ----11---| ----12----|
| ----13---| ----14---- | ----15---| ----16----|

```




  ```
  {
  "app": "live",  //生成流的app
  "id": "test", //生成流的stream 也是任务id
  "height": 1920, //生成视频的宽
  "width": 1080, //生成视频的高
  "row": 4,  //生成多少列
  "col": 4,  //生成多少行
  "fillColor": "00ff00", //空块填充颜色 RGB hex字符串 
  "fillImgUrl": ""  //空块填充的图片 如果设置这个填充图片 则上面的fillColor失效, 需要图片像素格式为BGR格式的png或者jpg如果不是则默认填充颜色
  "gridLineEnable": true, //是否开启分割线
  "gridLineColor": "000000", //分割线颜色  RGB hex字符串 
  "gridLineWidth": 1, //分割线宽度
  "windowList": [  //块配置数组，可为空
    {
      "imageUrl": "", //块内填充的图片地址  需要图片像素格式为BGR格式的png或者jpg如果不是则默认填充颜色
      "span": [1,2,5,6],  //块所占的位置 多个则合并单元格 1 2 5 6 则代表占用  1 2 5 6四个单元格组成一个单元格
      "url": "rtsp://admin:Hk@123456@192.168.1.64/streaming/tracks/101"  //块内填充的视频地址 如果设置这个地址 则块内填充的图片地址imageUrl失效
    },
     {
      "imageUrl": "http://127.0.0.1/upload/test.jpg", //块内填充的图片地址  需要图片像素格式为BGR格式的png或者jpg如果不是则默认填充颜色
      "span": [13],  //块所占的位置 多个则合并单元格 1 意思就是这个占用编号为13 的单元格 对应上面表
      "url": "" //块内填充的视频地址 如果设置这个地址 则块内填充的图片地址imageUrl失效
    }
  ]
}
  ```


1. 参见[ZLM4J常见问题 ](https://ux5phie02ut.feishu.cn/wiki/SzIAwyxnpilVMlkccS4cfJFGn1g)

## 😁学习探讨

 <p align="center">
  <a >
   <img alt="zlm4j-qun" src="doc/images/qun.jpg" width="350px">
  </a>
</p>
