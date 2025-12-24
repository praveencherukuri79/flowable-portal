import React, { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import {
  Box,
  Card,
  Typography,
  IconButton,
  Chip,
  Skeleton,
  alpha,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  SelectChangeEvent,
  Alert,
  Button,
} from '@mui/material'
import {
  Refresh as RefreshIcon,
  PlayArrow as StartIcon,
  CheckCircle as CompleteIcon,
  Person as UserIcon,
  Error as ErrorIcon,
  Info as InfoIcon,
} from '@mui/icons-material'
import { adminApi } from '../api/adminApi'
import type { EventLog } from '../types'

export const Events: React.FC = () => {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [events, setEvents] = useState<EventLog[]>([])
  const [typeFilter, setTypeFilter] = useState('')

  useEffect(() => {
    loadEvents()
  }, [])

  const loadEvents = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await adminApi.getEvents(200)
      setEvents(data || [])
    } catch (err) {
      console.error('Failed to load events:', err)
      setError('Failed to load events. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  const getEventIcon = (type: string) => {
    if (type.includes('START')) return <StartIcon sx={{ fontSize: 16 }} />
    if (type.includes('END') || type.includes('COMPLETE')) return <CompleteIcon sx={{ fontSize: 16 }} />
    if (type.includes('USER') || type.includes('TASK')) return <UserIcon sx={{ fontSize: 16 }} />
    if (type.includes('ERROR') || type.includes('FAIL')) return <ErrorIcon sx={{ fontSize: 16 }} />
    return <InfoIcon sx={{ fontSize: 16 }} />
  }

  const getEventColor = (type: string) => {
    if (type.includes('START')) return '#3b82f6'
    if (type.includes('END') || type.includes('COMPLETE')) return '#10b981'
    if (type.includes('ERROR') || type.includes('FAIL')) return '#ef4444'
    if (type.includes('USER') || type.includes('TASK')) return '#8b5cf6'
    return '#6b7280'
  }

  const filteredEvents = typeFilter
    ? events.filter((e) => e.type.toLowerCase().includes(typeFilter.toLowerCase()))
    : events

  const eventTypes = [...new Set(events.map((e) => e.type))]

  return (
    <Box>
      {/* Header */}
      <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 4 }}>
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 700, color: 'white', mb: 0.5 }}>
            Event Log
          </Typography>
          <Typography variant="body2" color="text.secondary">
            Real-time activity log from all process instances ({events.length} events)
          </Typography>
        </Box>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <FormControl size="small" sx={{ minWidth: 180 }}>
            <InputLabel>Event Type</InputLabel>
            <Select
              value={typeFilter}
              label="Event Type"
              onChange={(e: SelectChangeEvent) => setTypeFilter(e.target.value)}
            >
              <MenuItem value="">All Events</MenuItem>
              {eventTypes.map((type) => (
                <MenuItem key={type} value={type}>
                  {type}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
          <IconButton onClick={loadEvents} disabled={loading}>
            <RefreshIcon />
          </IconButton>
        </Box>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 3 }}>
          {error}
          <Button size="small" onClick={loadEvents} sx={{ ml: 2 }}>
            Retry
          </Button>
        </Alert>
      )}

      {/* Events Timeline */}
      <Card sx={{ p: 3 }}>
        {loading ? (
          [...Array(5)].map((_, idx) => (
            <Skeleton key={idx} variant="rectangular" height={60} sx={{ mb: 2, bgcolor: 'background.default' }} />
          ))
        ) : (
          <Box sx={{ position: 'relative', pl: 4, borderLeft: '2px solid', borderColor: 'divider' }}>
            {filteredEvents.length > 0 ? (
              filteredEvents.slice(0, 50).map((event, idx) => {
                const color = getEventColor(event.type)
                return (
                  <Box
                    key={event.id || idx}
                    sx={{
                      mb: 3,
                      position: 'relative',
                      p: 2,
                      borderRadius: 2,
                      bgcolor: 'background.default',
                      border: '1px solid',
                      borderColor: 'divider',
                      '&:hover': {
                        borderColor: alpha(color, 0.5),
                      },
                    }}
                  >
                    <Box
                      sx={{
                        position: 'absolute',
                        left: -26,
                        top: 16,
                        width: 12,
                        height: 12,
                        borderRadius: '50%',
                        bgcolor: color,
                        border: '3px solid',
                        borderColor: 'background.paper',
                      }}
                    />
                    <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', mb: 1 }}>
                      <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                        <Box sx={{ color, display: 'flex' }}>{getEventIcon(event.type)}</Box>
                        <Typography variant="body2" sx={{ fontWeight: 600, color: 'white' }}>
                          {event.type}
                        </Typography>
                      </Box>
                      <Typography variant="caption" sx={{ color: 'text.disabled', fontFamily: 'monospace' }}>
                        {new Date(event.timestamp).toLocaleString()}
                      </Typography>
                    </Box>
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mt: 1 }}>
                      {event.processInstanceId && (
                        <Chip
                          label={`Process: ${event.processInstanceId.slice(0, 8)}`}
                          size="small"
                          onClick={() => navigate(`/instances/${event.processInstanceId}`)}
                          sx={{
                            bgcolor: 'background.paper',
                            border: '1px solid',
                            borderColor: 'divider',
                            cursor: 'pointer',
                            fontSize: '0.625rem',
                            '&:hover': {
                              borderColor: 'primary.main',
                            },
                          }}
                        />
                      )}
                      {event.executionId && (
                        <Typography variant="caption" color="text.secondary">
                          Execution: {event.executionId.slice(0, 8)}
                        </Typography>
                      )}
                      {event.data && (
                        <Typography variant="caption" color="text.secondary">
                          {event.data.length > 50 ? `${event.data.slice(0, 50)}...` : event.data}
                        </Typography>
                      )}
                    </Box>
                  </Box>
                )
              })
            ) : (
              <Typography color="text.secondary" sx={{ textAlign: 'center', py: 4 }}>
                No events found
              </Typography>
            )}
          </Box>
        )}
      </Card>
    </Box>
  )
}

export default Events
