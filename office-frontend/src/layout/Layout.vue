<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '220px'" class="layout-aside">
      <div class="logo-area">
        <img src="@/assets/echo.jpg" alt="" class="logo-icon" />
        <span v-show="!isCollapse" class="logo-text">OA办公系统</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        :collapse-transition="false"
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
        router
      >
        <el-menu-item index="/dashboard" v-if="hasPermission('stats:view')">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>首页看板</template>
        </el-menu-item>

        <el-menu-item index="/notice" v-if="hasPermission('notice:view')">
          <el-icon><Bell /></el-icon>
          <template #title>公告管理</template>
        </el-menu-item>

        <el-menu-item index="/attendance" v-if="hasPermission('attendance:view')">
          <el-icon><Clock /></el-icon>
          <template #title>考勤管理</template>
        </el-menu-item>

        <el-menu-item index="/schedule" v-if="hasPermission('schedule:view')">
          <el-icon><Calendar /></el-icon>
          <template #title>日程管理</template>
        </el-menu-item>

        <el-menu-item index="/file" v-if="hasPermission('file:view')">
          <el-icon><FolderOpened /></el-icon>
          <template #title>文件管理</template>
        </el-menu-item>

        <el-menu-item index="/document" v-if="hasPermission('document:view')">
          <el-icon><Document /></el-icon>
          <template #title>公文管理</template>
        </el-menu-item>

        <el-sub-menu index="system" v-if="hasAnyPermission(['user:view', 'dept:view', 'role:view', 'system:view'])">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/system/user" v-if="hasPermission('user:view')">
            <el-icon><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/system/department" v-if="hasPermission('dept:view')">
            <el-icon><OfficeBuilding /></el-icon>
            <template #title>部门管理</template>
          </el-menu-item>
          <el-menu-item index="/system/role" v-if="hasPermission('role:view')">
            <el-icon><UserFilled /></el-icon>
            <template #title>角色管理</template>
          </el-menu-item>
          <el-menu-item index="/system/permission" v-if="hasPermission('system:view')">
            <el-icon><Key /></el-icon>
            <template #title>权限管理</template>
          </el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <!-- 右侧主区域 -->
    <el-container>
      <!-- 顶部栏 -->
      <el-header class="layout-header">
        <div class="header-left">
          <el-icon class="collapse-btn" @click="toggleCollapse">
            <Expand v-if="isCollapse" />
            <Fold v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentRouteName">{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar 
                :size="32" 
                :src="userInfo.avatar || ''" 
                icon="UserFilled"
              />
              <span class="username">{{ userInfo.realName || userInfo.username || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 主内容区 -->
      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { logout } from '@/api/user'
import { hasPermission, hasAnyPermission } from '@/utils/permission'

const router = useRouter()
const route = useRoute()

const isCollapse = ref(false)


// 从 localStorage 获取用户信息
const userInfo = computed(() => {
  try {
    const user = JSON.parse(localStorage.getItem('user') || '{}')
    //如果已经有‘/api’前缀，不处理
    if(user.avatar){
      if (user.avatar.startsWith('/api')) {}
      else if (!user.avatar.startsWith('http')) {
        user.avatar = '/api' + (user.avatar.startsWith('/') ? '' : '/') + user.avatar
      }
    }
    return user
  } catch {
    return {}
  }
})

// 当前激活的菜单项
const activeMenu = computed(() => route.path)

// 当前路由名称（用于面包屑）
const currentRouteName = computed(() => {
  const meta = route.meta
  return meta && meta.title ? meta.title : ''
})

const toggleCollapse = () => {
  isCollapse.value = !isCollapse.value
}

const handleCommand = async (command) => {
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
      await logout()
      localStorage.removeItem('token')
      localStorage.removeItem('user')
      localStorage.removeItem('permissions')
      ElMessage.success('已退出登录')
      router.push('/login')
    } catch {
      // 用户取消
    }
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'password') {
    router.push('/update-password')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  transition: width 0.3s;
  overflow: hidden;
}

.logo-area {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  white-space: nowrap;
  overflow: hidden;
}

.logo-icon {
  width: 32px;
  height: 32px;
}

.logo-text {
  margin-left: 8px;
}

.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
  padding: 0 20px;
  height: 60px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #333;
}

.collapse-btn:hover {
  color: #409eff;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #333;
}

.username {
  font-size: 14px;
}

.layout-main {
  background: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
