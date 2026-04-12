<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span><el-icon><User /></el-icon> 个人信息</span>
        </div>
      </template>

      <el-form :model="formData" :rules="formRules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户头像" prop="avatar">
              <div class="avatar-upload">
                <el-avatar :size="100" :src="formData.avatar" icon="UserFilled" />
                <el-upload
                  class="avatar-uploader"
                  action="/api/file/upload/avatar"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="handleAvatarSuccess"
                  :before-upload="beforeAvatarUpload"
                >
                  <el-button type="primary" size="small">更换头像</el-button>
                </el-upload>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="formData.username" placeholder="请输入用户名" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="真实姓名" prop="realName">
              <el-input v-model="formData.realName" placeholder="请输入真实姓名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="formData.phone" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="formData.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="部门" prop="departmentName">
              <el-input v-model="formData.departmentName" placeholder="部门名称" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="角色" prop="roleName">
              <el-input v-model="formData.roleName" placeholder="角色名称" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="创建时间" prop="createTime">
              <el-input v-model="formData.createTime" placeholder="创建时间" disabled />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="最后登录" prop="lastLoginTime">
              <el-input v-model="formData.lastLoginTime" placeholder="最后登录时间" disabled />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="个人简介" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入个人简介"
            maxlength="200"
            show-word-limit
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">保存修改</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { getCurrentUser, updateMyself } from '@/api/user'

const formRef = ref(null)
const loading = ref(false)

// 表单数据
const formData = reactive({
  id: null,
  username: '',
  realName: '',
  phone: '',
  email: '',
  avatar: '',
  departmentName: '',
  roleName: '',
  createTime: '',
  lastLoginTime: '',
  description: ''
})

// 表单验证规则
const formRules = reactive({
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' },
    { min: 2, max: 10, message: '姓名长度在 2 到 10 个字符', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ]
})

// 加载用户信息
const loadUserInfo = async () => {
  try {
    const res = await getCurrentUser()
    const userData = res.data

    if(userData.avatar){
        if(userData.avatar.startsWith('/api')) {}
        else if (!userData.avatar.startsWith('http')) {
            userData.avatar = '/api' + (userData.avatar.startsWith('/') ? '' : '/') + userData.avatar
        }
      }
    
    Object.assign(formData, {
      id: userData.id,
      username: userData.username || '',
      realName: userData.realName || '',
      phone: userData.phone || '',
      email: userData.email || '',
      avatar: userData.avatar || '',
      departmentName: userData.departmentName || '',
      roleName: userData.roleName || '',
      createTime: userData.createTime || '',
      lastLoginTime: userData.lastLoginTime || '',
      description: userData.description || ''
    })
  } catch (error) {
    ElMessage.error('获取用户信息失败')
  }
}

// 上传请求头（携带Token）
const uploadHeaders = computed(() => {
  const token = localStorage.getItem('token')
  return {
    'Authorization': token || ''
  }
})


// 头像上传成功
const handleAvatarSuccess = (response) => {
  if (response.code === 200) {
    let avatarUrl = response.data.fileUrl
    formData.avatar = avatarUrl
    ElMessage.success('头像上传成功')
  } else {
    ElMessage.error(response.message || '头像上传失败')
  }
}

// 头像上传前验证
const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPG) {
    ElMessage.error('头像只能是 JPG/PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

// 提交表单
const handleSubmit = async () => {
  try {
    const valid = await formRef.value.validate()
    if (!valid) return

    loading.value = true
    await updateMyself(formData)
    ElMessage.success('个人信息更新成功')
    
    // 更新本地存储的用户信息
    const userInfo = JSON.parse(localStorage.getItem('user') || '{}')
    Object.assign(userInfo, {
      realName: formData.realName,
      phone: formData.phone,
      email: formData.email,
      avatar: formData.avatar
    })
    localStorage.setItem('user', JSON.stringify(userInfo))
  } catch (error) {
    // 错误已在拦截器中处理
  } finally {
    loading.value = false
  }
}

// 重置表单
const handleReset = () => {
  loadUserInfo()
}

onMounted(() => {
  loadUserInfo()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.profile-card {
  min-height: 600px;
}

.card-header {
  display: flex;
  align-items: center;
  font-size: 16px;
  font-weight: bold;
}

.card-header .el-icon {
  margin-right: 8px;
}

.avatar-upload {
  display: flex;
  align-items: center;
  gap: 20px;
}

.avatar-uploader {
  display: inline-block;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-input.is-disabled .el-input__inner) {
  background-color: #f5f7fa;
  color: #909399;
}
</style>
