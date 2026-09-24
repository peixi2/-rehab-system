<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索用户名/姓名" style="width: 240px" clearable />
      <el-button type="primary" :icon="Plus" @click="openCreate">新增账号</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="realName" label="姓名" width="120">
        <template #default="{ row }">{{ row.realName || '-' }}</template>
      </el-table-column>
      <el-table-column label="角色" width="130">
        <template #default="{ row }">
          <el-tag size="small" :type="roleTagType(row.role)">{{ roleLabel(row.role) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column v-if="hasPatientRole" label="关联病人" width="120">
        <template #default="{ row }">
          <template v-if="row.role === 'PATIENT'">{{ patientName(row.patientId) }}</template>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-switch :model-value="row.enabled" @change="v => toggleStatus(row, v)" />
        </template>
      </el-table-column>
      <el-table-column label="创建时间" width="160">
        <template #default="{ row }">{{ fmtTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="250" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.role === 'PATIENT'" size="small" type="primary" @click="openBindPatient(row)">绑定病人</el-button>
          <el-button size="small" @click="openResetPwd(row)">重置密码</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 新增账号 -->
    <el-dialog v-model="createVisible" title="新增账号" width="460px">
      <el-form label-width="90px">
        <el-form-item label="用户名" required>
          <el-input v-model="createForm.username" placeholder="登录用户名" />
        </el-form-item>
        <el-form-item label="密码" required>
          <el-input v-model="createForm.password" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
        <el-form-item label="姓名">
          <el-input v-model="createForm.realName" />
        </el-form-item>
        <el-form-item label="角色" required>
          <el-select v-model="createForm.role" style="width: 100%">
            <el-option v-for="r in roles" :key="r.value" :label="r.label" :value="r.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="createForm.role === 'PATIENT'" label="绑定病人">
          <el-select v-model="createForm.patientId" filterable clearable placeholder="可选，创建后也可绑定" style="width: 100%">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" @click="submitCreate">创建</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码 -->
    <el-dialog v-model="pwdVisible" :title="`重置密码 - ${pwdUser?.username}`" width="420px">
      <el-form label-width="90px">
        <el-form-item label="新密码" required>
          <el-input v-model="newPassword" type="password" show-password placeholder="至少 6 位" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="pwdVisible = false">取消</el-button>
        <el-button type="primary" @click="submitResetPwd">确认重置</el-button>
      </template>
    </el-dialog>

    <!-- 绑定病人 -->
    <el-dialog v-model="bindVisible" :title="`绑定病人 - ${bindUser?.username}`" width="420px">
      <el-form label-width="90px">
        <el-form-item label="关联病人">
          <el-select v-model="bindPatientId" filterable clearable placeholder="选择病人（清空则解绑）" style="width: 100%">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bindVisible = false">取消</el-button>
        <el-button type="primary" @click="submitBindPatient">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../api/request'

const list = ref([])
const keyword = ref('')
const createVisible = ref(false)
const createForm = ref({})
const pwdVisible = ref(false)
const pwdUser = ref(null)
const newPassword = ref('')
const patients = ref([])
const bindVisible = ref(false)
const bindUser = ref(null)
const bindPatientId = ref(null)

const hasPatientRole = computed(() => list.value.some(u => u.role === 'PATIENT'))
const patientName = id => patients.value.find(p => p.id === id)?.name || '-'

const roles = [
  { value: 'ADMIN', label: '系统管理员' },
  { value: 'DEPT_ADMIN', label: '科室管理员' },
  { value: 'DOCTOR', label: '康复医师' },
  { value: 'THERAPIST', label: '康复治疗师' },
  { value: 'PATIENT', label: '患者' }
]

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(u =>
    (u.username || '').includes(keyword.value) || (u.realName || '').includes(keyword.value))
})

const roleLabels = { ADMIN: '系统管理员', DOCTOR: '康复医师', THERAPIST: '康复治疗师', PATIENT: '患者', DEPT_ADMIN: '科室管理员' }
const roleLabel = r => roleLabels[r] || r
const roleTagType = r => ({ ADMIN: 'danger', DOCTOR: 'primary', THERAPIST: 'success', PATIENT: 'info', DEPT_ADMIN: 'warning' })[r] || 'info'
const fmtTime = t => t ? String(t).slice(0, 16).replace('T', ' ') : '-'

async function load() {
  list.value = await request.get('/users')
}

async function loadPatients() {
  patients.value = await request.get('/patients')
}

function openCreate() {
  createForm.value = { username: '', password: '', realName: '', role: 'DOCTOR', patientId: null }
  createVisible.value = true
}

async function submitCreate() {
  if (!createForm.value.username || !createForm.value.password) {
    ElMessage.warning('请填写用户名和密码')
    return
  }
  if (createForm.value.password.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  try {
    await request.post('/users', { username: createForm.value.username, password: createForm.value.password }, {
      params: { role: createForm.value.role, realName: createForm.value.realName, patientId: createForm.value.patientId }
    })
    ElMessage.success('账号已创建')
    createVisible.value = false
    load()
  } catch {}
}

function openBindPatient(row) {
  bindUser.value = row
  bindPatientId.value = row.patientId || null
  bindVisible.value = true
}

async function submitBindPatient() {
  try {
    await request.put(`/users/${bindUser.value.id}/patient`, { patientId: bindPatientId.value })
    ElMessage.success('绑定已更新')
    bindVisible.value = false
    load()
  } catch {}
}

async function toggleStatus(row, enabled) {
  try {
    await request.put(`/users/${row.id}/status`, null, { params: { enabled } })
    row.enabled = enabled
    ElMessage.success(enabled ? '账号已启用' : '账号已停用')
  } catch {}
}

function openResetPwd(row) {
  pwdUser.value = row
  newPassword.value = ''
  pwdVisible.value = true
}

async function submitResetPwd() {
  if (newPassword.value.length < 6) {
    ElMessage.warning('密码至少 6 位')
    return
  }
  try {
    await request.put(`/users/${pwdUser.value.id}/password`, { password: newPassword.value })
    ElMessage.success('密码已重置')
    pwdVisible.value = false
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除账号「${row.username}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/users/${row.id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

onMounted(() => {
  load()
  loadPatients()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
</style>
