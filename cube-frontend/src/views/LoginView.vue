<template>
  <div class="login-container">
    <el-card class="login-card">
      <div class="login-header">
        <el-icon size="40" color="#409EFF"><Grid /></el-icon>
        <h2>欢迎来到中国魔方赛事网</h2>
      </div>

      <el-tabs v-model="activeTab" class="login-tabs" stretch>
        <el-tab-pane label="登录" name="login">
          <el-form
            :model="loginForm"
            ref="loginFormRef"
            :rules="rules"
            label-width="0"
          >
            <el-form-item prop="email">
              <el-input
                v-model="loginForm.email"
                placeholder="请输入邮箱"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-button
              type="primary"
              style="width: 100%"
              size="large"
              @click="handleLogin"
              :loading="loading"
            >
              立即登录
            </el-button>
          </el-form>
        </el-tab-pane>

        <el-tab-pane label="注册" name="register">
          <el-form
            :model="regForm"
            ref="regFormRef"
            :rules="rules"
            label-width="0"
          >
            <el-form-item prop="name">
              <el-input
                v-model="regForm.name"
                placeholder="真实姓名 (用于比赛)"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="email">
              <el-input
                v-model="regForm.email"
                placeholder="邮箱"
                prefix-icon="Message"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="password">
              <el-input
                v-model="regForm.password"
                type="password"
                placeholder="设置密码"
                prefix-icon="Lock"
                size="large"
              />
            </el-form-item>

            <el-form-item prop="gender">
              <el-select
                v-model="regForm.gender"
                placeholder="请选择性别"
                style="width: 100%"
                size="large"
              >
                <el-option label="男" value="M" />
                <el-option label="女" value="F" />
              </el-select>
            </el-form-item>

            <el-form-item prop="location">
              <el-cascader
                v-model="selectedLocation"
                :options="cityData"
                placeholder="请选择省份 / 城市"
                @change="handleLocationChange"
                style="width: 100%"
                size="large"
                separator=" / "
              />
            </el-form-item>

            <el-button
              type="success"
              style="width: 100%"
              size="large"
              @click="handleRegister"
              :loading="loading"
            >
              注册账号
            </el-button>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { useUserStore } from "@/stores/user";
import { provinceAndCityData } from "element-china-area-data";
import { Grid, Message, Lock, User } from "@element-plus/icons-vue";
// 1. 引入我们刚写好的 API
import { login, register } from "@/api/user";

const router = useRouter();
const userStore = useUserStore();
const activeTab = ref("login");
const loading = ref(false);

// 数据源
const cityData = provinceAndCityData;

// ★★★ 这部分就是你刚才丢失的代码 ★★★
// 登录表单
const loginForm = reactive({ email: "", password: "" });

// 注册表单
const regForm = reactive({
  name: "",
  email: "",
  password: "",
  gender: "",
  province: "",
  city: "",
});

// 级联选择器绑定值
const selectedLocation = ref([]);

// 校验规则
const rules = {
  email: [{ required: true, message: "请输入邮箱", trigger: "blur" }],
  password: [{ required: true, message: "请输入密码", trigger: "blur" }],
  name: [{ required: true, message: "请输入真实姓名", trigger: "blur" }],
  gender: [{ required: true, message: "请选择性别", trigger: "change" }],
};

// 处理省市选择
const handleLocationChange = (value) => {
  if (value && value.length === 2) {
    const provinceCode = value[0];
    const cityCode = value[1];

    const provinceItem = cityData.find((item) => item.value === provinceCode);
    const cityItem = provinceItem
      ? provinceItem.children.find((item) => item.value === cityCode)
      : null;

    if (provinceItem && cityItem) {
      regForm.province = provinceItem.label;
      regForm.city = cityItem.label;
    }
  }
};

// 处理登录 (已修改为调用 API)
const handleLogin = async () => {
  if (!loginForm.email || !loginForm.password)
    return ElMessage.warning("请填写完整");
  loading.value = true;
  try {
    // 使用 API 函数 login
    const res = await login(loginForm);
    if (res.data.code === 200) {
      ElMessage.success("登录成功");
      userStore.login(res.data.data);
      router.push("/");
    } else {
      ElMessage.error(res.data.msg || "登录失败");
    }
  } catch (e) {
    console.error(e);
    // 拦截器通常会处理错误提示，但也保留 catch
  } finally {
    loading.value = false;
  }
};

// 处理注册 (已修改为调用 API)
const handleRegister = async () => {
  if (
    !regForm.name ||
    !regForm.email ||
    !regForm.password ||
    !regForm.province
  ) {
    return ElMessage.warning("请填写完整的注册信息（含省市）");
  }

  loading.value = true;
  try {
    // 使用 API 函数 register
    const res = await register(regForm);
    if (res.data.code === 200) {
      ElMessage.success("注册成功，请登录");
      activeTab.value = "login";
      loginForm.email = regForm.email;
    } else {
      ElMessage.error(res.data.msg || "注册失败");
    }
  } catch (e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 80vh;
}
.login-card {
  width: 450px;
  border-radius: 10px;
}
.login-header {
  text-align: center;
  margin-bottom: 20px;
}
</style>
