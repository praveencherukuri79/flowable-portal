import React, { useState, useEffect } from 'react'
import { useNavigate, useLocation, Outlet } from 'react-router-dom'
import { useRecoilValue, useSetRecoilState } from 'recoil'
import {
  Box,
  Drawer,
  AppBar,
  Toolbar,
  List,
  Typography,
  Divider,
  IconButton,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Badge,
  Avatar,
  Menu,
  MenuItem,
  InputBase,
  Chip,
  Tooltip,
  alpha,
} from '@mui/material'
import {
  Dashboard as DashboardIcon,
  PlayCircleOutline as RunningIcon,
  CheckCircleOutline as CompletedIcon,
  Assignment as TasksIcon,
  Schema as DefinitionsIcon,
  History as EventsIcon,
  Settings as SettingsIcon,
  Group as UsersIcon,
  Search as SearchIcon,
  Notifications as NotificationsIcon,
  Help as HelpIcon,
  Layers as LayersIcon,
  ExpandMore as ExpandMoreIcon,
  Logout as LogoutIcon,
  Person as PersonIcon,
} from '@mui/icons-material'
import { authState } from '../state/auth'
import { adminApi } from '../api/adminApi'
import type { Metrics } from '../types'

const DRAWER_WIDTH = 280

interface NavItem {
  title: string
  path: string
  icon: React.ReactNode
  badgeKey?: 'runningInstances' | 'pendingTasks'
  badgeColor?: 'primary' | 'error' | 'warning' | 'success'
}

const navSections: { title: string; items: NavItem[] }[] = [
  {
    title: 'Overview',
    items: [
      { title: 'Dashboard', path: '/dashboard', icon: <DashboardIcon /> },
    ],
  },
  {
    title: 'Process Management',
    items: [
      { title: 'Running Instances', path: '/instances/running', icon: <RunningIcon />, badgeKey: 'runningInstances', badgeColor: 'primary' },
      { title: 'Completed Instances', path: '/instances/completed', icon: <CompletedIcon /> },
      { title: 'Tasks', path: '/tasks', icon: <TasksIcon />, badgeKey: 'pendingTasks', badgeColor: 'warning' },
      { title: 'Events', path: '/events', icon: <EventsIcon /> },
    ],
  },
  {
    title: 'Configuration',
    items: [
      { title: 'Definitions', path: '/definitions', icon: <DefinitionsIcon /> },
      { title: 'Users & Groups', path: '/users', icon: <UsersIcon /> },
    ],
  },
  {
    title: 'System',
    items: [
      { title: 'Settings', path: '/settings', icon: <SettingsIcon /> },
    ],
  },
]

