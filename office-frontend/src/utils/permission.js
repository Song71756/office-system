/**
 * 权限判断工具函数
 */

/**
 * 获取用户权限列表
 * @returns {string[]} 权限编码数组
 */
export function getPermissions() {
  const permissions = localStorage.getItem('permissions')
  if (!permissions) return []
  try {
    return JSON.parse(permissions)
  } catch (e) {
    console.error('解析权限列表失败:', e)
    return []
  }
}

/**
 * 判断是否有某个权限
 * @param {string} code 权限编码
 * @returns {boolean}
 */
export function hasPermission(code) {
  const permissions = getPermissions()
  return permissions.includes(code)
}

/**
 * 判断是否有任意一个权限
 * @param {string[]} codes 权限编码数组
 * @returns {boolean}
 */
export function hasAnyPermission(codes) {
  if (!codes || codes.length === 0) return true
  const permissions = getPermissions()
  return codes.some(code => permissions.includes(code))
}

/**
 * 判断是否有所有权限
 * @param {string[]} codes 权限编码数组
 * @returns {boolean}
 */
export function hasAllPermissions(codes) {
  if (!codes || codes.length === 0) return true
  const permissions = getPermissions()
  return codes.every(code => permissions.includes(code))
}

/**
 * 设置用户权限列表
 * @param {string[]} permissions 权限编码数组
 */
export function setPermissions(permissions) {
  localStorage.setItem('permissions', JSON.stringify(permissions || []))
}

/**
 * 清除用户权限列表
 */
export function clearPermissions() {
  localStorage.removeItem('permissions')
}
