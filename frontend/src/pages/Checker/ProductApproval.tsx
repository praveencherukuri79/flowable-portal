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

  useEffect(() => {
    if (taskId && processInstanceId) {
      loadTaskVariables()
    }
  }, [taskId, processInstanceId])

  useEffect(() => {
    if (sheetId) {
      loadProducts()
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

  const loadProducts = async () => {
    try {
      setLoading(true)
      const data = await dataQueryApi.getProductsBySheet(sheetId)
      setProducts(data)
    } catch (err) {
      setError('Failed to load products')
      console.error('Error:', err)
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
      setError('Please provide rejection comments')
      return
    }

    try {
      await flowableApi.completeTask(taskId, {
        checkerComments: comments,
        checkerDecision: 'REJECT',
        approved: false,
        stage1Decision: 'REJECT'
      })
      setSuccessMessage('Products rejected successfully')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err) {
      setError('Failed to reject products')
      console.error('Error:', err)
    }
  }

  if (!taskId || !processInstanceId) {
    return (
      <Box sx={{ p: 3 }}>
        <Alert severity="warning">
          This page can only be accessed via a claimed task. Please go to "Pending Approvals" and claim a task.
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
        <Typography variant="body2" color="text.secondary">
          Sheet ID: {sheetId}
        </Typography>
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
          placeholder="Add your comments (required for rejection)"
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

