package com.ldf.media.api.controller;

import com.ldf.media.api.model.Result;
import com.ldf.media.api.model.StreamProxyParam;
import com.ldf.media.api.service.IApiService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Api接口
 *
 * @author lidaofu
 * @since 2023/11/29
 **/
@Api(tags = "【API】流媒体Api")
@RequestMapping("/index/api")
@RestController
@RequiredArgsConstructor
public class ApiController {
    private final IApiService iApiService;

    @ApiOperation(value = "添加rtmp/rtsp拉流代理")
    @GetMapping("/addStreamProxy")
    public Result<Boolean> addStreamProxy(@Validated StreamProxyParam param) {
        Boolean flag = iApiService.addStreamProxy(param);
        return new Result<>(flag);
    }
}
