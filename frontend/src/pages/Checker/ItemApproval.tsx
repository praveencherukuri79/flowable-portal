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
import { flowableApi, dataQueryApi, Item } from '../../api/flowableApi'

export function ItemApproval() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId } = location.state || {}

  const [items, setItems] = useState<Item[]>([])
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
      loadItems()
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

  const loadItems = async () => {
    try {
      setLoading(true)
      const data = await dataQueryApi.getItemsBySheet(sheetId)
      setItems(data)
    } catch (err) {
      setError('Failed to load items')
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
        stage3Decision: 'APPROVE'
      })
      setSuccessMessage('Items approved successfully')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err) {
      setError('Failed to approve items')
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
        stage3Decision: 'REJECT'
      })
      setSuccessMessage('Items rejected successfully')
      setTimeout(() => navigate('/checker'), 2000)
    } catch (err) {
      setError('Failed to reject items')
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
        Approve Items - Stage 3
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
              <TableCell>Item Code</TableCell>
              <TableCell>Item Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Edited By</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  No items found
                </TableCell>
              </TableRow>
            ) : (
              items.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.itemCode}</TableCell>
                  <TableCell>{item.itemName}</TableCell>
                  <TableCell>{item.description}</TableCell>
                  <TableCell>{item.status}</TableCell>
                  <TableCell>{item.editedBy}</TableCell>
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

