import React, { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  Button,
  IconButton,
  Menu,
  MenuItem,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Tooltip,
  Avatar,
  Skeleton,
  alpha,
  FormControl,
  InputLabel,
  Select,
  SelectChangeEvent,
  Checkbox,
  Grid,
  Alert,
  TextField,
} from '@mui/material'
import {
  Refresh as RefreshIcon,
  PersonAdd as AssignIcon,
  DataObject as VariablesIcon,
  Done as CompleteIcon,
  Pause as SuspendIcon,
  Delete as DeleteIcon,
  MoreVert as MoreIcon,
  Warning as WarningIcon,
  CheckCircle as CheckCircleIcon,
  HourglassTop as HourglassIcon,
  Dataset as DatasetIcon,
  Add as AddIcon,
  Download as DownloadIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import type { ProcessInstance, ProcessDefinition, Metrics } from '../types'

interface StatCardProps {
  title: string
  value: number | string
  icon: React.ReactNode
  color: string
  loading?: boolean
  onClick?: () => void
}

const StatCard: React.FC<StatCardProps> = ({ title, value, icon, color, loading, onClick }) => (
  <Card
    onClick={onClick}
    sx={{
      cursor: onClick ? 'pointer' : 'default',
      transition: 'all 0.2s',
      '&:hover': onClick
        ? { borderColor: `${color}80`, transform: 'translateY(-2px)' }
        : {},
    }}
  >
    <CardContent sx={{ p: 2.5 }}>
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 2 }}>
        <Box>
          <Typography variant="body2" color="text.secondary">
            {title}
          </Typography>
          {loading ? (
            <Skeleton variant="text" width={60} height={40} />
          ) : (
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mt: 0.5 }}>
              {typeof value === 'number' ? value.toLocaleString() : value}
            </Typography>
          )}
        </Box>
        <Box
          sx={{
            p: 1.5,
            borderRadius: 2,
            bgcolor: alpha(color, 0.15),
            color: color,
          }}
        >
          {icon}
        </Box>
      </Box>
    </CardContent>
  </Card>
)

