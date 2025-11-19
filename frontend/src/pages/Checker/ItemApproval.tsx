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
import { flowableApi, dataQueryApi } from '../../api/flowableApi'
import { stagingApi, ItemStaging } from '../../api/stagingApi'
import { useRecoilValue } from 'recoil'
import { authState } from '../../state/auth'
import { WorkflowDecision, WorkflowDecisionValue } from '../../constants/workflowConstants'

export function ItemApproval() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}
  const auth = useRecoilValue(authState)

  const [items, setItems] = useState<ItemStaging[]>([])
  const [selectedItems, setSelectedItems] = useState<Set<number>>(new Set())
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
      
      // Single API call gets everything: sheetId, items, and sheet status
      const data = await dataQueryApi.getApprovalData(processInstanceId, 'item')
      
      setSheetId(data.sheetId)
      setItems(data.items || [])
      setSheetApproved(!!data.sheet.approvedAt)
      
      // Check if all rows are approved
      const allApproved = data.items && data.items.length > 0 && data.items.every(i => i.approved === true)
      setAllRowsApproved(!!allApproved)
      
      console.log('✓ Loaded approval data:', data)
      
    } catch (err: any) {
      console.error('Failed to load approval data:', err)
      setError('Failed to load data: ' + (err.response?.data?.message || err.message || 'Unknown error'))
    } finally {
      setLoading(false)
    }
  }

  const loadItems = async (sid: string) => {
    // Reload data after approvals
    try {
      const data = await stagingApi.getItemsStaging(sid)
      setItems(data)
      
      // Check if all rows are approved
      const allApproved = data.length > 0 && data.every(i => i.approved === true)
      setAllRowsApproved(allApproved)
      
      // Load sheet status
      const sheet = await stagingApi.getSheet(sid)
      setSheetApproved(!!sheet.approvedAt)
    } catch (err) {
      console.error('Failed to reload items:', err)
    }
  }

  const handleToggleItem = (id: number) => {
    const newSelected = new Set(selectedItems)
    if (newSelected.has(id)) {
      newSelected.delete(id)
    } else {
      newSelected.add(id)
    }
    setSelectedItems(newSelected)
  }

  const handleApproveSelected = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      // Approve each selected item individually
      for (const id of selectedItems) {
        await stagingApi.approveItemIndividual(id, approverUsername)
      }

      setSuccessMessage(`Approved ${selectedItems.size} item(s) individually`)
      setSelectedItems(new Set())
      await loadItems(sheetId) // Reload to show updated status
    } catch (err: any) {
      setError('Failed to approve items: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleApproveAll = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      // Bulk approve all items for this sheet
      await stagingApi.approveItemsBulk(sheetId, approverUsername)

      setSuccessMessage('All items approved in bulk')
      await loadItems(sheetId) // Reload to show updated status
    } catch (err: any) {
      setError('Failed to bulk approve: ' + err.message)
    } finally {
      setLoading(false)
    }
  }

  const handleApproveSheet = async () => {
    try {
      setLoading(true)
      const approverUsername = auth.username || 'checker'
      
      // Backend will approve sheet AND complete task
      await stagingApi.approveSheet(sheetId, approverUsername, taskId, WorkflowDecision.ITEM)
      
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
      // Complete task with reject decision (sends back to maker)
      await flowableApi.completeTask(taskId, {
        [WorkflowDecision.ITEM]: WorkflowDecisionValue.REJECT
      })
      setSuccessMessage('Items rejected! Sending back to maker...')
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
              Stage 3: Approve Items (First Stage)
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Sheet ID: {sheetId}
            </Typography>
          </Box>
          {/* No back button for Items - it's the first stage */}
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}

        <Box mb={2} display="flex" gap={2}>
          <Button
            variant="outlined"
            onClick={handleApproveSelected}
            disabled={selectedItems.size === 0 || loading}
          >
            Approve Selected ({selectedItems.size})
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
                    checked={selectedItems.size === items.length && items.length > 0}
                    onChange={() => {
                      if (selectedItems.size === items.length) {
                        setSelectedItems(new Set())
                      } else {
                        setSelectedItems(new Set(items.map(i => i.id!)))
                      }
                    }}
                  />
                </TableCell>
                <TableCell>Item Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Price</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Effective Date</TableCell>
                <TableCell>Approval Status</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography color="text.secondary">
                      No items to approve
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                items.filter(item => item.id !== undefined).map((item) => (
                  <TableRow key={item.id}>
                    <TableCell padding="checkbox">
                      <Checkbox
                        checked={selectedItems.has(item.id as number)}
                        onChange={() => handleToggleItem(item.id as number)}
                      />
                    </TableCell>
                    <TableCell>{item.itemName || 'N/A'}</TableCell>
                    <TableCell>{item.itemCategory || 'N/A'}</TableCell>
                    <TableCell>${(item.price || 0).toFixed(2)}</TableCell>
                    <TableCell>{item.quantity || 0}</TableCell>
                    <TableCell>{item.effectiveDate || 'N/A'}</TableCell>
                    <TableCell>
                      {item.approved ? (
                        <Chip 
                          label={`Approved by ${item.approvedBy || 'N/A'}`} 
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
              Reject Items (Send Back to Maker)
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
