<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索评估项目" style="width: 220px" clearable />
      <el-button v-if="!isPatient" type="primary" :icon="Plus" @click="openDialog()">新增评估记录</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column v-if="!isPatient" label="病人" width="100">
        <template #default="{ row }">{{ row.patient?.name }}</template>
      </el-table-column>
      <el-table-column label="评估项目" width="150">
        <template #default="{ row }">{{ row.item?.name }}</template>
      </el-table-column>
      <el-table-column label="评估医生" width="100">
        <template #default="{ row }">{{ row.doctor?.name }}</template>
      </el-table-column>
      <el-table-column prop="score" label="评分" width="80" />
      <el-table-column prop="result" label="评定结果" min-width="200" show-overflow-tooltip />
      <el-table-column prop="assessDate" label="评估日期" width="110" />
      <el-table-column v-if="!isPatient" label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑评估记录' : '新增评估记录'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="病人" required>
          <el-select v-model="form.patientId" filterable style="width: 100%">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估项目" required>
          <el-select v-model="form.itemId" filterable style="width: 100%">
            <el-option v-for="i in items" :key="i.id" :label="i.name" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评估医生" required>
          <el-select v-model="form.doctorId" filterable style="width: 100%">
            <el-option v-for="d in doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="评分">
          <el-input-number v-model="form.score" :min="0" :max="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="评定结果">
          <el-input v-model="form.result" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="评估日期">
          <el-date-picker v-model="form.assessDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
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
const patients = ref([])
const items = ref([])
const doctors = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})

const user = JSON.parse(localStorage.getItem('user') || 'null')
const isPatient = user?.role === 'PATIENT'

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(r =>
    (r.patient?.name || '').includes(keyword.value) || (r.item?.name || '').includes(keyword.value))
})

async function load() {
  list.value = await request.get('/assessment-records')
}

async function loadOptions() {
  patients.value = await request.get('/patients')
  items.value = await request.get('/assessment-items')
  doctors.value = await request.get('/doctors')
}

function openDialog(row) {
  form.value = row
    ? { ...row, patientId: row.patient?.id, itemId: row.item?.id, doctorId: row.doctor?.id }
    : { patientId: null, itemId: null, doctorId: null, score: null, result: '', assessDate: '' }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.patientId || !form.value.itemId || !form.value.doctorId) {
    ElMessage.warning('请完整选择病人、评估项目和评估医生')
    return
  }
  const payload = {
    patientId: form.value.patientId,
    itemId: form.value.itemId,
    doctorId: form.value.doctorId,
    score: form.value.score,
    result: form.value.result,
    assessDate: form.value.assessDate
  }
  try {
    if (form.value.id) {
      await request.put(`/assessment-records/${form.value.id}`, payload)
    } else {
      await request.post('/assessment-records', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm('确定删除这条评估记录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/assessment-records/${row.id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

onMounted(() => {
  load()
  if (!isPatient) loadOptions()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
</style>
