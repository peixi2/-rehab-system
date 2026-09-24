<template>
  <div class="login-page">
    <el-card class="login-card">
      <div class="login-title">
        <el-icon :size="30" color="#409EFF"><FirstAidKit /></el-icon>
        <h2>康复评估过程管理系统</h2>
      </div>
      <el-form :model="form" @keyup.enter="login">
        <el-form-item>
          <el-input v-model="form.username" placeholder="用户名" size="large">
            <template #prefix><el-icon><User /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码" size="large" show-password>
            <template #prefix><el-icon><Lock /></el-icon></template>
          </el-input>
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="login">
          登 录
        </el-button>
      </el-form>
      <el-divider>演示账号（密码均为 123456）</el-divider>
      <div class="demo-accounts">
        <el-tag v-for="acc in accounts" :key="acc.username" class="account-tag" @click="fill(acc)">
          {{ acc.label }}：{{ acc.username }}
        </el-tag>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'

const router = useRouter()
const form = ref({ username: '', password: '' })
const loading = ref(false)

const accounts = [
  { username: 'admin', password: '123456', label: '管理员' },
  { username: 'doctor', password: '123456', label: '医师' },
  { username: 'therapist', password: '123456', label: '治疗师' },
  { username: 'deptadmin', password: '123456', label: '科室管理员' },
  { username: 'patient', password: '123456', label: '患者' }
]

function fill(acc) {
  form.value = { username: acc.username, password: acc.password }
}

async function login() {
  if (!form.value.username || !form.value.password) {
    ElMessage.warning('请输入用户名和密码')
    return
  }
  loading.value = true
  try {
    const data = await request.post('/auth/login', form.value)
    localStorage.setItem('token', data.token)
    localStorage.setItem('user', JSON.stringify(data.user))
    ElMessage.success(`欢迎，${data.user.realName || data.user.username}`)
    router.push('/')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1f6feb 0%, #0d3b66 100%);
}
.login-card {
  width: 420px;
  padding: 10px 20px;
}
.login-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: 20px;
}
.login-title h2 {
  margin: 0;
  font-size: 20px;
}
.demo-accounts {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: center;
}
.account-tag {
  cursor: pointer;
}
</style>
