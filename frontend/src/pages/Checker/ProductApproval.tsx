import React, { useState, useEffect } from 'react'
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
  TextField
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import CancelIcon from '@mui/icons-material/Cancel'
import { flowableApi, dataQueryApi, Product } from '../../api/flowableApi'
import dayjs from 'dayjs'

export function ProductApproval() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId } = location.state || {}

  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [sheetId, setSheetId] = useState<string>('')
  const [comments, setComments] = useState('')
  const [makerComments, setMakerComments] = useState<string>('')
  const [rejectionHistory, setRejectionHistory] = useState<string>('')

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task. Please go to Pending Approvals and claim a task first.')
      setTimeout(() => navigate('/checker'), 3000)
      return
    }
    loadTaskVariables()
  }, [taskId, processInstanceId, navigate])

  useEffect(() => {
    if (sheetId) {
      loadProducts()
    }
  }, [sheetId])

  const loadTaskVariables = async () => {
    try {
      setLoading(true)
      const response = await flowableApi.getTaskVariables(taskId)
      console.log('Task variables:', response)
      
      const sid = response.sheetId as string
      console.log('Extracted sheetId:', sid)
      
      if (!sid) {
        setError('Sheet ID not found in task variables. This task may not have been started correctly.')
        return
      }
      
      setSheetId(sid)
      
      // Load previous rejection comments if any
      if (response.stage1RejectionComments) {
        setRejectionHistory(response.stage1RejectionComments as string)
      }
      
      // Load maker's comments
      if (response.makerComments) {
        setMakerComments(response.makerComments as string)
      }
    } catch (err: any) {
      if (err?.response?.status === 404 || err?.message?.includes("doesn't exist")) {
        setError('This task no longer exists. It may have been completed or cancelled. Please refresh the task list.')
        setTimeout(() => navigate('/checker'), 3000)
      } else {
        setError('Failed to load task details: ' + (err?.response?.data?.message || err?.message))
      }
      console.error('Error loading task variables:', err)
    } finally {
      setLoading(false)
    }
  }

  const loadProducts = async () => {
    if (!sheetId) {
      console.warn('Cannot load products: sheetId is empty')
      return
    }
    
    try {
      setLoading(true)
      console.log('Loading products for sheetId:', sheetId)
      const data = await dataQueryApi.getProductsBySheet(sheetId)
      console.log('Loaded products:', data)
      setProducts(data)
    } catch (err) {
      setError('Failed to load products')
      console.error('Error loading products:', err)
    } finally {
      setLoading(false)
    }
  }

  const handleApprove = async () => {
    try {
      await flowableApi.completeTask(taskId, {
        checkerComments: comments,
        checkerDecision: 'APPROVE',
        approved: true,
        stage1Decision: 'APPROVE'
      })
      setSuccessMessage('Products approved successfully')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err) {
      setError('Failed to approve products')
      console.error('Error:', err)
    }
  }

  const handleReject = async () => {
    if (!comments.trim()) {
      setError('Please provide rejection comments explaining what needs to be fixed')
      return
    }

    try {
      await flowableApi.completeTask(taskId, {
        checkerComments: comments,
        checkerDecision: 'REJECT',
        approved: false,
        stage1Decision: 'REJECT',
        stage1RejectionComments: comments, // Store for maker to see
        rejectedAt: new Date().toISOString()
      })
      setSuccessMessage('Products rejected - Sent back to Maker for corrections')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err) {
      setError('Failed to reject products')
      console.error('Error:', err)
    }
  }

  if (!taskId || !processInstanceId) {
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
      <Typography variant="h4" gutterBottom>
        Approve Products - Stage 1
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
        <Typography variant="body2" color="text.secondary" gutterBottom>
          <strong>Sheet ID:</strong> {sheetId}
        </Typography>
        
        {makerComments && (
          <Box sx={{ mt: 2, p: 2, bgcolor: 'info.light', borderRadius: 1 }}>
            <Typography variant="subtitle2" gutterBottom>
              Maker's Comments:
            </Typography>
            <Typography variant="body2">
              {makerComments}
            </Typography>
          </Box>
        )}
        
        {rejectionHistory && (
          <Box sx={{ mt: 2, p: 2, bgcolor: 'warning.light', borderRadius: 1 }}>
            <Typography variant="subtitle2" gutterBottom color="error">
              Previous Rejection Comments:
            </Typography>
            <Typography variant="body2">
              {rejectionHistory}
            </Typography>
          </Box>
        )}
      </Paper>

      <TableContainer component={Paper} sx={{ mb: 3 }}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Product Name</TableCell>
              <TableCell>Rate</TableCell>
              <TableCell>API</TableCell>
              <TableCell>Effective Date</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Edited By</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {products.length === 0 ? (
              <TableRow>
                <TableCell colSpan={6} align="center">
                  No products found
                </TableCell>
              </TableRow>
            ) : (
              products.map((product) => (
                <TableRow key={product.id}>
                  <TableCell>{product.productName}</TableCell>
                  <TableCell>{product.rate}</TableCell>
                  <TableCell>{product.api}</TableCell>
                  <TableCell>{dayjs(product.effectiveDate).format('YYYY-MM-DD')}</TableCell>
                  <TableCell>{product.status}</TableCell>
                  <TableCell>{product.editedBy}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      <Paper sx={{ p: 2, mb: 3 }}>
        <TextField
          fullWidth
          label="Checker Comments"
          value={comments}
          onChange={(e) => setComments(e.target.value)}
          multiline
          rows={4}
          placeholder="Provide feedback here. For rejection, clearly explain what needs to be corrected."
          helperText="For rejection: Specify which products need changes and what corrections are required"
          required
        />
      </Paper>

      <Box sx={{ display: 'flex', gap: 2 }}>
        <Button
          variant="contained"
          color="success"
          startIcon={<CheckCircleIcon />}
          onClick={handleApprove}
          disabled={loading}
        >
          Approve
        </Button>
        <Button
          variant="contained"
          color="error"
          startIcon={<CancelIcon />}
          onClick={handleReject}
          disabled={loading || !comments.trim()}
        >
          Reject
        </Button>
        <Button
          variant="outlined"
          onClick={() => navigate('/checker')}
        >
          Cancel
        </Button>
      </Box>
    </Box>
  )
}

