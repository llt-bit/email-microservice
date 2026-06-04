import Vue from "vue";
import Vuex from "vuex";
Vue.use(Vuex);

// 导入 api
import api from "@/api/api";
// 导入 布局配置
import Layout from "./modules/layout";
// 右键弹框数据
import contextMenu from "./modules/contextMenu";

export default new Vuex.Store({
    modules: {
        layout: Layout,
        contextMenu: contextMenu,
    },
    state: {

        isredNet: true, // 显示密级选择控件
        isWriteEamil: false, //是否写信
        passTheAudit:'',
        lxr: 6,
        zy: 18,
        isall: true,
        emliaId: "",
        itemnum: "",

        num: {
            allNum: null,
            collect: null,
            // 定制标签(新增文件夹\菜单)
            customFloder: [],
            delete: null,
            draft: null,
            encryption: null,
            handled: null,
            inBox: null,
            notHandled: null,
            notRead: null,
            sent: null,
        },

        // 菜单信息(基础数据)
        menus: [{
                name: "收件箱",
                type: "inBox",
                path: "收件箱",
                icon: "el-icon-message",
                num: null,
                index: "1",
                Menus: [{
                        fileName: "所有邮件",
                        folderId: "1",
                        path: "所有邮件",
                        number: null,
                        index: "1-1",
                        type: "inBox",
                        inBoxType: "allMail",
                    },
                    {
                        fileName: "未读",
                        folderId: "2",
                        path: "未读",
                        number: null,
                        index: "1-2",
                        type: "inBox",
                        inBoxType: "notRead",
                    },
                    {
                        fileName: "已办理",
                        folderId: "3",
                        path: "已办理",
                        number: null,
                        index: "1-3",
                        type: "inBox",
                        inBoxType: "Handled",
                    },
                    {
                        fileName: "未办理",
                        folderId: "4",
                        path: "未办理",
                        number: null,
                        index: "1-4",
                        type: "inBox",
                        inBoxType: "NotHandled",
                    },
                ],
            },
            {
                name: "草稿箱",
                type: "draft",
                path: "草稿箱",
                icon: "el-icon-document-remove",
                num: 9,
                index: "2",
                Menus: [],
            },
            {
                name: "发件箱",
                type: "sent",
                path: "发件箱",
                icon: "el-icon-s-promotion",
                num: null,
                index: "3",
                Menus: [],
            },
            {
                name: "已删除",
                type: "delete",
                path: "已删除",
                icon: "el-icon-delete-solid",
                num: null,
                index: "4",
                Menus: [],
            },
            {
                name: "标红旗",
                type: "collection",
                path: "标红旗",
                icon: "el-icon-s-flag",
                num: null,
                index: "5",
                Menus: [],
            },
            {
                name: "加密箱",
                type: "encryption",
                path: "加密箱",
                icon: "el-icon-lock",
                num: null,
                index: "6",
                Menus: [],
            },
        ],

        moveType: "",
        // 当前点击的二级菜单信息
        folderCurrentClicked: {},
        // 记录 请求得到的 所有自定义文件夹信息
        customFloder: [],
        //当前点击的二级菜单类型  用于判断是否跨级移动
        currentMenuType: "inBox",

        // 取消保存草稿的随机数，在contacts中监听，如果他发生了变化那么再获取一次数据
        cancelSaveDraftRandom: "",
        // 快速搜索输入框的内容
        quickSearchInputValue: "",
        // 详情正文字符串
        detailContent: "",
        // 保存当前菜单信息
        currentMenuInfo: "",

        numberLists: {
            affiarNum: 0,
            hasPendingCount: 0,
            noReadMsgNum: 0,
            readMsgNum: 0,
            allAffiarNum: 0,
            allHasPendingCount: 0,
            allNoReadMsgNum: 0,
            allReadMsgNum: 0,
        },
        currentID: "", //当前点击的菜单id
        newMenuItem: {},

    },
    mutations: {
        setPassTheAudit(state,val){
            state.passTheAudit = val
        },
        // 开关之一
        changeFlag(state, boole) {
            state.flag = boole
        },

        showlxr(state, arr) {
            state.lxr = arr[0];
            state.zy = arr[1];
        },
        /**
         * 修改快速搜索输入框的内容
         * @Date: 2022-06-13 09:33:22
         */
        changeQuickSearchInputValue(state, value) {
            state.quickSearchInputValue = value;
        },
        loadNum(state, value) {
            state.num = value
                // console.log('请求邮件的数量:>> ', value);
        },
        /**
         * 设置正文详情的html字符串
         * @Date: 2022-06-13 16:36:50
         */
        setDetailContent(state, value) {
            state.detailContent = value;
        },
        /**
         * 设置当前点击的菜单信息
         * @Date: 2022-06-15 17:32:29
         */
        setCurrentMenuInfo(state, value) {
            state.currentMenuInfo = value;
        },

        /**
         * 修改草稿状态值
         * @Date: 2022-06-09 18:08:21
         * @param {*} state 状态
         */
        changeCancelSaveDraftRandom(state) {
            // 13位的随机数
            state.cancelSaveDraftRandom = Math.random().toString().slice(2, 15);
        },

        /**
         * 设置 邮件数量
         * @Date: 2022-04-28 19:11:46
         * @param {object} state: vuex state
         * @param {object} emailInfo: 邮件信息
         */
        setnum(state, emailInfo) {
            // 更新本地信息
            state.num = emailInfo;
            // 存储 菜单信息
            let arrr = [{
                    name: "收件箱",
                    type: "inBox",
                    path: "0",
                    icon: "el-icon-message",
                    num: state.num.inBox,
                    index: "1",
                    Menus: [{
                            fileName: "所有邮件",
                            folderId: "1",
                            path: "所有邮件",
                            number: state.num.inBox,
                            index: "1-1",
                            type: "inBox",
                            inBoxType: "allMail",
                        },
                        {
                            fileName: "未读",
                            folderId: "2",
                            path: "未读",
                            number: state.num.notRead,
                            index: "1-2",
                            type: "inBox",
                            inBoxType: "notRead",
                        },
                        {
                            fileName: "已办理",
                            folderId: "3",
                            path: "已办理",
                            number: state.num.handled,
                            index: "1-3",
                            type: "inBox",
                            inBoxType: "Handled",
                        },
                        {
                            fileName: "未办理",
                            folderId: "4",
                            path: "未办理",
                            number: state.num.notHandled,
                            index: "1-4",
                            type: "inBox",
                            inBoxType: "NotHandled",
                        },
                    ],
                },
                {
                    name: "草稿箱",
                    type: "draft",
                    path: "0",
                    icon: "el-icon-document-remove",
                    num: state.num.draft,
                    index: "2",
                    Menus: [],
                },
                {
                    name: "发件箱",
                    type: "sent",
                    path: "0",
                    icon: "el-icon-s-promotion",
                    num: state.num.sent,
                    index: "3",
                    Menus: [],
                },
                {
                    name: "已删除",
                    type: "delete",
                    path: "0",
                    icon: "el-icon-delete-solid",
                    num: state.num.delete,
                    index: "4",
                    Menus: [],
                },
                {
                    name: "标红旗",
                    type: "collection",
                    path: "0",
                    icon: "el-icon-s-flag",
                    num: state.num.collect,
                    index: "5",
                    Menus: [],
                },
                {
                    name: "加密箱",
                    type: "encryption",
                    path: "0",
                    icon: "el-icon-lock",
                    num: state.num.encryption,
                    index: "6",
                    Menus: [],
                },




            ];
            // console.log('11state.newMenuItem :>> ', state.newMenuItem);
            if (JSON.stringify(state.newMenuItem).length > 2) {
                arrr.push(state.newMenuItem);
            }
            state.menus = arrr;
            // }
        },
        setNewMenu(state, menusInfo) {
            state.newMenuItem = menusInfo
            let newMen = state.menus;
            setTimeout(() => {
              
                if (JSON.stringify(newMen).indexOf('appName') == -1) {
                    newMen.push(menusInfo);
                    state.menus = newMen;
                } else {
                      //  <!-- 屏蔽三方待办功能  -->
                    // state.menus.splice(6, 1, menusInfo)
                    // state.menus = state.menus
                }
            }, 10);
        },

        moveEmailType(state, str) {
            state.moveType = str;
        },

        /**
         * 更新 菜单信息
         * @Date: 2022-04-28 17:04:45
         * @param {object} state: vuex state
         * @param {object} menuInfo: 菜单信息
         */
        updateMenuInfo(state, menuInfo) {
            // 遍历菜单信息
            for (const item of state.menus) {
                // 通过 标识 匹配 需要更新的菜单信息是哪个分组中
                if (item.type == menuInfo.type) {
                    // 查询当前分组中是否存在 当前菜单信息
                    const currentMenuIndex = item.Menus.findIndex(
                        (item) => item.index == menuInfo.index
                    );
                    if (currentMenuIndex == -1) return;
                    // 数据更新
                    Vue.set(item.Menus, currentMenuIndex, menuInfo);

                    return;
                }
            }
        },

        //修改写信标志
        setWriteEamil(state, obj) {
            state.isWriteEamil = obj;
        },

        showall(state, n) {
            state.isall = n;
        },

        isslect(state, Id) {
            state.emliaId = Id;
        },
        itemslect(state, item) {
            state.itemnum = item;
        },
        currentMenuType(state, type) {
            state.currentMenuType = type;
            // console.log("lyyyyyyyy", state.currentMenuType);
        },

        //修改当前ID的值
        changeCurrentID(state, id) {
            // console.log('当前获取到的id :>> ', id);
            state.currentID = id
        },

        //修改 
        changeAllNumber(state, obj) {
            state.numberLists = obj
        },

        //初次加载完页面时 从菜单列表中获取的 一级菜单的 待办和未读数量
        changeMenuNumber(state, obj) {
            state.numberLists.affiarNum = obj.affiarNum
            state.numberLists.affiarNum = obj.noReadMsgNum
                // state.numberLists.affiarNum = obj.noReadMsgNum
                // state.numberLists.noReadMsgNum = obj.noReadMsgNum
        },
        setNewMenuItem(state, item) {
            state.newMenuItem = item
        }
    },

    actions: {
        // changeSelectState({ state, commit, dispatch }) {
        //     EventBus.$on("watchBtn", (obj) => {
        //         commit("stateSelect", obj);
        //     })
        // },
        /**
         * 获取第三方待办菜单信息
         * @Date: 2022-08-01
         * @param {state.numberLists} state: vuex state
         */
        // getNewMenus({ state, commit, dispatch }) {
        //     // 获取菜单
        //     api.getNewMenu().then((res) => {
        //         // console.log("res :>> ", res);
        //         for (let i = 0; i < res.data.length; i++) {
        //             //  <!-- 屏蔽三方待办功能  -->
        //             // res.data[i].index = res.data.length + 6 + "";
        //             // res.data[i].path = "0";
        //             // res.data[i].type = "thirdToDo";
        //             // res.data[i].icon = "el-icon-document";
        //             // res.data[i].num = 5;
        //             // res.data[i].name = res.data[i].appName;
        //             // for (let j = 0; j < res.data[i].subMenu.length; j++) {
        //             //     res.data[i].subMenu[j].fileName = res.data[i].subMenu[j].appName;
        //             //     res.data[i].subMenu[j].path = res.data[i].subMenu[j].appName;
        //             //     res.data[i].subMenu[j].folderId = "1";
        //             //     res.data[i].subMenu[j].type = "thirdToDo";
        //             //     res.data[i].subMenu[j].index = res.data[i].index + "-" + (j + 1);
        //             // }
        //             // res.data[i].Menus = res.data[i].subMenu;
        //             // console.log(' res.data[i] :>> ', res.data[i]);
        //             commit("setNewMenuItem", res.data[i]);
        //             commit("setNewMenu", res.data[i]);
        //         }
        //     });


        // },
        /**
         * 获取 待办 已办 已读 未读 数量
         * @Date: 2022
         * @param {object} state: vuex state
         */
        getAllNumber({ state, commit, dispatch }) {
            if (state.currentID) {
                // console.log('发起数量请求');
                api.getAffiarNoReadNum(state.currentID).then((res) => {
                    // console.log('所有的数量 :>> ', res.data);
                    let obj = {}
                        // console.log('res.data.length :>> ', res.data.length);
                    if (res.data.length > 1) {
                        obj.affiarNum = res.data.reduce((pre, cur) => {
                            return pre + cur.affiarNum;
                        }, 0);
                        obj.hasPendingCount = res.data.reduce((pre, cur) => {
                            return pre + cur.hasPendingCount;
                        }, 0);
                        obj.noReadMsgNum = res.data.reduce((pre, cur) => {
                            return pre + cur.noReadMsgNum;
                        }, 0);
                        obj.readMsgNum = res.data.reduce((pre, cur) => {
                            return pre + cur.readMsgNum;
                        }, 0);

                    } else {
                        obj.affiarNum = res.data.affiarNum;
                        obj.hasPendingCount = res.data.hasPendingCount;
                        obj.noReadMsgNum = res.data.noReadMsgNum;
                        obj.readMsgNum = res.data.readMsgNum;

                    }
                    commit("changeAllNumber", obj);
                })
            }

        },

        /**
         * 获取 邮箱 信息
         * @Date: 2022-04-28 19:54:42
         * @param {object} state: vuex state
         */
        getMailInfo({ state, commit, dispatch }) {
            // 获取 邮箱 信息
            api.emlianum().then((res) => {
                // 适配独立部署API: code="00010001", 数据在 msg 字段
                const code = res.code || (res.data && res.data.code);
                const emailInfo = res.msg || res.data;
                if (emailInfo) {
                    // 存储(初始化) 请求得到的菜单信息(邮件数量、新增标签(文件夹))
                    commit("setnum", emailInfo);
                    // 获取 新建文件夹信息
                    const { customFloder } = emailInfo;
                    if (Array.isArray(customFloder)) {
                        // 存储 自定义文件夹信息
                        state.customFloder = customFloder;
                        // 生成菜单结构
                        dispatch("generateMenuInfo", customFloder);
                    }
                } else {
                    this.$message.error(res.message || "邮件信息加载失败");
                }
            });
        },

        /**
         * 生成 菜单信息
         * @Date: 2022-04-30 17:34:29
         * @param {state} state: vuex state
         * @param {Array} customFloder: 需要处理的 自定义文件夹信息
         */
        generateMenuInfo({ state }, customFloder = []) {
            // 遍历 自定义文件夹信息
            customFloder.forEach((item) => {
                // debugger
                // 文件夹 类型匹配(一级菜单匹配)
                const oneLevelIndex = state.menus.findIndex((i) => i.type == item.type);
                if (oneLevelIndex == -1) return;

                // 验证在 当前 文件夹组 中是否存在当条 记录
                let subMenuGroup = state.menus[oneLevelIndex].Menus || [];
                const subMenuIndex = subMenuGroup.findIndex((i) => {
                    if (i.folderId && item.folderId) {
                        return i.folderId == item.folderId;
                    } else if (i.type && item.type) {
                        if (i.index && item.index) return i.index == item.index;
                        if (i.path && item.path) return i.path == item.path;
                    }
                });
                // console.log("subMenuIndex", subMenuIndex);
                if (subMenuIndex == -1) {
                    // 不存在, 存储当条记录
                    state.menus[oneLevelIndex].Menus.push(item);
                } else {
                    // 更新当条记录
                    state.menus[oneLevelIndex].Menus.splice(subMenuIndex, 1, item);
                }
            });
        },
        // newMenuList({ commit }) {
        //     commit('newMenuLists', )
        // },
        /**
         * 创建新文件夹
         * @Date: 2022-04-28 17:03:31
         * @param {object} state: vuex state
         * @param {object} menuInfo: 新增数据
         */
        createNewFolder({ state }, menuInfo) {
            // 遍历菜单信息
            state.menus.forEach((item) => {
                // 菜单标识对别
                if (item.type == menuInfo.type) {
                    menuInfo = {
                        ...menuInfo,
                        // 添加下标
                        index: item.index + "-" + item.Menus.length,
                    };
                    item.Menus.push(menuInfo);
                }
            });
        },
    },
});