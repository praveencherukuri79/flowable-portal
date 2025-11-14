import React, { useEffect, useState } from 'react'
import {
  Box,
  Card,
  CardContent,
  Typography,
  CircularProgress,
  Alert,
  Button,
  Chip,
  Snackbar
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import { flowableApi, TaskDto } from '../../api/flowableApi'
import dayjs from 'dayjs'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import LockOpenIcon from '@mui/icons-material/LockOpen'

export const MyTasks: React.FC = () => {
  const [tasks, setTasks] = useState<TaskDto[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const navigate = useNavigate()

  const loadTasks = async () => {
    try {
      setLoading(true)
      const data = await flowableApi.getTasksByGroupOrAssigned('MAKER')
      setTasks(data)
    } catch (err) {
      setError('Failed to load tasks')
      console.error('Load tasks error:', err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTasks()
  }, [])

  const handleClaim = async (taskId: string) => {
    try {
      await flowableApi.claimTask(taskId)
      setSuccess('Task claimed successfully')
      loadTasks()
    } catch (err) {
      setError('Failed to claim task')
      console.error('Claim task error:', err)
    }
  }

  const handleOpen = (task: TaskDto) => {
    if (task.formKey) {
      // Navigate using formKey
      navigate(task.formKey, { state: { taskId: task.id, processInstanceId: task.processInstanceId } })
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
      width: 200,
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
          <Button
            size="small"
            variant="contained"
            startIcon={<PlayArrowIcon />}
            onClick={() => handleOpen(task)}
            disabled={!task.formKey}
          >
            Open
          </Button>
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
      {error && <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError('')}>{error}</Alert>}

      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            My Tasks
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

      <Snackbar
        open={!!success}
        autoHideDuration={3000}
        onClose={() => setSuccess('')}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert severity="success">{success}</Alert>
      </Snackbar>
    </Box>
  )
}

