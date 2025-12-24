import React, { useEffect, useState } from 'react'
import { useParams, useSearchParams, useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Tabs,
  Tab,
  Button,
  IconButton,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Avatar,
  Tooltip,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Skeleton,
  alpha,
  Grid,
  Alert,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from '@mui/material'
import {
  Refresh as RefreshIcon,
  Pause as SuspendIcon,
  Cancel as TerminateIcon,
  PlayArrow as ResumeIcon,
  AccountTree as DiagramIcon,
  DataObject as VariablesIcon,
  Assignment as TasksIcon,
  History as EventsIcon,
  Add as AddIcon,
  Edit as EditIcon,
  PersonAdd as AssignIcon,
  CheckCircle as CompleteIcon,
  ArrowBack as BackIcon,
  ContentCopy as CopyIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import { BpmnViewer } from '../components/BpmnViewer'
import type { ProcessInstance, Task, EventLog } from '../types'

interface TabPanelProps {
  children?: React.ReactNode
  index: number
  value: number
}

const TabPanel: React.FC<TabPanelProps> = ({ children, value, index }) => (
  <Box
    role="tabpanel"
    hidden={value !== index}
    sx={{ flex: 1, overflow: 'auto', display: value === index ? 'flex' : 'none', flexDirection: 'column' }}
  >
    {value === index && children}
  </Box>
)

export const InstanceDetails: React.FC = () => {
  const { instanceId } = useParams<{ instanceId: string }>()
  const [searchParams, setSearchParams] = useSearchParams()
  const navigate = useNavigate()
  
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [instance, setInstance] = useState<ProcessInstance | null>(null)
  const [tasks, setTasks] = useState<Task[]>([])
  const [events, setEvents] = useState<EventLog[]>([])
  const [variables, setVariables] = useState<Record<string, unknown>>({})
  const [bpmnXml, setBpmnXml] = useState<string>('')
  const [activeTab, setActiveTab] = useState(0)
  const [addVariableDialog, setAddVariableDialog] = useState(false)
  const [newVariable, setNewVariable] = useState({ name: '', value: '' })
  const [assignDialog, setAssignDialog] = useState<{ open: boolean; taskId: string } | null>(null)
  const [assignee, setAssignee] = useState('')
  const [editVariableDialog, setEditVariableDialog] = useState<{ open: boolean; name: string; value: string } | null>(null)
  const [users, setUsers] = useState<{ username: string; fullName: string; role: string }[]>([])
  const [currentUser, setCurrentUser] = useState<string>('')

  useEffect(() => {
    const tab = searchParams.get('tab')
    if (tab === 'variables') setActiveTab(1)
    else if (tab === 'tasks') setActiveTab(2)
    else if (tab === 'events') setActiveTab(3)
    else setActiveTab(0)
  }, [searchParams])

  useEffect(() => {
    if (instanceId) {
      loadInstanceData()
    }
    loadUsers()
    setCurrentUser(localStorage.getItem('username') || '')
  }, [instanceId])

  const loadUsers = async () => {
    try {
      const userList = await adminApi.getUsers()
      setUsers(userList)
    } catch (err) {
      console.error('Failed to load users:', err)
    }
  }

  const loadInstanceData = async () => {
    if (!instanceId) return
    setLoading(true)
    setError(null)
    try {
      // First load the instance to check its status
      const instanceData = await adminApi.getInstanceById(instanceId)
      setInstance(instanceData)
      
      // Load other data in parallel
      const [tasksData, variablesData, bpmnData, eventsData] = await Promise.all([
        adminApi.getTasksByInstance(instanceId).catch(() => []),
        // For completed instances, use embedded variables; for running, fetch from runtime
        instanceData.status === 'COMPLETED' || instanceData.status === 'TERMINATED'
          ? Promise.resolve(instanceData.variables || {})
          : adminApi.getInstanceVariables(instanceId).catch(() => instanceData.variables || {}),
        // Fetch BPMN XML for the diagram viewer
        instanceData.processDefinitionKey 
          ? adminApi.getBpmnXml(instanceData.processDefinitionKey).catch(() => '')
          : Promise.resolve(''),
        adminApi.getEventsByInstance(instanceId).catch(() => []),
      ])
      
      setTasks(tasksData || [])
      setVariables(variablesData || {})
      setBpmnXml(bpmnData || '')
      setEvents(eventsData || [])
    } catch (err) {
      console.error('Failed to load instance data:', err)
      setError('Failed to load instance details. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleTabChange = (_: React.SyntheticEvent, newValue: number) => {
    setActiveTab(newValue)
    const tabNames = ['diagram', 'variables', 'tasks', 'events']
    setSearchParams({ tab: tabNames[newValue] })
  }

  const handleAddVariable = async () => {
    if (!instanceId || !newVariable.name) return
    try {
      let value: unknown = newVariable.value
      try {
        value = JSON.parse(newVariable.value)
      } catch {
        // Keep as string
      }
      await adminApi.setInstanceVariable(instanceId, newVariable.name, value)
      loadInstanceData()
      setAddVariableDialog(false)
      setNewVariable({ name: '', value: '' })
    } catch (err) {
      console.error('Failed to add variable:', err)
    }
  }

  const handleEditVariable = async () => {
    if (!instanceId || !editVariableDialog?.name) return
    try {
      let value: unknown = editVariableDialog.value
      try {
        value = JSON.parse(editVariableDialog.value)
      } catch {
        // Keep as string
      }
      await adminApi.setInstanceVariable(instanceId, editVariableDialog.name, value)
      loadInstanceData()
      setEditVariableDialog(null)
    } catch (err) {
      console.error('Failed to edit variable:', err)
    }
  }

  const handleSuspend = async () => {
    if (!instanceId) return
    try {
      await adminApi.suspendInstance(instanceId)
      loadInstanceData()
    } catch (err) {
      console.error('Failed to suspend instance:', err)
    }
  }

  const handleActivate = async () => {
    if (!instanceId) return
    try {
      await adminApi.activateInstance(instanceId)
      loadInstanceData()
    } catch (err) {
      console.error('Failed to activate instance:', err)
    }
  }

  const handleTerminate = async () => {
    if (!instanceId) return
    try {
      await adminApi.deleteInstance(instanceId, 'Terminated by admin')
      navigate('/instances/running')
    } catch (err) {
      console.error('Failed to terminate instance:', err)
    }
  }

  const handleAssignTask = async () => {
    if (!assignDialog?.taskId || !assignee) return
    try {
      await adminApi.assignTask(assignDialog.taskId, assignee)
      loadInstanceData()
      setAssignDialog(null)
      setAssignee('')
    } catch (err) {
      console.error('Failed to assign task:', err)
    }
  }

  const handleClaimTask = async (taskId: string) => {
    try {
      await adminApi.claimTask(taskId)
      loadInstanceData()
    } catch (err) {
      console.error('Failed to claim task:', err)
    }
  }

  const handleCompleteTask = async (taskId: string) => {
    try {
      await adminApi.completeTask(taskId, {})
      loadInstanceData()
    } catch (err) {
      console.error('Failed to complete task:', err)
    }
  }

  // Determine instance state
  const isRunning = instance?.status === 'ACTIVE' || (instance?.status === 'RUNNING' && !instance?.suspended)
  const isSuspended = instance?.suspended === true
  const isCompleted = instance?.status === 'COMPLETED' || instance?.endTime !== null

  if (loading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={120} sx={{ mb: 3, bgcolor: 'background.paper' }} />
        <Skeleton variant="rounded" height={400} sx={{ bgcolor: 'background.paper' }} />
      </Box>
    )
  }

  if (error) {
    return (
      <Box>
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
        <Button onClick={() => navigate(-1)}>Go Back</Button>
      </Box>
    )
  }

  if (!instance) {
    return (
      <Box sx={{ textAlign: 'center', py: 8 }}>
        <Typography variant="h6" color="text.secondary">
          Instance not found
        </Typography>
        <Button onClick={() => navigate('/instances/running')} sx={{ mt: 2 }}>
          Back to Instances
        </Button>
      </Box>
    )
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      {/* Header Card */}
      <Card>
        <CardContent sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 2 }}>
            <Box>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
                <IconButton onClick={() => navigate(-1)} size="small">
                  <BackIcon />
                </IconButton>
                <Typography variant="h5" sx={{ fontWeight: 700, color: 'white' }}>
                  {instance.processDefinitionKey?.replace(/-/g, ' ') || instance.name || 'Process'}
                </Typography>
                <Chip
                  icon={
                    isRunning ? <Box sx={{ width: 8, height: 8, borderRadius: '50%', bgcolor: '#10b981', animation: 'pulse 2s infinite' }} /> : undefined
                  }
                  label={isSuspended ? 'SUSPENDED' : instance.status}
                  sx={{
                    bgcolor: isRunning
                      ? alpha('#3b82f6', 0.15)
                      : isSuspended
                      ? alpha('#8b5cf6', 0.15)
                      : alpha('#10b981', 0.15),
                    color: isRunning ? '#3b82f6' : isSuspended ? '#8b5cf6' : '#10b981',
                    fontWeight: 600,
                    textTransform: 'uppercase',
                    fontSize: '0.625rem',
                    letterSpacing: '0.05em',
                  }}
                />
              </Box>
              <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 3, mt: 2 }}>
                <Box>
                  <Typography variant="caption" sx={{ color: 'text.disabled', textTransform: 'uppercase', fontWeight: 600 }}>
                    Instance ID
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'white' }}>
                      {instance.id.slice(0, 12)}
                    </Typography>
                    <Tooltip title="Copy ID">
                      <IconButton size="small" onClick={() => navigator.clipboard.writeText(instance.id)}>
                        <CopyIcon sx={{ fontSize: 14 }} />
                      </IconButton>
                    </Tooltip>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" sx={{ color: 'text.disabled', textTransform: 'uppercase', fontWeight: 600 }}>
                    Business Key
                  </Typography>
                  <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'white' }}>
                    {instance.businessKey || '-'}
                  </Typography>
                </Box>
                <Box>
                  <Typography variant="caption" sx={{ color: 'text.disabled', textTransform: 'uppercase', fontWeight: 600 }}>
                    Started By
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Avatar sx={{ width: 20, height: 20, fontSize: '0.625rem' }}>
                      {instance.startUserId?.charAt(0) || 'S'}
                    </Avatar>
                    <Typography variant="body2" sx={{ color: 'white' }}>
                      {instance.startUserId || 'System'}
                    </Typography>
                  </Box>
                </Box>
                <Box>
                  <Typography variant="caption" sx={{ color: 'text.disabled', textTransform: 'uppercase', fontWeight: 600 }}>
                    Started At
                  </Typography>
                  <Typography variant="body2" sx={{ color: 'white' }}>
                    {new Date(instance.startTime).toLocaleString()}
                  </Typography>
                </Box>
                {instance.endTime && (
                  <Box>
                    <Typography variant="caption" sx={{ color: 'text.disabled', textTransform: 'uppercase', fontWeight: 600 }}>
                      Ended At
                    </Typography>
                    <Typography variant="body2" sx={{ color: 'white' }}>
                      {new Date(instance.endTime).toLocaleString()}
                    </Typography>
                  </Box>
                )}
              </Box>
            </Box>
            
            {/* Action Buttons */}
            {!isCompleted && (
              <Box sx={{ display: 'flex', gap: 1 }}>
                <Button
                  variant="outlined"
                  size="small"
                  startIcon={<RefreshIcon />}
                  onClick={loadInstanceData}
                  sx={{ borderColor: 'divider' }}
                >
                  Refresh
                </Button>
                {isRunning && !isSuspended && (
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<SuspendIcon />}
                    onClick={handleSuspend}
                    sx={{ borderColor: alpha('#f59e0b', 0.5), color: '#f59e0b' }}
                  >
                    Suspend
                  </Button>
                )}
                {isSuspended && (
                  <Button
                    variant="outlined"
                    size="small"
                    startIcon={<ResumeIcon />}
                    onClick={handleActivate}
                    sx={{ borderColor: alpha('#10b981', 0.5), color: '#10b981' }}
                  >
                    Activate
                  </Button>
                )}
                <Button
                  variant="contained"
                  size="small"
                  color="error"
                  startIcon={<TerminateIcon />}
                  onClick={handleTerminate}
                >
                  Terminate
                </Button>
              </Box>
            )}
          </Box>
        </CardContent>
      </Card>

      {/* Tabs */}
      <Box sx={{ borderBottom: 1, borderColor: 'divider' }}>
        <Tabs value={activeTab} onChange={handleTabChange}>
          <Tab
            icon={<DiagramIcon />}
            iconPosition="start"
            label="Diagram"
            sx={{ minHeight: 48 }}
          />
          <Tab
            icon={<VariablesIcon />}
            iconPosition="start"
            label={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                Variables
                <Chip
                  size="small"
                  label={Object.keys(variables).length}
                  sx={{ height: 18, fontSize: '0.625rem', bgcolor: 'background.default' }}
                />
              </Box>
            }
            sx={{ minHeight: 48 }}
          />
          <Tab
            icon={<TasksIcon />}
            iconPosition="start"
            label={
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                Tasks
                {tasks.filter((t) => t.state === 'CLAIMABLE' || t.state === 'ASSIGNED').length > 0 && (
                  <Chip
                    size="small"
                    label={tasks.filter((t) => t.state === 'CLAIMABLE' || t.state === 'ASSIGNED').length}
                    color="primary"
                    sx={{ height: 18, fontSize: '0.625rem', fontWeight: 700 }}
                  />
                )}
              </Box>
            }
            sx={{ minHeight: 48 }}
          />
          <Tab
            icon={<EventsIcon />}
            iconPosition="start"
            label="Events"
            sx={{ minHeight: 48 }}
          />
        </Tabs>
      </Box>

      {/* Tab Panels */}
      <Box sx={{ display: 'flex', flex: 1, gap: 3 }}>
        {/* Diagram Tab */}
        <TabPanel value={activeTab} index={0}>
          <Grid container spacing={3}>
            <Grid item xs={12} lg={8}>
              <Card sx={{ height: 500 }}>
                <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <Typography variant="subtitle2" sx={{ textTransform: 'uppercase', color: 'text.secondary', fontWeight: 600, letterSpacing: '0.05em' }}>
                    BPMN Visualization
                  </Typography>
                  <Box sx={{ display: 'flex', gap: 1 }}>
                    <IconButton size="small" onClick={loadInstanceData}>
                      <RefreshIcon fontSize="small" />
                    </IconButton>
                  </Box>
                </Box>
                <Box
                  sx={{
                    flex: 1,
                    height: 'calc(100% - 56px)',
                    overflow: 'hidden',
                  }}
                >
                  <BpmnViewer
                    xml={bpmnXml}
                    activeActivityIds={instance?.activeActivityIds || []}
                    completedActivityIds={instance?.completedActivityIds || []}
                    loading={loading}
                  />
                </Box>
              </Card>
            </Grid>
            
            {/* Current Task Info */}
            <Grid item xs={12} lg={4}>
              <Card>
                <Box sx={{ p: 3, background: 'linear-gradient(135deg, rgba(59, 130, 246, 0.15) 0%, transparent 100%)', borderBottom: '1px solid', borderColor: alpha('#3b82f6', 0.2) }}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: alpha('#3b82f6', 0.2), color: 'primary.main' }}>
                      <TasksIcon />
                    </Box>
                    <Box>
                      <Typography variant="subtitle2" color="text.secondary">
                        Current Task
                      </Typography>
                      <Typography variant="h6" sx={{ fontWeight: 600, color: 'primary.main' }}>
                        {tasks.find((t) => t.state === 'CLAIMABLE' || t.state === 'ASSIGNED')?.name || 'No active task'}
                      </Typography>
                    </Box>
                  </Box>
                </Box>
                <Box sx={{ p: 3 }}>
                  {tasks.filter((t) => t.state === 'CLAIMABLE' || t.state === 'ASSIGNED').map((task) => (
                    <Box key={task.id} sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
                      <Box>
                        <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase', fontWeight: 600 }}>
                          Assignee
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, p: 1, bgcolor: 'background.default', borderRadius: 1, border: '1px solid', borderColor: 'divider' }}>
                          <Avatar sx={{ width: 20, height: 20, fontSize: '0.625rem' }}>
                            {task.assignee?.charAt(0) || '?'}
                          </Avatar>
                          <Typography variant="body2" sx={{ color: task.assignee ? 'white' : 'text.secondary', fontStyle: task.assignee ? 'normal' : 'italic' }}>
                            {task.assignee || 'Unassigned'}
                          </Typography>
                        </Box>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase', fontWeight: 600 }}>
                          Candidate Group
                        </Typography>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5, p: 1, bgcolor: 'background.default', borderRadius: 1, border: '1px solid', borderColor: 'divider' }}>
                          <Typography variant="body2">
                            {task.candidateGroups?.join(', ') || '-'}
                          </Typography>
                        </Box>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase', fontWeight: 600 }}>
                          Created
                        </Typography>
                        <Typography variant="body2">
                          {new Date(task.createTime).toLocaleString()}
                        </Typography>
                      </Box>
                      <Box>
                        <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase', fontWeight: 600 }}>
                          Due Date
                        </Typography>
                        <Typography variant="body2" sx={{ color: task.dueDate ? 'error.main' : 'text.primary' }}>
                          {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}
                        </Typography>
                      </Box>
                      <Box sx={{ gridColumn: '1 / -1', mt: 2, pt: 2, borderTop: '1px solid', borderColor: 'divider' }}>
                        <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase', fontWeight: 600, mb: 1, display: 'block' }}>
                          Quick Actions
                        </Typography>
                        <Box sx={{ display: 'flex', gap: 1 }}>
                          <Button
                            fullWidth
                            variant="contained"
                            size="small"
                            onClick={() => handleClaimTask(task.id)}
                          >
                            Claim Task
                          </Button>
                          <IconButton
                            size="small"
                            onClick={() => setAssignDialog({ open: true, taskId: task.id })}
                            sx={{ bgcolor: 'background.default', border: '1px solid', borderColor: 'divider' }}
                          >
                            <AssignIcon fontSize="small" />
                          </IconButton>
                          <IconButton
                            size="small"
                            onClick={() => handleCompleteTask(task.id)}
                            sx={{ bgcolor: 'background.default', border: '1px solid', borderColor: 'divider' }}
                          >
                            <CompleteIcon fontSize="small" />
                          </IconButton>
                        </Box>
                      </Box>
                    </Box>
                  ))}
                  {tasks.filter((t) => t.state === 'CLAIMABLE' || t.state === 'ASSIGNED').length === 0 && (
                    <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                      {isCompleted ? 'Process completed' : 'No active tasks'}
                    </Typography>
                  )}
                </Box>
              </Card>
            </Grid>
          </Grid>
        </TabPanel>

        {/* Variables Tab */}
        <TabPanel value={activeTab} index={1}>
          <Card sx={{ flex: 1 }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <Typography variant="subtitle2" sx={{ textTransform: 'uppercase', color: 'text.secondary', fontWeight: 600 }}>
                Process Variables
              </Typography>
              {!isCompleted && (
                <Button
                  size="small"
                  startIcon={<AddIcon />}
                  onClick={() => setAddVariableDialog(true)}
                  sx={{ color: 'primary.main' }}
                >
                  Add Variable
                </Button>
              )}
            </Box>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Name</TableCell>
                    <TableCell>Value</TableCell>
                    <TableCell>Type</TableCell>
                    {!isCompleted && <TableCell align="right">Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {Object.entries(variables).map(([name, value]) => (
                    <TableRow key={name} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace', color: '#60a5fa' }}>
                          {name}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          label={typeof value === 'object' ? JSON.stringify(value) : String(value)}
                          size="small"
                          sx={{
                            maxWidth: 300,
                            fontFamily: 'monospace',
                            fontSize: '0.75rem',
                            bgcolor: 'background.default',
                            border: '1px solid',
                            borderColor: 'divider',
                          }}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption" color="text.secondary">
                          {typeof value}
                        </Typography>
                      </TableCell>
                      {!isCompleted && (
                        <TableCell align="right">
                          <Tooltip title="Edit Variable">
                            <IconButton 
                              size="small"
                              onClick={() => setEditVariableDialog({ 
                                open: true, 
                                name, 
                                value: typeof value === 'object' ? JSON.stringify(value) : String(value) 
                              })}
                            >
                              <EditIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                  {Object.keys(variables).length === 0 && (
                    <TableRow>
                      <TableCell colSpan={isCompleted ? 3 : 4} sx={{ textAlign: 'center', py: 4 }}>
                        <Typography color="text.secondary">No variables found</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </TabPanel>

        {/* Tasks Tab */}
        <TabPanel value={activeTab} index={2}>
          <Card sx={{ flex: 1 }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Typography variant="subtitle2" sx={{ textTransform: 'uppercase', color: 'text.secondary', fontWeight: 600 }}>
                Task History
              </Typography>
            </Box>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Task Name</TableCell>
                    <TableCell>Assignee</TableCell>
                    <TableCell>Created</TableCell>
                    <TableCell>Completed</TableCell>
                    <TableCell>Status</TableCell>
                    {!isCompleted && <TableCell align="right">Actions</TableCell>}
                  </TableRow>
                </TableHead>
                <TableBody>
                  {tasks.map((task) => (
                    <TableRow key={task.id} hover>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontWeight: 500, color: 'white' }}>
                          {task.name}
                        </Typography>
                        <Typography variant="caption" color="text.secondary">
                          {task.taskDefinitionKey}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Avatar sx={{ width: 24, height: 24, fontSize: '0.625rem' }}>
                            {task.assignee?.charAt(0) || '?'}
                          </Avatar>
                          <Typography variant="body2">
                            {task.assignee || 'Unassigned'}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {new Date(task.createTime).toLocaleString()}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                          {task.endTime ? new Date(task.endTime).toLocaleString() : '-'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          label={task.state}
                          sx={{
                            bgcolor: (task.state === 'CLAIMABLE' || task.state === 'ASSIGNED')
                              ? alpha('#3b82f6', 0.15)
                              : alpha('#10b981', 0.15),
                            color: (task.state === 'CLAIMABLE' || task.state === 'ASSIGNED') ? '#3b82f6' : '#10b981',
                            fontWeight: 500,
                          }}
                        />
                      </TableCell>
                      {!isCompleted && (
                        <TableCell align="right">
                          {(task.state === 'CLAIMABLE' || task.state === 'ASSIGNED') && (
                            <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
                              <Tooltip title="Claim">
                                <IconButton size="small" onClick={() => handleClaimTask(task.id)}>
                                  <AssignIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                              <Tooltip title="Complete">
                                <IconButton size="small" onClick={() => handleCompleteTask(task.id)}>
                                  <CompleteIcon fontSize="small" />
                                </IconButton>
                              </Tooltip>
                            </Box>
                          )}
                        </TableCell>
                      )}
                    </TableRow>
                  ))}
                  {tasks.length === 0 && (
                    <TableRow>
                      <TableCell colSpan={isCompleted ? 5 : 6} sx={{ textAlign: 'center', py: 4 }}>
                        <Typography color="text.secondary">No tasks found</Typography>
                      </TableCell>
                    </TableRow>
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </Card>
        </TabPanel>

        {/* Events Tab */}
        <TabPanel value={activeTab} index={3}>
          <Card sx={{ flex: 1 }}>
            <Box sx={{ p: 2, borderBottom: '1px solid', borderColor: 'divider' }}>
              <Typography variant="subtitle2" sx={{ textTransform: 'uppercase', color: 'text.secondary', fontWeight: 600 }}>
                Process Events
              </Typography>
            </Box>
            <Box sx={{ p: 3 }}>
              <Box sx={{ position: 'relative', pl: 3, borderLeft: '2px solid', borderColor: 'divider' }}>
                {events.length > 0 ? events.map((event, idx) => (
                  <Box key={event.id} sx={{ mb: 4, position: 'relative' }}>
                    <Box
                      sx={{
                        position: 'absolute',
                        left: -19,
                        top: 4,
                        width: 10,
                        height: 10,
                        borderRadius: '50%',
                        bgcolor: idx === 0 ? 'primary.main' : 'text.disabled',
                        border: '3px solid',
                        borderColor: 'background.paper',
                      }}
                    />
                    <Typography variant="body2" sx={{ fontWeight: 600, color: 'white' }}>
                      {event.type}
                    </Typography>
                    <Typography variant="caption" color="text.secondary" sx={{ display: 'block' }}>
                      {event.data || '-'}
                    </Typography>
                    <Typography variant="caption" sx={{ color: 'text.disabled', fontSize: '0.625rem' }}>
                      {new Date(event.timestamp).toLocaleString()}
                    </Typography>
                  </Box>
                )) : (
                  // Show basic events from instance data if no events available
                  [
                    { type: 'Process Started', time: instance.startTime, color: 'primary.main' },
                    ...(isCompleted && instance.endTime ? [{ type: 'Process Completed', time: instance.endTime, color: 'success.main' }] : []),
                  ].map((event, idx) => (
                    <Box key={idx} sx={{ mb: 4, position: 'relative' }}>
                      <Box
                        sx={{
                          position: 'absolute',
                          left: -19,
                          top: 4,
                          width: 10,
                          height: 10,
                          borderRadius: '50%',
                          bgcolor: event.color,
                          border: '3px solid',
                          borderColor: 'background.paper',
                        }}
                      />
                      <Typography variant="body2" sx={{ fontWeight: 600, color: 'white' }}>
                        {event.type}
                      </Typography>
                      <Typography variant="caption" sx={{ color: 'text.disabled', fontSize: '0.625rem' }}>
                        {event.time ? new Date(event.time).toLocaleString() : '-'}
                      </Typography>
                    </Box>
                  ))
                )}
              </Box>
            </Box>
          </Card>
        </TabPanel>
      </Box>

      {/* Add Variable Dialog */}
      <Dialog open={addVariableDialog} onClose={() => setAddVariableDialog(false)}>
        <DialogTitle>Add Variable</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Variable Name"
            fullWidth
            value={newVariable.name}
            onChange={(e) => setNewVariable((prev) => ({ ...prev, name: e.target.value }))}
            sx={{ mb: 2 }}
          />
          <TextField
            margin="dense"
            label="Value (JSON or string)"
            fullWidth
            multiline
            rows={3}
            value={newVariable.value}
            onChange={(e) => setNewVariable((prev) => ({ ...prev, value: e.target.value }))}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAddVariableDialog(false)}>Cancel</Button>
          <Button onClick={handleAddVariable} variant="contained">
            Add
          </Button>
        </DialogActions>
      </Dialog>

      {/* Assign Task Dialog */}
      <Dialog open={!!assignDialog?.open} onClose={() => setAssignDialog(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Assign Task</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Select a user to assign this task to:
          </Typography>
          
          {/* Self Assign Button */}
          {currentUser && (
            <Button
              fullWidth
              variant="outlined"
              onClick={() => setAssignee(currentUser)}
              sx={{ 
                mb: 2, 
                justifyContent: 'flex-start',
                borderColor: assignee === currentUser ? 'primary.main' : 'divider',
                bgcolor: assignee === currentUser ? 'action.selected' : 'transparent',
              }}
            >
              Assign to myself ({currentUser})
            </Button>
          )}
          
          {/* User Selection */}
          <FormControl fullWidth>
            <InputLabel>Select User</InputLabel>
            <Select
              value={assignee}
              label="Select User"
              onChange={(e) => setAssignee(e.target.value as string)}
            >
              {users.map((user) => (
                <MenuItem key={user.username} value={user.username}>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                    <Avatar sx={{ width: 24, height: 24, fontSize: '0.75rem', bgcolor: 'primary.main' }}>
                      {user.fullName?.charAt(0) || user.username.charAt(0).toUpperCase()}
                    </Avatar>
                    <Box>
                      <Typography variant="body2">{user.fullName || user.username}</Typography>
                      <Typography variant="caption" color="text.secondary">
                        @{user.username} • {user.role}
                      </Typography>
                    </Box>
                  </Box>
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => { setAssignDialog(null); setAssignee(''); }}>Cancel</Button>
          <Button onClick={handleAssignTask} variant="contained" disabled={!assignee}>
            Assign
          </Button>
        </DialogActions>
      </Dialog>

      {/* Edit Variable Dialog */}
      <Dialog open={!!editVariableDialog?.open} onClose={() => setEditVariableDialog(null)}>
        <DialogTitle>Edit Variable: {editVariableDialog?.name}</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Value (JSON or string)"
            fullWidth
            multiline
            rows={3}
            value={editVariableDialog?.value || ''}
            onChange={(e) => setEditVariableDialog(prev => prev ? { ...prev, value: e.target.value } : null)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setEditVariableDialog(null)}>Cancel</Button>
          <Button onClick={handleEditVariable} variant="contained">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default InstanceDetails
