<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索项目名称" style="width: 220px" clearable />
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增治疗项目</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="项目名称" width="200" />
      <el-table-column prop="description" label="项目说明" min-width="260" show-overflow-tooltip />
      <el-table-column prop="durationMinutes" label="时长(分钟)" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑治疗项目' : '新增治疗项目'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="项目名称" required>
          <el-input v-model="form.name" placeholder="如：物理治疗" />
        </el-form-item>
        <el-form-item label="项目说明">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="时长(分钟)">
          <el-input-number v-model="form.durationMinutes" :min="0" :max="600" style="width: 100%" />
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

const list = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(i => (i.name || '').includes(keyword.value))
})

function formatTime(t) {
  return t ? t.replace('T', ' ').substring(0, 16) : ''
}

async function load() {
  list.value = await request.get('/treatment-items')
}

function openDialog(row) {
  form.value = row ? { ...row } : { name: '', description: '', durationMinutes: null }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('请输入项目名称')
    return
  }
  try {
    if (form.value.id) {
      await request.put(`/treatment-items/${form.value.id}`, form.value)
    } else {
      await request.post('/treatment-items', form.value)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除治疗项目「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/treatment-items/${row.id}`)
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
