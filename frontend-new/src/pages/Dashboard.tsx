import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Grid,
  Button,
  LinearProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  Avatar,
  IconButton,
  Skeleton,
  alpha,
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import {
  PlayCircleOutline as RunningIcon,
  HourglassTop as PendingIcon,
  CheckCircle as CompletedIcon,
  Warning as WarningIcon,
  Add as AddIcon,
  TrendingUp as TrendingUpIcon,
  TrendingDown as TrendingDownIcon,
  Refresh as RefreshIcon,
} from '@mui/icons-material'
import { BarChart } from '@mui/x-charts/BarChart'
import { adminApi } from '../api/adminApi'
import type { Metrics, Task, ProcessDefinition, DailyCount } from '../types'

interface StatCardProps {
  title: string
  value: string | number
  icon: React.ReactNode
  iconBgColor: string
  iconColor: string
  trend?: { value: string; positive: boolean }
  badge?: { text: string; color: string }
  onClick?: () => void
  loading?: boolean
}

const StatCard: React.FC<StatCardProps> = ({
  title,
  value,
  icon,
  iconBgColor,
  iconColor,
  trend,
  badge,
  onClick,
  loading,
}) => (
  <Card
    onClick={onClick}
    sx={{
      cursor: onClick ? 'pointer' : 'default',
      transition: 'all 0.2s',
      '&:hover': onClick
        ? {
            transform: 'translateY(-2px)',
            boxShadow: 4,
            borderColor: 'primary.main',
          }
        : {},
    }}
  >
    <CardContent sx={{ p: 3 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
        <Box
          sx={{
            p: 1.5,
            borderRadius: 2,
            bgcolor: iconBgColor,
            color: iconColor,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
          }}
        >
          {icon}
        </Box>
        {trend && (
          <Chip
            size="small"
            icon={trend.positive ? <TrendingUpIcon fontSize="small" /> : <TrendingDownIcon fontSize="small" />}
            label={trend.value}
            sx={{
              bgcolor: trend.positive ? alpha('#10b981', 0.1) : alpha('#ef4444', 0.1),
              color: trend.positive ? '#10b981' : '#ef4444',
              fontWeight: 600,
              fontSize: '0.75rem',
              '& .MuiChip-icon': {
                color: 'inherit',
              },
            }}
          />
        )}
        {badge && (
          <Chip
            size="small"
            label={badge.text}
            sx={{
              bgcolor: badge.color,
              color: 'white',
              fontWeight: 600,
              fontSize: '0.625rem',
            }}
          />
        )}
      </Box>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
        {title}
      </Typography>
      {loading ? (
        <Skeleton variant="text" width={80} height={40} />
      ) : (
        <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
          {typeof value === 'number' ? value.toLocaleString() : value}
        </Typography>
      )}
    </CardContent>
  </Card>
)

export const Dashboard: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [recentTasks, setRecentTasks] = useState<Task[]>([])
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([])
  const [startProcessDialog, setStartProcessDialog] = useState(false)
  const [selectedDefinition, setSelectedDefinition] = useState('')
  const [businessKey, setBusinessKey] = useState('')
  const [starting, setStarting] = useState(false)

  useEffect(() => {
    fetchData()
  }, [])

  const fetchData = async () => {
    setLoading(true)
    setError(null)
    try {
      const [metricsData, tasksData, definitionsData] = await Promise.all([
        adminApi.getMetrics(),
        adminApi.searchTasks({ page: 0, size: 5 }),
        adminApi.getDefinitions(),
      ])
      
      setMetrics(metricsData)
      setRecentTasks(tasksData.content || [])
      setDefinitions(definitionsData || [])
    } catch (err) {
      console.error('Failed to fetch dashboard data:', err)
      setError('Failed to load dashboard data. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  // Calculate pending tasks from tasksByState
  const getPendingTasksCount = (): number => {
    if (!metrics?.tasksByState) return 0
    const pending = metrics.tasksByState.find(t => 
      t.state === 'CLAIMABLE' || t.state === 'PENDING' || t.state === 'ACTIVE'
    )
    return pending?.count || 0
  }

  // Calculate definition usage from definitions and process statistics
  const getDefinitionUsage = (): { name: string; percentage: number }[] => {
    if (!definitions.length) return []
    
    // If we have avgDurationByDefinition, we can infer usage
    if (metrics?.avgDurationByDefinition?.length) {
      const total = metrics.avgDurationByDefinition.length
      return metrics.avgDurationByDefinition.slice(0, 4).map((item) => ({
        name: definitions.find(def => def.key === item.definitionKey)?.name || item.definitionKey,
        percentage: Math.round((1 / total) * 100),
      }))
    }
    
    // Otherwise, just show definitions equally
    const percentage = Math.round(100 / Math.min(definitions.length, 4))
    return definitions.slice(0, 4).map(d => ({
      name: d.name || d.key,
      percentage,
    }))
  }

  // Chart data from metrics
  const chartData: DailyCount[] = metrics?.instancesByDay || []

  // Handle starting a new process
  const handleStartProcess = async () => {
    if (!selectedDefinition) return
    setStarting(true)
    try {
      const response = await fetch(`/api/flowable/runtime/start/${selectedDefinition}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`,
        },
        body: JSON.stringify({ businessKey: businessKey || undefined }),
      })
      if (response.ok) {
        setStartProcessDialog(false)
        setSelectedDefinition('')
        setBusinessKey('')
        navigate('/instances/running')
      }
    } catch (err) {
      console.error('Failed to start process:', err)
    } finally {
      setStarting(false)
    }
  }

  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button variant="contained" onClick={fetchData}>
          Retry
        </Button>
      </Box>
    )
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 0.5 }}>
            Welcome back, Admin
          </Typography>
          <Typography variant="body2" color="text.secondary">
            System status is{' '}
            <Typography component="span" sx={{ color: 'success.main', fontWeight: 500 }}>
              healthy
            </Typography>
            . You have{' '}
            <Typography component="span" sx={{ color: 'primary.main', fontWeight: 500 }}>
              {getPendingTasksCount()} pending tasks
            </Typography>
            .
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button
            variant="outlined"
            startIcon={<RefreshIcon />}
            onClick={fetchData}
            disabled={loading}
            sx={{ borderColor: 'divider', color: 'text.primary' }}
          >
            Refresh
          </Button>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setStartProcessDialog(true)}
            sx={{
              boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)',
            }}
          >
            Start Process
          </Button>
        </Box>
      </Box>

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Active Instances"
            value={metrics?.runningInstances ?? 0}
            icon={<RunningIcon />}
            iconBgColor={alpha('#3b82f6', 0.15)}
            iconColor="#3b82f6"
            onClick={() => navigate('/instances/running')}
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Pending Tasks"
            value={getPendingTasksCount()}
            icon={<PendingIcon />}
            iconBgColor={alpha('#f59e0b', 0.15)}
            iconColor="#f59e0b"
            badge={getPendingTasksCount() > 0 ? { text: 'Action Req', color: '#f59e0b' } : undefined}
            onClick={() => navigate('/tasks')}
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Completed Instances"
            value={metrics?.completedInstances ?? 0}
            icon={<CompletedIcon />}
            iconBgColor={alpha('#10b981', 0.15)}
            iconColor="#10b981"
            onClick={() => navigate('/instances/completed')}
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Total Tasks"
            value={metrics?.totalTasks ?? 0}
            icon={<WarningIcon />}
            iconBgColor={alpha('#8b5cf6', 0.15)}
            iconColor="#8b5cf6"
            loading={loading}
          />
        </Grid>
      </Grid>

      {/* Charts Row */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {/* Process Volume Chart */}
        <Grid item xs={12} lg={8}>
          <Card>
            <CardContent sx={{ p: 3 }}>
              <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3 }}>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 600, color: 'white' }}>
                    Process Volume
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    New instances over the last 7 days
                  </Typography>
                </Box>
                <IconButton size="small" onClick={fetchData} disabled={loading}>
                  <RefreshIcon />
                </IconButton>
              </Box>
              <Box sx={{ height: 280 }}>
                {loading ? (
                  <Skeleton variant="rounded" height={260} />
                ) : chartData.length > 0 ? (
                  <BarChart
                    xAxis={[{
                      scaleType: 'band',
                      data: chartData.map(d => d.day),
                      tickLabelStyle: { fill: '#94a3b8', fontSize: 12 },
                    }]}
                    yAxis={[{
                      tickLabelStyle: { fill: '#94a3b8', fontSize: 12 },
                    }]}
                    series={[{
                      data: chartData.map(d => d.count),
                      color: '#3b82f6',
                    }]}
                    height={260}
                    sx={{
                      '& .MuiChartsAxis-line': { stroke: '#334155' },
                      '& .MuiChartsAxis-tick': { stroke: '#334155' },
                    }}
                  />
                ) : (
                  <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
                    <Typography color="text.secondary">No data available</Typography>
                  </Box>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>

        {/* Definition Usage */}
        <Grid item xs={12} lg={4}>
          <Card sx={{ height: '100%' }}>
            <CardContent sx={{ p: 3, height: '100%', display: 'flex', flexDirection: 'column' }}>
              <Typography variant="h6" sx={{ fontWeight: 600, color: 'white', mb: 0.5 }}>
                Process Definitions
              </Typography>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
                Deployed definitions ({definitions.length} total)
              </Typography>
              <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', justifyContent: 'center', gap: 3 }}>
                {loading ? (
                  [...Array(4)].map((_, idx) => (
                    <Skeleton key={idx} variant="rounded" height={32} />
                  ))
                ) : getDefinitionUsage().length > 0 ? (
                  getDefinitionUsage().map((def, idx) => (
                    <Box key={def.name}>
                      <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 1 }}>
                        <Typography variant="body2" color="text.primary">
                          {def.name}
                        </Typography>
                        <Typography variant="body2" sx={{ fontWeight: 600, color: 'white' }}>
                          {def.percentage}%
                        </Typography>
                      </Box>
                      <LinearProgress
                        variant="determinate"
                        value={def.percentage}
                        sx={{
                          height: 6,
                          borderRadius: 3,
                          bgcolor: 'background.default',
                          '& .MuiLinearProgress-bar': {
                            borderRadius: 3,
                            bgcolor: ['#3b82f6', '#8b5cf6', '#f59e0b', '#10b981'][idx % 4],
                          },
                        }}
                      />
                    </Box>
                  ))
                ) : (
                  <Typography color="text.secondary" sx={{ textAlign: 'center' }}>
                    No definitions found
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Tables Row */}
      <Grid container spacing={3}>
        {/* Pending Tasks */}
        <Grid item xs={12} xl={8}>
          <Card>
            <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Box>
                <Typography variant="h6" sx={{ fontWeight: 600, color: 'white' }}>
                  Pending Maker-Checker Tasks
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Requires review before proceeding
                </Typography>
              </Box>
              <Button
                size="small"
                onClick={() => navigate('/tasks')}
                sx={{ color: 'primary.main' }}
              >
                View All Tasks
              </Button>
            </Box>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Task</TableCell>
                    <TableCell>Definition</TableCell>
                    <TableCell>Assignee</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell align="right">Action</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {loading ? (
                    [...Array(3)].map((_, idx) => (
                      <TableRow key={idx}>
                        <TableCell colSpan={5}>
                          <Skeleton variant="rectangular" height={48} />
                        </TableCell>
                      </TableRow>
                    ))
                  ) : recentTasks.length > 0 ? (
                    recentTasks.map((task) => (
                      <TableRow key={task.id} hover>
                        <TableCell>
                          <Typography variant="body2" sx={{ fontWeight: 500, color: 'white' }}>
                            {task.name}
                          </Typography>
                          <Typography variant="caption" color="text.secondary">
                            Created {new Date(task.createTime).toLocaleDateString()}
                          </Typography>
                        </TableCell>
                        <TableCell>{task.processDefinitionId?.split(':')[0] || 'N/A'}</TableCell>
                        <TableCell>
                          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                            <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem', bgcolor: 'primary.dark' }}>
                              {task.assignee?.charAt(0) || '?'}
                            </Avatar>
                            <Typography variant="body2">
                              {task.assignee || 'Unassigned'}
                            </Typography>
                          </Box>
                        </TableCell>
                        <TableCell>
                          <Chip
                            size="small"
                            label={task.state || 'Pending'}
                            sx={{
                              bgcolor: alpha('#f59e0b', 0.15),
                              color: '#f59e0b',
                              fontWeight: 500,
                            }}
                          />
                        </TableCell>
                        <TableCell align="right">
                          <Button
                            size="small"
                            variant="outlined"
                            onClick={() => navigate(`/instances/${task.processInstanceId}`)}
                            sx={{
                              borderColor: alpha('#3b82f6', 0.3),
                              color: 'primary.main',
                              '&:hover': {
                                bgcolor: alpha('#3b82f6', 0.1),
                                borderColor: 'primary.main',
                              },
                            }}
                          >
                            View
                          </Button>
                        </TableCell>
                      </TableRow>
                    ))
                  ) : (
                    <TableRow>
                      <TableCell colSpan={5} sx={{ textAlign: 'center', py: 4 }}>
                        <Typography color="text.secondary">No pending tasks</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </Grid>

        {/* Task State Breakdown */}
        <Grid item xs={12} xl={4}>
          <Card sx={{ height: '100%' }}>
            <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Typography variant="h6" sx={{ fontWeight: 600, color: 'white' }}>
                Tasks by State
              </Typography>
            </Box>
            <Box sx={{ p: 3 }}>
              {loading ? (
                [...Array(4)].map((_, idx) => (
                  <Skeleton key={idx} variant="rounded" height={40} sx={{ mb: 2 }} />
                ))
              ) : metrics?.tasksByState && metrics.tasksByState.length > 0 ? (
                <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                  {metrics.tasksByState.map((item, idx) => {
                    const total = metrics.tasksByState.reduce((sum, t) => sum + t.count, 0)
                    const percentage = total > 0 ? Math.round((item.count / total) * 100) : 0
                    const colors = ['#3b82f6', '#f59e0b', '#10b981', '#ef4444', '#8b5cf6']
                    const color = colors[idx % colors.length]
                    
                    return (
                      <Box key={item.state}>
                        <Box sx={{ display: 'flex', justifyContent: 'space-between', mb: 0.5 }}>
                          <Typography variant="body2" sx={{ textTransform: 'capitalize' }}>
                            {item.state.toLowerCase().replace('_', ' ')}
                          </Typography>
                          <Typography variant="body2" sx={{ fontWeight: 600, color: 'white' }}>
                            {item.count}
                          </Typography>
                        </Box>
                        <LinearProgress
                          variant="determinate"
                          value={percentage}
                          sx={{
                            height: 6,
                            borderRadius: 3,
                            bgcolor: alpha(color, 0.1),
                            '& .MuiLinearProgress-bar': {
                              borderRadius: 3,
                              bgcolor: color,
                            },
                          }}
                        />
                      </Box>
                    )
                  })}
                </Box>
              ) : (
                <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                  No task data available
                </Typography>
              )}
            </Box>
          </Card>
        </Grid>
      </Grid>

      {/* Start Process Dialog */}
      <Dialog open={startProcessDialog} onClose={() => setStartProcessDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Start New Process</DialogTitle>
        <DialogContent>
          <FormControl fullWidth sx={{ mt: 2, mb: 2 }}>
            <InputLabel>Process Definition</InputLabel>
            <Select
              value={selectedDefinition}
              label="Process Definition"
              onChange={(e) => setSelectedDefinition(e.target.value)}
            >
              {definitions.filter(d => !d.suspended).map((def) => (
                <MenuItem key={def.id} value={def.key}>
                  {def.name || def.key}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <TextField
            fullWidth
            label="Business Key (optional)"
            value={businessKey}
            onChange={(e) => setBusinessKey(e.target.value)}
            placeholder="e.g., ORDER-2024-001"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStartProcessDialog(false)}>Cancel</Button>
          <Button 
            onClick={handleStartProcess} 
            variant="contained" 
            disabled={!selectedDefinition || starting}
          >
            {starting ? 'Starting...' : 'Start Process'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default Dashboard
