import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Button,
  Paper,
  Alert,
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Checkbox,
  Chip
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import ArrowBackIcon from '@mui/icons-material/ArrowBack'
import { flowableApi, dataQueryApi } from '../../api/flowableApi'
import { stagingApi, PlanStaging } from '../../api/stagingApi'
import { useRecoilValue } from 'recoil'
import { authState } from '../../state/auth'

export function PlanApproval() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}
  const auth = useRecoilValue(authState)

  const [plans, setPlans] = useState<PlanStaging[]>([])
  const [selectedPlans, setSelectedPlans] = useState<Set<number>>(new Set())
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [sheetId, setSheetId] = useState<string>('')
  const [allRowsApproved, setAllRowsApproved] = useState(false)
  const [sheetApproved, setSheetApproved] = useState(false)

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId || !formKey) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task. Please go to Pending Approvals and claim a task first.')
      setTimeout(() => navigate('/checker'), 3000)
      return
    }
    loadApprovalData()
  }, [taskId, processInstanceId, formKey, navigate])

  const loadApprovalData = async () => {
    try {
      setLoading(true)
      
      // Single API call gets everything: sheetId, plans, and sheet status
      const data = await dataQueryApi.getApprovalData(processInstanceId, 'plan')
      
      setSheetId(data.sheetId)
      setPlans(data.plans || [])
      setSheetApproved(!!data.sheet.approvedAt)
      
      // Check if all rows are approved
      const allApproved = data.plans && data.plans.length > 0 && data.plans.every(p => p.approved === true)
      setAllRowsApproved(!!allApproved)
      
      console.log('✓ Loaded approval data:', data)
      
    } catch (err: any) {
      console.error('Failed to load approval data:', err)
      setError('Failed to load data: ' + (err.response?.data?.message || err.message || 'Unknown error'))
    } finally {
      setLoading(false)
    }
  }

  const loadPlans = async (sid: string) => {
    // Reload data after approvals
    try {
      const data = await stagingApi.getPlansStaging(sid)
      setPlans(data)
      
      // Check if all rows are approved
      const allApproved = data.length > 0 && data.every(p => p.approved === true)
      setAllRowsApproved(allApproved)
      
      // Load sheet status
      const sheet = await stagingApi.getSheet(sid)
      setSheetApproved(!!sheet.approvedAt)
    } catch (err) {
      console.error('Failed to reload plans:', err)
    }
  }

  const handleTogglePlan = (id: number) => {
    const newSelected = new Set(selectedPlans)
    if (newSelected.has(id)) {
      newSelected.delete(id)
    } else {
      newSelected.add(id)
    }
    setSelectedPlans(newSelected)
  }

  const handleApproveSelected = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      for (const id of selectedPlans) {
        await stagingApi.approvePlanIndividual(id, approverUsername)
      }

      setSuccessMessage(`Approved ${selectedPlans.size} plan(s) individually`)
      setSelectedPlans(new Set())
      await loadPlans(sheetId)
    } catch (err: any) {
      setError('Failed to approve plans: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleApproveAll = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      await stagingApi.approvePlansBulk(sheetId, approverUsername)

      setSuccessMessage('All plans approved in bulk')
      await loadPlans(sheetId)
    } catch (err: any) {
      setError('Failed to bulk approve: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleBack = async () => {
    try {
      setLoading(true)
      await flowableApi.completeTask(taskId, {
        stage2Decision: 'BACK'
      })
      setSuccessMessage('Going back to Items...')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err: any) {
      setError('Failed to go back: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleApproveSheet = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      // Backend will approve sheet AND complete task
      await stagingApi.approveSheet(sheetId, approverUsername, taskId, 'stage2Decision')
      
      setSuccessMessage('Sheet approved and task completed! Redirecting...')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err: any) {
      setError('Failed to approve and complete: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleReject = async () => {
    try {
      setLoading(true)
      await flowableApi.completeTask(taskId, {
        stage2Decision: 'REJECT'
      })
      setSuccessMessage('Plans rejected! Sending back to maker...')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err: any) {
      setError('Failed to reject: ' + err.message)
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
            This page can only be accessed from a claimed task. Please go to <strong>Pending Approvals</strong> and claim a task first.
          </Typography>
          <Typography sx={{ mt: 2, fontStyle: 'italic', fontSize: '0.9rem' }}>
            Redirecting to Checker Portal in 3 seconds...
          </Typography>
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
      <Paper sx={{ p: 3 }}>
        <Box mb={3} display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography variant="h5" gutterBottom>
              Stage 2: Approve Plans (Second Stage)
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sheet ID: {sheetId}
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

        <Box mb={2} display="flex" gap={2}>
          <Button
            variant="outlined"
            onClick={handleApproveSelected}
            disabled={selectedPlans.size === 0 || loading}
          >
            Approve Selected ({selectedPlans.size})
          </Button>
          <Button
            variant="outlined"
            color="primary"
            onClick={handleApproveAll}
            disabled={loading}
          >
            Approve All
          </Button>
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell padding="checkbox">
                  <Checkbox 
                    checked={selectedPlans.size === plans.length && plans.length > 0}
                    onChange={() => {
                      if (selectedPlans.size === plans.length) {
                        setSelectedPlans(new Set())
                      } else {
                        setSelectedPlans(new Set(plans.map(p => p.id!)))
                      }
                    }}
                  />
                </TableCell>
                <TableCell>Plan Name</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Premium</TableCell>
                <TableCell>Coverage Amount</TableCell>
                <TableCell>Effective Date</TableCell>
                <TableCell>Approval Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {plans.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary">
                      No plans to approve
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                plans.filter(plan => plan.id !== undefined).map((plan) => (
                  <TableRow key={plan.id}>
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedPlans.has(plan.id as number)}
                        onChange={() => handleTogglePlan(plan.id as number)}
                      />
                    </TableCell>
                    <TableCell>{plan.planName || 'N/A'}</TableCell>
                    <TableCell>{plan.planType || 'N/A'}</TableCell>
                    <TableCell>${(plan.premium || 0).toFixed(2)}</TableCell>
                    <TableCell>${(plan.coverageAmount || 0).toLocaleString()}</TableCell>
                    <TableCell>{plan.effectiveDate || 'N/A'}</TableCell>
                    <TableCell>
                      {plan.approved ? (
                        <Chip 
                          label={`Approved by ${plan.approvedBy || 'N/A'}`} 
                          color="success" 
                          size="small" 
                        />
                      ) : (
                        <Chip label="Pending" color="default" size="small" />
                      )}
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>

        <Box mt={3} display="flex" gap={2} flexDirection="column">
          {/* Row Approval Status */}
          {allRowsApproved && !sheetApproved && (
            <Alert severity="info">
              ✅ All rows are approved! You can now approve the entire sheet.
            </Alert>
          )}
          {sheetApproved && (
            <Alert severity="success">
              ✅ Sheet has been approved and task completed!
            </Alert>
          )}
          
          {/* Action Buttons */}
          <Box display="flex" gap={2}>
            <Button
              variant="outlined"
              color="error"
              onClick={handleReject}
              disabled={loading}
            >
              Reject Plans (Send Back to Maker)
            </Button>
            <Button
              variant="contained"
              color="primary"
              onClick={handleApproveSheet}
              disabled={loading || !allRowsApproved}
            >
              Approve Sheet & Complete Task
            </Button>
          </Box>
        </Box>
      </Paper>
    </Box>
  )
}
