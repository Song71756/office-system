<template>
  <div class="dashboard-container">
    <!-- 欢迎区域 -->
    <el-card class="welcome-card" shadow="hover">
      <div class="welcome-content">
        <div>
          <h2>欢迎回来，{{ userInfo.realName || userInfo.username || '用户' }}！</h2>
          <p class="welcome-desc">今天是 {{ currentDate }}，祝您工作顺利！</p>
        </div>
      </div>
    </el-card>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6" v-if="hasPermission('api:user:list')">
        <el-card shadow="hover" class="stat-card stat-card-blue">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">用户总数</p>
              <p class="stat-value">{{ systemStats.userCount || 0 }}</p>
            </div>
            <el-icon class="stat-icon"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" v-if="hasPermission('dept:list')">
        <el-card shadow="hover" class="stat-card stat-card-green">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">部门总数</p>
              <p class="stat-value">{{ systemStats.deptCount || 0 }}</p>
            </div>
            <el-icon class="stat-icon"><OfficeBuilding /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" v-if="hasPermission('notice:view')">
        <el-card shadow="hover" class="stat-card stat-card-orange">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">公告数量</p>
              <p class="stat-value">{{ noticeStats.totalPublished || 0 }}</p>
            </div>
            <el-icon class="stat-icon"><Bell /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6" v-if="hasPermission('file:view')">
        <el-card shadow="hover" class="stat-card stat-card-purple">
          <div class="stat-content">
            <div class="stat-info">
              <p class="stat-label">文件总数</p>
              <p class="stat-value">{{ fileStats.fileCount || 0 }}</p>
            </div>
            <el-icon class="stat-icon"><FolderOpened /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 详细统计 -->
    <el-row :gutter="20" class="detail-row">
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Document /></el-icon> 公文统计</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span>草稿</span>
              <el-tag type="info">{{ documentStats[0] || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span>审核中</span>
              <el-tag type="warning">{{ documentStats[1] || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span>已通过</span>
              <el-tag type="success">{{ documentStats[2] || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span>已驳回</span>
              <el-tag type="danger">{{ documentStats[3] || 0 }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header">
              <span><el-icon><Calendar /></el-icon> 日程统计</span>
            </div>
          </template>
          <div class="detail-list">
            <div class="detail-item">
              <span>待办日程</span>
              <el-tag type="warning">{{ scheduleStats[0] || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span>进行中</span>
              <el-tag type="primary">{{ scheduleStats[1] || 0 }}</el-tag>
            </div>
            <div class="detail-item">
              <span>已完成</span>
              <el-tag type="success">{{ scheduleStats[2] || 0 }}</el-tag>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getDashboard } from '@/api/stats'
import { hasPermission } from '@/utils/permission'

// 从 localStorage 获取用户信息
const userInfo = computed(() => {
  try {
    return JSON.parse(localStorage.getItem('user') || '{}')
  } catch {
    return {}
  }
})

// 当前日期
const currentDate = computed(() => {
  const now = new Date()
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  return `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日 ${weekDays[now.getDay()]}`
})

// 统计数据
const systemStats = ref({})
const documentStats = ref({})
const scheduleStats = ref({})
const fileStats = ref({})
const noticeStats = ref({})

const loadDashboard = async () => {
  try {
    const res = await getDashboard()
    const data = res.data
    systemStats.value = data.systemStats || {}
    documentStats.value = data.documentStats || {}
    scheduleStats.value = data.scheduleStats || {}
    fileStats.value = data.fileStats || {}
    noticeStats.value = data.noticeStats || {}
  } catch {
    // 接口异常时使用默认空数据
  }
}

onMounted(() => {
  loadDashboard()
})
</script>

<style scoped>
.dashboard-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.welcome-card {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  border: none;
}

.welcome-card :deep(.el-card__body) {
  padding: 24px 32px;
}

.welcome-content h2 {
  font-size: 22px;
  margin-bottom: 8px;
  color: #fff;
}

.welcome-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
}

.stats-row {
  margin: 0;
}

.stat-card {
  border: none;
  border-radius: 8px;
}

.stat-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
}

.stat-icon {
  font-size: 48px;
  opacity: 0.15;
}

.stat-card-blue .stat-icon { color: #409eff; }
.stat-card-green .stat-icon { color: #67c23a; }
.stat-card-orange .stat-icon { color: #e6a23c; }
.stat-card-purple .stat-icon { color: #9b59b6; }

.stat-card-blue { border-left: 4px solid #409eff; }
.stat-card-green { border-left: 4px solid #67c23a; }
.stat-card-orange { border-left: 4px solid #e6a23c; }
.stat-card-purple { border-left: 4px solid #9b59b6; }

.detail-row {
  margin: 0;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: bold;
}

.detail-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;
  color: #555;
}
</style>
