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
import { flowableApi, dataQueryApi } from '../../api/flowableApi'
import { ItemStaging } from '../../api/stagingApi'
import dayjs from 'dayjs'

export function ItemEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}

  const [items, setItems] = useState<ItemStaging[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingItem, setEditingItem] = useState<ItemStaging | null>(null)
  const [existingSheetId, setExistingSheetId] = useState<string | null>(null)

  // Form fields
  const [itemName, setItemName] = useState('')
  const [itemCategory, setItemCategory] = useState('')
  const [price, setPrice] = useState<number>(0)
  const [quantity, setQuantity] = useState<number>(0)
  const [effectiveDate, setEffectiveDate] = useState(dayjs().format('YYYY-MM-DD'))

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId || !formKey) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task. Please go to My Tasks and claim a task first.')
      setTimeout(() => navigate('/maker'), 3000)
      return
    }
    // Check if sheetId already exists for this task
    loadItems()
  }, [taskId, processInstanceId, formKey, navigate])

  const loadItems = async () => {
    try {
      setLoading(true)
      
      // Single API call - backend checks Sheet table and returns staging if exists, else master
      const data = await dataQueryApi.getMakerData(processInstanceId, 'item')
      
      if (data.isExistingSheet) {
        // Sheet exists - load existing staging data (rejection/back navigation case)
        console.log('Loading existing staging data for sheetId:', data.sheetId)
        setExistingSheetId(data.sheetId || '')
        setItems(data.items || [])
        if (data.items && data.items.length > 0) {
          setSuccessMessage('ℹ️ Loaded existing data. You can edit and resubmit.')
        }
      } else {
        // No sheet exists - load fresh data from MASTER
        console.log('No existing sheet found. Loading MASTER data.')
        const masterData = data.items || []
        
        // Convert to staging format for editing (sheetId will be created by TaskListener)
        const stagingItems: ItemStaging[] = masterData.map((item: any) => ({
          sheetId: '', // Will be set by backend TaskListener
          itemName: item.itemName || '',
          itemCategory: item.itemCategory || '',
          price: item.price || 0,
          quantity: item.quantity || 0,
          effectiveDate: item.effectiveDate || '',
          status: 'PENDING'
        }))
        setItems(stagingItems)
      }
    } catch (err: any) {
      console.error('Failed to load items:', err)
      setError('Failed to load items: ' + (err.message || 'Unknown error'))
      setItems([])
    } finally {
      setLoading(false)
    }
  }

  const handleOpenDialog = (item?: ItemStaging) => {
    if (item) {
      setEditingItem(item)
      setItemName(item.itemName)
      setItemCategory(item.itemCategory)
      setPrice(item.price)
      setQuantity(item.quantity)
      setEffectiveDate(item.effectiveDate)
    } else {
      setEditingItem(null)
      setItemName('')
      setItemCategory('')
      setPrice(0)
      setQuantity(0)
      setEffectiveDate(dayjs().format('YYYY-MM-DD'))
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingItem(null)
  }

  const handleSaveItem = () => {
    const newItem: ItemStaging = {
      sheetId: '', // Will be set by backend TaskListener
      itemName,
      itemCategory,
      price,
      quantity,
      effectiveDate,
      status: 'PENDING'
    }

    if (editingItem) {
      // Update existing
      setItems(items.map(i => 
        i === editingItem ? newItem : i
      ))
    } else {
      // Add new
      setItems([...items, newItem])
    }

    handleCloseDialog()
    setSuccessMessage('Item saved locally. Click "Submit Items" to save.')
  }

  const handleDeleteItem = (item: ItemStaging) => {
    setItems(items.filter(i => i !== item))
    setSuccessMessage('Item removed locally. Click "Submit Items" to save.')
  }

  const handleCompleteTask = async () => {
    try {
      if (items.length === 0) {
        setError('Please add at least one item before submitting.')
        return
      }

      setLoading(true)
      setError(null)

      // Strip database IDs - send only business data
      const cleanItems = items.map(item => ({
        itemName: item.itemName,
        itemCategory: item.itemCategory,
        price: item.price,
        quantity: item.quantity,
        effectiveDate: item.effectiveDate
      }))

      // Complete task with reason and clean items
      await flowableApi.completeTask(taskId, {
        reason: 'submit',
        items: cleanItems
      })

      setSuccessMessage('Items submitted successfully! Redirecting...')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError('Failed to submit items: ' + (err.response?.data?.message || err.message))
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
              Stage 3: Edit Items (First Stage)
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Task ID: {taskId}
            </Typography>
          </Box>
          {/* No back button - it's the first stage */}
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}

        <Box mb={2}>
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Item
          </Button>
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Item Name</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Price</TableCell>
                <TableCell>Quantity</TableCell>
                <TableCell>Effective Date</TableCell>
                {existingSheetId && <TableCell>Approval Status</TableCell>}
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {items.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={existingSheetId ? 7 : 6} align="center">
                    <Typography color="text.secondary">
                      No items. Click "Add Item" to start.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                items.map((item, index) => (
                  <TableRow key={index}>
                    <TableCell>{item.itemName}</TableCell>
                    <TableCell>{item.itemCategory}</TableCell>
                    <TableCell>${item.price.toFixed(2)}</TableCell>
                    <TableCell>{item.quantity}</TableCell>
                    <TableCell>{item.effectiveDate}</TableCell>
                    {existingSheetId && (
                      <TableCell>
                        {item.approved ? (
                          <Chip label={`✓ ${item.approvedBy}`} color="success" size="small" />
                        ) : (
                          <Chip label="Pending" color="default" size="small" />
                        )}
                      </TableCell>
                    )}
                    <TableCell>
                      <IconButton size="small" onClick={() => handleOpenDialog(item)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton size="small" onClick={() => handleDeleteItem(item)}>
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
            disabled={loading || items.length === 0}
          >
            {loading ? 'Submitting...' : 'Submit Items'}
          </Button>
        </Box>
      </Paper>

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingItem ? 'Edit Item' : 'Add Item'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="Item Name"
              value={itemName}
              onChange={(e) => setItemName(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Category"
              value={itemCategory}
              onChange={(e) => setItemCategory(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Price"
              type="number"
              value={price}
              onChange={(e) => setPrice(Number(e.target.value))}
              fullWidth
              required
            />
            <TextField
              label="Quantity"
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
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
            onClick={handleSaveItem} 
            variant="contained"
            disabled={!itemName || !itemCategory || price <= 0 || quantity <= 0}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
