<template>
  <el-card>
    <div class="toolbar">
      <el-radio-group v-model="statusFilter" @change="load">
        <el-radio-button value="">全部</el-radio-button>
        <el-radio-button value="待执行">待执行</el-radio-button>
        <el-radio-button value="已完成">已完成</el-radio-button>
        <el-radio-button value="已驳回">已驳回</el-radio-button>
      </el-radio-group>
      <el-button type="primary" :icon="Plus" @click="openApply">发起评估申请</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column label="病人" width="90">
        <template #default="{ row }">{{ row.patient?.name }}</template>
      </el-table-column>
      <el-table-column label="量表" width="170">
        <template #default="{ row }">{{ row.item?.name }}</template>
      </el-table-column>
      <el-table-column label="开单医师" width="90">
        <template #default="{ row }">{{ row.doctor?.name }}</template>
      </el-table-column>
      <el-table-column label="执行治疗师" width="100">
        <template #default="{ row }">{{ row.therapist?.name || '-' }}</template>
      </el-table-column>
      <el-table-column prop="applyDate" label="申请日期" width="105" />
      <el-table-column label="状态" width="85">
        <template #default="{ row }">
          <el-tag :type="statusTag(row.status)" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="note" label="备注" min-width="150" show-overflow-tooltip />
      <el-table-column label="操作" width="300" fixed="right">
        <template #default="{ row }">
          <template v-if="row.status === '待执行'">
            <el-button size="small" type="primary" @click="openExecute(row)">执行</el-button>
            <el-button size="small" type="success" @click="openEdit(row)">编辑</el-button>
            <el-button size="small" type="warning" @click="openReject(row)">驳回</el-button>
          </template>
          <el-button v-else size="small" @click="showDetail(row)">详情</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 发起申请 -->
    <el-dialog v-model="applyVisible" title="发起评估申请" width="480px">
      <el-form label-width="90px">
        <el-form-item label="病人" required>
          <el-select v-model="applyForm.patientId" filterable style="width: 100%">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="量表" required>
          <el-select v-model="applyForm.itemId" filterable style="width: 100%">
            <el-option v-for="i in enabledItems" :key="i.id" :label="i.name" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开单医师" required>
          <el-select v-model="applyForm.doctorId" filterable style="width: 100%">
            <el-option v-for="d in doctors" :key="d.id" :label="`${d.name}（${d.title || ''}）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="applyForm.note" type="textarea" :rows="2" placeholder="如：入院首次评估" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="applyVisible = false">取消</el-button>
        <el-button type="primary" @click="submitApply">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 编辑任务 -->
    <el-dialog v-model="editVisible" title="编辑评估任务" width="480px">
      <el-form label-width="90px">
        <el-form-item label="病人" required>
          <el-select v-model="editForm.patientId" filterable style="width: 100%">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="量表" required>
          <el-select v-model="editForm.itemId" filterable style="width: 100%">
            <el-option v-for="i in enabledItems" :key="i.id" :label="i.name" :value="i.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="开单医师" required>
          <el-select v-model="editForm.doctorId" filterable style="width: 100%">
            <el-option v-for="d in doctors" :key="d.id" :label="`${d.name}（${d.title || ''}）`" :value="d.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="editForm.note" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="submitEdit">保存修改</el-button>
      </template>
    </el-dialog>

    <!-- 驳回 -->
    <el-dialog v-model="rejectVisible" title="驳回任务" width="420px">
      <el-form label-width="80px">
        <el-form-item label="驳回原因" required>
          <el-input v-model="rejectReason" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectVisible = false">取消</el-button>
        <el-button type="warning" @click="submitReject">确认驳回</el-button>
      </template>
    </el-dialog>

    <!-- 执行：在线填写量表 -->
    <el-dialog v-model="executeVisible" :title="`执行评估 - ${executeTask.item?.name || ''}`" width="720px" top="3vh">
      <el-alert v-if="executeScale.instructions" :title="executeScale.instructions" type="info" :closable="false" class="mb10" />
      <div class="patient-info">
        <span>病人：<b>{{ executeTask.patient?.name }}</b></span>
        <span>{{ executeTask.patient?.gender }} / {{ executeTask.patient?.age }}岁</span>
        <span>诊断：{{ executeTask.patient?.diagnosis }}</span>
      </div>
      <el-form label-width="70px">
        <el-form-item label="治疗师" required>
          <el-select v-model="executeForm.therapistId" filterable style="width: 220px">
            <el-option v-for="d in doctors" :key="d.id" :label="d.name" :value="d.id" />
          </el-select>
        </el-form-item>
      </el-form>
      <el-scrollbar height="380px">
        <div v-for="item in executeItems" :key="item.id" class="question-block">
          <div class="question-title">{{ item.itemNo }}. {{ item.question }}
            <el-tag size="small" type="info" class="ml10">{{ typeLabel(item.type) }}</el-tag>
          </div>
          <el-radio-group v-if="item.type === 'SINGLE'" v-model="answers[item.id].optionIds[0]" class="option-group" @change="calcScore">
            <el-radio v-for="opt in item.options" :key="opt.id" :value="opt.id" class="option-item">
              {{ opt.label }}<span class="option-score">（{{ opt.score }}分）</span>
            </el-radio>
          </el-radio-group>
          <el-checkbox-group v-else-if="item.type === 'MULTI'" v-model="answers[item.id].optionIds" @change="calcScore">
            <el-checkbox v-for="opt in item.options" :key="opt.id" :value="opt.id" class="option-item">
              {{ opt.label }}<span class="option-score">（{{ opt.score }}分）</span>
            </el-checkbox>
          </el-checkbox-group>
          <el-input-number v-else-if="item.type === 'NUMBER'" v-model="answers[item.id].numberValue" :min="0" :max="100" @change="calcScore" />
          <el-input v-else v-model="answers[item.id].textValue" placeholder="填写文字记录" />
        </div>
      </el-scrollbar>
      <div class="score-bar">
        <div>
          实时得分：<span class="total-score">{{ totalScore }}</span> 分
          <el-tag v-if="grade" type="danger" class="ml10">{{ grade }}</el-tag>
        </div>
        <div class="result-input">
          <el-input v-model="executeForm.result" placeholder="评估小结（自动生成分级后可补充说明）" style="width: 320px" />
        </div>
      </div>
      <template #footer>
        <el-button @click="executeVisible = false">取消</el-button>
        <el-button type="primary" @click="submitExecute">提交评估</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情 -->
    <el-dialog v-model="detailVisible" title="任务详情" width="480px">
      <el-descriptions :column="1" border>
        <el-descriptions-item label="病人">{{ detailTask.patient?.name }}</el-descriptions-item>
        <el-descriptions-item label="量表">{{ detailTask.item?.name }}</el-descriptions-item>
        <el-descriptions-item label="开单医师">{{ detailTask.doctor?.name }}</el-descriptions-item>
        <el-descriptions-item label="执行治疗师">{{ detailTask.therapist?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detailTask.status }}</el-descriptions-item>
        <el-descriptions-item label="申请日期">{{ detailTask.applyDate }}</el-descriptions-item>
        <el-descriptions-item label="执行日期">{{ detailTask.executeDate || '-' }}</el-descriptions-item>
        <el-descriptions-item v-if="detailTask.rejectReason" label="驳回原因">{{ detailTask.rejectReason }}</el-descriptions-item>
        <el-descriptions-item v-if="detailTask.note" label="备注">{{ detailTask.note }}</el-descriptions-item>
      </el-descriptions>
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
const doctors = ref([])
const items = ref([])
const statusFilter = ref('')
const applyVisible = ref(false)
const rejectVisible = ref(false)
const executeVisible = ref(false)
const detailVisible = ref(false)
const editVisible = ref(false)
const editForm = ref({})
const applyForm = ref({})
const rejectTask = ref(null)
const rejectReason = ref('')
const executeTask = ref({})
const executeForm = ref({})
const executeScale = ref({})
const executeItems = ref([])
const answers = ref({})
const totalScore = ref(0)
const grade = ref('')
const detailTask = ref({})

