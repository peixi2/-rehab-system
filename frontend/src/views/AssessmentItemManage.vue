<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索量表名称/编码" style="width: 240px" clearable />
      <el-button type="primary" :icon="Plus" @click="openEdit()">新增量表</el-button>
    </div>
    <el-table :data="filteredList" stripe border>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="量表名称" min-width="200" />
      <el-table-column prop="code" label="编码" width="100" />
      <el-table-column prop="version" label="版本" width="80" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === '启用' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="description" label="说明" min-width="220" show-overflow-tooltip />
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openItems(row)">配置条目</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 基本信息编辑 -->
    <el-dialog v-model="editVisible" :title="form.id ? '编辑量表' : '新增量表'" width="560px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="量表名称" required>
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="编码">
          <el-input v-model="form.code" placeholder="如 BARTHEL" />
        </el-form-item>
        <el-form-item label="版本">
          <el-input v-model="form.version" placeholder="如 1.0" style="width: 150px" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="启用">启用</el-radio>
            <el-radio value="停用">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="说明">
          <el-input v-model="form.description" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="指导语">
          <el-input v-model="form.instructions" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="分级规则">
          <el-input v-model="form.gradingRules" type="textarea" :rows="4"
            placeholder='JSON数组，如 [{"min":0,"max":20,"grade":"高跌倒风险"}]' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" @click="save">保存</el-button>
      </template>
    </el-dialog>

    <!-- 条目配置 -->
    <el-dialog v-model="itemsVisible" :title="`配置条目 - ${current.name || ''}`" width="900px" top="4vh">
      <div class="items-layout">
        <div class="item-list">
          <div class="section-title">条目（{{ items.length }}）</div>
          <el-scrollbar height="520px">
            <div v-for="(item, idx) in items" :key="idx" class="item-row" :class="{ active: idx === activeIdx }" @click="activeIdx = idx">
              <span class="item-no">{{ idx + 1 }}.</span>
              <div class="item-content">
                <div class="item-question">{{ item.question || '（未填写题干）' }}</div>
                <el-tag size="small" :type="typeTag(item.type)">{{ typeLabel(item.type) }}</el-tag>
              </div>
              <div class="item-actions">
                <el-button link type="primary" size="small" @click.stop="moveItem(idx, -1)" :disabled="idx === 0">上移</el-button>
                <el-button link type="primary" size="small" @click.stop="moveItem(idx, 1)" :disabled="idx === items.length - 1">下移</el-button>
                <el-button link type="danger" size="small" @click.stop="removeItem(idx)">删除</el-button>
              </div>
            </div>
          </el-scrollbar>
          <el-button style="width: 100%; margin-top: 10px" :icon="Plus" @click="addItem">添加条目</el-button>
        </div>
        <div class="item-editor">
          <template v-if="items[activeIdx]">
            <el-form label-width="70px" size="small">
              <el-form-item label="题号">
                <el-input-number v-model="items[activeIdx].itemNo" :min="1" />
              </el-form-item>
              <el-form-item label="题干" required>
                <el-input v-model="items[activeIdx].question" type="textarea" :rows="2" />
              </el-form-item>
              <el-form-item label="类型">
                <el-select v-model="items[activeIdx].type">
                  <el-option label="单选题（选项计分）" value="SINGLE" />
                  <el-option label="多选题（选项求和）" value="MULTI" />
                  <el-option label="数值题（直接填分）" value="NUMBER" />
                  <el-option label="文本题（不计分）" value="TEXT" />
                </el-select>
              </el-form-item>
            </el-form>
            <template v-if="['SINGLE', 'MULTI'].includes(items[activeIdx].type)">
              <div class="section-title">选项</div>
              <div v-for="(opt, oIdx) in (items[activeIdx].options || [])" :key="oIdx" class="option-row">
                <el-input v-model="opt.label" placeholder="选项描述" />
                <el-input-number v-model="opt.score" :min="0" placeholder="分值" style="width: 110px" />
                <el-button link type="danger" @click="items[activeIdx].options.splice(oIdx, 1)">删除</el-button>
              </div>
              <el-button size="small" :icon="Plus" @click="addOption(activeIdx)">添加选项</el-button>
            </template>
            <div v-else-if="items[activeIdx].type === 'NUMBER'" class="tip">数值题由评估者直接填写数值，自动计入总分</div>
            <div v-else class="tip">文本题用于记录定性信息，不计入总分</div>
          </template>
          <el-empty v-else description="请添加或选择左侧条目" />
        </div>
      </div>
      <template #footer>
        <el-button @click="itemsVisible = false">取消</el-button>
        <el-button type="primary" @click="saveItems">保存条目</el-button>
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
const editVisible = ref(false)
const itemsVisible = ref(false)
const form = ref({})
const current = ref({})
const items = ref([])
const activeIdx = ref(-1)

