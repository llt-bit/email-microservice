const webpack = require("webpack");

let vueConfig = {
  lintOnSave: false,
  publicPath: "./",
  transpileDependencies: [
    "./src", "element-ui",
    'sm-crypto',
    'bpmn-js', 'bpmn-js-properties-panel', 'diagram-js', 'bpmn-moddle',
    '@bpmn-io', '@bpmn-io/element-templates-validator'
  ],
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
        target: "http://127.0.0.1:8099",
        changeOrigin: true,
      },
    },
  },
};

module.exports = vueConfig;
