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
import { ProductStaging } from '../../api/stagingApi'
import dayjs from 'dayjs'
import { WorkflowDecision, WorkflowDecisionValue } from '../../constants/workflowConstants'

export function ProductEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}

  const [products, setProducts] = useState<ProductStaging[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingProduct, setEditingProduct] = useState<ProductStaging | null>(null)
  const [existingSheetId, setExistingSheetId] = useState<string | null>(null)

  // Form fields
  const [productName, setProductName] = useState('')
  const [rate, setRate] = useState<number>(0)
  const [api, setApi] = useState('')
  const [effectiveDate, setEffectiveDate] = useState(dayjs().format('YYYY-MM-DD'))

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId || !formKey) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task. Please go to My Tasks and claim a task first.')
      setTimeout(() => navigate('/maker'), 3000)
      return
    }
    // Check if sheetId already exists for this task
    loadProducts()
  }, [taskId, processInstanceId, formKey, navigate])

  const loadProducts = async () => {
    try {
      setLoading(true)
      
      // Single API call - backend checks Sheet table and returns staging if exists, else master
      const data = await dataQueryApi.getMakerData(processInstanceId, 'product')
      
      if (data.isExistingSheet) {
        // Sheet exists - load existing staging data (rejection/back navigation case)
        console.log('Loading existing staging data for sheetId:', data.sheetId)
        setExistingSheetId(data.sheetId || '')
        setProducts(data.products || [])
        if (data.products && data.products.length > 0) {
          setSuccessMessage('ℹ️ Loaded existing data. You can edit and resubmit.')
        }
      } else {
        // No sheet exists - load fresh data from MASTER
        console.log('No existing sheet found. Loading MASTER data.')
        const masterData = data.products || []
        
        // Convert to staging format for editing (sheetId will be created by TaskListener)
        const stagingProducts: ProductStaging[] = masterData.map((product: any) => ({
          sheetId: '', // Will be set by backend TaskListener
          productName: product.productName || '',
          rate: product.rate || 0,
          api: product.api || '',
          effectiveDate: product.effectiveDate || '',
          status: 'PENDING'
        }))
        setProducts(stagingProducts)
      }
    } catch (err: any) {
      console.error('Failed to load products:', err)
      setError('Failed to load products: ' + (err.message || 'Unknown error'))
      setProducts([])
    } finally {
      setLoading(false)
    }
  }

  const handleOpenDialog = (product?: ProductStaging) => {
    if (product) {
      setEditingProduct(product)
      setProductName(product.productName)
      setRate(product.rate)
      setApi(product.api)
      setEffectiveDate(product.effectiveDate)
    } else {
      setEditingProduct(null)
      setProductName('')
      setRate(0)
      setApi('')
      setEffectiveDate(dayjs().format('YYYY-MM-DD'))
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingProduct(null)
  }

  const handleSaveProduct = () => {
    const newProduct: ProductStaging = {
      sheetId: '', // Will be set by backend TaskListener
      productName,
      rate,
      api,
      effectiveDate,
      status: 'PENDING'
    }

    if (editingProduct) {
      setProducts(products.map(p => p === editingProduct ? newProduct : p))
    } else {
      setProducts([...products, newProduct])
    }

    handleCloseDialog()
    setSuccessMessage('Product saved locally. Click "Submit Products" to save.')
  }

  const handleDeleteProduct = (product: ProductStaging) => {
    setProducts(products.filter(p => p !== product))
    setSuccessMessage('Product removed locally. Click "Submit Products" to save.')
  }

  const handleBack = async () => {
    try {
      setLoading(true)
      
      // Navigation only - don't send products data
      await flowableApi.completeTask(taskId, {
        [WorkflowDecision.PRODUCT]: WorkflowDecisionValue.BACK
      })
      setSuccessMessage('Going back to Plans...')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError('Failed to go back: ' + (err.response?.data?.message || err.message))
    } finally {
      setLoading(false)
    }
  }

  const handleCompleteTask = async () => {
    try {
      if (products.length === 0) {
        setError('Please add at least one product before submitting.')
        return
      }

      setLoading(true)
      setError(null)

      // Strip database IDs - send only business data
      const cleanProducts = products.map(product => ({
        productName: product.productName,
        rate: product.rate,
        api: product.api,
        effectiveDate: product.effectiveDate
      }))

      // Complete task with reason and clean products
      // IMPORTANT: Set productDecision to FORWARD to override any previous BACK
      await flowableApi.completeTask(taskId, {
        reason: 'submit',
        [WorkflowDecision.PRODUCT]: WorkflowDecisionValue.FORWARD, // Override any previous BACK decision
        products: cleanProducts
      })

      setSuccessMessage('Products submitted successfully! Redirecting...')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError('Failed to submit products: ' + (err.response?.data?.message || err.message))
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
              Stage 1: Edit Products (Final Stage)
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
            Back to Plans
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
            Add Product
          </Button>
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Product Name</TableCell>
                <TableCell>Rate</TableCell>
                <TableCell>API</TableCell>
                <TableCell>Effective Date</TableCell>
                {existingSheetId && <TableCell>Approval Status</TableCell>}
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {products.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={existingSheetId ? 6 : 5} align="center">
                    <Typography color="text.secondary">
                      No products. Click "Add Product" to start.
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                products.map((product, index) => (
                  <TableRow key={index}>
                    <TableCell>{product.productName}</TableCell>
                    <TableCell>${product.rate.toFixed(2)}</TableCell>
                    <TableCell>{product.api}</TableCell>
                    <TableCell>{product.effectiveDate}</TableCell>
                    {existingSheetId && (
                      <TableCell>
                        {product.approved ? (
                          <Chip label={`✓ ${product.approvedBy}`} color="success" size="small" />
                        ) : (
                          <Chip label="Pending" color="default" size="small" />
                        )}
                      </TableCell>
                    )}
                    <TableCell>
                      <IconButton size="small" onClick={() => handleOpenDialog(product)}>
                        <EditIcon />
                      </IconButton>
                      <IconButton size="small" onClick={() => handleDeleteProduct(product)}>
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
            disabled={loading || products.length === 0}
          >
            {loading ? 'Submitting...' : 'Submit Products'}
          </Button>
        </Box>
      </Paper>

      {/* Add/Edit Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingProduct ? 'Edit Product' : 'Add Product'}</DialogTitle>
        <DialogContent>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2, mt: 2 }}>
            <TextField
              label="Product Name"
              value={productName}
              onChange={(e) => setProductName(e.target.value)}
              fullWidth
              required
            />
            <TextField
              label="Rate"
              type="number"
              value={rate}
              onChange={(e) => setRate(Number(e.target.value))}
              fullWidth
              required
            />
            <TextField
              label="API"
              value={api}
              onChange={(e) => setApi(e.target.value)}
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
            onClick={handleSaveProduct} 
            variant="contained"
            disabled={!productName || rate <= 0 || !api}
          >
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
