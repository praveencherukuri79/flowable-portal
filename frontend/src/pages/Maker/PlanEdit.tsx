import React, { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Button,
  Paper,
  Alert,
  CircularProgress,
  IconButton,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SaveIcon from '@mui/icons-material/Save'
import { flowableApi, dataQueryApi, Plan } from '../../api/flowableApi'

export function PlanEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId } = location.state || {}

  const [plans, setPlans] = useState<Plan[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingPlan, setEditingPlan] = useState<Plan | null>(null)
  const [sheetId, setSheetId] = useState<string>('')

  const [planCode, setPlanCode] = useState('')
  const [planName, setPlanName] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    if (taskId && processInstanceId) {
      loadTaskVariables()
    }
  }, [taskId, processInstanceId])

  useEffect(() => {
    if (sheetId) {
      loadPlans()
    }
  }, [sheetId])

  const loadTaskVariables = async () => {
    try {
      setLoading(true)
      const response = await flowableApi.getTaskVariables(taskId)
      const sid = response.sheetId as string
      setSheetId(sid)
    } catch (err) {
      setError('Failed to load task details')
      console.error('Error:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadPlans = async () => {
    try {
      setLoading(true)
      const data = await dataQueryApi.getPlansBySheet(sheetId)
      setPlans(data)
    } catch (err) {
      setError('Failed to load plans')
      console.error('Error:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleOpenDialog = (plan?: Plan) => {
    if (plan) {
      setEditingPlan(plan)
      setPlanCode(plan.planCode)
      setPlanName(plan.planName)
      setDescription(plan.description || '')
    } else {
      setEditingPlan(null)
      setPlanCode('')
      setPlanName('')
      setDescription('')
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingPlan(null)
    setPlanCode('')
    setPlanName('')
    setDescription('')
  }

  const handleSave = async () => {
    try {
      const planData: Plan = {
        sheetId,
        planCode,
        planName,
        description,
        status: 'PENDING'
      }

      // Add to local state (will be saved by task listener on complete)
      if (editingPlan?.id) {
        setPlans(plans.map(p => p.id === editingPlan.id ? { ...planData, id: editingPlan.id } : p))
        setSuccessMessage('Plan updated')
      } else {
        setPlans([...plans, { ...planData, id: Date.now() }]) // Temp ID
        setSuccessMessage('Plan added')
      }

      handleCloseDialog()
    } catch (err) {
      setError('Failed to save plan')
      console.error('Error:', err)
    }
  }

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this plan?')) {
      setPlans(plans.filter(p => p.id !== id))
      setSuccessMessage('Plan removed')
    }
  }

  const handleComplete = async () => {
    if (plans.length === 0) {
      setError('Please add at least one plan before completing')
      return
    }

    try {
      // Pass plans as generic data - task listener will save to DB
      await flowableApi.completeTask(taskId, { 
        sheetId,
        plans: plans
      })
      setSuccessMessage('Task completed successfully')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err) {
      setError('Failed to complete task')
      console.error('Error:', err)
    }
  }

  if (!taskId || !processInstanceId) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">
          This page can only be accessed via a claimed task. Please go to "My Tasks" and claim a task.
        </Alert>
      </Box>
    )
  }

  if (loading && !sheetId) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Edit Plans - Stage 2
      </Typography>
      
      {error && (
        <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
          {error}
        </Alert>
      )}
      
      {successMessage && (
        <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage(null)}>
          {successMessage}
        </Alert>
      )}

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="body2" color="text.secondary">
          Sheet ID: {sheetId}
        </Typography>
      </Paper>

      <Box sx={{ mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => handleOpenDialog()}
        >
          Add Plan
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Plan Code</TableCell>
              <TableCell>Plan Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {plans.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  No plans found. Click "Add Plan" to create one.
                </TableCell>
              </TableRow>
            ) : (
              plans.map((plan) => (
                <TableRow key={plan.id}>
                  <TableCell>{plan.planCode}</TableCell>
                  <TableCell>{plan.planName}</TableCell>
                  <TableCell>{plan.description}</TableCell>
                  <TableCell>{plan.status}</TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => handleOpenDialog(plan)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(plan.id!)}>
                      <DeleteIcon />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Box sx={{ mt: 3, display: 'flex', gap: 2 }}>
        <Button
          variant="contained"
          color="primary"
          startIcon={<SaveIcon />}
          onClick={handleComplete}
          disabled={loading}
        >
          Complete Task
        </Button>
        <Button
          variant="outlined"
          onClick={() => navigate('/maker')}
        >
          Cancel
        </Button>
      </Box>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingPlan ? 'Edit Plan' : 'Add Plan'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Plan Code"
            value={planCode}
            onChange={(e) => setPlanCode(e.target.value)}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Plan Name"
            value={planName}
            onChange={(e) => setPlanName(e.target.value)}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Description"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            margin="normal"
            multiline
            rows={3}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button
            onClick={handleSave}
            variant="contained"
            disabled={!planCode || !planName}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

