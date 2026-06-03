import axios from "axios";

const service = axios.create({
  baseURL: "",
  timeout: 100 * 1000,
  withCredentials: false, // 独立部署，不用 cookie
});

// JWT Token 管理
function getToken() {
  return localStorage.getItem("email_token") || new URLSearchParams(window.location.search).get("token");
}

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    const token = getToken();
    if (token) {
      config.headers["Authorization"] = "Bearer " + token;
    }
    if (config.data && !(config.data instanceof FormData) && config.upload !== true) {
      config.data = JSON.stringify(config.data);
      config.headers["Content-Type"] = "application/json";
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    if (response.data && response.data.code === 401) {
      localStorage.removeItem("email_token");
      window.location.href = "/login";
    }
    return response.data;
  },
  (error) => {
    if (error.response) {
      if (error.response.status === 401) {
        localStorage.removeItem("email_token");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  }
);

export default {
  get(url) { return service.get(url); },
  post(url, params) { return service.post(url, params); },
  put(url, params) { return service.put(url, params); },
  delete(url) { return service.delete(url); },
  upload(url, formData, onUploadProgress) {
    return service.post(url, formData, {
      headers: { "Content-Type": "multipart/form-data" },
      upload: true,
      onUploadProgress,
    });
  },
  /** AI SSE 流式请求（保留原 chrome 49 兼容方式） */
  lingmaStream(url, params) {
    return new Promise((resolve, reject) => {
      const xhr = new XMLHttpRequest();
      const queryStr = Object.keys(params).map(k => encodeURIComponent(k) + "=" + encodeURIComponent(params[k])).join("&");
      xhr.open("POST", url + "?" + queryStr, true);
      xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
      const token = getToken();
      if (token) xhr.setRequestHeader("Authorization", "Bearer " + token);

      let lastIndex = 0;
      xhr.onprogress = function () {
        const newText = xhr.responseText.substring(lastIndex);
        lastIndex = xhr.responseText.length;
        // SSE 格式: data: {...}\n\n
        const lines = newText.split("\n\n");
        for (const line of lines) {
          if (line.startsWith("data: ")) {
            try {
              const data = JSON.parse(line.substring(6));
              if (params.onData) params.onData(data);
              if (data.type === "done") resolve(data);
              if (data.type === "error") reject(data);
            } catch (e) {}
          }
        }
      };
      xhr.onload = () => { if (!xhr.responseText) resolve({ type: "done" }); };
      xhr.onerror = () => reject(new Error("AI请求失败"));
      xhr.send(new URLSearchParams(params));
    });
  },
};
