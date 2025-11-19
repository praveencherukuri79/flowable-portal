import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Button,
  Paper,
  Alert,
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
  DialogActions,
  Chip
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import AddIcon from '@mui/icons-material/Add'
import EditIcon from '@mui/icons-material/Edit'
import DeleteIcon from '@mui/icons-material/Delete'
import SaveIcon from '@mui/icons-material/Save'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { flowableApi, dataQueryApi } from '../../api/flowableApi'
import { PlanStaging } from '../../api/stagingApi'
import dayjs from 'dayjs'
import { WorkflowDecision, WorkflowDecisionValue } from '../../constants/workflowConstants'

export function PlanEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}

  const [plans, setPlans] = useState<PlanStaging[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingPlan, setEditingPlan] = useState<PlanStaging | null>(null)
  const [existingSheetId, setExistingSheetId] = useState<string | null>(null)

  // Form fields
  const [planName, setPlanName] = useState('')
  const [planType, setPlanType] = useState('')
  const [premium, setPremium] = useState<number>(0)
  const [coverageAmount, setCoverageAmount] = useState<number>(0)
  const [effectiveDate, setEffectiveDate] = useState(dayjs().format('YYYY-MM-DD'))

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId || !formKey) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task. Please go to My Tasks and claim a task first.')
      setTimeout(() => navigate('/maker'), 3000)
      return
    }
    // Check if sheetId already exists for this task
    loadPlans()
  }, [taskId, processInstanceId, formKey, navigate])

  const loadPlans = async () => {
    try {
      setLoading(true)
      
      // Single API call - backend checks Sheet table and returns staging if exists, else master
      const data = await dataQueryApi.getMakerData(processInstanceId, 'plan')
      
      if (data.isExistingSheet) {
        // Sheet exists - load existing staging data (rejection/back navigation case)
        console.log('Loading existing staging data for sheetId:', data.sheetId)
        setExistingSheetId(data.sheetId || '')
        setPlans(data.plans || [])
        if (data.plans && data.plans.length > 0) {
          setSuccessMessage('ℹ️ Loaded existing data. You can edit and resubmit.')
        }
      } else {
        // No sheet exists - load fresh data from MASTER
        console.log('No existing sheet found. Loading MASTER data.')
        const masterData = data.plans || []
        
        // Convert to staging format for editing (sheetId will be created by TaskListener)
        const stagingPlans: PlanStaging[] = masterData.map((plan: any) => ({
          sheetId: '', // Will be set by backend TaskListener
          planName: plan.planName || '',
          planType: plan.planType || '',
          premium: plan.premium || 0,
          coverageAmount: plan.coverageAmount || 0,
          effectiveDate: plan.effectiveDate || '',
          status: 'PENDING'
        }))
        setPlans(stagingPlans)
      }
    } catch (err: any) {
      console.error('Failed to load plans:', err)
      setError('Failed to load plans: ' + (err.message || 'Unknown error'))
      setPlans([])
    } finally {
      setLoading(false)
    }
  }

  const handleOpenDialog = (plan?: PlanStaging) => {
    if (plan) {
      setEditingPlan(plan)
      setPlanName(plan.planName)
      setPlanType(plan.planType)
      setPremium(plan.premium)
      setCoverageAmount(plan.coverageAmount)
      setEffectiveDate(plan.effectiveDate)
    } else {
      setEditingPlan(null)
      setPlanName('')
      setPlanType('')
      setPremium(0)
      setCoverageAmount(0)
      setEffectiveDate(dayjs().format('YYYY-MM-DD'))
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingPlan(null)
  }

  const handleSavePlan = () => {
    const newPlan: PlanStaging = {
      sheetId: '', // Will be set by backend TaskListener
      planName,
      planType,
      premium,
      coverageAmount,
      effectiveDate,
      status: 'PENDING'
    }

    if (editingPlan) {
      setPlans(plans.map(p => p === editingPlan ? newPlan : p))
    } else {
      setPlans([...plans, newPlan])
    }

    handleCloseDialog()
    setSuccessMessage('Plan saved locally. Click "Submit Plans" to save.')
  }

  const handleDeletePlan = (plan: PlanStaging) => {
    setPlans(plans.filter(p => p !== plan))
    setSuccessMessage('Plan removed locally. Click "Submit Plans" to save.')
  }

  const handleBack = async () => {
    try {
      setLoading(true)
      
      // Navigation only - don't send plans data
      await flowableApi.completeTask(taskId, {
        [WorkflowDecision.PLAN]: WorkflowDecisionValue.BACK
      })
      setSuccessMessage('Going back to Items...')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError('Failed to go back: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const handleCompleteTask = async () => {
    try {
      if (plans.length === 0) {
        setError('Please add at least one plan before submitting.')
        return
      }

      setLoading(true)
      setError(null)

      // Strip database IDs - send only business data
      const cleanPlans = plans.map(plan => ({
        planName: plan.planName,
        planType: plan.planType,
        premium: plan.premium,
        coverageAmount: plan.coverageAmount,
        effectiveDate: plan.effectiveDate
      }))

      // Complete task with reason and clean plans
      // IMPORTANT: Set planDecision to FORWARD to override any previous BACK
      await flowableApi.completeTask(taskId, {
        reason: 'submit',
        [WorkflowDecision.PLAN]: WorkflowDecisionValue.FORWARD, // Override any previous BACK decision
        plans: cleanPlans
      })

      setSuccessMessage('Plans submitted successfully! Redirecting...')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError('Failed to submit plans: ' + (err.response?.data?.message || err.message))
      console.error('Error completing task:', err)
    } finally {
      setLoading(false)
    }
  }

  if (!taskId || !processInstanceId || !formKey) {
    return (
      <Box sx={{ p: 3, display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '60vh' }}>
        <Alert severity="error" sx={{ maxWidth: 600, mb: 3 }}>
          <Typography variant="h6" gutterBottom>❌ Unauthorized Access</Typography>
          <Typography>
            This page can only be accessed from a claimed task. Please go to <strong>My Tasks</strong> and claim a task first.
          </Typography>
          <Typography sx={{ mt: 2, fontStyle: 'italic', fontSize: '0.9rem' }}>
            Redirecting to Maker Portal in 3 seconds...
          </Typography>
        </Alert>
      </Box>
    )
  }

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 3 }}>
        <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h5" gutterBottom>
              Stage 2: Edit Plans (Second Stage)
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Task ID: {taskId}
            </Typography>
          </Box>
          {/* Back to previous stage button */}
          <Button
            variant="outlined"
            startIcon={<ArrowBackIcon />}
            onClick={handleBack}
            disabled={loading}
            sx={{ minWidth: 150 }}
          >
            Back to Items
          </Button>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}

        <Box mb={2}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Plan
          </Button>
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Plan Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Premium</TableCell>
                <TableCell>Coverage Amount</TableCell>
                <TableCell>Effective Date</TableCell>
                {existingSheetId && <TableCell>Approval Status</TableCell>}
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {plans.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={existingSheetId ? 7 : 6} align="center">
                    <Typography color="text.secondary">
                      No plans. Click "Add Plan" to start.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                plans.map((plan, index) => (
                  <TableRow key={index}>
                    <TableCell>{plan.planName}</TableCell>
                    <TableCell>{plan.planType}</TableCell>
                    <TableCell>${plan.premium.toFixed(2)}</TableCell>
                    <TableCell>${plan.coverageAmount.toLocaleString()}</TableCell>
                    <TableCell>{plan.effectiveDate}</TableCell>
                    {existingSheetId && (
                      <TableCell>
                        {plan.approved ? (
                          <Chip label={`✓ ${plan.approvedBy}`} color="success" size="small" />
                        ) : (
                          <Chip label="Pending" color="default" size="small" />
                        )}
                      </TableCell>
                    )}
                    <TableCell>
                      <IconButton size="small" onClick={() => handleOpenDialog(plan)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton size="small" onClick={() => handleDeletePlan(plan)}>
                        <DeleteIcon />
                      </IconButton>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <Box mt={3} display="flex" gap={2}>
          <Button
            variant="contained"
            color="primary"
            startIcon={<SaveIcon />}
            onClick={handleCompleteTask}
            disabled={loading || plans.length === 0}
          >
            {loading ? 'Submitting...' : 'Submit Plans'}
          </Button>
        </Box>
      </Paper>

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingPlan ? 'Edit Plan' : 'Add Plan'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="Plan Name"
              value={planName}
              onChange={(e) => setPlanName(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Plan Type"
              value={planType}
              onChange={(e) => setPlanType(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Premium"
              type="number"
              value={premium}
              onChange={(e) => setPremium(Number(e.target.value))}
              fullWidth
              required
            />
            <TextField
              label="Coverage Amount"
              type="number"
              value={coverageAmount}
              onChange={(e) => setCoverageAmount(Number(e.target.value))}
              fullWidth
              required
            />
            <TextField
              label="Effective Date"
              type="date"
              value={effectiveDate}
              onChange={(e) => setEffectiveDate(e.target.value)}
              fullWidth
              required
              InputLabelProps={{ shrink: true }}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button 
            onClick={handleSavePlan} 
            variant="contained"
            disabled={!planName || !planType || premium <= 0 || coverageAmount <= 0}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
