<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索床位号/病房号" style="width: 220px" clearable />
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增床位</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="bedNo" label="床位号" width="100" />
      <el-table-column prop="roomNo" label="病房号" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === '空闲' ? 'success' : 'danger'">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="录入时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === '空闲'" size="small" type="success" @click="openAssign(row)">分配病人</el-button>
          <el-button v-else size="small" type="warning" @click="release(row)">退床</el-button>
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑床位' : '新增床位'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="床位号" required>
          <el-input v-model="form.bedNo" placeholder="如：B101" />
        </el-form-item>
        <el-form-item label="病房号">
          <el-input v-model="form.roomNo" placeholder="如：101" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="assignVisible" :title="`分配病人到 ${assignBed.bedNo || ''}`" width="480px">
      <el-form label-width="80px">
        <el-form-item label="选择病人">
          <el-select v-model="assignPatientId" filterable style="width: 100%" placeholder="请选择病人">
            <el-option v-for="p in patients" :key="p.id" :label="`${p.name}（${p.diagnosis || '无诊断'}）`" :value="p.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" @click="doAssign">确认分配</el-button>
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
const patients = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})
const assignVisible = ref(false)
const assignBed = ref({})
const assignPatientId = ref(null)

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(b =>
    (b.bedNo || '').includes(keyword.value) || (b.roomNo || '').includes(keyword.value)
  )
})

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

async function load() {
  list.value = await request.get('/beds')
}

function openDialog(row) {
  form.value = row ? { ...row } : { bedNo: '', roomNo: '' }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.bedNo) {
    ElMessage.warning('请输入床位号')
    return
  }
  try {
    if (form.value.id) {
      await request.put(`/beds/${form.value.id}`, form.value)
    } else {
      await request.post('/beds', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除床位「${row.bedNo}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/beds/${row.id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

async function openAssign(row) {
  assignBed.value = row
  assignPatientId.value = null
  patients.value = await request.get('/patients')
  assignVisible.value = true
}

async function doAssign() {
  if (!assignPatientId.value) {
    ElMessage.warning('请选择病人')
    return
  }
  try {
    await request.post(`/beds/${assignBed.value.id}/assign`, null, { params: { patientId: assignPatientId.value } })
    ElMessage.success('分配成功')
    assignVisible.value = false
    load()
  } catch {}
}

async function release(row) {
  try {
    await ElMessageBox.confirm(`确定给「${row.bedNo}」办理退床吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.post(`/beds/${row.id}/release`)
    ElMessage.success('退床成功')
    load()
  } catch {}
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
</style>
