import { useState, useEffect } from 'react'
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
import { flowableApi, dataQueryApi, Product } from '../../api/flowableApi'
import dayjs from 'dayjs'

export function ProductEdit() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId } = location.state || {}
  const [products, setProducts] = useState<Product[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [openDialog, setOpenDialog] = useState(false)
  const [editingProduct, setEditingProduct] = useState<Product | null>(null)
  const [sheetId, setSheetId] = useState<string>('')
  const [rejectionComments, setRejectionComments] = useState<string>('')
  const [makerResponse, setMakerResponse] = useState<string>('')

  // Form state
  const [productName, setProductName] = useState('')
  const [rate, setRate] = useState<number>(0)
  const [api, setApi] = useState('')
  const [effectiveDate, setEffectiveDate] = useState(dayjs().format('YYYY-MM-DD'))
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
      
      // Check if this is a rejected task coming back
      if (response.stage1RejectionComments) {
        setRejectionComments(response.stage1RejectionComments as string)
      }
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

  const handleOpenDialog = (product?: Product) => {
    if (product) {
      setEditingProduct(product)
      setProductName(product.productName)
      setRate(product.rate)
      setApi(product.api)
      setEffectiveDate(product.effectiveDate)
      setComments('')
    } else {
      setEditingProduct(null)
      setProductName('')
      setRate(0)
      setApi('')
      setEffectiveDate(dayjs().format('YYYY-MM-DD'))
      setComments('')
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setEditingProduct(null)
  }

  const handleSaveProduct = async () => {
    // Save locally - will be submitted with task completion
    const newProduct: Product = {
      id: editingProduct?.id || Date.now(),
      sheetId,
      productName,
      rate,
      api,
      effectiveDate,
      status: 'PENDING',
      editedBy: 'current-user'
    }

    if (editingProduct) {
      setProducts(products.map(p => p.id === editingProduct.id ? newProduct : p))
      setSuccessMessage('Product updated')
    } else {
      setProducts([...products, newProduct])
      setSuccessMessage('Product added')
    }

    handleCloseDialog()
  }

  const handleDeleteProduct = (id: number) => {
    if (!window.confirm('Are you sure you want to delete this product?')) return
    setProducts(products.filter(p => p.id !== id))
    setSuccessMessage('Product deleted')
  }

  const handleCompleteTask = async () => {
    if (!taskId) return
    
    if (products.length === 0) {
      setError('Please add at least one product before submitting')
      return
    }

    try {
      setLoading(true)
      await flowableApi.completeTask(taskId, {
        products: products,
        sheetId,
        makerComments: makerResponse || 'Products submitted for approval',
        stage1Decision: 'COMPLETE',
        submittedAt: new Date().toISOString()
      })
      setSuccessMessage('Products submitted successfully!')
      setTimeout(() => navigate('/maker'), 2000)
    } catch (err: any) {
      setError(err?.response?.data?.message || 'Failed to complete task')
    } finally {
      setLoading(false)
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
      <Paper sx={{ p: 3 }}>
        <Box mb={3}>
          <Typography variant="h5" gutterBottom>
            Edit Products - Stage 1
          </Typography>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Sheet ID: {sheetId}
          </Typography>
          
          {rejectionComments && (
            <Alert severity="warning" sx={{ mt: 2 }}>
              <Typography variant="subtitle2" gutterBottom>
                <strong>Checker's Feedback - Please Address:</strong>
              </Typography>
              <Typography variant="body2">
                {rejectionComments}
              </Typography>
            </Alert>
          )}
        </Box>

        <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
          <Box></Box>
          <Button
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
            onClick={() => handleOpenDialog()}
          >
            Add Product
          </Button>
        </Box>

        {successMessage && (
          <Alert severity="success" sx={{ mb: 2 }} onClose={() => setSuccessMessage(null)}>
            {successMessage}
          </Alert>
        )}
        {error && (
          <Alert severity="error" sx={{ mb: 2 }} onClose={() => setError(null)}>
            {error}
          </Alert>
        )}

        {loading && !products.length ? (
          <Box display="flex" justifyContent="center" py={5}>
            <CircularProgress />
          </Box>
        ) : (
          <>
            <TableContainer>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Product Name</TableCell>
                    <TableCell>Rate</TableCell>
                    <TableCell>API</TableCell>
                    <TableCell>Effective Date</TableCell>
                    <TableCell>Status</TableCell>
                    <TableCell>Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {products.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={6} align="center">
                        <Typography variant="body2" color="text.secondary">
                          No products added yet. Click "Add Product" to get started.
                        </Typography>
                      </TableCell>
                    </TableRow>
                  ) : (
                    products.map((product) => (
                      <TableRow key={product.id}>
                        <TableCell>{product.productName}</TableCell>
                        <TableCell>{typeof product.rate === 'number' ? product.rate.toFixed(2) : product.rate}</TableCell>
                        <TableCell>{product.api}</TableCell>
                        <TableCell>{dayjs(product.effectiveDate).format('YYYY-MM-DD')}</TableCell>
                        <TableCell>{product.status || 'PENDING'}</TableCell>
                        <TableCell>
                          <IconButton
                            size="small"
                            color="primary"
                            onClick={() => handleOpenDialog(product)}
                          >
                            <EditIcon />
                          </IconButton>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleDeleteProduct(product.id!)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>

            <Paper sx={{ p: 2, mb: 3 }}>
              <TextField
                fullWidth
                label="Comments for Checker"
                value={makerResponse}
                onChange={(e) => setMakerResponse(e.target.value)}
                multiline
                rows={3}
                placeholder={rejectionComments ? "Explain what you've corrected..." : "Optional: Add any notes for the checker..."}
                helperText={rejectionComments ? "Please explain the changes you made to address the feedback" : ""}
              />
            </Paper>

            <Box mt={3} display="flex" justifyContent="flex-end" gap={2}>
              <Button
                variant="outlined"
                onClick={() => navigate('/maker')}
              >
                Cancel
              </Button>
              <Button
                variant="contained"
                color="success"
                size="large"
                startIcon={<SaveIcon />}
                onClick={handleCompleteTask}
                disabled={loading || products.length === 0}
              >
                Submit for Approval
              </Button>
            </Box>
          </>
        )}
      </Paper>

      {/* Add/Edit Product Dialog */}
      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingProduct ? 'Edit Product' : 'Add Product'}</DialogTitle>
        <DialogContent>
          <TextField
            label="Product Name"
            fullWidth
            margin="normal"
            value={productName}
            onChange={(e) => setProductName(e.target.value)}
            required
          />
          <TextField
            label="Rate"
            type="number"
            fullWidth
            margin="normal"
            value={rate}
            onChange={(e) => setRate(Number(e.target.value))}
            required
          />
          <TextField
            label="API"
            fullWidth
            margin="normal"
            value={api}
            onChange={(e) => setApi(e.target.value)}
            required
            helperText="API name or identifier"
          />
          <TextField
            label="Effective Date"
            type="date"
            fullWidth
            margin="normal"
            value={effectiveDate}
            onChange={(e) => setEffectiveDate(e.target.value)}
            InputLabelProps={{ shrink: true }}
            required
          />
          <TextField
            label="Comments"
            fullWidth
            margin="normal"
            multiline
            rows={3}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog} color="secondary">
            Cancel
          </Button>
          <Button
            onClick={handleSaveProduct}
            color="primary"
            variant="contained"
            disabled={!productName || !api}
          >
            {editingProduct ? 'Update' : 'Add'}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

