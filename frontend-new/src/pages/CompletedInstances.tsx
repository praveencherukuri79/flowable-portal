import React, { useEffect, useState, useCallback } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TablePagination,
  Chip,
  Button,
  IconButton,
  TextField,
  InputAdornment,
  Tooltip,
  Avatar,
  Skeleton,
  alpha,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Alert,
  Menu,
} from '@mui/material'
import {
  Search as SearchIcon,
  Download as DownloadIcon,
  Refresh as RefreshIcon,
  CheckCircle as CompletedIcon,
  Cancel as TerminatedIcon,
  Delete as DeletedIcon,
  CalendarMonth as CalendarIcon,
  Add as AddIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import type { ProcessInstance, ProcessDefinition } from '../types'

export const CompletedInstances: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [instances, setInstances] = useState<ProcessInstance[]>([])
  const [totalCount, setTotalCount] = useState(0)
  const [page, setPage] = useState(0)
  const [rowsPerPage, setRowsPerPage] = useState(10)
  const [searchTerm, setSearchTerm] = useState('')
  const [definitionFilter, setDefinitionFilter] = useState('')
  const [definitions, setDefinitions] = useState<ProcessDefinition[]>([])
  const [selectedInstance, setSelectedInstance] = useState<ProcessInstance | null>(null)
  const [dateFilterAnchor, setDateFilterAnchor] = useState<null | HTMLElement>(null)
  const [dateRange, setDateRange] = useState('Last 30 Days')

  useEffect(() => {
    loadDefinitions()
  }, [])

  useEffect(() => {
    loadInstances()
  }, [page, rowsPerPage, definitionFilter])

  const loadDefinitions = async () => {
    try {
      const defs = await adminApi.getDefinitions()
      setDefinitions(defs)
    } catch (err) {
      console.error('Failed to load definitions:', err)
    }
  }

  const loadInstances = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const response = await adminApi.searchInstances({
        state: 'COMPLETED',
        definitionKey: definitionFilter || undefined,
        page,
        size: rowsPerPage,
      })
      setInstances(response.content || [])
      setTotalCount(response.total || 0)
    } catch (err) {
      console.error('Failed to load instances:', err)
      setError('Failed to load completed instances. Please try again.')
    } finally {
      setLoading(false)
    }
  }, [page, rowsPerPage, definitionFilter])

  const handlePageChange = (_: unknown, newPage: number) => {
    setPage(newPage)
  }

  const handleRowsPerPageChange = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10))
    setPage(0)
  }

  const handleDefinitionFilterChange = (event: SelectChangeEvent) => {
    setDefinitionFilter(event.target.value)
    setPage(0)
  }

  const handleInstanceClick = (instance: ProcessInstance) => {
    setSelectedInstance(instance)
    navigate(`/instances/${instance.id}`)
  }

  const getStatusChip = (status: string, outcome?: string) => {
    let color = '#10b981'
    let label = 'Completed'
    let icon = <CompletedIcon sx={{ fontSize: 12 }} />

    if (status === 'TERMINATED' || outcome === 'Rejected') {
      color = '#ef4444'
      label = 'Terminated'
      icon = <TerminatedIcon sx={{ fontSize: 12 }} />
    } else if (status === 'DELETED') {
      color = '#6b7280'
      label = 'Deleted'
      icon = <DeletedIcon sx={{ fontSize: 12 }} />
    }

    return (
      <Chip
        size="small"
        icon={icon}
        label={label}
        sx={{
          bgcolor: alpha(color, 0.15),
          color: color,
          fontWeight: 500,
          fontSize: '0.75rem',
          border: `1px solid ${alpha(color, 0.3)}`,
          '& .MuiChip-icon': { color: 'inherit' },
        }}
      />
    )
  }

  const getOutcomeDisplay = (instance: ProcessInstance) => {
    const outcome = instance.variables?.outcome as string
    if (!outcome) return '-'
    
    const color = outcome === 'Approved' || outcome === 'Completed' ? '#10b981' : '#ef4444'
    return (
      <Typography variant="body2" sx={{ color }}>
        {outcome}
      </Typography>
    )
  }

  const getDuration = (startTime: string, endTime: string | null) => {
    if (!endTime) return '-'
    const start = new Date(startTime).getTime()
    const end = new Date(endTime).getTime()
    const diff = end - start
    
    const hours = Math.floor(diff / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    
    if (hours > 24) {
      const days = Math.floor(hours / 24)
      return `${days}d ${hours % 24}h`
    }
    return `${hours}h ${minutes}m`
  }

  // Handle exporting instances to CSV
  const handleExportCSV = () => {
    const headers = ['Instance ID', 'Process Name', 'Initiator', 'End Date', 'Duration', 'Status', 'Outcome']
    const rows = instances.map(i => [
      i.id,
      i.processDefinitionKey || '',
      i.startUserId || 'System',
      i.endTime ? new Date(i.endTime).toLocaleString() : '-',
      getDuration(i.startTime, i.endTime),
      i.status,
      (i.variables?.outcome as string) || '-',
    ])
    
    const csvContent = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
    const blob = new Blob([csvContent], { type: 'text/csv' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `completed-instances-${new Date().toISOString().split('T')[0]}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }

  // Handle date range selection
  const handleDateRangeSelect = (range: string) => {
    setDateRange(range)
    setDateFilterAnchor(null)
    // In a real app, this would filter by date range via API
    // For now, just update the label
  }

  return (
    <Box sx={{ display: 'flex', gap: 3, height: 'calc(100vh - 180px)' }}>
      {/* Main Table Section */}
      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column', minWidth: 0 }}>
        {/* Header */}
        <Box sx={{ mb: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
            <Typography variant="h4" sx={{ fontWeight: 700, color: 'white' }}>
              Completed Instances
            </Typography>
            <Chip
              label={`${totalCount} Total`}
              size="small"
              sx={{
                bgcolor: 'background.paper',
                border: '1px solid',
                borderColor: 'divider',
                fontWeight: 500,
              }}
            />
          </Box>
          <Typography variant="body2" color="text.secondary">
            Archive of all finalized maker-checker workflows and audits
          </Typography>
        </Box>

        {error && (
          <Alert severity="error" sx={{ mb: 3 }}>
            {error}
          </Alert>
        )}

        {/* Filters */}
        <Box
          sx={{
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            mb: 3,
            flexWrap: 'wrap',
            gap: 2,
          }}
        >
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <TextField
              size="small"
              placeholder="Search by Process ID, Initiator..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: 'text.secondary' }} />
                  </InputAdornment>
                ),
              }}
              sx={{ width: 320 }}
            />
            <FormControl size="small" sx={{ minWidth: 160 }}>
              <InputLabel>Definition</InputLabel>
              <Select
                value={definitionFilter}
                label="Definition"
                onChange={handleDefinitionFilterChange}
              >
                <MenuItem value="">All</MenuItem>
                {definitions.map((def) => (
                  <MenuItem key={def.id} value={def.key}>
                    {def.name || def.key}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
            <Button
              variant="outlined"
              size="small"
              startIcon={<CalendarIcon />}
              onClick={(e) => setDateFilterAnchor(e.currentTarget)}
              sx={{ borderColor: 'divider' }}
            >
              {dateRange}
            </Button>
            {definitionFilter && (
              <Button
                size="small"
                onClick={() => setDefinitionFilter('')}
                sx={{ color: 'primary.main' }}
              >
                Clear Filters
              </Button>
            )}
          </Box>
          <Box sx={{ display: 'flex', gap: 1 }}>
            <Tooltip title="Refresh">
              <IconButton onClick={loadInstances} sx={{ bgcolor: 'background.paper', border: '1px solid', borderColor: 'divider' }}>
                <RefreshIcon />
              </IconButton>
            </Tooltip>
            <Button
              variant="outlined"
              startIcon={<DownloadIcon />}
              onClick={handleExportCSV}
              sx={{ borderColor: 'divider' }}
            >
              Export CSV
            </Button>
          </Box>
        </Box>

        {/* Date Range Menu */}
        <Menu
          anchorEl={dateFilterAnchor}
          open={Boolean(dateFilterAnchor)}
          onClose={() => setDateFilterAnchor(null)}
        >
          <MenuItem onClick={() => handleDateRangeSelect('Last 7 Days')}>Last 7 Days</MenuItem>
          <MenuItem onClick={() => handleDateRangeSelect('Last 30 Days')}>Last 30 Days</MenuItem>
          <MenuItem onClick={() => handleDateRangeSelect('Last 90 Days')}>Last 90 Days</MenuItem>
          <MenuItem onClick={() => handleDateRangeSelect('This Year')}>This Year</MenuItem>
          <MenuItem onClick={() => handleDateRangeSelect('All Time')}>All Time</MenuItem>
        </Menu>

        {/* Table */}
        <Card sx={{ flex: 1, display: 'flex', flexDirection: 'column', overflow: 'hidden' }}>
          <TableContainer sx={{ flex: 1 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell>Process ID</TableCell>
                  <TableCell>Definition</TableCell>
                  <TableCell>End Date</TableCell>
                  <TableCell>Initiator</TableCell>
                  <TableCell>Duration</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Outcome</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {loading ? (
                  [...Array(5)].map((_, idx) => (
                    <TableRow key={idx}>
                      <TableCell colSpan={7}>
                        <Skeleton variant="rectangular" height={48} sx={{ bgcolor: 'background.default' }} />
                      </TableCell>
                    </TableRow>
                  ))
                ) : instances.length > 0 ? (
                  instances.map((instance) => (
                    <TableRow
                      key={instance.id}
                      hover
                      onClick={() => handleInstanceClick(instance)}
                      selected={selectedInstance?.id === instance.id}
                      sx={{
                        cursor: 'pointer',
                        borderLeft: '2px solid',
                        borderLeftColor: selectedInstance?.id === instance.id ? 'primary.main' : 'transparent',
                        '&.Mui-selected': {
                          bgcolor: alpha('#3b82f6', 0.08),
                        },
                      }}
                    >
                      <TableCell>
                        <Typography
                          variant="body2"
                          sx={{
                            fontFamily: 'monospace',
                            color: selectedInstance?.id === instance.id ? 'primary.main' : 'text.primary',
                            fontWeight: 500,
                          }}
                        >
                          {instance.id.slice(0, 12)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ color: 'white' }}>
                          {instance.processDefinitionKey?.replace(/-/g, ' ') || instance.name || 'Unknown'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" sx={{ fontFamily: 'monospace', color: 'text.secondary' }}>
                          {instance.endTime
                            ? new Date(instance.endTime).toLocaleDateString()
                            : '-'}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                          <Avatar
                            sx={{
                              width: 24,
                              height: 24,
                              fontSize: '0.625rem',
                              bgcolor: 'primary.dark',
                            }}
                          >
                            {instance.startUserId?.charAt(0)?.toUpperCase() || 'S'}
                          </Avatar>
                          <Typography variant="body2" color="text.secondary">
                            {instance.startUserId || 'System'}
                          </Typography>
                        </Box>
                      </TableCell>
                      <TableCell>
                        <Typography variant="body2" color="text.secondary">
                          {getDuration(instance.startTime, instance.endTime)}
                        </Typography>
                      </TableCell>
                      <TableCell>
                        {getStatusChip(instance.status, instance.variables?.outcome as string)}
                      </TableCell>
                      <TableCell>{getOutcomeDisplay(instance)}</TableCell>
                    </TableRow>
                  ))
                ) : (
                  <TableRow>
                    <TableCell colSpan={7} sx={{ textAlign: 'center', py: 6 }}>
                      <Typography color="text.secondary">No completed instances found</Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
          <TablePagination
            component="div"
            count={totalCount}
            page={page}
            onPageChange={handlePageChange}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={handleRowsPerPageChange}
            rowsPerPageOptions={[10, 25, 50]}
            sx={{ borderTop: '1px solid', borderColor: 'divider' }}
          />
        </Card>
      </Box>

      {/* Details Sidebar (shown when instance is selected) */}
      {selectedInstance && (
        <Card
          sx={{
            width: 420,
            flexShrink: 0,
            display: 'flex',
            flexDirection: 'column',
            overflow: 'hidden',
          }}
        >
          {/* Header */}
          <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 1 }}>
              <Typography variant="h6" sx={{ fontWeight: 700, color: 'white' }}>
                {selectedInstance.id.slice(0, 12)}
              </Typography>
              <Chip
                size="small"
                label={selectedInstance.processDefinitionKey}
                sx={{
                  bgcolor: alpha('#3b82f6', 0.15),
                  color: 'primary.main',
                  fontWeight: 500,
                }}
              />
            </Box>
            <Typography variant="caption" color="text.secondary">
              Started {new Date(selectedInstance.startTime).toLocaleDateString()}
            </Typography>
          </Box>

          {/* Quick Details */}
          <Box sx={{ p: 3, borderBottom: '1px solid', borderColor: 'divider' }}>
            <Typography variant="subtitle2" sx={{ color: 'white', mb: 2 }}>
              Key Details
            </Typography>
            <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 2 }}>
              <Box>
                <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase' }}>
                  Initiator
                </Typography>
                <Box sx={{ display: 'flex', alignItems: 'center', gap: 1, mt: 0.5 }}>
                  <Avatar sx={{ width: 20, height: 20, fontSize: '0.625rem' }}>
                    {selectedInstance.startUserId?.charAt(0) || 'S'}
                  </Avatar>
                  <Typography variant="body2" color="text.primary">
                    {selectedInstance.startUserId || 'System'}
                  </Typography>
                </Box>
              </Box>
              <Box>
                <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase' }}>
                  Outcome
                </Typography>
                {getOutcomeDisplay(selectedInstance)}
              </Box>
              <Box>
                <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase' }}>
                  Start Date
                </Typography>
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                  {new Date(selectedInstance.startTime).toLocaleString()}
                </Typography>
              </Box>
              <Box>
                <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase' }}>
                  End Date
                </Typography>
                <Typography variant="body2" sx={{ fontFamily: 'monospace' }}>
                  {selectedInstance.endTime
                    ? new Date(selectedInstance.endTime).toLocaleString()
                    : '-'}
                </Typography>
              </Box>
              <Box sx={{ gridColumn: '1 / -1' }}>
                <Typography variant="caption" color="text.disabled" sx={{ textTransform: 'uppercase' }}>
                  Duration
                </Typography>
                <Typography variant="body2">
                  {getDuration(selectedInstance.startTime, selectedInstance.endTime)}
                </Typography>
              </Box>
            </Box>
          </Box>

          {/* Actions */}
          <Box sx={{ p: 3, mt: 'auto', display: 'flex', gap: 2 }}>
            <Button
              fullWidth
              variant="contained"
              onClick={() => navigate(`/instances/${selectedInstance.id}`)}
              sx={{ boxShadow: '0 4px 12px rgba(59, 130, 246, 0.3)' }}
            >
              View Details
            </Button>
          </Box>
        </Card>
      )}
    </Box>
  )
}

export default CompletedInstances
