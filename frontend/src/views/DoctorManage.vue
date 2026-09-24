<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索姓名/科室" style="width: 220px" clearable />
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增医生</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="title" label="职称" width="130" />
      <el-table-column prop="department" label="科室" width="120" />
      <el-table-column prop="phone" label="联系电话" width="130" />
      <el-table-column prop="createTime" label="录入时间" width="150">
        <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
      </el-table-column>
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑医生' : '新增医生'" width="480px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入医生姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称">
          <el-select v-model="form.title" clearable filterable allow-create style="width: 100%">
            <el-option v-for="t in titles" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="科室">
          <el-input v-model="form.department" placeholder="如：康复科" />
        </el-form-item>
        <el-form-item label="电话">
          <el-input v-model="form.phone" placeholder="联系电话" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import request from '../api/request'

const titles = ['主任医师', '副主任医师', '主治医师', '住院医师', '康复治疗师']

const list = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(d =>
    (d.name || '').includes(keyword.value) || (d.department || '').includes(keyword.value)
  )
})

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

async function load() {
  list.value = await request.get('/doctors')
}

function openDialog(row) {
  form.value = row
    ? { ...row }
    : { name: '', gender: '', title: '', department: '', phone: '' }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('请输入医生姓名')
    return
  }
  try {
    if (form.value.id) {
      await request.put(`/doctors/${form.value.id}`, form.value)
    } else {
      await request.post('/doctors', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除医生「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/doctors/${row.id}`)
    ElMessage.success('删除成功')
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
