<!--
 * @LastEditTime : 2022-04-28 15:31:33
 * @FilePath     : /emial-true(not-edit)/src/Layout/userSettingLayout/components/leavePage.vue
 * @Description  : 用户设置 > 离开(退出当前界面)
-->
<template>
  <span class="leave-page" @click="handleLeavePage">
    <el-tooltip effect="dark" content="点击退出当前界面" placement="bottom">
      <svg
        t="1651111081977"
        class="icon"
        viewBox="0 0 1024 1024"
        version="1.1"
        xmlns="http://www.w3.org/2000/svg"
        p-id="2532"
      >
        <path
          d="M561.295186 969.005371H81.142076V54.894639h480.05312c15.198516 0 27.497315-12.298799 27.497314-27.497315S576.493702 0 561.295186 0H53.744751C38.546236 0 26.247437 12.298799 26.247437 27.497315v969.00537c0 15.198516 12.298799 27.497315 27.497314 27.497315h507.550435c15.198516 0 27.497315-12.298799 27.497315-27.497315s-12.298799-27.497315-27.497315-27.497314z"
          p-id="2533"
        ></path>
        <path
          d="M989.653354 492.551899L739.077824 241.876379c-10.698955-10.698955-28.097256-10.698955-38.796211 0-10.698955 10.698955-10.698955 28.097256 0 38.796212L903.961722 484.45269H387.812128c-15.198516 0-27.497315 12.298799-27.497315 27.497315s12.298799 27.497315 27.497315 27.497315H903.961722L700.181623 743.127429c-10.698955 10.698955-10.698955 28.097256 0 38.796211 5.399473 5.399473 12.398789 7.999219 19.398106 7.999219s14.098623-2.699736 19.398105-7.999219l250.67552-250.67552c10.798945-10.598965 10.798945-27.997266 0-38.696221z"
          p-id="2534"
        ></path>
      </svg>
    </el-tooltip>
  </span>
</template>
<script>
export default {
  name: "LeavePage",
  methods: {
    /**
     * 点击 离开界面
     * @Date: 2022-04-28 10:00:20
     */
    handleLeavePage() {
      this.$store.dispatch("toggleLayoutType");
    },
  },
};
</script>
<style lang="less" scoped>
.leave-page {
  svg {
    width: 20px;
    height: 20px;
    cursor: pointer;

    &:hover {
      fill: red;
    }
  }
}
</style>