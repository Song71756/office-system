import { createRouter, createWebHistory } from 'vue-router'
import { hasPermission } from '@/utils/permission'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/',
    component: () => import('@/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页看板' }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息' }
      },
      {
        path: 'update-password',
        name: 'UpdatePassword',
        component: () => import('@/views/UpdatePassword.vue'),
        meta: { title: '修改密码' }
      },
      {

        path: 'notice',
        name: 'Notice',
        component: () => import('@/views/Notice.vue'),
        meta: { title: '公告管理' }
      },
      {
        path: 'attendance',
        name: 'Attendance',
        component: () => import('@/views/Attendance.vue'),
        meta: { title: '考勤管理' }
      },
      {
        path: 'schedule',
        name: 'Schedule',
        component: () => import('@/views/Schedule.vue'),
        meta: { title: '日程管理' }
      },
      {
        path: 'file',
        name: 'File',
        component: () => import('@/views/File.vue'),
        meta: { title: '文件管理' }
      },
      {
        path: 'document',
        name: 'Document',
        component: () => import('@/views/Document.vue'),
        meta: { title: '公文管理' }
      },
      {
        path: 'system/user',
        name: 'SystemUser',
        component: () => import('@/views/system/User.vue'),
        meta: { title: '用户管理' }
      },
      {
        path: 'system/department',
        name: 'SystemDepartment',
        component: () => import('@/views/system/Department.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'system/role',
        name: 'SystemRole',
        component: () => import('@/views/system/Role.vue'),
        meta: { title: '角色管理' }
      },
      {
        path: 'system/permission',
        name: 'SystemPermission',
        component: () => import('@/views/system/Permission.vue'),
        meta: { title: '权限管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 全局前置路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  // 未登录且访问的不是登录/注册页，则跳转到登录页
  if (!token && to.path !== '/login' && to.path !== '/register') {
    next('/login')
    return
  }
  
  // 已登录，检查路由权限
  if (token) {
    // 定义路由对应的权限编码
    const routePermissionMap = {
      '/notice': 'notice:view',
      '/attendance': 'attendance:view',
      '/schedule': 'schedule:view',
      '/file': 'file:view',
      '/document': 'document:view',
      '/system/user': 'user:view',
      '/system/department': 'dept:view',
      '/system/role': 'role:view',
      '/system/permission': 'role:assign'
    }
    
    // 检查当前路由是否需要权限
    const requiredPermission = routePermissionMap[to.path]
    if (requiredPermission && !hasPermission(requiredPermission)) {
      // 没有权限，重定向到首页
      next('/dashboard')
      return
    }
  }
  
  next()
})

export default router
