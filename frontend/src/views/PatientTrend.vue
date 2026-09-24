<template>
  <div>
    <el-card>
      <div class="toolbar">
        <div class="filters">
          <el-select v-model="patientId" filterable placeholder="选择病人" style="width: 200px" @change="load">
            <el-option v-for="p in patients" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
          <el-select v-model="itemId" filterable clearable placeholder="全部量表" style="width: 240px" @change="load">
            <el-option v-for="i in items" :key="i.id" :label="i.name" :value="i.id" />
          </el-select>
        </div>
        <span class="tip">选择病人后展示历次评估得分走势，切换量表可对比单项变化</span>
      </div>
      <el-empty v-if="!patientId" description="请先选择病人" />
      <el-empty v-else-if="!points.length" description="该病人暂无评估记录" />
      <template v-else>
        <div ref="chartRef" class="chart" />
        <el-table :data="points" stripe border class="mt10">
          <el-table-column prop="assessDate" label="评估日期" width="110" />
          <el-table-column prop="itemName" label="量表" width="170" />
          <el-table-column prop="score" label="得分" width="80" />
          <el-table-column label="较上次" width="100">
            <template #default="{ row, $index }">
              <el-tag v-if="$index === 0" size="small" type="info">首次</el-tag>
              <el-tag v-else-if="row.delta > 0" size="small" type="success">+{{ row.delta }}</el-tag>
              <el-tag v-else-if="row.delta < 0" size="small" type="danger">{{ row.delta }}</el-tag>
              <el-tag v-else size="small" type="info">持平</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="result" label="评定结果" min-width="220" show-overflow-tooltip />
          <el-table-column prop="doctorName" label="评估医师" width="100" />
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '../api/request'

const patients = ref([])
const items = ref([])
const patientId = ref(null)
const itemId = ref(null)
const data = ref([])
const chartRef = ref(null)
let chart = null

const points = computed(() => {
  const sorted = [...data.value].sort((a, b) => (a.assessDate || '').localeCompare(b.assessDate || ''))
  let prevScore = null
  return sorted.map(p => {
    const delta = prevScore === null ? null : Math.round((p.score - prevScore) * 10) / 10
    prevScore = p.score
    return { ...p, delta }
  })
})

function renderChart() {
  if (!chartRef.value) return
  if (!chart) chart = echarts.init(chartRef.value)
  const filtered = points.value
  const dates = [...new Set(filtered.map(p => p.assessDate))]
  const groups = {}
  for (const p of filtered) {
    const name = p.itemName || '未知名量表'
    if (!groups[name]) groups[name] = []
    groups[name].push({ date: p.assessDate, score: p.score })
  }
  chart.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: Object.keys(groups) },
    grid: { left: 50, right: 30, top: 50, bottom: 40 },
    xAxis: { type: 'category', data: dates, boundaryGap: false },
    yAxis: { type: 'value', name: '得分' },
    series: Object.entries(groups).map(([name, arr]) => ({
      name,
      type: 'line',
      smooth: true,
      symbolSize: 8,
      data: dates.map(d => arr.find(a => a.date === d)?.score ?? null),
      markPoint: {
        data: [
          { type: 'max', name: '最高分' },
          { type: 'min', name: '最低分' }
        ]
      }
    }))
  }, true)
}

async function load() {
  if (!patientId.value) {
    data.value = []
    return
  }
  const params = itemId.value ? { itemId: itemId.value } : {}
  data.value = await request.get(`/stats/patient/${patientId.value}/trend`, { params })
  await nextTick()
  renderChart()
}

function handleResize() {
  chart?.resize()
}

onMounted(async () => {
  patients.value = await request.get('/patients')
  items.value = await request.get('/assessment-items')
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.filters {
  display: flex;
  gap: 10px;
}
.tip {
  color: #909399;
  font-size: 13px;
}
.chart {
  height: 360px;
}
.mt10 {
  margin-top: 12px;
}
</style>
