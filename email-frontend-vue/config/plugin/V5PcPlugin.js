// 用于Pc依赖注入v5的外置文件
var version = `?buildversion=${new Date().getTime()}`;
var smpOption = {
  script: `
        var mainIfram_ctxPath,
            mainIfram_ctxServer,
            mainIfram_CsrfGuard,
            _ctxPath,
            _ctxServer,
            _mine_getUrlSurffix,
            loadFile,
            cssFile,
            jsFile;
        mainIfram_ctxPath = window.top.mainIfram && window.top.mainIfram._ctxPath;
        mainIfram_ctxServer = window.top.mainIfram && window.top.mainIfram._ctxServer;
        _ctxPath = window.top._ctxPath || mainIfram_ctxPath || '/seeyon',
        _ctxServer = window.top._ctxServer || mainIfram_ctxServer || _ctxPath,
        _mine_getUrlSurffix = window.top.CsrfGuard && window.top.CsrfGuard.getUrlSurffix && window.top.CsrfGuard.getUrlSurffix() || '';

        loadFile = {
            css: function(file) {
                file.forEach(function(v) {
                    var cssLink = document.createElement('link');
                    cssLink.rel = 'stylesheet';
                    cssLink.href = v;
                    document.getElementsByTagName('head')[0].appendChild(cssLink);
                });
            },
            js: function(file) {
                file.forEach(function(v) {
                    var script = document.createElement('script');
                    script.type = 'text/javascript';
                    script.async = false;
                    script.src = v;
                    document.getElementsByTagName('head')[0].appendChild(script);
                });
            }
        }

        cssFile = [
            _ctxPath + '/common/cap4/template/base/cap4-icon/iconfont.css${version}',
            _ctxPath + '/skin/dist/common/skin.css${version}',
            _ctxPath + '/skin/dist/components/components_layout.css${version}',
            _ctxPath + '/skin/dist/components/components_theme_default.css${version}'
        ];

        jsFile = [
            _ctxPath + '/cap4/fieldDesign.do?method=headerJs',
            _ctxPath + '/common/all-min.js${version}',
            _ctxPath + '/common/cap4/common/i18n.js${version}',
            _ctxPath + '/common/cap4/template/base/browse-images/jquery.touchTouch-debug.js${version}'
        ]

        loadFile.css(cssFile);
        loadFile.js(jsFile);
    `,
  js: [],
  css: [],
};

function V5Plugin() {
  // 本地启动，自动添加一些外部依赖。如：$
  if (process.env.NODE_ENV == "development") {
    smpOption.js.push("/seeyon/common/all-min.js");
    smpOption.js.push("/seeyon/common/js/ui/calendar/calendar-language.js");
    smpOption.css.push("/seeyon/skin/dist/common/skin.css");
    // smpOption.css.push('/seeyon/skin/dist/components/components_layout.css')
    smpOption.css.push(
      "/seeyon/skin/dist/components/components_theme_default.css"
    );
    smpOption.css.push("/seeyon/common/css/peoplecard.css");
    smpOption.css.push("/seeyon/common/css/dd.css");
    smpOption.css.push("/seeyon/main/skin/frame/default/default_common.css");
    
    this.v5options = smpOption;
    return;
  }

  this.v5options = {};
}

V5Plugin.prototype.apply = function (compiler) {
  var v5options = this.v5options;
  compiler.plugin("compilation", function (compilation) {
    compilation.plugin(
      "html-webpack-plugin-before-html-generation",
      function (htmlPluginData, callback) {
        htmlPluginData.plugin.options.v5options = v5options;
      }
    );
  });
};

module.exports = V5Plugin;
