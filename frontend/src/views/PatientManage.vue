<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索姓名/诊断" style="width: 220px" clearable />
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增病人</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="gender" label="性别" width="70" />
      <el-table-column prop="age" label="年龄" width="70" />
      <el-table-column prop="diagnosis" label="诊断" min-width="140" show-overflow-tooltip />
      <el-table-column label="负责医生" width="100">
        <template #default="{ row }">{{ row.doctor?.name || '未分配' }}</template>
      </el-table-column>
      <el-table-column label="床位" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.bed" type="success">{{ row.bed.bedNo }}</el-tag>
          <span v-else style="color: #909399">未分配</span>
        </template>
      </el-table-column>
      <el-table-column prop="admissionDate" label="入院日期" width="110" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openDialog(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑病人' : '新增病人'" width="520px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="姓名" required>
          <el-input v-model="form.name" placeholder="请输入病人姓名" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" clearable style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄">
          <el-input-number v-model="form.age" :min="0" :max="120" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="诊断">
          <el-input v-model="form.diagnosis" placeholder="如：脑卒中后偏瘫" />
        </el-form-item>
        <el-form-item label="入院日期">
          <el-date-picker v-model="form.admissionDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
        </el-form-item>
        <el-form-item label="负责医生">
          <el-select v-model="form.doctorId" clearable filterable style="width: 100%">
            <el-option v-for="d in doctors" :key="d.id" :label="`${d.name}（${d.title || ''}）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="床位">
          <el-select v-model="form.bedId" clearable style="width: 100%">
            <el-option
              v-for="b in beds"
              :key="b.id"
              :label="`${b.bedNo}（${b.status}）`"
              :value="b.id"
              :disabled="b.status === '占用' && form.bedId !== b.id"
            />
          </el-select>
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
const doctors = ref([])
const beds = ref([])
const keyword = ref('')
const dialogVisible = ref(false)
const form = ref({})

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(p =>
    (p.name || '').includes(keyword.value) || (p.diagnosis || '').includes(keyword.value)
  )
})

async function load() {
  list.value = await request.get('/patients')
}

async function loadOptions() {
  doctors.value = await request.get('/doctors')
  beds.value = await request.get('/beds')
}

function openDialog(row) {
  form.value = row
    ? { ...row, doctorId: row.doctor?.id ?? null, bedId: row.bed?.id ?? null }
    : { name: '', gender: '', age: null, phone: '', diagnosis: '', admissionDate: '', doctorId: null, bedId: null }
  dialogVisible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('请输入病人姓名')
    return
  }
  const payload = {
    name: form.value.name,
    gender: form.value.gender,
    age: form.value.age,
    phone: form.value.phone,
    diagnosis: form.value.diagnosis,
    admissionDate: form.value.admissionDate,
    doctorId: form.value.doctorId,
    bedId: form.value.bedId
  }
  try {
    if (form.value.id) {
      await request.put(`/patients/${form.value.id}`, payload)
    } else {
      await request.post('/patients', payload)
    }
    ElMessage.success('保存成功')
    dialogVisible.value = false
    load()
    loadOptions()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除病人「${row.name}」吗？其评估/治疗记录将一并删除。`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/patients/${row.id}`)
    ElMessage.success('删除成功')
    load()
    loadOptions()
  } catch {}
}

onMounted(() => {
  load()
  loadOptions()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 14px;
}
</style>
