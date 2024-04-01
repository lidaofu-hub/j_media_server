package com.ldf.media.callback;

import com.aizuda.zlm4j.callback.IMKHttpAccessCallBack;
import com.aizuda.zlm4j.structure.MK_HTTP_ACCESS_PATH_INVOKER;
import com.aizuda.zlm4j.structure.MK_PARSER;
import com.aizuda.zlm4j.structure.MK_SOCK_INFO;
import com.sun.jna.Callback;

/**
 * 在http文件服务器中,收到http访问文件或目录的广播,通过该事件控制访问http目录的权限
 */
public class MKHttpAccessCallBack implements IMKHttpAccessCallBack {
    /**
     * 在http文件服务器中,收到http访问文件或目录的广播,通过该事件控制访问http目录的权限
     * @param parser http请求内容对象
     * @param path 文件绝对路径
     * @param is_dir path是否为文件夹
     * @param invoker 执行invoker返回本次访问文件的结果
     * @param sender http客户端相关信息
     */
    public void invoke(MK_PARSER parser, String path, int is_dir, MK_HTTP_ACCESS_PATH_INVOKER invoker, MK_SOCK_INFO sender){

    }
}