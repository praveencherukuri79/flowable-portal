import { useState, useEffect } from 'react'
import {
  Box,
  Typography,
  Card,
  CardContent,
  Button,
  Alert,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Chip,
  IconButton
} from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import VisibilityIcon from '@mui/icons-material/Visibility'
import RefreshIcon from '@mui/icons-material/Refresh'
import {
  processControlApi,
  ProcessDefinition,
  ProcessInstance
} from '../../api/processControlApi'
import { adminApi } from '../../api/adminApi'
import { InstanceDetailsView } from './InstanceDetailsView'

export function ProcessControl() {
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  
  // Process Definitions
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([])
  const [loadingDefinitions, setLoadingDefinitions] = useState(false)
  
  // Running Instances
  const [instances, setInstances] = useState<ProcessInstance[]>([])
  const [loadingInstances, setLoadingInstances] = useState(false)
  
  // Start Process Dialog
  const [openStartDialog, setOpenStartDialog] = useState(false)
  const [selectedDefinition, setSelectedDefinition] = useState<ProcessDefinition | null>(null)
  const [businessKey, setBusinessKey] = useState('')
  const [startVariables, setStartVariables] = useState('{}')
  
  // Instance Details
  const [selectedInstance, setSelectedInstance] = useState<ProcessInstance | null>(null)
  const [openDetailsDialog, setOpenDetailsDialog] = useState(false)
  
  useEffect(() => {
    loadDefinitions()
    loadInstances()
  }, [])
  
  const loadDefinitions = async () => {
    try {
      setLoadingDefinitions(true)
      const data = await adminApi.getAllDefinitions()
      setDefinitions(data)
    } catch (err: any) {
      console.error('Failed to load definitions:', err)
      setError(err.response?.data?.message || 'Failed to load process definitions')
    } finally {
      setLoadingDefinitions(false)
    }
  }
  
  const loadInstances = async () => {
    try {
      setLoadingInstances(true)
      const data = await processControlApi.getAllRunningInstances()
      setInstances(data)
    } catch (err: any) {
      console.error('Failed to load instances:', err)
      setError(err.response?.data?.message || 'Failed to load running instances')
    } finally {
      setLoadingInstances(false)
    }
  }
  
  const handleOpenStartDialog = (definition: ProcessDefinition) => {
    setSelectedDefinition(definition)
    setBusinessKey('')
    setStartVariables('{}')
    setOpenStartDialog(true)
  }
  
  const handleStartProcess = async () => {
    if (!selectedDefinition) return
    
    try {
      setLoading(true)
      setError(null)
      const variables = JSON.parse(startVariables)
      await processControlApi.startProcessByKey(selectedDefinition.key, businessKey || undefined, variables)
      setSuccessMessage(`Process "${selectedDefinition.name}" started successfully`)
      setOpenStartDialog(false)
      loadInstances() // Refresh instances
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start process. Check JSON format.')
    } finally {
      setLoading(false)
    }
  }
  
  const handleViewInstance = (instance: ProcessInstance) => {
    setSelectedInstance(instance)
    setOpenDetailsDialog(true)
  }
  
  const handleRefreshAll = () => {
    loadDefinitions()
    loadInstances()
  }
  
  const definitionColumns: GridColDef[] = [
    { field: 'key', headerName: 'Key', flex: 1 },
    { field: 'name', headerName: 'Name', flex: 1.5 },
    { field: 'version', headerName: 'Version', width: 100 },
    { field: 'category', headerName: 'Category', flex: 1 },
    {
      field: 'suspended',
      headerName: 'Status',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value ? 'Suspended' : 'Active'}
          color={params.value ? 'warning' : 'success'}
          size="small"
        />
      )
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        <Button
          variant="contained"
          color="primary"
          size="small"
          startIcon={<PlayArrowIcon />}
          onClick={() => handleOpenStartDialog(params.row as ProcessDefinition)}
          disabled={params.row.suspended}
        >
          Start
        </Button>
      )
    }
  ]
  
  const instanceColumns: GridColDef[] = [
    { field: 'id', headerName: 'Instance ID', flex: 1.5 },
    { field: 'name', headerName: 'Process Name', flex: 1.2 },
    { field: 'businessKey', headerName: 'Business Key', flex: 1 },
    {
      field: 'status',
      headerName: 'Status',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <Chip
          label={params.value}
          color={params.value === 'ACTIVE' ? 'success' : 'warning'}
          size="small"
        />
      )
    },
    { field: 'startTime', headerName: 'Started', flex: 1 },
    { field: 'startUserId', headerName: 'Started By', width: 150 },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 120,
      renderCell: (params: GridRenderCellParams) => (
        <IconButton
          color="primary"
          size="small"
          onClick={() => handleViewInstance(params.row as ProcessInstance)}
          title="View Details"
        >
          <VisibilityIcon />
        </IconButton>
      )
    }
  ]
  
  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h5">Process Control Center</Typography>
        <Button
          variant="outlined"
          startIcon={<RefreshIcon />}
          onClick={handleRefreshAll}
        >
          Refresh All
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
      
      {/* Process Definitions Table */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Process Definitions
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            All deployed process definitions. Click "Start" to create a new instance.
          </Typography>
          <div style={{ height: 400, width: '100%' }}>
            <DataGrid
              rows={definitions}
              columns={definitionColumns}
              loading={loadingDefinitions}
              getRowId={(row) => row.id}
              pageSizeOptions={[5, 10, 25]}
              initialState={{
                pagination: { paginationModel: { pageSize: 5 } }
              }}
              disableRowSelectionOnClick
            />
          </div>
        </CardContent>
      </Card>
      
      {/* Running Instances Table */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Running Process Instances
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            All active and suspended process instances. Click the eye icon to view details and manage tasks.
          </Typography>
          <div style={{ height: 400, width: '100%' }}>
            <DataGrid
              rows={instances}
              columns={instanceColumns}
              loading={loadingInstances}
              getRowId={(row) => row.id}
              pageSizeOptions={[5, 10, 25]}
              initialState={{
                pagination: { paginationModel: { pageSize: 5 } }
              }}
              disableRowSelectionOnClick
            />
          </div>
        </CardContent>
      </Card>
      
      {/* Start Process Dialog */}
      <Dialog open={openStartDialog} onClose={() => setOpenStartDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>
          Start Process: {selectedDefinition?.name}
        </DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Process Key: <strong>{selectedDefinition?.key}</strong>
          </Typography>
          <TextField
            label="Business Key (optional)"
            fullWidth
            margin="normal"
            value={businessKey}
            onChange={(e) => setBusinessKey(e.target.value)}
            helperText="Optional identifier for this process instance"
          />
          <TextField
            label="Process Variables (JSON)"
            fullWidth
            multiline
            rows={8}
            margin="normal"
            value={startVariables}
            onChange={(e) => setStartVariables(e.target.value)}
            sx={{ fontFamily: 'monospace' }}
            helperText='Example: {"requestTitle": "New Request", "amount": 1000}'
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenStartDialog(false)} color="secondary">
            Cancel
          </Button>
          <Button
            onClick={handleStartProcess}
            color="primary"
            variant="contained"
            disabled={loading}
          >
            {loading ? <CircularProgress size={24} /> : 'Start Process'}
          </Button>
        </DialogActions>
      </Dialog>
      
      {/* Instance Details Dialog */}
      {selectedInstance && (
        <InstanceDetailsView
          open={openDetailsDialog}
          instance={selectedInstance}
          onClose={() => {
            setOpenDetailsDialog(false)
            setSelectedInstance(null)
            loadInstances() // Refresh after any changes
          }}
          onSuccess={(message) => {
            setSuccessMessage(message)
            loadInstances()
          }}
          onError={(message) => setError(message)}
        />
      )}
    </Box>
  )
}
