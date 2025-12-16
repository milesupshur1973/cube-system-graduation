<template>
  <div style="max-width: 800px; margin: 20px auto">
    <el-card>
      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本资料" name="info">
          <div style="text-align: center; margin-bottom: 20px; margin-top: 20px;">
            <el-upload
              class="avatar-uploader"
              :action="uploadActionUrl"
              :show-file-list="false"
              :on-success="handleAvatarSuccess"
              :before-upload="beforeAvatarUpload"
              :headers="uploadHeaders"
            >
              <div v-if="userInfo.avatarUrl" class="avatar-wrapper">
                <el-avatar :size="80" :src="userInfo.avatarUrl" />
                <div class="avatar-mask">
                  <el-icon><Camera /></el-icon>
                </div>
              </div>
              <el-avatar
                v-else
                :size="80"
                icon="UserFilled"
                class="avatar-placeholder"
              />
            </el-upload>
            <div style="font-size: 12px; color: #999; margin-top: 5px">
              点击头像可更换
            </div>
            <h2 style="margin-top: 10px">{{ userInfo.name }}</h2>
            <el-tag>{{ userInfo.displayId }}</el-tag>
          </div>

          <el-descriptions border :column="1" style="margin-bottom: 20px;">
            <el-descriptions-item label="邮箱">{{ userInfo.email }}</el-descriptions-item>
            <el-descriptions-item label="省份">{{ userInfo.province }}</el-descriptions-item>
            <el-descriptions-item label="城市">{{ userInfo.city }}</el-descriptions-item>
            <el-descriptions-item label="角色">{{ userInfo.role === "admin" ? "管理员" : "选手" }}</el-descriptions-item>
            <el-descriptions-item label="注册时间">{{ formatTime(userInfo.createTime) }}</el-descriptions-item>
          </el-descriptions>
        </el-tab-pane>

        <el-tab-pane label="安全设置" name="security">
          <div style="max-width: 500px; margin: 30px auto;">
            <el-form 
              :model="pwdForm" 
              ref="pwdFormRef" 
              :rules="pwdRules" 
              label-width="100px"
              status-icon
            >
              <el-form-item label="旧密码" prop="oldPassword">
                <el-input 
                  v-model="pwdForm.oldPassword" 
                  type="password" 
                  show-password 
                  placeholder="请输入当前使用的密码"
                />
              </el-form-item>
              
              <el-form-item label="新密码" prop="newPassword">
                <el-input 
                  v-model="pwdForm.newPassword" 
                  type="password" 
                  show-password 
                  placeholder="请输入新密码（至少6位）"
                />
              </el-form-item>
              
              <el-form-item label="确认新密码" prop="confirmPassword">
                <el-input 
                  v-model="pwdForm.confirmPassword" 
                  type="password" 
                  show-password 
                  placeholder="请再次输入新密码"
                />
              </el-form-item>

              <el-form-item>
                <el-button type="primary" @click="submitPassword" :loading="pwdLoading">确认修改</el-button>
                <el-button @click="resetPwdForm">重置</el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { computed, ref, reactive } from "vue";
import { useUserStore } from "@/stores/user";
import { ElMessage } from "element-plus";
import { Camera, UserFilled } from "@element-plus/icons-vue";
import { updateUser, updatePassword } from "@/api/user"; // 引入新API

const activeTab = ref("info"); // 默认显示基本资料

// === 原有的资料逻辑 ===
const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;
const uploadActionUrl = `${apiBaseUrl}/upload/image`;
const userStore = useUserStore();
const userInfo = computed(() => userStore.userInfo);
const uploadHeaders = computed(() => ({ token: userStore.token }));
const formatTime = (str) => {
  if (!str) return "";
  return str.toString().split("T")[0];
};
const beforeAvatarUpload = (rawFile) => {
  if (rawFile.type !== "image/jpeg" && rawFile.type !== "image/png") {
    ElMessage.error("头像必须是 JPG 或 PNG 格式!");
    return false;
  } else if (rawFile.size / 1024 / 1024 > 2) {
    ElMessage.error("头像大小不能超过 2MB!");
    return false;
  }
  return true;
};

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    try {
      const res = await updateUser({ avatarUrl: response.data });
      if (res.data.code === 200) {
        ElMessage.success("头像修改成功");
        userStore.login({ token: userStore.token, user: res.data.data });
      }
    } catch (e) {
      ElMessage.error("保存头像失败");
    }
  } else {
    ElMessage.error("上传失败");
  }
};

// === 新增：修改密码逻辑 ===
const pwdFormRef = ref(null);
const pwdLoading = ref(false);
const pwdForm = reactive({
  oldPassword: "",
  newPassword: "",
  confirmPassword: ""
});

// 自定义校验规则：确认密码必须一致
const validateConfirm = (rule, value, callback) => {
  if (value !== pwdForm.newPassword) {
    callback(new Error("两次输入的密码不一致"));
  } else {
    callback();
  }
};

const pwdRules = {
  oldPassword: [{ required: true, message: "请输入旧密码", trigger: "blur" }],
  newPassword: [
    { required: true, message: "请输入新密码", trigger: "blur" },
    { min: 6, message: "密码长度不能少于6位", trigger: "blur" }
  ],
  confirmPassword: [
    { required: true, message: "请确认新密码", trigger: "blur" },
    { validator: validateConfirm, trigger: "blur" }
  ]
};

const submitPassword = async () => {
  if (!pwdFormRef.value) return;
  await pwdFormRef.value.validate(async (valid) => {
    if (valid) {
      pwdLoading.value = true;
      try {
        const res = await updatePassword({
          oldPassword: pwdForm.oldPassword,
          newPassword: pwdForm.newPassword
        });
        
        if (res.data.code === 200) {
          ElMessage.success("修改成功，请重新登录");
          // 修改密码后强制登出
          userStore.logout();
          // 跳转登录页
          setTimeout(() => {
             window.location.href = "/login";
          }, 1000);
        } else {
          ElMessage.error(res.data.msg);
        }
      } catch (e) {
        console.error(e);
        ElMessage.error("请求失败");
      } finally {
        pwdLoading.value = false;
      }
    }
  });
};

const resetPwdForm = () => {
  if (pwdFormRef.value) pwdFormRef.value.resetFields();
};
</script>

<style scoped>
.avatar-uploader {
  display: inline-block;
  cursor: pointer;
  position: relative;
}
.avatar-wrapper {
  position: relative;
  width: 80px;
  height: 80px;
  border-radius: 50%;
  overflow: hidden;
}
.avatar-mask {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: rgba(0, 0, 0, 0.5);
  color: #fff;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.avatar-wrapper:hover .avatar-mask {
  opacity: 1;
}
</style>