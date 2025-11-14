import React, { useState, useEffect } from 'react'
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  Typography,
  Chip,
  CircularProgress,
  Alert,
  Tabs,
  Tab,
  TextField,
  Grid,
  Card,
  CardContent,
  IconButton,
  Divider,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Accordion,
  AccordionSummary,
  AccordionDetails
} from '@mui/material'
import ExpandMoreIcon from '@mui/icons-material/ExpandMore'
import AssignmentIndIcon from '@mui/icons-material/AssignmentInd'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import DeleteIcon from '@mui/icons-material/Delete'
import PauseIcon from '@mui/icons-material/Pause'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import StopIcon from '@mui/icons-material/Stop'
import EditIcon from '@mui/icons-material/Edit'
import SaveIcon from '@mui/icons-material/Save'
import {
  processControlApi,
  ProcessInstance,
  TaskInfo
} from '../../api/processControlApi'
import dayjs from 'dayjs'

interface InstanceDetailsViewProps {
  open: boolean
  instance: ProcessInstance
  onClose: () => void
  onSuccess: (message: string) => void
  onError: (message: string) => void
}

export function InstanceDetailsView({
  open,
  instance,
  onClose,
  onSuccess,
  onError
}: InstanceDetailsViewProps) {
  const [loading, setLoading] = useState(false)
  const [tab, setTab] = useState(0)
  
  // Variables
  const [variables, setVariables] = useState<Record<string, unknown>>({})
  const [variablesJson, setVariablesJson] = useState('{}')
  const [editingVariables, setEditingVariables] = useState(false)
  
  // Tasks
  const [tasks, setTasks] = useState<TaskInfo[]>([])
  
  // Task Actions
  const [assignTaskId, setAssignTaskId] = useState<string | null>(null)
  const [assignUsername, setAssignUsername] = useState('')
  
  useEffect(() => {
    if (open) {
      loadInstanceDetails()
    }
  }, [open, instance.id])
  
  const loadInstanceDetails = async () => {
    try {
      setLoading(true)
      const [vars, taskList] = await Promise.all([
        processControlApi.getInstanceVariables(instance.id),
        processControlApi.getTasksByProcessInstance(instance.id)
      ])
      setVariables(vars)
      setVariablesJson(JSON.stringify(vars, null, 2))
      setTasks(taskList)
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to load instance details')
    } finally {
      setLoading(false)
    }
  }
  
  const handleSuspend = async () => {
    try {
      setLoading(true)
      await processControlApi.suspendInstance(instance.id)
      onSuccess('Process instance suspended')
      onClose()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to suspend instance')
    } finally {
      setLoading(false)
    }
  }
  
  const handleActivate = async () => {
    try {
      setLoading(true)
      await processControlApi.activateInstance(instance.id)
      onSuccess('Process instance activated')
      onClose()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to activate instance')
    } finally {
      setLoading(false)
    }
  }
  
  const handleDelete = async () => {
    if (!window.confirm('Are you sure you want to delete this process instance? This cannot be undone.')) {
      return
    }
    
    try {
      setLoading(true)
      await processControlApi.deleteInstance(instance.id, 'Deleted by admin')
      onSuccess('Process instance deleted')
      onClose()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to delete instance')
    } finally {
      setLoading(false)
    }
  }
  
  const handleSaveVariables = async () => {
    try {
      setLoading(true)
      const vars = JSON.parse(variablesJson)
      await processControlApi.updateInstanceVariables(instance.id, vars)
      setVariables(vars)
      setEditingVariables(false)
      onSuccess('Variables updated successfully')
      loadInstanceDetails()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to update variables. Check JSON format.')
    } finally {
      setLoading(false)
    }
  }
  
  const handleAssignTask = async (taskId: string) => {
    if (!assignUsername.trim()) {
      onError('Please enter a username')
      return
    }
    
    try {
      setLoading(true)
      await processControlApi.assignTask(taskId, assignUsername)
      onSuccess(`Task assigned to ${assignUsername}`)
      setAssignTaskId(null)
      setAssignUsername('')
      loadInstanceDetails()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to assign task')
    } finally {
      setLoading(false)
    }
  }
  
  const handleUnclaimTask = async (taskId: string) => {
    try {
      setLoading(true)
      await processControlApi.unclaimTask(taskId)
      onSuccess('Task unclaimed')
      loadInstanceDetails()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to unclaim task')
    } finally {
      setLoading(false)
    }
  }
  
  const handleDeleteTask = async (taskId: string) => {
    if (!window.confirm('Are you sure you want to delete this task?')) {
      return
    }
    
    try {
      setLoading(true)
      await processControlApi.deleteTask(taskId, 'Deleted by admin')
      onSuccess('Task deleted')
      loadInstanceDetails()
    } catch (err: any) {
      onError(err.response?.data?.message || 'Failed to delete task')
    } finally {
      setLoading(false)
    }
  }
  
  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Process Instance Details</Typography>
          <Box>
            {instance.status === 'ACTIVE' ? (
              <Button
                startIcon={<PauseIcon />}
                onClick={handleSuspend}
                disabled={loading}
                size="small"
                sx={{ mr: 1 }}
              >
                Suspend
              </Button>
            ) : (
              <Button
                startIcon={<PlayArrowIcon />}
                onClick={handleActivate}
                disabled={loading}
                size="small"
                sx={{ mr: 1 }}
              >
                Activate
              </Button>
            )}
            <Button
              startIcon={<StopIcon />}
              color="error"
              onClick={handleDelete}
              disabled={loading}
              size="small"
            >
              Delete
            </Button>
          </Box>
        </Box>
      </DialogTitle>
      
      <DialogContent dividers>
        {loading && (
          <Box display="flex" justifyContent="center" py={3}>
            <CircularProgress />
          </Box>
        )}
        
        {!loading && (
          <>
            {/* Instance Info */}
            <Card sx={{ mb: 2 }}>
              <CardContent>
                <Grid container spacing={2}>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Instance ID
                    </Typography>
                    <Typography variant="body1" sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}>
                      {instance.id}
                    </Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Process Name
                    </Typography>
                    <Typography variant="body1">{instance.name}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Business Key
                    </Typography>
                    <Typography variant="body1">{instance.businessKey || '-'}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Status
                    </Typography>
                    <Chip
                      label={instance.status}
                      color={instance.status === 'ACTIVE' ? 'success' : 'warning'}
                      size="small"
                    />
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Started
                    </Typography>
                    <Typography variant="body1">{instance.startTime}</Typography>
                  </Grid>
                  <Grid item xs={12} sm={6}>
                    <Typography variant="body2" color="text.secondary">
                      Started By
                    </Typography>
                    <Typography variant="body1">{instance.startUserId || '-'}</Typography>
                  </Grid>
                </Grid>
              </CardContent>
            </Card>
            
            <Tabs value={tab} onChange={(_, v) => setTab(v)} sx={{ mb: 2 }}>
              <Tab label={`Active Tasks (${tasks.length})`} />
              <Tab label="Process Variables" />
            </Tabs>
            
            {/* Tasks Tab */}
            {tab === 0 && (
              <Box>
                {tasks.length === 0 ? (
                  <Alert severity="info">No active tasks for this instance</Alert>
                ) : (
                  tasks.map((task) => (
                    <Accordion key={task.id} sx={{ mb: 1 }}>
                      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                        <Box display="flex" alignItems="center" width="100%">
                          <Typography sx={{ flexGrow: 1 }}>{task.name}</Typography>
                          <Chip
                            label={task.assignee ? 'Assigned' : 'Unassigned'}
                            color={task.assignee ? 'success' : 'default'}
                            size="small"
                            sx={{ mr: 2 }}
                          />
                        </Box>
                      </AccordionSummary>
                      <AccordionDetails>
                        <Grid container spacing={2}>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Task ID
                            </Typography>
                            <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                              {task.id}
                            </Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Assignee
                            </Typography>
                            <Typography variant="body2">{task.assignee || 'None'}</Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Task Key
                            </Typography>
                            <Typography variant="body2">{task.taskDefinitionKey}</Typography>
                          </Grid>
                          <Grid item xs={12} sm={6}>
                            <Typography variant="body2" color="text.secondary">
                              Created
                            </Typography>
                            <Typography variant="body2">
                              {task.createTime ? dayjs(task.createTime).format('YYYY-MM-DD HH:mm:ss') : '-'}
                            </Typography>
                          </Grid>
                          {task.description && (
                            <Grid item xs={12}>
                              <Typography variant="body2" color="text.secondary">
                                Description
                              </Typography>
                              <Typography variant="body2">{task.description}</Typography>
                            </Grid>
                          )}
                        </Grid>
                        
                        <Divider sx={{ my: 2 }} />
                        
                        {/* Task Actions */}
                        <Box>
                          <Typography variant="subtitle2" gutterBottom>
                            Actions
                          </Typography>
                          {assignTaskId === task.id ? (
                            <Box display="flex" gap={1} mt={1}>
                              <TextField
                                size="small"
                                label="Username"
                                value={assignUsername}
                                onChange={(e) => setAssignUsername(e.target.value)}
                                sx={{ flexGrow: 1 }}
                              />
                              <Button
                                size="small"
                                variant="contained"
                                onClick={() => handleAssignTask(task.id)}
                                disabled={loading}
                              >
                                Assign
                              </Button>
                              <Button
                                size="small"
                                onClick={() => {
                                  setAssignTaskId(null)
                                  setAssignUsername('')
                                }}
                              >
                                Cancel
                              </Button>
                            </Box>
                          ) : (
                            <Box display="flex" gap={1} flexWrap="wrap" mt={1}>
                              <Button
                                size="small"
                                variant="outlined"
                                startIcon={<AssignmentIndIcon />}
                                onClick={() => {
                                  setAssignTaskId(task.id)
                                  setAssignUsername(task.assignee || '')
                                }}
                              >
                                {task.assignee ? 'Reassign' : 'Assign'}
                              </Button>
                              {task.assignee && (
                                <Button
                                  size="small"
                                  variant="outlined"
                                  onClick={() => handleUnclaimTask(task.id)}
                                  disabled={loading}
                                >
                                  Unclaim
                                </Button>
                              )}
                              <Button
                                size="small"
                                variant="outlined"
                                color="error"
                                startIcon={<DeleteIcon />}
                                onClick={() => handleDeleteTask(task.id)}
                                disabled={loading}
                              >
                                Delete Task
                              </Button>
                            </Box>
                          )}
                        </Box>
                      </AccordionDetails>
                    </Accordion>
                  ))
                )}
              </Box>
            )}
            
            {/* Variables Tab */}
            {tab === 1 && (
              <Box>
                <Box display="flex" justifyContent="space-between" mb={2}>
                  <Typography variant="subtitle2">
                    Process Variables ({Object.keys(variables).length})
                  </Typography>
                  {!editingVariables ? (
                    <Button
                      size="small"
                      startIcon={<EditIcon />}
                      onClick={() => setEditingVariables(true)}
                    >
                      Edit
                    </Button>
                  ) : (
                    <Box>
                      <Button
                        size="small"
                        startIcon={<SaveIcon />}
                        onClick={handleSaveVariables}
                        disabled={loading}
                        sx={{ mr: 1 }}
                      >
                        Save
                      </Button>
                      <Button
                        size="small"
                        onClick={() => {
                          setEditingVariables(false)
                          setVariablesJson(JSON.stringify(variables, null, 2))
                        }}
                      >
                        Cancel
                      </Button>
                    </Box>
                  )}
                </Box>
                
                {editingVariables ? (
                  <TextField
                    fullWidth
                    multiline
                    rows={15}
                    value={variablesJson}
                    onChange={(e) => setVariablesJson(e.target.value)}
                    sx={{ fontFamily: 'monospace', fontSize: '0.85rem' }}
                    helperText="Edit JSON and click Save"
                  />
                ) : (
                  <Paper variant="outlined" sx={{ p: 2, bgcolor: 'grey.50' }}>
                    <pre style={{ margin: 0, fontFamily: 'monospace', fontSize: '0.85rem' }}>
                      {variablesJson}
                    </pre>
                  </Paper>
                )}
              </Box>
            )}
          </>
        )}
      </DialogContent>
      
      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Close
        </Button>
      </DialogActions>
    </Dialog>
  )
}