const filteredList = computed(() =>
  statusFilter.value ? list.value.filter(t => t.status === statusFilter.value) : list.value
)
const enabledItems = computed(() => items.value.filter(i => i.status === '启用'))

async function load() {
  list.value = await request.get('/assessment-tasks')
}

async function loadOptions() {
  patients.value = await request.get('/patients')
  doctors.value = await request.get('/doctors')
  items.value = await request.get('/assessment-items')
}

function openApply() {
  applyForm.value = { patientId: null, itemId: null, doctorId: null, note: '' }
  applyVisible.value = true
}

async function submitApply() {
  if (!applyForm.value.patientId || !applyForm.value.itemId || !applyForm.value.doctorId) {
    ElMessage.warning('请完整选择病人、量表和开单医师')
    return
  }
  try {
    await request.post('/assessment-tasks', applyForm.value)
    ElMessage.success('评估申请已提交')
    applyVisible.value = false
    load()
  } catch {}
}

function openEdit(row) {
  editForm.value = {
    id: row.id,
    patientId: row.patient?.id,
    itemId: row.item?.id,
    doctorId: row.doctor?.id,
    note: row.note || ''
  }
  editVisible.value = true
}

async function submitEdit() {
  if (!editForm.value.patientId || !editForm.value.itemId || !editForm.value.doctorId) {
    ElMessage.warning('请完整选择病人、量表和开单医师')
    return
  }
  const { id, ...payload } = editForm.value
  try {
    await request.put(`/assessment-tasks/${id}`, payload)
    ElMessage.success('任务已修改')
    editVisible.value = false
    load()
  } catch {}
}

