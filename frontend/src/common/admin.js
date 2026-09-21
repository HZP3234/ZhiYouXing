import { apiPrefix, roleNameOf } from '@/config/config'

/*
 * 管理端的登录态读写。
 *
 * 复用前台那套 localStorage 键（login.vue 写的 frontToken / UserTableName / username …），
 * 这样 <safe-image>、http.js 的请求拦截器、common/user.js 的 isLogin() 都不用改。
 * 新增的只有 frontRole——管理端顶栏要显示中文角色名。
 */

export function adminTable() {
  return localStorage.getItem('frontSessionTable') || localStorage.getItem('UserTableName') || ''
}

export function adminPrefix() {
  return apiPrefix(adminTable())
}

export function adminAccount() {
  return localStorage.getItem('username') || ''
}

export function adminRoleName() {
  const saved = localStorage.getItem('frontRole')
  if (saved) return saved
  return roleNameOf(adminTable())
}

export function setAdminSession({ tableName, token, account, roleName }) {
  localStorage.setItem('frontToken', token)
  localStorage.setItem('UserTableName', tableName)
  localStorage.setItem('frontSessionTable', tableName)
  localStorage.setItem('username', account)
  localStorage.setItem('adminName', account)
  localStorage.setItem('frontRole', roleName)
}

export function clearAdminSession() {
  const keys = [
    'frontToken', 'UserTableName', 'frontSessionTable', 'username', 'adminName',
    'frontRole', 'keyPath', 'frontUserid', 'sessionForm', 'frontDisplayName',
  ]
  keys.forEach((key) => localStorage.removeItem(key))
}
