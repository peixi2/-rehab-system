<template>
  <div>
    <el-card class="welcome-card">
      <h2>康复评估过程管理系统</h2>
      <p>管理病床、病人、医生及评估/治疗项目，记录康复评估与治疗全过程。</p>
    </el-card>
    <div class="stat-grid">
      <el-card v-for="item in cards" :key="item.label" class="stat-card">
        <div class="stat-icon" :style="{ backgroundColor: item.color }">
          <el-icon :size="24"><component :is="item.icon" /></el-icon>
        </div>
        <div>
          <div class="stat-value">{{ item.value }}</div>
          <div class="stat-label">{{ item.label }}</div>
        </div>
      </el-card>
    </div>
    <el-card v-if="diagnosis.length" class="diag-card">
      <template #header>病种分布</template>
      <div class="diag-layout">
        <div ref="diagChartRef" class="diag-chart" />
        <div class="diag-list">
          <div v-for="d in diagnosis" :key="d.diagnosis" class="diag-row">
            <span class="diag-name">{{ d.diagnosis || '未诊断' }}</span>
            <el-progress :percentage="pct(d.count)" :stroke-width="14" text-inside class="diag-progress" />
            <span class="diag-count">{{ d.count }} 人</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const stats = ref({})

const cards = ref([])
const diagnosis = ref([])
const diagChartRef = ref(null)
let diagChart = null

const maxDiagCount = computed(() => Math.max(...diagnosis.value.map(d => d.count), 1))
const pct = c => Math.round((c / maxDiagCount.value) * 100)

async function load() {
  stats.value = await request.get('/stats')
  const s = stats.value
  cards.value = [
    { label: '病人总数', value: s.patientCount, icon: 'FirstAidKit', color: '#409eff' },
    { label: '医生总数', value: s.doctorCount, icon: 'User', color: '#67c23a' },
    { label: '床位总数', value: s.bedCount, icon: 'OfficeBuilding', color: '#e6a23c' },
    { label: '空闲床位', value: s.freeBedCount, icon: 'CircleCheck', color: '#67c23a' },
    { label: '占用床位', value: s.occupiedBedCount, icon: 'Warning', color: '#f56c6c' },
    { label: '评估项目', value: s.assessmentItemCount, icon: 'EditPen', color: '#409eff' },
    { label: '治疗项目', value: s.treatmentItemCount, icon: 'Suitcase', color: '#e6a23c' },
    { label: '评估记录', value: s.assessmentRecordCount, icon: 'Notebook', color: '#67c23a' },
    { label: '治疗记录', value: s.treatmentRecordCount, icon: 'List', color: '#f56c6c' }
  ]
}

async function loadDiagnosis() {
  diagnosis.value = await request.get('/stats/diagnosis')
  await nextTick()
  if (!diagChartRef.value) return
  diagChart = echarts.init(diagChartRef.value)
  diagChart.setOption({
    tooltip: { trigger: 'item', formatter: '{b}: {c} 人（{d}%）' },
    legend: { orient: 'vertical', right: 10, top: 'center' },
    series: [{
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['38%', '50%'],
      label: { formatter: '{b}\n{c}人' },
      data: diagnosis.value.map(d => ({ name: d.diagnosis || '未诊断', value: d.count }))
    }]
  })
}

function handleResize() {
  diagChart?.resize()
}

onMounted(() => {
  load()
  loadDiagnosis()
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  diagChart?.dispose()
})
</script>

<style scoped>
.welcome-card {
  margin-bottom: 16px;
}
.welcome-card h2 {
  margin: 0 0 8px;
}
.welcome-card p {
  margin: 0;
  color: #909399;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}
.stat-value {
  font-size: 24px;
  font-weight: bold;
}
.stat-label {
  color: #909399;
}
.diag-card {
  margin-top: 16px;
}
.diag-layout {
  display: flex;
  gap: 20px;
  align-items: center;
}
.diag-chart {
  width: 45%;
  height: 320px;
}
.diag-list {
  flex: 1;
}
.diag-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
}
.diag-name {
  width: 130px;
  flex-shrink: 0;
  font-size: 13px;
}
.diag-progress {
  flex: 1;
}
.diag-count {
  width: 60px;
  flex-shrink: 0;
  text-align: right;
  color: #606266;
  font-size: 13px;
}
</style>
