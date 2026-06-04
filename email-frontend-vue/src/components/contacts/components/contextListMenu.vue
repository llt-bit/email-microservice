<template>

  <ul v-show="isContextListMenuShow" class="contextListMenu" @click="fileOperate" :style="style">
    <li
      v-for="item in menus"
      :key="item.id"
      data-symbol="contextListMenu"
      :data-index="item.id"
    >
      {{ item.name }}
    </li>
  </ul>
</template>

<script>
export default {
  props: {
    menuPosition: {
      type: Object,
      default: () => ({}),
    },
    currentListMenuData: {
      type: Object,
      default: () => ({}),
    },
    isContextListMenuShow: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      menus: [
        {
          name: "打开详情页查看",
          type: 1,
          id: 0,
        },
      ],
      style: {
        top: 0,
        left: 0,
      },
      // 当前的操作信息
      operationCustomFloder: {},
    };
  },
  computed: {
      myMenuPosition() {
          return this.menuPosition;
      },
      myIsContextListMenuShow() {
          return this.isContextListMenuShow
      }
  },
  watch: {
    // 监听内容改变
    myMenuPosition: {
      handler(val) {
        // 修改样式
        this.style = {
          top: val.mouse_y + 10 + "px",
          left: val.mouse_x + 10 + "px",
        };
      },
      immediate: true,
      deep: true,
    },
  },
  mounted() {
    this.style = { top: this.myMenuPosition.mouse_y - 100 + "px" };
    // 监听点击事件
    document.addEventListener( "click", (e) => {
        const mySymbol = e.target.getAttribute("data-symbol");
        // console.log("mySymbol", mySymbol);
        if (mySymbol != "contextListMenu") {
            this.$emit("onCloseContextListMenu");
        }
      },
      true
    );
    // 监听鼠标滚轮
    document.addEventListener( "scroll", () => {
        // console.log(1111);
        // 隐藏右键弹框
        this.$emit("onCloseContextListMenu");
      },
      true
    );
  },
  methods: {
    // 操作文件
    fileOperate(e) {
      const type = e.target.getAttribute("data-index");
      //   console.log(type, typeof type);
      switch (type) {
        // 打开详情页查看
        case "0":
          this.handleOpenDetail();
          break;
      }
    },
    /**
     * 打开详情
     */
    handleOpenDetail() {
        window.open(`/inboxDet/${this.currentListMenuData.idStr}/true`, '详情页', "_self")
        this.$emit("onCloseContextListMenu");
    },
  },
};
</script>
<style scoped lang="less">
.contextListMenu {
  position: fixed;
  // top: 0;
  // left: 200px;
  // border: 1px solid black;
  min-width: 150px;

  background-color: white;
  z-index: 999999999999999;
  border-radius: 3px;
  background-color: #fbfbfb;
  box-shadow: 0 0 5px 1px rgb(183, 181, 181);

  li {
    line-height: 30px;
    // color: black;
    height: 30px;
    color: black;
    font-size: 12px;
    padding: 0 20px;
    cursor: pointer;
    user-select: none;
  }

  li:hover {
    background-color: #f9eceb;
    // background-color: #cbcbcc;
    color: #c8423c;
  }
}
</style>
