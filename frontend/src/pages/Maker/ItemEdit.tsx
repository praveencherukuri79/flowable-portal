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
import { flowableApi, dataQueryApi, Item } from '../../api/flowableApi'

export function ItemEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId } = location.state || {}

  const [items, setItems] = useState<Item[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingItem, setEditingItem] = useState<Item | null>(null)
  const [sheetId, setSheetId] = useState<string>('')

  const [itemCode, setItemCode] = useState('')
  const [itemName, setItemName] = useState('')
  const [description, setDescription] = useState('')

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

  const handleOpenDialog = (item?: Item) => {
    if (item) {
      setEditingItem(item)
      setItemCode(item.itemCode)
      setItemName(item.itemName)
      setDescription(item.description || '')
    } else {
      setEditingItem(null)
      setItemCode('')
      setItemName('')
      setDescription('')
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingItem(null)
    setItemCode('')
    setItemName('')
    setDescription('')
  }

  const handleSave = async () => {
    try {
      const itemData: Item = {
        sheetId,
        itemCode,
        itemName,
        description,
        status: 'PENDING'
      }

      // Add to local state (will be saved by task listener on complete)
      if (editingItem?.id) {
        setItems(items.map(i => i.id === editingItem.id ? { ...itemData, id: editingItem.id } : i))
        setSuccessMessage('Item updated')
      } else {
        setItems([...items, { ...itemData, id: Date.now() }]) // Temp ID
        setSuccessMessage('Item added')
      }

      handleCloseDialog()
    } catch (err) {
      setError('Failed to save item')
      console.error('Error:', err)
    }
  }

  const handleDelete = async (id: number) => {
    if (window.confirm('Are you sure you want to delete this item?')) {
      setItems(items.filter(i => i.id !== id))
      setSuccessMessage('Item removed')
    }
  }

  const handleComplete = async () => {
    if (items.length === 0) {
      setError('Please add at least one item before completing')
      return
    }

    try {
      // Pass items as generic data - task listener will save to DB
      await flowableApi.completeTask(taskId, { 
        sheetId,
        items: items
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
        Edit Items - Stage 3
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
          Add Item
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Item Code</TableCell>
              <TableCell>Item Name</TableCell>
              <TableCell>Description</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {items.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  No items found. Click "Add Item" to create one.
                </TableCell>
              </TableRow>
            ) : (
              items.map((item) => (
                <TableRow key={item.id}>
                  <TableCell>{item.itemCode}</TableCell>
                  <TableCell>{item.itemName}</TableCell>
                  <TableCell>{item.description}</TableCell>
                  <TableCell>{item.status}</TableCell>
                  <TableCell>
                    <IconButton size="small" onClick={() => handleOpenDialog(item)}>
                      <EditIcon />
                    </IconButton>
                    <IconButton size="small" onClick={() => handleDelete(item.id!)}>
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
        <DialogTitle>{editingItem ? 'Edit Item' : 'Add Item'}</DialogTitle>
        <DialogContent>
          <TextField
            fullWidth
            label="Item Code"
            value={itemCode}
            onChange={(e) => setItemCode(e.target.value)}
            margin="normal"
            required
          />
          <TextField
            fullWidth
            label="Item Name"
            value={itemName}
            onChange={(e) => setItemName(e.target.value)}
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
            disabled={!itemCode || !itemName}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

