import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  CardContent,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Chip,
  IconButton,
  Tooltip,
  Skeleton,
  alpha,
  Grid,
  Alert,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
} from '@mui/material'
import {
  Refresh as RefreshIcon,
  PlayCircleOutline as StartIcon,
  Visibility as ViewIcon,
  Schema as SchemaIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import { BpmnViewer } from '../components/BpmnViewer'
import type { ProcessDefinition } from '../types'
import axios from 'axios'

export const Definitions: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([])
  const [startProcessDialog, setStartProcessDialog] = useState<{ open: boolean; definition: ProcessDefinition | null }>({ open: false, definition: null })
  const [businessKey, setBusinessKey] = useState('')
  const [starting, setStarting] = useState(false)
  const [diagramDialog, setDiagramDialog] = useState<{ open: boolean; definition: ProcessDefinition | null }>({ open: false, definition: null })
  const [bpmnXml, setBpmnXml] = useState<string>('')
  const [diagramLoading, setDiagramLoading] = useState(false)

  useEffect(() => {
    loadDefinitions()
  }, [])

  // Load BPMN XML when diagram dialog opens
  useEffect(() => {
    if (diagramDialog.open && diagramDialog.definition) {
      loadDiagram(diagramDialog.definition.key)
    } else {
      setBpmnXml('')
    }
  }, [diagramDialog.open, diagramDialog.definition])

  const loadDiagram = async (processDefinitionKey: string) => {
    setDiagramLoading(true)
    try {
      const xml = await adminApi.getBpmnXml(processDefinitionKey)
      setBpmnXml(xml)
    } catch (err) {
      console.error('Failed to load diagram:', err)
      setBpmnXml('')
    } finally {
      setDiagramLoading(false)
    }
  }

  const loadDefinitions = async () => {
    setLoading(true)
    setError(null)
    try {
      const defs = await adminApi.getDefinitions()
      setDefinitions(defs || [])
    } catch (err) {
      console.error('Failed to load definitions:', err)
      setError('Failed to load process definitions. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const handleStartProcess = async () => {
    if (!startProcessDialog.definition) return
    setStarting(true)
    try {
      const api = axios.create({ baseURL: '/api' })
      const token = localStorage.getItem('token')
      if (token) {
        api.defaults.headers.common['Authorization'] = `Bearer ${token}`
      }
      
      await api.post(`/flowable/runtime/start/${startProcessDialog.definition.key}`, {
        businessKey: businessKey || undefined,
      })
      
      setStartProcessDialog({ open: false, definition: null })
      setBusinessKey('')
      // Navigate to running instances
      navigate('/instances/running')
    } catch (err) {
      console.error('Failed to start process:', err)
    } finally {
      setStarting(false)
    }
  }

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 0.5 }}>
            Process Definitions
          </Typography>
          <Typography variant="body2" color="text.secondary">
            All deployed process definitions available for execution
          </Typography>
        </Box>
        <IconButton onClick={loadDefinitions} disabled={loading}>
          <RefreshIcon />
        </IconButton>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
          <Button size="small" onClick={loadDefinitions} sx={{ ml: 2 }}>
            Retry
          </Button>
        </Alert>
      )}

      {/* Stats */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        <Grid item xs={12} sm={6} lg={3}>
          <Card>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: alpha('#3b82f6', 0.15), color: '#3b82f6' }}>
                <SchemaIcon />
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Total Definitions
                </Typography>
                {loading ? (
                  <Skeleton variant="text" width={40} height={32} />
                ) : (
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'white' }}>
                    {definitions.length}
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={12} sm={6} lg={3}>
          <Card>
            <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
              <Box sx={{ p: 1.5, borderRadius: 2, bgcolor: alpha('#10b981', 0.15), color: '#10b981' }}>
                <StartIcon />
              </Box>
              <Box>
                <Typography variant="body2" color="text.secondary">
                  Active
                </Typography>
                {loading ? (
                  <Skeleton variant="text" width={40} height={32} />
                ) : (
                  <Typography variant="h5" sx={{ fontWeight: 700, color: 'white' }}>
                    {definitions.filter((d) => !d.suspended).length}
                  </Typography>
                )}
              </Box>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Table */}
      <Card>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Name</TableCell>
                <TableCell>Key</TableCell>
                <TableCell>Version</TableCell>
                <TableCell>Category</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {loading ? (
                [...Array(3)].map((_, idx) => (
                  <TableRow key={idx}>
                    <TableCell colSpan={6}>
                      <Skeleton variant="rectangular" height={48} sx={{ bgcolor: 'background.default' }} />
                    </TableCell>
                  </TableRow>
                ))
              ) : definitions.length > 0 ? (
                definitions.map((def) => (
                  <TableRow key={def.id} hover>
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 500, color: 'white' }}>
                        {def.name || def.key}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {def.description || 'No description'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={def.key}
                        size="small"
                        sx={{
                          fontFamily: 'monospace',
                          bgcolor: 'background.default',
                          border: '1px solid',
                          borderColor: 'divider',
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={`v${def.version}`}
                        size="small"
                        sx={{
                          bgcolor: alpha('#3b82f6', 0.15),
                          color: '#3b82f6',
                          fontWeight: 600,
                        }}
                      />
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {def.category || '-'}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={def.suspended ? 'Suspended' : 'Active'}
                        size="small"
                        sx={{
                          bgcolor: def.suspended ? alpha('#ef4444', 0.15) : alpha('#10b981', 0.15),
                          color: def.suspended ? '#ef4444' : '#10b981',
                          fontWeight: 500,
                        }}
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Box sx={{ display: 'flex', justifyContent: 'flex-end', gap: 0.5 }}>
                        <Tooltip title="View Diagram">
                          <IconButton 
                            size="small"
                            onClick={() => setDiagramDialog({ open: true, definition: def })}
                          >
                            <ViewIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Start Process">
                          <IconButton 
                            size="small" 
                            disabled={def.suspended}
                            onClick={() => setStartProcessDialog({ open: true, definition: def })}
                          >
                            <StartIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={6} sx={{ textAlign: 'center', py: 6 }}>
                    <Typography color="text.secondary">No definitions found</Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Card>

      {/* Start Process Dialog */}
      <Dialog open={startProcessDialog.open} onClose={() => setStartProcessDialog({ open: false, definition: null })}>
        <DialogTitle>Start Process</DialogTitle>
        <DialogContent>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Start a new instance of <strong>{startProcessDialog.definition?.name || startProcessDialog.definition?.key}</strong>
          </Typography>
          <TextField
            autoFocus
            margin="dense"
            label="Business Key (optional)"
            fullWidth
            value={businessKey}
            onChange={(e) => setBusinessKey(e.target.value)}
            placeholder="e.g., ORDER-2024-001"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setStartProcessDialog({ open: false, definition: null })}>Cancel</Button>
          <Button onClick={handleStartProcess} variant="contained" disabled={starting}>
            {starting ? 'Starting...' : 'Start Process'}
          </Button>
        </DialogActions>
      </Dialog>

      {/* View Diagram Dialog */}
      <Dialog 
        open={diagramDialog.open} 
        onClose={() => setDiagramDialog({ open: false, definition: null })}
        maxWidth="lg"
        fullWidth
      >
        <DialogTitle>
          Process Diagram: {diagramDialog.definition?.name || diagramDialog.definition?.key}
        </DialogTitle>
        <DialogContent>
          <Box sx={{ height: 500 }}>
            <BpmnViewer
              xml={bpmnXml}
              loading={diagramLoading}
            />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDiagramDialog({ open: false, definition: null })}>Close</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}

export default Definitions
