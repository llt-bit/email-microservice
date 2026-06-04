// const proxySite = 'http://web.fsylhs.com/seeyon'
// const proxySite = 'http://7t68df.natappfree.cc/seeyon'
const proxySite = 'http://127.0.0.1:80/seeyon'
// const proxySite = 'http://192.168.120.199:801/seeyon'
// const proxySite = 'http://192.168.119.17:801/seeyon'
    // const proxySite = "http://192.168.2.207:801/seeyon";

module.exports = {
    proxy: {
        "/seeyon": {
            target: proxySite,
            changeOrigin: true,
            pathRewrite: {
                "^/seeyon": "",
            },
            logLevel: 'debug'
        },
    },
    port: 8010,
};
