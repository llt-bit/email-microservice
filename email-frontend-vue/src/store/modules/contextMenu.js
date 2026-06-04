export default {
  namespaced: true,
  state: {
    // 控制右键弹框显示
    show: true,
    // 传递的信息
    msg: {},
    // 设置过滤规则弹框是否显示 tangxiangping 2022-06-06
    roleDialogVisible: false,
  },
  mutations: {
    // 修改状态
    setShow(state, data) {
      state.show = data;
    },
    // 修改传递的信息
    setMsg(state, data) {
      state.msg = { ...data };
    },
    // 修改设置过滤规则弹框展示状态 tangxiangping 2022-06-06
    setRoleDialogVisible(state, data) {
      state.roleDialogVisible = data;
    }
  },
};
