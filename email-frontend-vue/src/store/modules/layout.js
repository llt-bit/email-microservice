/*
 * @LastEditTime : 2022-04-29 17:03:28
 * @FilePath     : /emial-true(not-edit)/src/store/modules/layout.js
 * @Description  : 布局
 */
// 导入 路由配置
import router from "@/router/index.js";

// 默认布局
const TYPE_DEFAULT = "default";
// 个人设置
const TYPE_USER_SETTING = "userSetting";

export default {
    state: {
        // 布局状态
        type: TYPE_DEFAULT,
        // 是否不可操作(true: 用户对界面功能不可操作)
        isNotOperable: false,
    },
    mutations: {
        // 设置状态
        SET_TYPE: (state) => {
            // 更新操作状态
            state.type = state.isNotOperable ? TYPE_USER_SETTING : TYPE_DEFAULT;
            // 更新可操作性
            state.isNotOperable = !state.isNotOperable;

            // 验证可操作性
            if (state.isNotOperable) {
                // 不可操作, 表示进入 个人设置界面
                router.push("/userSetting");
            } else {
                // 进去默认界面
                router.push("/");
            }
        },
    },
    actions: {
        /**
         * 切换布局状态
         */
        toggleLayoutType: ({ commit, state }) => {
            switch (state.type) {
                case TYPE_DEFAULT:
                    commit("SET_TYPE");
                    break;
                case TYPE_USER_SETTING:
                    commit("SET_TYPE");
                    break;
            }
        },
    },
};