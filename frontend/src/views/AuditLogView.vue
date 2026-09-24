<template>
  <el-card>
    <div class="toolbar">
      <div class="filters">
        <el-input v-model="keyword" placeholder="搜索用户名/模块/操作" style="width: 240px" clearable />
        <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width: 150px">
          <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
        </el-select>
      </div>
      <el-button :icon="Refresh" @click="load">刷新</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="时间" width="160">
        <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column prop="username" label="操作人" width="110" />
      <el-table-column label="角色" width="110">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="module" label="模块" width="110" />
      <el-table-column prop="action" label="操作" width="110" />
      <el-table-column prop="detail" label="详情" min-width="240" show-overflow-tooltip />
      <el-table-column prop="ip" label="IP" width="130" />
    </el-table>
    <el-pagination
      class="pager"
      layout="prev, pager, next, total"
      :total="total"
      :page-size="size"
      :current-page="page + 1"
      @current-change="changePage"
    />
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import request from '../api/request'

const list = ref([])
const keyword = ref('')
const roleFilter = ref('')
const page = ref(0)
const size = 50
const total = ref(0)

const roles = [
  { value: 'ADMIN', label: '系统管理员' },
  { value: 'DEPT_ADMIN', label: '科室管理员' },
  { value: 'DOCTOR', label: '康复医师' },
  { value: 'THERAPIST', label: '康复治疗师' },
  { value: 'PATIENT', label: '患者' }
]

const filteredList = computed(() => {
  if (!keyword.value && !roleFilter.value) return list.value
  return list.value.filter(l =>
    (!roleFilter.value || l.role === roleFilter.value) &&
    (!keyword.value ||
      (l.username || '').includes(keyword.value) ||
      (l.module || '').includes(keyword.value) ||
      (l.action || '').includes(keyword.value)))
})

const roleLabels = { ADMIN: '系统管理员', DOCTOR: '康复医师', THERAPIST: '康复治疗师', PATIENT: '患者', DEPT_ADMIN: '科室管理员' }
const roleLabel = r => roleLabels[r] || r
const roleTagType = r => ({ ADMIN: 'danger', DOCTOR: 'primary', THERAPIST: 'success', PATIENT: 'info', DEPT_ADMIN: 'warning' })[r] || 'info'
const fmtTime = t => t ? String(t).slice(0, 19).replace('T', ' ') : '-'

async function load() {
  list.value = await request.get('/audit-logs', { params: { page: page.value, size } })
  total.value = list.value.length === size ? (page.value + 2) * size : page.value * size + list.value.length
}

function changePage(p) {
  page.value = p - 1
  load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
.filters {
  display: flex;
  gap: 10px;
}
.pager {
  margin-top: 14px;
  justify-content: flex-end;
}
</style>
