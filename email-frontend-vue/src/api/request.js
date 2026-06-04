import axios from "axios";
import qs from "qs";

import { Message } from "element-ui";
import loadEvents from "../api/loading";
import Router from "@/router";

let loads = new loadEvents();

const service = axios.create({
    // 公共接口--这里注意后面会讲
    baseURL: "",
    // 超时时间 单位是ms，这里设置了3s的超时时间
    timeout: 100 * 1000,
    withCredentials: false,  // 独立部署，跨域不传 cookie
});

service.interceptors.request.use(
    (config) => {
        let rules =
            config.url != "/api/email/MailPersonSecretVerify" &&
            config.url != "/api/email/counts" &&
            config.url != "/api/attachment" &&
            config.url != "/api/email/MailFileSecret" &&
            config.url != "/api/org/groupMembers";
        if (config.data && config.data.autosave && config.data.autosave == "1") {
            rules = false;
        }
        if (rules) {
            loads.open();
        }
        //发请求前做的一些处理，数据转化，配置请求头，设置token,设置loading等，根据需求去添加
        //数据转化,也可以使用qs转换

        // if  (config.method == 'post'){
        //   config.data = qs.stringify(config.data)
        // }
        if (config.upload == true) {
            // config.headers = {
            //   'Content-Type':'multipart/form-data' //配置请求头
            // }
        /* ===== AI智能体新增：请求拦截器特殊处理 START ===== */
        } else if (config.url && (config.url.includes('lingmaAgent.do') || config.url.includes('/ai/agent'))) {
            // AI智能体接口：保持原始表单格式，不JSON化
            // URLSearchParams会自动设置 application/x-www-form-urlencoded
        /* ===== AI智能体新增：请求拦截器特殊处理 END ===== */
        } else {
            config.data = JSON.stringify(config.data);
            config.headers = {
                "Content-Type": "application/json", //配置请求头
            };
        }
        // JWT Token：从 localStorage 取（OA SSO跳转或独立登录时设置）
        const token = localStorage.getItem("email_token");
        if (token) {
            config.headers["Authorization"] = "Bearer " + token;
        }
        return config;
    },
    (error) => {
        Promise.reject(error);
    }
);

// 3.响应拦截器
service.interceptors.response.use(
    (response) => {
        //接收到响应数据并成功后的一些共有的处理，关闭loading等
        // Token 过期或无效 → 跳转登录页
        if (response.data && (response.data.code === 401 || response.status === 401)) {
            localStorage.removeItem("email_token");
            loads.close();
            Router.replace("/login");
            return Promise.reject(new Error("登录已过期"));
        }

        loads.close();
        return response.data;
    },
    (error) => {
        /***** 接收到异常响应的处理开始 *****/
        if (error && error.response) {
            // 1.公共错误处理
            // 2.根据响应码具体处理
            switch (error.response.status) {
                case 400:
                    error.message = "错误请求";
                    break;
                case 401:
                    localStorage.removeItem("email_token");
                    Router.replace("/login");
                    break;
                case 403:
                    error.message = "拒绝访问";
                    break;
                case 404:
                    error.message = "请求错误,未找到该资源";
                    // window.location.href = "/NotFound"
                    break;
                case 405:
                    error.message = "请求方法未允许";
                    break;
                case 408:
                    error.message = "请求超时";
                    break;
                case 500:
                    error.message = "服务端出错";
                    break;
                case 501:
                    error.message = "网络未实现";
                    break;
                case 502:
                    error.message = "网络错误";
                    break;
                case 503:
                    error.message = "服务不可用";
                    break;
                case 504:
                    error.message = "网络超时";
                    break;
                case 505:
                    error.message = "http版本不支持该请求";
                    break;
                default:
                    error.message = `连接错误${error.response.status}`;
            }
        } else {
            // 超时处理
            if (JSON.stringify(error).includes("timeout")) {
                Message.error("服务器响应超时，请刷新当前页");
            }
            error.message = "连接服务器失败";
        }
        loads.close();
        Message.error(error.message);

        /***** 处理结束 *****/
        //如果不需要错误处理，以上的处理过程都可省略
        return Promise.resolve(error.response);
    }
);

//4.导入文件

export default service;