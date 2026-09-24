import { createRouter, createWebHistory } from 'vue-router'
import Layout from '../layout/Layout.vue'

export const menuConfig = [
  { path: '/dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '系统概览', icon: 'Odometer', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST'] } },
  { path: '/patients', name: 'Patients', component: () => import('../views/PatientManage.vue'), meta: { title: '病人管理', icon: 'FirstAidKit', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST'] } },
  { path: '/doctors', name: 'Doctors', component: () => import('../views/DoctorManage.vue'), meta: { title: '医生管理', icon: 'User', roles: ['ADMIN'] } },
  { path: '/beds', name: 'Beds', component: () => import('../views/BedManage.vue'), meta: { title: '床位管理', icon: 'OfficeBuilding', roles: ['ADMIN', 'DEPT_ADMIN'] } },
  { path: '/assessment-items', name: 'AssessmentItems', component: () => import('../views/AssessmentItemManage.vue'), meta: { title: '量表库管理', icon: 'EditPen', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST'] } },
  { path: '/treatment-items', name: 'TreatmentItems', component: () => import('../views/TreatmentItemManage.vue'), meta: { title: '治疗项目管理', icon: 'Suitcase', roles: ['ADMIN', 'DEPT_ADMIN'] } },
  { path: '/assessment-tasks', name: 'AssessmentTasks', component: () => import('../views/AssessmentTaskManage.vue'), meta: { title: '评估任务', icon: 'Tickets', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST'] } },
  { path: '/assessment-records', name: 'AssessmentRecords', component: () => import('../views/AssessmentRecordManage.vue'), meta: { title: '评估记录', icon: 'Notebook', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST', 'PATIENT'] } },
  { path: '/treatment-records', name: 'TreatmentRecords', component: () => import('../views/TreatmentRecordManage.vue'), meta: { title: '治疗记录', icon: 'List', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST', 'PATIENT'] } },
  { path: '/patient-trend', name: 'PatientTrend', component: () => import('../views/PatientTrend.vue'), meta: { title: '康复趋势分析', icon: 'TrendCharts', roles: ['ADMIN', 'DEPT_ADMIN', 'DOCTOR', 'THERAPIST'] } },
  { path: '/users', name: 'Users', component: () => import('../views/UserManage.vue'), meta: { title: '账号管理', icon: 'Key', roles: ['ADMIN'] } },
  { path: '/audit-logs', name: 'AuditLogs', component: () => import('../views/AuditLogView.vue'), meta: { title: '操作日志', icon: 'Document', roles: ['ADMIN'] } },
  { path: '/patient-home', name: 'PatientHome', component: () => import('../views/PatientHome.vue'), meta: { title: '我的床位', icon: 'HomeFilled', roles: ['PATIENT'] } }
]

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: Layout,
    redirect: () => {
      const user = JSON.parse(localStorage.getItem('user') || 'null')
      return user?.role === 'PATIENT' ? '/patient-home' : '/dashboard'
    },
    children: menuConfig
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const homeFor = role => (role === 'PATIENT' ? '/patient-home' : '/dashboard')

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')
  if (to.path === '/login') {
    return token ? '/' : true
  }
  if (!token) {
    return '/login'
  }
  if (to.meta.roles && user && !to.meta.roles.includes(user.role)) {
    return homeFor(user.role)
  }
  return true
})

export default router
