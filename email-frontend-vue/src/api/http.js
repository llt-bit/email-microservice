import request from './request'

const http ={
  /**
   * methods: 请求
   * @param url 请求地址
   * @param params 请求参数
   */
  get(url,params){
    const config = {
      method: 'get',
      url:url,
    }

    if(params) config.params = params
    return request(config)

  },
  all(arr){
    return request(arr)

  },



  upload(url,params,onUploadProgress){
    const config = {
      upload:true,
      method: 'post',
      url:url,
      headers:{
        "Content-Type":'multipart/form-data'
      },
      onUploadProgress,
      // onUploadProgress: progressEvent => {
      //   this.showProcess = true
      //   let process = (progressEvent.loaded / progressEvent.total * 100 | 0)
      //   this.progress = `上传进度：${process}%`
      //   console.log(process)
      // }

    }
    if(params)
     config.data = params
    config.headers = {
      'Content-Type':'multipart/form-data' //配置请求头
    }
    return request(config)
  },


  post(url,params){
    const config = {
      method: 'post',
      url:url,
    }
    if(params) config.data = params
    return request(config)
  },

  put(url,params){
    const config = {
      method: 'put',
      url:url
    }
    if(params) config.data = params
    return request(config)
  },

  putget(url,params){
    const config = {
      method: 'put',
      url:url
    }
    if(params) config.params = params
    return request(config)
  },

  delete(url,params){
    const config = {
      method: 'delete',
      url:url,
    }
    if(params) config.params = params
    return request(config)
  },


  download(url,params){
    const config = {
      method: 'get',
      url:url,
      responseType: 'blob',
    }
    if(params) config.params = params
    return request(config)
  },

  downloadget(url){
    const config = {
      method: 'get',
      url:url,
      responseType: 'blob',
    }

    return request(config)
  },

  getimg(url,params){
    const config = {
      method: 'get',
      url:url,
      responseType: "arraybuffer"
    }

    if(params) config.params = params
    return request(config)

  },


}



//导出
export default http
