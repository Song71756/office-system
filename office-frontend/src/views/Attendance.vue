<template>
  <div class="attendance-container">
    <!-- 打卡区域 -->
    <el-card class="punch-card" shadow="hover">
      <div class="punch-content">
        <div class="punch-time">
          <div class="current-time">{{ currentTime }}</div>
          <div class="current-date">{{ currentDate }}</div>
        </div>
        <el-button
          type="primary"
          size="large"
          round
          :loading="punchLoading"
          class="punch-btn"
          @click="handlePunch"
        >
          <el-icon><Clock /></el-icon>
          <span style="margin-left: 6px">打卡</span>
        </el-button>
      </div>
    </el-card>

    <!-- 月度统计 -->
    <el-card class="stats-card" shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>月度考勤统计</span>
          <div>
            <el-date-picker
              v-model="statsMonth"
              type="month"
              placeholder="选择月份"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              style="width: 160px"
              @change="loadMonthStats"
            />
          </div>
        </div>
      </template>
      <el-row :gutter="16">
        <el-col :span="4" v-for="item in statsList" :key="item.label">
          <div class="stat-item" :style="{ borderColor: item.color }">
            <div class="stat-value" :style="{ color: item.color }">{{ item.value }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 考勤历史记录 -->
    <el-card class="history-card" shadow="hover" style="margin-top: 16px">
      <template #header>
        <div class="card-header">
          <span>考勤记录</span>
        </div>
      </template>
      <el-table :data="historyList" stripe border style="width: 100%">
        <el-table-column prop="attendanceDate" label="日期" width="120" />
        <el-table-column label="签到时间" width="180">
          <template #default="{ row }">
            <span v-if="row.signInTime">{{ formatTime(row.signInTime) }}</span>
            <el-tag v-else type="info" size="small">未签到</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="签退时间" width="180">
          <template #default="{ row }">
            <span v-if="row.signOutTime">{{ formatTime(row.signOutTime) }}</span>
            <el-tag v-else type="info" size="small">未签退</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="打卡地点" min-width="150">
          <template #default="{ row }">
            {{ row.location || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="140">
          <template #default="{ row }">
            {{ row.ipAddress || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="120">
          <template #default="{ row }">
            {{ row.remark || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <!-- 分页 -->
      <div class="pagination-area">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadHistory"
          @current-change="loadHistory"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage } from 'element-plus'
import { punch, getMyHistory, getMyMonthStats } from '@/api/attendance'

// ===== 实时时钟 =====
const currentTime = ref('')
const currentDate = ref('')
let timer = null

const updateClock = () => {
  const now = new Date()
  currentTime.value = now.toLocaleTimeString('zh-CN', { hour12: false })
  const options = { year: 'numeric', month: 'long', day: 'numeric', weekday: 'long' }
  currentDate.value = now.toLocaleDateString('zh-CN', options)
}

// ===== 打卡 =====
const punchLoading = ref(false)

const handlePunch = async () => {
  punchLoading.value = true
  try {
    const res = await punch({ location: '', ipAddress: '' })
    ElMessage.success(res.message || '打卡成功')
    loadHistory()
    loadMonthStats()
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    punchLoading.value = false
  }
}

// ===== 月度统计 =====
const now = new Date()
const statsMonth = ref(`${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`)
const monthStats = ref({})

const statsList = computed(() => [
  { label: '正常', value: monthStats.value.normal || 0, color: '#67c23a' },
  { label: '迟到', value: monthStats.value.late || 0, color: '#e6a23c' },
  { label: '早退', value: monthStats.value.earlyLeave || 0, color: '#f56c6c' },
  { label: '缺卡', value: monthStats.value.missing || 0, color: '#909399' },
  { label: '请假', value: monthStats.value.leave || 0, color: '#409eff' },
  { label: '出勤天数', value: monthStats.value.totalDays || 0, color: '#333' }
])

const loadMonthStats = async () => {
  if (!statsMonth.value) return
  const [year, month] = statsMonth.value.split('-')
  try {
    const res = await getMyMonthStats({ year: parseInt(year), month: parseInt(month) })
    monthStats.value = res.data || {}
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// ===== 考勤历史 =====
const historyList = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const loadHistory = async () => {
  try {
    const res = await getMyHistory({ pageNum: pageNum.value, pageSize: pageSize.value })
    historyList.value = res.data.list || []
    total.value = res.data.total || 0
  } catch (error) {
    // 错误已在拦截器中处理
  }
}

// ===== 工具方法 =====
const statusText = (status) => {
  const map = { 1: '正常', 2: '迟到', 3: '早退', 4: '缺卡', 5: '请假' }
  return map[status] || '未知'
}

const statusTagType = (status) => {
  const map = { 1: 'success', 2: 'warning', 3: 'danger', 4: 'info', 5: '' }
  return map[status] || 'info'
}

const formatTime = (datetime) => {
  if (!datetime) return ''
  return datetime.replace('T', ' ').substring(0, 19)
}

// ===== 生命周期 =====
onMounted(() => {
  updateClock()
  timer = setInterval(updateClock, 1000)
  loadHistory()
  loadMonthStats()
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.attendance-container {
  padding: 20px;
}

.punch-card .punch-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.punch-time .current-time {
  font-size: 42px;
  font-weight: bold;
  color: #303133;
  font-family: 'Courier New', monospace;
}

.punch-time .current-date {
  font-size: 14px;
  color: #909399;
  margin-top: 4px;
}

.punch-btn {
  width: 140px;
  height: 140px;
  font-size: 20px;
  border-radius: 50% !important;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.stat-item {
  text-align: center;
  padding: 16px 0;
  border-left: 3px solid #eee;
  border-radius: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
}

.stat-label {
  font-size: 13px;
  color: #909399;
  margin-top: 6px;
}

.pagination-area {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}
</style>
