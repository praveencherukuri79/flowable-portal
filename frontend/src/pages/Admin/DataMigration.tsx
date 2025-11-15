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
  Chip,
  Divider
} from '@mui/material'
import { useLocation, useNavigate } from 'react-router-dom'
import CloudUploadIcon from '@mui/icons-material/CloudUpload'
import CheckCircleIcon from '@mui/icons-material/CheckCircle'
import { flowableApi, dataQueryApi } from '../../api/flowableApi'
import { useRecoilValue } from 'recoil'
import { authState } from '../../state/auth'

export function DataMigration() {
  const location = useLocation()
  const navigate = useNavigate()
  const { taskId, processInstanceId, formKey } = location.state || {}
  const auth = useRecoilValue(authState)

  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [itemSheet, setItemSheet] = useState<any>(null)
  const [planSheet, setPlanSheet] = useState<any>(null)
  const [productSheet, setProductSheet] = useState<any>(null)
  const [itemsCount, setItemsCount] = useState(0)
  const [plansCount, setPlansCount] = useState(0)
  const [productsCount, setProductsCount] = useState(0)
  const [allApproved, setAllApproved] = useState(false)
  const [dataLoaded, setDataLoaded] = useState(false)

  // Check access - must come from formKey navigation
  useEffect(() => {
    if (!taskId || !processInstanceId || !formKey) {
      setError('❌ Unauthorized Access: This page can only be accessed from a claimed task.')
      setTimeout(() => navigate('/admin'), 3000)
      return
    }
    loadMigrationData()
  }, [taskId, processInstanceId, formKey, navigate])

  const loadMigrationData = async () => {
    try {
      setLoading(true)
      
      // Load Items data
      const itemData = await dataQueryApi.getApprovalData(processInstanceId, 'item')
      setItemSheet(itemData.sheet)
      setItemsCount(itemData.items?.length || 0)
      
      // Load Plans data
      const planData = await dataQueryApi.getApprovalData(processInstanceId, 'plan')
      setPlanSheet(planData.sheet)
      setPlansCount(planData.plans?.length || 0)
      
      // Load Products data
      const productData = await dataQueryApi.getApprovalData(processInstanceId, 'product')
      setProductSheet(productData.sheet)
      setProductsCount(productData.products?.length || 0)
      
      // Check if all sheets are approved
      const allSheetsApproved = 
        !!itemData.sheet?.approvedAt && 
        !!planData.sheet?.approvedAt && 
        !!productData.sheet?.approvedAt
      
      setAllApproved(allSheetsApproved)
      setDataLoaded(true)
      
      console.log('✓ Loaded migration data:', { itemData, planData, productData })
      
    } catch (err: any) {
      console.error('Failed to load migration data:', err)
      setError('Failed to load data: ' + (err.response?.data?.message || err.message || 'Unknown error'))
    } finally {
      setLoading(false)
    }
  }

  const handleMigrate = async () => {
    try {
      if (!allApproved) {
        setError('Cannot migrate: Not all sheets are approved')
        return
      }

      setLoading(true)
      setError(null)
      
      // Complete task - TaskListener will handle migration
      // Backend will look up all 3 sheets using processInstanceId
      await flowableApi.completeTask(taskId, {
        migratedBy: auth.username || 'admin'
      })
      
      setSuccessMessage('✅ Data migration initiated successfully! Redirecting...')
      setTimeout(() => navigate('/admin'), 3000)
    } catch (err: any) {
      setError('Failed to migrate: ' + (err.response?.data?.message || err.message))
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
            This page can only be accessed from a claimed task.
          </Typography>
          <Typography sx={{ mt: 2, fontStyle: 'italic', fontSize: '0.9rem' }}>
            Redirecting to Admin Portal in 3 seconds...
          </Typography>
        </Alert>
      </Box>
    )
  }

  if (loading && !dataLoaded) {
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
            Final: Migrate Data to Production
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Task ID: {taskId}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Process Instance: {processInstanceId}
          </Typography>
        </Box>

        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        {successMessage && <Alert severity="success" sx={{ mb: 2 }}>{successMessage}</Alert>}

        {!allApproved && (
          <Alert severity="warning" sx={{ mb: 2 }}>
            ⚠️ Warning: Not all sheets are fully approved. Please ensure all stages are approved before migrating.
          </Alert>
        )}

        <Divider sx={{ my: 3 }} />

        <Typography variant="h6" gutterBottom>
          Migration Summary
        </Typography>

        <TableContainer sx={{ mb: 3 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell><strong>Entity Type</strong></TableCell>
                <TableCell align="center"><strong>Record Count</strong></TableCell>
                <TableCell align="center"><strong>Approval Status</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              <TableRow>
                <TableCell>Items (Stage 3)</TableCell>
                <TableCell align="center">{itemsCount}</TableCell>
                <TableCell align="center">
                  {itemSheet?.approvedAt ? (
                    <Chip label="✓ Approved" color="success" size="small" />
                  ) : (
                    <Chip label="Pending" color="warning" size="small" />
                  )}
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell>Plans (Stage 2)</TableCell>
                <TableCell align="center">{plansCount}</TableCell>
                <TableCell align="center">
                  {planSheet?.approvedAt ? (
                    <Chip label="✓ Approved" color="success" size="small" />
                  ) : (
                    <Chip label="Pending" color="warning" size="small" />
                  )}
                </TableCell>
              </TableRow>
              <TableRow>
                <TableCell>Products (Stage 1)</TableCell>
                <TableCell align="center">{productsCount}</TableCell>
                <TableCell align="center">
                  {productSheet?.approvedAt ? (
                    <Chip label="✓ Approved" color="success" size="small" />
                  ) : (
                    <Chip label="Pending" color="warning" size="small" />
                  )}
                </TableCell>
              </TableRow>
              <TableRow sx={{ bgcolor: 'action.hover' }}>
                <TableCell><strong>Total Records</strong></TableCell>
                <TableCell align="center"><strong>{itemsCount + plansCount + productsCount}</strong></TableCell>
                <TableCell align="center">
                  {allApproved ? (
                    <Chip label="✓ All Approved" color="success" size="small" icon={<CheckCircleIcon />} />
                  ) : (
                    <Chip label="⚠ Incomplete" color="warning" size="small" />
                  )}
                </TableCell>
              </TableRow>
            </TableBody>
          </Table>
        </TableContainer>

        <Divider sx={{ my: 3 }} />

        <Box>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            <strong>What will happen:</strong>
          </Typography>
          <Typography variant="body2" color="text.secondary" component="ul" sx={{ pl: 2 }}>
            <li>All approved staging data (Items, Plans, Products) will be migrated to production tables</li>
            <li>Staging records will be marked as migrated</li>
            <li>The workflow process will be completed</li>
            <li>This action cannot be undone</li>
          </Typography>
        </Box>

        <Box mt={4} display="flex" gap={2}>
          <Button
            variant="outlined"
            onClick={() => navigate('/admin')}
            disabled={loading}
          >
            Cancel
          </Button>
          <Button
            variant="contained"
            color="primary"
            startIcon={<CloudUploadIcon />}
            onClick={handleMigrate}
            disabled={loading || !allApproved}
          >
            {loading ? 'Migrating...' : 'Migrate to Production'}
          </Button>
        </Box>
      </Paper>
    </Box>
  )
}

