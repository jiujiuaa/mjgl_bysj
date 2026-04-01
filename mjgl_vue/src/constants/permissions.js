export const ROLE = Object.freeze({
  ADMIN: 'ADMIN',
  INSPECTOR: 'INSPECTOR',
  PRODUCTION: 'PRODUCTION',
  MAINTENANCE: 'MAINTENANCE',
  OPERATOR: 'OPERATOR',
  USER: 'USER',
})

export const ROUTE_ROLE_MAP = Object.freeze({
  '/user-management': [ROLE.ADMIN],
  '/alert-rules': [ROLE.ADMIN],
  '/approval-center': [ROLE.ADMIN],
  '/alert-test': [ROLE.ADMIN],
  '/monitoring-temperature': [ROLE.ADMIN, ROLE.INSPECTOR],
  '/monitoring-lubrication': [ROLE.ADMIN, ROLE.INSPECTOR],
  '/monitoring-abnormal': [ROLE.ADMIN, ROLE.INSPECTOR],
})

export const canAccessPathByRole = (path, role) => {
  if (!path) return false
  const allowedRoles = ROUTE_ROLE_MAP[path]
  if (!allowedRoles || allowedRoles.length === 0) return true
  return allowedRoles.includes(role)
}