const filteredList = computed(() => {
  if (!keyword.value) return list.value
  return list.value.filter(i =>
    (i.name || '').includes(keyword.value) || (i.code || '').includes(keyword.value))
})

async function load() {
  list.value = await request.get('/assessment-items')
}

function openEdit(row) {
  form.value = row
    ? { ...row }
    : { name: '', code: '', version: '1.0', status: '启用', description: '', instructions: '', gradingRules: '' }
  editVisible.value = true
}

async function save() {
  if (!form.value.name) {
    ElMessage.warning('请填写量表名称')
    return
  }
  try {
    if (form.value.id) {
      await request.put(`/assessment-items/${form.value.id}`, form.value)
    } else {
      await request.post('/assessment-items', form.value)
    }
    ElMessage.success('保存成功')
    editVisible.value = false
    load()
  } catch {}
}

async function remove(row) {
  try {
    await ElMessageBox.confirm(`确定删除量表「${row.name}」吗？`, '提示', { type: 'warning' })
  } catch {
    return
  }
  try {
    await request.delete(`/assessment-items/${row.id}`)
    ElMessage.success('删除成功')
    load()
  } catch {}
}

async function openItems(row) {
  current.value = row
  const data = await request.get(`/scales/${row.id}/detail`)
  items.value = (data.items || []).map(i => ({ ...i, options: i.options || [] }))
  activeIdx.value = items.value.length ? 0 : -1
  itemsVisible.value = true
}

function addItem() {
  items.value.push({ itemNo: items.value.length + 1, question: '', type: 'SINGLE', options: [] })
  activeIdx.value = items.value.length - 1
}

function removeItem(idx) {
  items.value.splice(idx, 1)
  activeIdx.value = items.value.length ? Math.min(idx, items.value.length - 1) : -1
}

function moveItem(idx, delta) {
  const target = idx + delta
  if (target < 0 || target >= items.value.length) return
  const tmp = items.value[idx]
  items.value[idx] = items.value[target]
  items.value[target] = tmp
  activeIdx.value = target
}

function addOption(itemIdx) {
  if (!items.value[itemIdx].options) items.value[itemIdx].options = []
  items.value[itemIdx].options.push({ label: '', score: 0 })
}

function typeLabel(type) {
  return { SINGLE: '单选', MULTI: '多选', NUMBER: '数值', TEXT: '文本' }[type] || type
}

function typeTag(type) {
  return { SINGLE: 'primary', MULTI: 'warning', NUMBER: 'success', TEXT: 'info' }[type] || 'info'
}

async function saveItems() {
  for (const item of items.value) {
    if (!item.question) {
      ElMessage.warning('存在未填写题干的条目')
      return
    }
  }
  const payload = items.value.map((item, idx) => ({
    itemNo: item.itemNo,
    question: item.question,
    type: item.type,
    sort: idx,
    options: (item.options || []).map((opt, oIdx) => ({ label: opt.label, score: opt.score, sort: oIdx }))
  }))
  try {
    await request.put(`/scales/${current.value.id}/items`, payload)
    ElMessage.success('条目已保存')
    itemsVisible.value = false
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
.items-layout {
  display: flex;
  gap: 14px;
}
.item-list {
  width: 340px;
  flex-shrink: 0;
}
.item-editor {
  flex: 1;
  border-left: 1px solid #ebeef5;
  padding-left: 14px;
}
.section-title {
  font-weight: bold;
  margin-bottom: 8px;
}
.item-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border: 1px solid #ebeef5;
  border-radius: 4px;
  margin-bottom: 6px;
  cursor: pointer;
}
.item-row.active {
  border-color: #409EFF;
  background: #ecf5ff;
}
.item-no {
  color: #909399;
  width: 30px;
}
.item-content {
  flex: 1;
}
.item-question {
  font-size: 13px;
  margin-bottom: 4px;
}
.item-actions {
  display: flex;
  flex-direction: column;
}
.option-row {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 8px;
}
.tip {
  color: #909399;
  font-size: 13px;
  background: #f4f4f5;
  padding: 10px;
  border-radius: 4px;
}
</style>