export const Layout: React.FC = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const auth = useRecoilValue(authState)
  const setAuth = useSetRecoilState(authState)
  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(null)
  const [metrics, setMetrics] = useState<Metrics | null>(null)

  // Fetch metrics for badge counts
  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        const data = await adminApi.getMetrics()
        setMetrics(data)
      } catch (error) {
        console.error('Failed to fetch metrics for sidebar:', error)
      }
    }
    
    fetchMetrics()
    
    // Refresh every 30 seconds
    const interval = setInterval(fetchMetrics, 30000)
    return () => clearInterval(interval)
  }, [])

  // Calculate pending tasks from metrics
  const getPendingTasksCount = (): number => {
    if (!metrics?.tasksByState) return 0
    const pending = metrics.tasksByState.find(t => 
      t.state === 'CLAIMABLE' || t.state === 'PENDING' || t.state === 'ACTIVE'
    )
    return pending?.count || 0
  }

  // Get badge value based on key
  const getBadgeValue = (badgeKey?: string): number | undefined => {
    if (!badgeKey || !metrics) return undefined
    
    switch (badgeKey) {
      case 'runningInstances':
        return metrics.runningInstances || undefined
      case 'pendingTasks':
        return getPendingTasksCount() || undefined
      default:
        return undefined
    }
  }

  const handleUserMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setUserMenuAnchor(event.currentTarget)
  }

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null)
  }

  const handleLogout = () => {
    localStorage.clear()
    setAuth({
      token: null,
      username: null,
      role: null,
      fullName: null,
      isAuthenticated: false,
    })
    navigate('/login')
  }

  const isActivePath = (path: string) => {
    return location.pathname === path || location.pathname.startsWith(path + '/')
  }

  return (
    <Box sx={{ display: 'flex', minHeight: '100vh', bgcolor: 'background.default' }}>
      {/* Sidebar */}
      <Drawer
        variant="permanent"
        sx={{
          width: DRAWER_WIDTH,
          flexShrink: 0,
          '& .MuiDrawer-paper': {
            width: DRAWER_WIDTH,
            boxSizing: 'border-box',
            bgcolor: '#111827',
            borderRight: '1px solid',
            borderColor: 'divider',
            display: 'flex',
            flexDirection: 'column',
          },
        }}
      >
        {/* Logo */}
        <Box
          sx={{
            height: 64,
            display: 'flex',
            alignItems: 'center',
            px: 3,
            gap: 1.5,
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Box
            sx={{
              width: 36,
              height: 36,
              borderRadius: 2,
              bgcolor: 'primary.main',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
            }}
          >
            <LayersIcon sx={{ color: 'white', fontSize: 22 }} />
          </Box>
          <Box>
            <Typography
              variant="h6"
              sx={{
                fontWeight: 700,
                color: 'white',
                lineHeight: 1.2,
                letterSpacing: '-0.01em',
              }}
            >
              Flowable<span style={{ fontWeight: 400, color: '#94a3b8' }}>Admin</span>
            </Typography>
            <Typography
              variant="caption"
              sx={{ color: 'text.secondary', fontSize: '0.625rem', textTransform: 'uppercase', letterSpacing: '0.08em' }}
            >
              Process Manager
            </Typography>
          </Box>
        </Box>

        {/* Navigation */}
        <Box sx={{ flex: 1, overflow: 'auto', py: 2 }}>
          {navSections.map((section) => (
            <Box key={section.title} sx={{ mb: 2 }}>
              <Typography
                variant="overline"
                sx={{
                  px: 3,
                  py: 1,
                  display: 'block',
                  color: 'text.disabled',
                  fontWeight: 600,
                }}
              >
                {section.title}
              </Typography>
              <List sx={{ px: 1.5 }}>
                {section.items.map((item) => {
                  const isActive = isActivePath(item.path)
                  const badgeValue = getBadgeValue(item.badgeKey)
                  return (
                    <ListItem key={item.path} disablePadding sx={{ mb: 0.5 }}>
                      <ListItemButton
                        onClick={() => navigate(item.path)}
                        sx={{
                          borderRadius: 2,
                          py: 1.25,
                          px: 2,
                          ...(isActive && {
                            bgcolor: alpha('#3b82f6', 0.12),
                            '&:hover': {
                              bgcolor: alpha('#3b82f6', 0.18),
                            },
                          }),
                        }}
                      >
                        <ListItemIcon
                          sx={{
                            minWidth: 36,
                            color: isActive ? 'primary.main' : 'text.secondary',
                          }}
                        >
                          {item.icon}
                        </ListItemIcon>
                        <ListItemText
                          primary={item.title}
                          primaryTypographyProps={{
                            fontSize: '0.875rem',
                            fontWeight: isActive ? 600 : 500,
                            color: isActive ? 'primary.main' : 'text.primary',
                          }}
                        />
                        {badgeValue !== undefined && badgeValue > 0 && (
                          <Chip
                            label={badgeValue > 999 ? '999+' : badgeValue}
                            size="small"
                            color={item.badgeColor || 'default'}
                            sx={{
                              height: 20,
                              fontSize: '0.625rem',
                              fontWeight: 700,
                              minWidth: 28,
                              '& .MuiChip-label': { px: 1 },
                            }}
                          />
                        )}
                      </ListItemButton>
                    </ListItem>
                  )
                })}
              </List>
            </Box>
          ))}
        </Box>

        {/* User Profile */}
        <Box
          sx={{
            p: 2,
            borderTop: '1px solid',
            borderColor: 'divider',
            bgcolor: 'rgba(0, 0, 0, 0.2)',
          }}
        >
          <Box
            onClick={handleUserMenuOpen}
            sx={{
              display: 'flex',
              alignItems: 'center',
              gap: 1.5,
              p: 1,
              borderRadius: 2,
              cursor: 'pointer',
              transition: 'background-color 0.2s',
              '&:hover': { bgcolor: 'action.hover' },
            }}
          >
            <Avatar
              sx={{
                width: 36,
                height: 36,
                bgcolor: 'primary.main',
                fontSize: '0.875rem',
                fontWeight: 600,
              }}
            >
              {auth.fullName?.charAt(0) || 'A'}
            </Avatar>
            <Box sx={{ flex: 1, minWidth: 0 }}>
              <Typography
                variant="body2"
                sx={{ fontWeight: 600, color: 'white', lineHeight: 1.3 }}
                noWrap
              >
                {auth.fullName || 'Administrator'}
              </Typography>
              <Typography variant="caption" sx={{ color: 'text.secondary' }} noWrap>
                {auth.username || 'admin@flowable.io'}
              </Typography>
            </Box>
            <ExpandMoreIcon sx={{ color: 'text.secondary', fontSize: 20 }} />
          </Box>
        </Box>
      </Drawer>

      {/* Main Content Area */}
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          display: 'flex',
          flexDirection: 'column',
          minHeight: '100vh',
          overflow: 'hidden',
        }}
      >
        {/* Top Header */}
        <AppBar
          position="sticky"
          elevation={0}
          sx={{
            bgcolor: 'rgba(17, 24, 39, 0.8)',
            backdropFilter: 'blur(12px)',
            borderBottom: '1px solid',
            borderColor: 'divider',
          }}
        >
          <Toolbar sx={{ gap: 2 }}>
            {/* Page Title Area */}
            <Box sx={{ flex: 1 }}>
              <Typography variant="h5" sx={{ fontWeight: 700, color: 'white' }}>
                {navSections.flatMap(s => s.items).find(i => isActivePath(i.path))?.title || 'Dashboard'}
              </Typography>
            </Box>

            {/* Search */}
            <Box
              sx={{
                display: 'flex',
                alignItems: 'center',
                bgcolor: 'background.paper',
                borderRadius: 2,
                px: 2,
                py: 0.5,
                border: '1px solid',
                borderColor: 'divider',
                width: 280,
              }}
            >
              <SearchIcon sx={{ color: 'text.secondary', mr: 1 }} />
              <InputBase
                placeholder="Search instances, tasks..."
                sx={{ flex: 1, color: 'text.primary', fontSize: '0.875rem' }}
              />
              <Typography
                variant="caption"
                sx={{
                  color: 'text.disabled',
                  px: 0.75,
                  py: 0.25,
                  borderRadius: 0.5,
                  border: '1px solid',
                  borderColor: 'divider',
                  fontSize: '0.625rem',
                }}
              >
                ⌘K
              </Typography>
            </Box>

            <Divider orientation="vertical" flexItem sx={{ mx: 1 }} />

            {/* Notifications */}
            <Tooltip title="Notifications">
              <IconButton sx={{ color: 'text.secondary' }}>
                <Badge badgeContent={getPendingTasksCount() > 0 ? getPendingTasksCount() : undefined} color="error" variant="dot">
                  <NotificationsIcon />
                </Badge>
              </IconButton>
            </Tooltip>

            {/* Help */}
            <Tooltip title="Help">
              <IconButton sx={{ color: 'text.secondary' }}>
                <HelpIcon />
              </IconButton>
            </Tooltip>
          </Toolbar>
        </AppBar>

        {/* Page Content */}
        <Box sx={{ flex: 1, overflow: 'auto', p: 3, bgcolor: 'background.default' }}>
          <Box sx={{ maxWidth: 1600, mx: 'auto' }}>
            <Outlet />
          </Box>
        </Box>
      </Box>

      {/* User Menu */}
      <Menu
        anchorEl={userMenuAnchor}
        open={Boolean(userMenuAnchor)}
        onClose={handleUserMenuClose}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
        transformOrigin={{ vertical: 'bottom', horizontal: 'right' }}
        sx={{ mt: -1 }}
      >
        <MenuItem onClick={handleUserMenuClose}>
          <ListItemIcon>
            <PersonIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Profile</ListItemText>
        </MenuItem>
        <MenuItem onClick={handleUserMenuClose}>
          <ListItemIcon>
            <SettingsIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Settings</ListItemText>
        </MenuItem>
        <Divider />
        <MenuItem onClick={handleLogout}>
          <ListItemIcon>
            <LogoutIcon fontSize="small" />
          </ListItemIcon>
          <ListItemText>Logout</ListItemText>
        </MenuItem>
      </Menu>
    </Box>
  )
}

export default Layout