export const RunningInstances: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [statsLoading, setStatsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [instances, setInstances] = useState<ProcessInstance[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [definitionFilter, setDefinitionFilter] = useState('')
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([])
  const [selectedInstances, setSelectedInstances] = useState<string[]>([])
  const [actionMenuAnchor, setActionMenuAnchor] = useState<null | HTMLElement>(null)
  const [selectedInstanceForAction, setSelectedInstanceForAction] = useState<ProcessInstance | null>(null)
  const [confirmDialog, setConfirmDialog] = useState<{ open: boolean; action: string; instanceId: string } | null>(null)
  const [metrics, setMetrics] = useState<Metrics | null>(null)
  const [startProcessDialog, setStartProcessDialog] = useState(false)
  const [selectedDefinition, setSelectedDefinition] = useState('')
  const [businessKey, setBusinessKey] = useState('')
  const [starting, setStarting] = useState(false)

  useEffect(() => {
    loadDefinitions()
    loadMetrics()
  }, [])

  useEffect(() => {
    loadInstances()
  }, [page, rowsPerPage, definitionFilter])

  const loadDefinitions = async () => {
    try {
      const defs = await adminApi.getDefinitions()
      setDefinitions(defs)
    } catch (err) {
      console.error('Failed to load definitions:', err)
    }
  }

  const loadMetrics = async () => {
    setStatsLoading(true)
    try {
      const metricsData = await adminApi.getMetrics()
      setMetrics(metricsData)
    } catch (err) {
      console.error('Failed to load metrics:', err)
    } finally {
      setStatsLoading(false)
    }
  }

  const loadInstances = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await adminApi.searchInstances({
        state: 'RUNNING',
        definitionKey: definitionFilter || undefined,
        page,
        size: rowsPerPage,
      })
      setInstances(response.content || [])
      setTotalCount(response.total || 0)
    } catch (err) {
      console.error('Failed to load instances:', err)
      setError('Failed to load instances. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [page, rowsPerPage, definitionFilter])

  const handlePageChange = (_: unknown, newPage: number) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0)
  }

  const handleDefinitionFilterChange = (event: SelectChangeEvent) => {
    setDefinitionFilter(event.target.value)
    setPage(0)
  }

  const handleSelectAll = (event: React.ChangeEvent<HTMLInputElement>) => {
    if (event.target.checked) {
      setSelectedInstances(instances.map((i) => i.id))
    } else {
      setSelectedInstances([])
    }
  }

  const handleSelectInstance = (instanceId: string) => {
    setSelectedInstances((prev) =>
      prev.includes(instanceId) ? prev.filter((id) => id !== instanceId) : [...prev, instanceId]
    )
  }

  const handleActionMenuOpen = (event: React.MouseEvent<HTMLElement>, instance: ProcessInstance) => {
    event.stopPropagation()
    setActionMenuAnchor(event.currentTarget)
    setSelectedInstanceForAction(instance)
  }

  const handleActionMenuClose = () => {
    setActionMenuAnchor(null)
    setSelectedInstanceForAction(null)
  }

  const handleSuspend = async (instanceId: string) => {
    try {
      await adminApi.suspendInstance(instanceId)
      loadInstances()
      loadMetrics()
    } catch (err) {
      console.error('Failed to suspend instance:', err)
    }
    handleActionMenuClose()
    setConfirmDialog(null)
  }

  const handleDelete = async (instanceId: string) => {
    try {
      await adminApi.deleteInstance(instanceId, 'Deleted by admin')
      loadInstances()
      loadMetrics()
    } catch (err) {
      console.error('Failed to delete instance:', err)
    }
    handleActionMenuClose()
    setConfirmDialog(null)
  }

  const handleInstanceClick = (instance: ProcessInstance) => {
    navigate(`/instances/${instance.id}`)
  }

  const getStatusChip = (status: string, suspended: boolean) => {
    let state = status
    if (suspended) state = 'SUSPENDED'
    
    const config: Record<string, { color: string; icon: React.ReactNode }> = {
      ACTIVE: { color: '#10b981', icon: <CheckCircleIcon sx={{ fontSize: 12 }} /> },
      RUNNING: { color: '#10b981', icon: <CheckCircleIcon sx={{ fontSize: 12 }} /> },
      SUSPENDED: { color: '#8b5cf6', icon: <SuspendIcon sx={{ fontSize: 12 }} /> },
      TERMINATED: { color: '#ef4444', icon: <DeleteIcon sx={{ fontSize: 12 }} /> },
    }
    const { color, icon } = config[state] || config.ACTIVE
    return (
      <Chip
        size="small"
        icon={icon}
        label={state}
        sx={{
          bgcolor: alpha(color, 0.15),
          color: color,
          fontWeight: 500,
          fontSize: '0.75rem',
          '& .MuiChip-icon': { color: 'inherit' },
        }}
      />
    )
  }

  // Calculate stats from metrics
  const getSuspendedCount = (): number => {
    // Count from current page instances that are suspended
    return instances.filter(i => i.suspended).length
  }

  const getPendingTasksCount = (): number => {
    if (!metrics?.tasksByState) return 0
    const pending = metrics.tasksByState.find(t => 
      t.state === 'CLAIMABLE' || t.state === 'PENDING' || t.state === 'ACTIVE'
    )
    return pending?.count || 0
  }

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
        loadInstances()
        loadMetrics()
      }
    } catch (err) {
      console.error('Failed to start process:', err)
    } finally {
      setStarting(false)
    }
  }

  // Handle exporting instances to CSV
  const handleExportCSV = () => {
    const headers = ['Instance ID', 'Process Name', 'Initiator', 'Business Key', 'Status', 'Created']
    const rows = instances.map(i => [
      i.id,
      i.processDefinitionKey || '',
      i.startUserId || 'System',
      i.businessKey || '',
      i.suspended ? 'SUSPENDED' : i.status,
      new Date(i.startTime).toLocaleString(),
    ])
    
    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `running-instances-${new Date().toISOString().split('T')[0]}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  return (
    <Box>
      {/* Breadcrumb */}
      <Box sx={{ mb: 3, display: 'flex', alignItems: 'center', gap: 1 }}>
        <Typography variant="body2" color="text.secondary">
          Home
        </Typography>
        <Typography variant="body2" color="text.disabled">
          /
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Process Management
        </Typography>
        <Typography variant="body2" color="text.disabled">
          /
        </Typography>
        <Typography variant="body2" color="white" sx={{ fontWeight: 500 }}>
          Running Instances
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Total Active"
            value={totalCount}
            icon={<DatasetIcon />}
            color="#3b82f6"
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Suspended"
            value={getSuspendedCount()}
            icon={<WarningIcon />}
            color="#ef4444"
            loading={loading}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Pending Tasks"
            value={getPendingTasksCount()}
            icon={<HourglassIcon />}
            color="#f59e0b"
            loading={statsLoading}
            onClick={() => navigate('/tasks')}
          />
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <StatCard
            title="Completed Today"
            value={metrics?.completedInstances ?? 0}
            icon={<CheckCircleIcon />}
            color="#10b981"
            loading={statsLoading}
            onClick={() => navigate('/instances/completed')}
          />
        </Grid>
      </Grid>

      {/* Filters */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, flexWrap: 'wrap', gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel>Definition</InputLabel>
            <Select
              value={definitionFilter}
              label="Definition"
              onChange={handleDefinitionFilterChange}
            >
              <MenuItem value="">All Definitions</MenuItem>
              {definitions.map((def) => (
                <MenuItem key={def.id} value={def.key}>
                  {def.name || def.key}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          {definitionFilter && (
            <Chip
              size="small"
              label={`Definition: ${definitionFilter}`}
              onDelete={() => setDefinitionFilter('')}
              sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}
            />
          )}
          {definitionFilter && (
            <Button size="small" onClick={() => setDefinitionFilter('')} sx={{ color: 'primary.main' }}>
              Clear all
            </Button>
          )}
        </Box>
        <Box sx={{ display: 'flex', gap: 1 }}>
          <Tooltip title="Export CSV">
            <IconButton 
              onClick={handleExportCSV}
              sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}
            >
              <DownloadIcon />
            </IconButton>
          </Tooltip>
          <Tooltip title="Refresh">
            <IconButton
              onClick={() => { loadInstances(); loadMetrics(); }}
              sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}
            >
              <RefreshIcon />
            </IconButton>
          </Tooltip>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => setStartProcessDialog(true)}
            sx={{ boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)' }}
          >
            Start Process
          </Button>
        </Box>
      </Box>

      {/* Table */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox
                    checked={selectedInstances.length === instances.length && instances.length > 0}
                    indeterminate={selectedInstances.length > 0 && selectedInstances.length < instances.length}
                    onChange={handleSelectAll}
                  />
                </TableCell>
                <TableCell>Instance ID</TableCell>
                <TableCell>Process Name</TableCell>
                <TableCell>Initiator</TableCell>
                <TableCell>Business Key</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                [...Array(5)].map((_, idx) => (
                  <TableRow key={idx}>
                    <TableCell colSpan={8}>
                      <Skeleton variant="rectangular" height={56} sx={{ bgcolor: 'background.default' }} />
                    </TableCell>
                  </TableRow>
                ))
              ) : instances.length > 0 ? (
                instances.map((instance) => (
                  <TableRow
                    key={instance.id}
                    hover
                    onClick={() => handleInstanceClick(instance)}
                    sx={{ cursor: 'pointer' }}
                  >
                    <TableCell padding="checkbox" onClick={(e) => e.stopPropagation()}>
                      <Checkbox
                        checked={selectedInstances.includes(instance.id)}
                        onChange={() => handleSelectInstance(instance.id)}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography
                        variant="body2"
                        sx={{
                          fontFamily: 'monospace',
                          color: 'primary.main',
                          fontWeight: 500,
                        }}
                      >
                        #{instance.id.slice(0, 8)}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 500, color: 'white' }}>
                        {instance.processDefinitionKey?.replace(/-/g, ' ') || instance.name || 'Unknown'}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {instance.description || 'No description'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
                        <Avatar
                          sx={{
                            width: 28,
                            height: 28,
                            fontSize: '0.75rem',
                            fontWeight: 600,
                            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
                          }}
                        >
                          {instance.startUserId?.charAt(0)?.toUpperCase() || 'S'}
                        </Avatar>
                        <Box>
                          <Typography variant="body2" sx={{ color: 'white', fontSize: '0.75rem' }}>
                            {instance.startUserId || 'System'}
                          </Typography>
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ color: 'text.secondary' }}>
                        {instance.businessKey || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" sx={{ color: 'white' }}>
                        {new Date(instance.startTime).toLocaleDateString()}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {new Date(instance.startTime).toLocaleTimeString()}
                      </Typography>
                    </TableCell>
                    <TableCell>{getStatusChip(instance.status, instance.suspended)}</TableCell>
                    <TableCell align="right" onClick={(e) => e.stopPropagation()}>
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
                        <Tooltip title="Manage Tasks">
                          <IconButton 
                            size="small" 
                            sx={{ color: 'text.secondary' }}
                            onClick={() => navigate(`/instances/${instance.id}?tab=tasks`)}
                          >
                            <AssignIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Add Variables">
                          <IconButton
                            size="small"
                            sx={{ color: 'text.secondary' }}
                            onClick={() => navigate(`/instances/${instance.id}?tab=variables`)}
                          >
                            <VariablesIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="View Details">
                          <IconButton
                            size="small"
                            sx={{ color: 'text.secondary' }}
                            onClick={() => navigate(`/instances/${instance.id}`)}
                          >
                            <CompleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <IconButton
                          size="small"
                          sx={{ color: 'text.secondary' }}
                          onClick={(e) => handleActionMenuOpen(e, instance)}
                        >
                          <MoreIcon fontSize="small" />
                        </IconButton>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} sx={{ textAlign: 'center', py: 6 }}>
                    <Typography color="text.secondary">No running instances found</Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          component="div"
          count={totalCount}
          page={page}
          onPageChange={handlePageChange}
          rowsPerPage={rowsPerPage}
          onRowsPerPageChange={handleRowsPerPageChange}
          rowsPerPageOptions={[10, 25, 50]}
          sx={{ borderTop: '1px solid', borderColor: 'divider' }}
        />
      </Card>

      {/* Action Menu */}
      <Menu
        anchorEl={actionMenuAnchor}
        open={Boolean(actionMenuAnchor)}
        onClose={handleActionMenuClose}
      >
        <MenuItem onClick={() => navigate(`/instances/${selectedInstanceForAction?.id}`)}>
          View Details
        </MenuItem>
        <MenuItem onClick={() => navigate(`/instances/${selectedInstanceForAction?.id}?tab=variables`)}>
          View Variables
        </MenuItem>
        <MenuItem onClick={() => navigate(`/instances/${selectedInstanceForAction?.id}?tab=tasks`)}>
          View Tasks
        </MenuItem>
        <MenuItem
          onClick={() =>
            setConfirmDialog({
              open: true,
              action: 'suspend',
              instanceId: selectedInstanceForAction?.id || '',
            })
          }
        >
          Suspend Instance
        </MenuItem>
        <MenuItem
          onClick={() =>
            setConfirmDialog({
              open: true,
              action: 'delete',
              instanceId: selectedInstanceForAction?.id || '',
            })
          }
          sx={{ color: 'error.main' }}
        >
          Delete Instance
        </MenuItem>
      </Menu>

      {/* Confirm Dialog */}
      <Dialog
        open={!!confirmDialog?.open}
        onClose={() => setConfirmDialog(null)}
      >
        <DialogTitle>
          {confirmDialog?.action === 'delete' ? 'Delete Instance' : 'Suspend Instance'}
        </DialogTitle>
        <DialogContent>
          <DialogContentText>
            {confirmDialog?.action === 'delete'
              ? 'Are you sure you want to delete this instance? This action cannot be undone.'
              : 'Are you sure you want to suspend this instance?'}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDialog(null)}>Cancel</Button>
          <Button
            onClick={() =>
              confirmDialog?.action === 'delete'
                ? handleDelete(confirmDialog.instanceId)
                : handleSuspend(confirmDialog?.instanceId || '')
            }
            color={confirmDialog?.action === 'delete' ? 'error' : 'primary'}
            variant="contained"
          >
            {confirmDialog?.action === 'delete' ? 'Delete' : 'Suspend'}
          </Button>
        </DialogActions>
      </Dialog>

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

export default RunningInstances
