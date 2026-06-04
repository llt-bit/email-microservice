<template>
    <div class="tipsmsg">


      <div>
        <i class="el-icon-document-delete" style="color: #DEDEDF;font-size: 100px"></i>
        <p style="color: #676767;font-size: 16px;margin-top: 10px;text-align: center">未选择邮件</p>
      </div>
    </div>
</template>

<script>
  import api from '@/api/api'
    export default {
        name: "index",
        data(){
          return {
              fileList:[],
          }
        },
        methods:{
            cahngeflie(file, fileList){
                //     let from = new FormData
                //
                // fileList.forEach( d => {
                //     from.append('file',d.raw)
                // })

                // api.loadflie(from).then(res =>{
                // })
            },
            uploadFile(file, fileList) {
                // this.filedata.append('file', file.file)
                setTimeout(()=>{this.$refs.upload.clearFiles()},2000)
            },
            submitUpload(fileList) {
                this.$refs.upload.submit();
                // this.$refs.upload.clearFiles()

            },
            handleRemove(file, fileList) {
            },
            handlePreview(file) {
            },

          getinfo(){
              parent.$.selectPeople({
                  params : {
                      text : '开发中心、人员1',
                      value : 'Department | -884316703172445_1,Member | 1730833917365171641'
                  },
                  callback : function(ret) {
                      alert(ret.value + ":" + ret.text);
                  }
              });
          }
      }
    }
</script>

<style scoped>
  .tipsmsg{
    width: 100%;
    height: 100%;
    background: #F7F8F9;
    min-height: 500px;
    display: flex;
    align-items: center;
    justify-items: center;
    align-content: center;
    justify-content: center;
  }

</style>
