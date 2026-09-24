<template>
  <div>
    <el-card v-if="patient" class="bed-card">
      <div class="bed-main">
        <div class="bed-icon">
          <el-icon :size="40"><OfficeBuilding /></el-icon>
        </div>
        <div class="bed-info">
          <div class="bed-label">我的床位</div>
          <div class="bed-no">{{ patient.bed ? patient.bed.bedNo : '暂未分配床位' }}</div>
          <div v-if="patient.bed" class="bed-room">房间：{{ patient.bed.roomNo || '-' }}</div>
        </div>
      </div>
    </el-card>

    <el-card v-if="patient">
      <template #header>基本信息</template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="姓名">{{ patient.name }}</el-descriptions-item>
        <el-descriptions-item label="性别">{{ patient.gender || '-' }}</el-descriptions-item>
        <el-descriptions-item label="年龄">{{ patient.age ? patient.age + ' 岁' : '-' }}</el-descriptions-item>
        <el-descriptions-item label="主治医生">{{ patient.doctor?.name || '-' }}</el-descriptions-item>
        <el-descriptions-item label="诊断">{{ patient.diagnosis || '-' }}</el-descriptions-item>
        <el-descriptions-item label="入院日期">{{ patient.admissionDate || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import request from '../api/request'

const patient = ref(null)

onMounted(async () => {
  try {
    patient.value = await request.get('/patients/me')
  } catch {}
})
</script>

<style scoped>
.bed-card {
  margin-bottom: 16px;
}
.bed-main {
  display: flex;
  align-items: center;
  gap: 24px;
}
.bed-icon {
  width: 80px;
  height: 80px;
  border-radius: 12px;
  background: #409eff;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}
.bed-label {
  color: #909399;
  font-size: 14px;
}
.bed-no {
  font-size: 36px;
  font-weight: bold;
  color: #303133;
}
.bed-room {
  color: #606266;
  font-size: 14px;
  margin-top: 4px;
}
</style>
