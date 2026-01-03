<template>
  <div style="max-width: 1200px; margin: 20px auto; padding: 0 20px">
    <div style="margin-bottom: 20px; display: flex; justify-content: space-between; align-items: center;">
      <h2 style="color: #409eff; margin: 0;">
        <el-icon><UserFilled /></el-icon> 用户权限管理
      </h2>
      <div style="display: flex; gap: 10px;">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索姓名或邮箱" 
          style="width: 200px;" 
          clearable 
          @clear="loadData"
          @keyup.enter="loadData"
        />
        <el-button type="primary" @click="loadData">搜索</el-button>
      </div>
    </div>

    <el-card shadow="never">
      <el-table :data="tableData" v-loading="loading" stripe border>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="姓名" width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="displayId" label="WCA ID" width="120" />
        
        <el-table-column label="当前角色" width="120" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'admin'" type="danger" effect="dark">管理员</el-tag>
            <el-tag v-else type="info">普通用户</el-tag>
          </template>
        </el-table-column>

        <el-table-column label="注册时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.createTime) }}
          </template>
        </el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <div v-if="scope.row.id !== myUserId">
              <el-button 
                v-if="scope.row.role === 'user'"
                type="primary" 
                size="small" 
                plain
                @click="changeRole(scope.row, 'admin')"
              >
                设为管理员
              </el-button>
              
              <el-button 
                v-else
                type="danger" 
                size="small" 
                plain
                @click="changeRole(scope.row, 'user')"
              >
                降级为用户
              </el-button>
            </div>
            <div v-else style="color: #ccc; font-size: 12px;">
              (当前登录)
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div style="margin-top: 20px; display: flex; justify-content: flex-end">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="loadData"
          background
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { UserFilled } from "@element-plus/icons-vue";
import { getUserList, updateUserRole } from "@/api/user";
import { useUserStore } from "@/stores/user";
import { ElMessage, ElMessageBox } from "element-plus";

const userStore = useUserStore();
const myUserId = userStore.userInfo.id; // 获取自己的ID，防止把自己玩死

const loading = ref(false);
const tableData = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const searchKeyword = ref("");

const formatTime = (str) => {
  if (!str) return "";
  return str.toString().split("T")[0];
};

// 加载列表 (复用已有的 getUserList 接口)
const loadData = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value,
      name: searchKeyword.value // 复用后端的 name 模糊查询
    };
    const res = await getUserList(params);
    if (res.data.code === 200) {
      tableData.value = res.data.data.records;
      total.value = res.data.data.total;
    }
  } catch(e) {
    console.error(e);
  } finally {
    loading.value = false;
  }
};

// 切换角色
const changeRole = async (row, newRole) => {
  const actionText = newRole === 'admin' ? '设为管理员' : '降级为普通用户';
  
  try {
    await ElMessageBox.confirm(
      `确定要将用户 "${row.name}" ${actionText} 吗？`,
      "权限变更",
      { type: "warning", confirmButtonText: "确定" }
    );

    const res = await updateUserRole({ userId: row.id, role: newRole });
    if (res.data.code === 200) {
      ElMessage.success("操作成功");
      loadData(); // 刷新列表
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch {
    // 取消
  }
};

onMounted(() => {
  loadData();
});
</script>