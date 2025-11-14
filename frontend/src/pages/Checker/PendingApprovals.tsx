import React, { useEffect, useState } from 'react'
import {
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Typography,
  Alert,
  Snackbar,
  Chip
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import LockOpenIcon from '@mui/icons-material/LockOpen'
import { flowableApi, TaskDto } from '../../api/flowableApi'
import dayjs from 'dayjs'

export const PendingApprovals: React.FC = () => {
  const [tasks, setTasks] = useState<TaskDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [dialogOpen, setDialogOpen] = useState(false)
  const [selectedTask, setSelectedTask] = useState<TaskDto | null>(null)
  const [taskVariables, setTaskVariables] = useState<Record<string, unknown>>({})
  const [comments, setComments] = useState('')
  const [action, setAction] = useState<'approve' | 'reject'>('approve')
  const [success, setSuccess] = useState(false)
  const navigate = useNavigate()

  const loadTasks = async () => {
    try {
      setLoading(true)
      const data = await flowableApi.getTasksByGroupOrAssigned('CHECKER')
      setTasks(data)
    } catch (err) {
      setError('Failed to load pending tasks')
      console.error('Load tasks error:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTasks()
  }, [])

  const handleOpenDialog = async (task: TaskDto, actionType: 'approve' | 'reject') => {
    setSelectedTask(task)
    setAction(actionType)
    setComments('')
    
    // Load task variables
    try {
      const variables = await flowableApi.getTaskVariables(task.id)
      setTaskVariables(variables)
    } catch (err) {
      console.error('Failed to load task variables:', err)
    }
    
    setDialogOpen(true)
  }

  const handleCloseDialog = () => {
    setDialogOpen(false)
    setSelectedTask(null)
    setTaskVariables({})
    setComments('')
  }

  const handleClaim = async (taskId: string) => {
    try {
      await flowableApi.claimTask(taskId)
      setSuccess(true)
      loadTasks()
    } catch (err) {
      setError('Failed to claim task')
      console.error('Claim task error:', err)
    }
  }

  const handleOpen = (task: TaskDto) => {
    if (task.formKey) {
      navigate(task.formKey, { state: { taskId: task.id, processInstanceId: task.processInstanceId } })
    }
  }

  const handleSubmit = async () => {
    if (!selectedTask) return

    try {
      const variables: Record<string, unknown> = {
        checkerComments: comments,
        checkerDecision: action === 'approve' ? 'APPROVE' : 'REJECT',
        approved: action === 'approve'
      }

      // Add stage-specific decision variables
      const taskDefKey = selectedTask.taskDefinitionKey
      if (taskDefKey) {
        if (taskDefKey.includes('stage1')) {
          variables.stage1Decision = action === 'approve' ? 'APPROVE' : 'REJECT'
        } else if (taskDefKey.includes('stage2')) {
          variables.stage2Decision = action === 'approve' ? 'APPROVE' : 'REJECT'
        } else if (taskDefKey.includes('stage3')) {
          variables.stage3Decision = action === 'approve' ? 'APPROVE' : 'REJECT'
        }
      }

      await flowableApi.completeTask(selectedTask.id, variables)
      
      setSuccess(true)
      handleCloseDialog()
      loadTasks()
    } catch (err) {
      setError(`Failed to ${action} task`)
      console.error(`${action} error:`, err)
    }
  }

  const columns: GridColDef[] = [
    { field: 'name', headerName: 'Task Name', flex: 1 },
    {
      field: 'createTime',
      headerName: 'Created',
      width: 180,
      valueFormatter: (params) => params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm:ss') : ''
    },
    {
      field: 'assignee',
      headerName: 'Status',
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? 'Assigned' : 'Claimable'}
          color={params.value ? 'success' : 'default'}
          size="small"
        />
      )
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 300,
      renderCell: (params: GridRenderCellParams) => {
        const task = params.row as TaskDto
        if (!task.assignee) {
          return (
            <Button
              size="small"
              variant="outlined"
              startIcon={<LockOpenIcon />}
              onClick={() => handleClaim(task.id)}
            >
              Claim
            </Button>
          )
        }
        return (
          <Box>
            <Button
              size="small"
              variant="contained"
              startIcon={<PlayArrowIcon />}
              onClick={() => handleOpen(task)}
              disabled={!task.formKey}
              sx={{ mr: 1 }}
            >
              Open
            </Button>
            <Button
              size="small"
              startIcon={<CheckCircleIcon />}
              onClick={() => handleOpenDialog(task, 'approve')}
              sx={{ mr: 1 }}
            >
              Approve
            </Button>
            <Button
              size="small"
              color="error"
              startIcon={<CancelIcon />}
              onClick={() => handleOpenDialog(task, 'reject')}
            >
              Reject
            </Button>
          </Box>
        )
      }
    }
  ]

  if (loading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Pending Approvals
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Assigned tasks and claimable tasks from your candidate group
          </Typography>

          <DataGrid
            rows={tasks}
            columns={columns}
            initialState={{
              pagination: {
                paginationModel: { page: 0, pageSize: 10 }
              }
            }}
            pageSizeOptions={[5, 10, 25]}
            autoHeight
          />
        </CardContent>
      </Card>

      {/* Approval/Rejection Dialog */}
      <Dialog open={dialogOpen} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>
          {action === 'approve' ? 'Approve Request' : 'Reject Request'}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Task: {selectedTask?.name}
          </Typography>
          
          {/* Display task variables */}
          {Object.keys(taskVariables).length > 0 && (
            <Box sx={{ mt: 2, mb: 2, p: 2, bgcolor: 'background.default', borderRadius: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Request Details:
              </Typography>
              {Object.entries(taskVariables).map(([key, value]) => (
                <Typography key={key} variant="body2">
                  <strong>{key}:</strong> {String(value)}
                </Typography>
              ))}
            </Box>
          )}

          <TextField
            fullWidth
            label="Comments"
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            margin="normal"
            multiline
            rows={4}
            required={action === 'reject'}
            placeholder={action === 'approve' ? 'Optional comments' : 'Required: Reason for rejection'}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            color={action === 'approve' ? 'primary' : 'error'}
            disabled={action === 'reject' && !comments.trim()}
          >
            {action === 'approve' ? 'Approve' : 'Reject'}
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={success}
        autoHideDuration={3000}
        onClose={() => setSuccess(false)}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert severity="success">
          {action ? `Task ${action}d successfully!` : 'Task claimed successfully!'}
        </Alert>
      </Snackbar>
    </Box>
  )
}

