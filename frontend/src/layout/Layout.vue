<template>
  <el-container class="layout">
    <el-aside width="220px" class="aside">
      <div class="logo">
        <el-icon :size="26"><FirstAidKit /></el-icon>
        <span>康复评估过程管理系统</span>
      </div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="#a6adb4" active-text-color="#ffffff">
        <el-menu-item v-for="menu in visibleMenus" :key="menu.path" :index="menu.path">
          <el-icon><component :is="menu.meta.icon" /></el-icon>{{ menu.meta.title }}
        </el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="title">{{ $route.meta.title }}</span>
        <div class="header-right">
          <el-tag size="small" :type="roleTagType">{{ roleLabel }}</el-tag>
          <span class="username">{{ user?.realName || user?.username }}</span>
          <el-button link type="primary" @click="logout">退出登录</el-button>
        </div>
      </el-header>
      <el-main class="main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { menuConfig } from '../router'

const router = useRouter()
const user = JSON.parse(localStorage.getItem('user') || 'null')

const visibleMenus = computed(() =>
  menuConfig.filter(m => !user || m.meta.roles.includes(user.role))
)

const roleLabels = {
  ADMIN: '系统管理员',
  DOCTOR: '康复医师',
  THERAPIST: '康复治疗师',
  PATIENT: '患者',
  DEPT_ADMIN: '科室管理员'
}
const roleLabel = computed(() => roleLabels[user?.role] || user?.role || '')
const roleTagType = computed(() =>
  ({ ADMIN: 'danger', DOCTOR: 'primary', THERAPIST: 'success', PATIENT: 'info', DEPT_ADMIN: 'warning' })[user?.role] || 'info'
)

function logout() {
  localStorage.removeItem('token')
  localStorage.removeItem('user')
  router.push('/login')
}
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}
.layout {
  height: 100%;
}
.aside {
  background-color: #001529;
}
.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #ffffff;
  font-size: 15px;
  font-weight: bold;
}
.aside .el-menu {
  border-right: none;
}
.header {
  background-color: #ffffff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.header .title {
  font-size: 18px;
  font-weight: bold;
}
.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.username {
  font-size: 14px;
  color: #333;
}
.main {
  background-color: #f0f2f5;
}
</style>
