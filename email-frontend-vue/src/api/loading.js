import { Loading } from "element-ui";
import "./loading_reset.css";

const loadOption = {
  fullscreen: true,
  lock: true,
  text: "加载中...",
  spinner: "el-icon-loading",
  background: "rgba(255,255,255,0.8 )",
};

let loadingInstance;

export default class loadEvents {
  constructor(vueThis) {
    this.vm = vueThis; //vue中的this  也可以不用
  }
  open() {
    loadingInstance = Loading.service(loadOption);

    setTimeout(() => {
      const loadingMask = document.querySelector(".el-loading-mask");
      loadingMask.style.cssText += " cursor: pointer !important;";
      loadingMask.addEventListener("contextmenu", (e) => {
        e.preventDefault();
      });
    });
  }
  close() {
    loadingInstance.close();
  }
}
