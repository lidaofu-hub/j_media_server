package com.ldf.media.api.service;

import javax.servlet.http.HttpServletResponse;

public interface ISnapService {

    /**
     * 获取截图
     * @param url
     * @param response
     */
    void getSnap(String url, HttpServletResponse response);
}
