import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
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
  TextField,
  InputAdornment,
  Tooltip,
  Avatar,
  Skeleton,
  alpha,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Alert,
} from '@mui/material'
import {
  Search as SearchIcon,
  Refresh as RefreshIcon,
  PersonAdd as AssignIcon,
  CheckCircle as CompleteIcon,
  Visibility as ViewIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import type { Task } from '../types'

export const Tasks: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [tasks, setTasks] = useState<Task[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [stateFilter, setStateFilter] = useState('')
  const [searchTerm, setSearchTerm] = useState('')
  const [assignDialog, setAssignDialog] = useState<{ open: boolean; taskId: string } | null>(null)
  const [assignee, setAssignee] = useState('')

  useEffect(() => {
    loadTasks()
  }, [page, rowsPerPage, stateFilter])

  const loadTasks = async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await adminApi.searchTasks({
        state: stateFilter || undefined,
        page,
        size: rowsPerPage,
      })
      setTasks(response.content || [])
      setTotalCount(response.total || 0)
    } catch (err) {
      console.error('Failed to load tasks:', err)
      setError('Failed to load tasks. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handlePageChange = (_: unknown, newPage: number) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0)
  }

  const handleStateFilterChange = (event: SelectChangeEvent) => {
    setStateFilter(event.target.value)
    setPage(0)
  }

  const handleClaimTask = async (taskId: string) => {
    try {
      await adminApi.claimTask(taskId)
      loadTasks()
    } catch (err) {
      console.error('Failed to claim task:', err)
    }
  }

  const handleAssignTask = async () => {
    if (!assignDialog?.taskId || !assignee) return
    try {
      await adminApi.assignTask(assignDialog.taskId, assignee)
      loadTasks()
      setAssignDialog(null)
      setAssignee('')
    } catch (err) {
      console.error('Failed to assign task:', err)
    }
  }

  const handleCompleteTask = async (taskId: string) => {
    try {
      await adminApi.completeTask(taskId, {})
      loadTasks()
    } catch (err) {
      console.error('Failed to complete task:', err)
    }
  }

  const getStatusChip = (state: string) => {
    const config: Record<string, { color: string; label: string }> = {
      CLAIMABLE: { color: '#f59e0b', label: 'Pending' },
      ASSIGNED: { color: '#3b82f6', label: 'Assigned' },
      COMPLETED: { color: '#10b981', label: 'Completed' },
      ACTIVE: { color: '#f59e0b', label: 'Active' },
    }
    const { color, label } = config[state] || { color: '#6b7280', label: state }
    return (
      <Chip
        size="small"
        label={label}
        sx={{
          bgcolor: alpha(color, 0.15),
          color: color,
          fontWeight: 500,
          border: `1px solid ${alpha(color, 0.3)}`,
        }}
      />
    )
  }

  // Filter tasks by search term
  const filteredTasks = searchTerm
    ? tasks.filter(t => 
        t.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.assignee?.toLowerCase().includes(searchTerm.toLowerCase()) ||
        t.processInstanceId.toLowerCase().includes(searchTerm.toLowerCase())
      )
    : tasks

  return (
    <Box>
      {/* Header */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 0.5 }}>
          Tasks
        </Typography>
        <Typography variant="body2" color="text.secondary">
          Manage all pending and completed tasks across all process instances
        </Typography>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
        </Alert>
      )}

      {/* Filters */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', mb: 3, gap: 2 }}>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <TextField
            size="small"
            placeholder="Search tasks..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon sx={{ color: 'text.secondary' }} />
                </InputAdornment>
              ),
            }}
            sx={{ width: 280 }}
          />
          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Status</InputLabel>
            <Select value={stateFilter} label="Status" onChange={handleStateFilterChange}>
              <MenuItem value="">All</MenuItem>
              <MenuItem value="CLAIMABLE">Pending</MenuItem>
              <MenuItem value="ASSIGNED">Assigned</MenuItem>
            </Select>
          </FormControl>
        </Box>
        <IconButton onClick={loadTasks}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {/* Table */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Task Name</TableCell>
                <TableCell>Process</TableCell>
                <TableCell>Assignee</TableCell>
                <TableCell>Created</TableCell>
                <TableCell>Due Date</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                [...Array(5)].map((_, idx) => (
                  <TableRow key={idx}>
                    <TableCell colSpan={7}>
                      <Skeleton variant="rectangular" height={48} sx={{ bgcolor: 'background.default' }} />
                    </TableCell>
                  </TableRow>
                ))
              ) : filteredTasks.length > 0 ? (
                filteredTasks.map((task) => (
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
                      <Typography
                        variant="body2"
                        sx={{
                          color: 'primary.main',
                          cursor: 'pointer',
                          '&:hover': { textDecoration: 'underline' },
                        }}
                        onClick={() => navigate(`/instances/${task.processInstanceId}`)}
                      >
                        {task.processInstanceId.slice(0, 8)}...
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
                        {new Date(task.createTime).toLocaleDateString()}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography
                        variant="body2"
                        sx={{
                          fontFamily: 'monospace',
                          color: task.dueDate && new Date(task.dueDate) < new Date() ? 'error.main' : 'text.primary',
                        }}
                      >
                        {task.dueDate ? new Date(task.dueDate).toLocaleDateString() : '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>{getStatusChip(task.state)}</TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
                        <Tooltip title="View Process">
                          <IconButton
                            size="small"
                            onClick={() => navigate(`/instances/${task.processInstanceId}`)}
                          >
                            <ViewIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        {(task.state === 'CLAIMABLE' || task.state === 'ACTIVE') && (
                          <>
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
                          </>
                        )}
                        {task.state === 'ASSIGNED' && (
                          <Tooltip title="Complete">
                            <IconButton size="small" onClick={() => handleCompleteTask(task.id)}>
                              <CompleteIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        )}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6 }}>
                    <Typography color="text.secondary">No tasks found</Typography>
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

      {/* Assign Dialog */}
      <Dialog open={!!assignDialog?.open} onClose={() => setAssignDialog(null)}>
        <DialogTitle>Assign Task</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Assignee Username"
            fullWidth
            value={assignee}
            onChange={(e) => setAssignee(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setAssignDialog(null)}>Cancel</Button>
          <Button onClick={handleAssignTask} variant="contained">
            Assign
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default Tasks
