const webpack = require("webpack");

let vueConfig = {
  lintOnSave: false,
  publicPath: "/",
  transpileDependencies: ["element-ui", "sm-crypto", "bpmn-js", "bpmn-js-properties-panel", "diagram-js", "bpmn-moddle"],
  configureWebpack: {
    plugins: [
      new webpack.ProvidePlugin({
        "window.Quill": "quill/dist/quill.js",
        Quill: "quill/dist/quill.js",
      }),
    ],
  },
  productionSourceMap: false,
  devServer: {
    port: 8010,
    proxy: {
      "/api": {
        target: "http://127.0.0.1:8080",
        changeOrigin: true,
        pathRewrite: { "^/api": "/api" },
      },
    },
  },
};

module.exports = vueConfig;