function openReject(row) {
  rejectTask.value = row
  rejectReason.value = ''
  rejectVisible.value = true
}

async function submitReject() {
  if (!rejectReason.value.trim()) {
    ElMessage.warning('请填写驳回原因')
    return
  }
  try {
    await request.put(`/assessment-tasks/${rejectTask.value.id}/reject`, { reason: rejectReason.value })
    ElMessage.success('任务已驳回')
    rejectVisible.value = false
    load()
  } catch {}
}

async function openExecute(row) {
  executeTask.value = row
  executeForm.value = { therapistId: null, result: '' }
  const data = await request.get(`/scales/${row.item.id}/detail`)
  executeScale.value = data.scale
  executeItems.value = data.items
  cachedRules = data.gradingRules || []
  if (executeScale.value.gradingRules && !cachedRules.length) {
    try { cachedRules = JSON.parse(executeScale.value.gradingRules) || [] } catch {}
  }
  answers.value = {}
  for (const item of data.items) {
    answers.value[item.id] = { optionIds: [], numberValue: item.type === 'NUMBER' ? 0 : null, textValue: '' }
  }
  totalScore.value = 0
  grade.value = ''
  executeVisible.value = true
}

function calcScore() {
  let total = 0
  for (const item of executeItems.value) {
    const ans = answers.value[item.id]
    if (!ans) continue
    if (item.type === 'SINGLE' || item.type === 'MULTI') {
      for (const optId of ans.optionIds || []) {
        const opt = item.options.find(o => o.id === optId)
        if (opt) total += opt.score
      }
    } else if (item.type === 'NUMBER') {
      total += Number(ans.numberValue || 0)
    }
  }
  totalScore.value = total
  // 前端按分级规则实时判定
  const rules = dataGradingRules()
  for (const rule of rules) {
    if (total >= rule.min && total <= rule.max) {
      grade.value = rule.grade
      break
    }
  }
}

let cachedRules = []
function dataGradingRules() {
  return cachedRules
}

async function submitExecute() {
  if (!executeForm.value.therapistId) {
    ElMessage.warning('请选择执行治疗师')
    return
  }
  // 提交前由后端权威算分（自动算分 + 分级）
  const answerPayload = {
    answers: executeItems.value.map(item => {
      const ans = answers.value[item.id]
      return {
        itemId: item.id,
        optionIds: ans?.optionIds || [],
        numberValue: ans?.numberValue
      }
    })
  }
  try {
    const scored = await request.post(`/scales/${executeTask.value.item.id}/score`, answerPayload)
    const payload = {
      therapistId: executeForm.value.therapistId,
      score: scored.totalScore,
      result: executeForm.value.result || scored.grade || '',
      detail: JSON.stringify({ answers: answerPayload.answers, grade: scored.grade }),
      assessDate: new Date().toISOString().slice(0, 10)
    }
    await request.post(`/assessment-tasks/${executeTask.value.id}/execute`, payload)
    ElMessage.success('评估完成，评估记录已生成')
    executeVisible.value = false
    load()
  } catch {}
}

function showDetail(row) {
  detailTask.value = row
  detailVisible.value = true
}

async function remove(row) {
  const tip = row.status === '已完成'
    ? '该任务已生成评估记录，删除任务将同时删除其评估记录，确定删除吗？'
    : `确定删除任务 #${row.id} 吗？`
  try {
    await ElMessageBox.confirm(tip, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/assessment-tasks/${row.id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

function statusTag(status) {
  return { 待执行: 'warning', 已完成: 'success', 已驳回: 'danger' }[status] || 'info'
}

function typeLabel(type) {
  return { SINGLE: '单选', MULTI: '多选', NUMBER: '数值', TEXT: '文本' }[type] || type
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
.patient-info {
  display: flex;
  gap: 16px;
  margin-bottom: 12px;
  color: #606266;
  font-size: 14px;
}
.question-block {
  border-bottom: 1px dashed #ebeef5;
  padding: 10px 0;
}
.question-title {
  font-weight: bold;
  margin-bottom: 8px;
}
.option-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.option-item {
  margin-bottom: 4px;
}
.option-score {
  color: #909399;
  font-size: 12px;
}
.score-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 12px;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 4px;
}
.total-score {
  font-size: 22px;
  font-weight: bold;
  color: #409EFF;
}
.ml10 {
  margin-left: 10px;
}
.mb10 {
  margin-bottom: 10px;
}
</style>
