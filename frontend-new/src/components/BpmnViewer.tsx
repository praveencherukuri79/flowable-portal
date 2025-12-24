import React, { useEffect, useRef } from 'react'
import { Box, Typography, CircularProgress } from '@mui/material'
import BpmnJS from 'bpmn-js/lib/NavigatedViewer'

interface BpmnViewerProps {
  xml: string
  activeActivityIds?: string[]
  completedActivityIds?: string[]
  loading?: boolean
}

export const BpmnViewer: React.FC<BpmnViewerProps> = ({
  xml,
  activeActivityIds = [],
  completedActivityIds = [],
  loading = false,
}) => {
  const containerRef = useRef<HTMLDivElement>(null)
  const viewerRef = useRef<BpmnJS | null>(null)

  useEffect(() => {
    if (!containerRef.current || !xml) return

    // Destroy previous viewer
    if (viewerRef.current) {
      viewerRef.current.destroy()
    }

    // Create new viewer
    const viewer = new BpmnJS({
      container: containerRef.current,
    })
    viewerRef.current = viewer

    viewer.importXML(xml).then(() => {
      // Fit to viewport
      const canvas = viewer.get('canvas') as { 
        zoom: (level: string) => void
        addMarker: (id: string, marker: string) => void 
      }
      canvas.zoom('fit-viewport')

      // Highlight completed activities (green)
      completedActivityIds.forEach((id) => {
        try {
          canvas.addMarker(id, 'completed')
        } catch {
          // Element might not exist
        }
      })

      // Highlight active activities (blue) - applied after completed so it takes precedence
      activeActivityIds.forEach((id) => {
        try {
          canvas.addMarker(id, 'active')
        } catch {
          // Element might not exist
        }
      })
    }).catch((err: Error) => {
      console.error('BPMN import error:', err)
    })

    return () => {
      if (viewerRef.current) {
        viewerRef.current.destroy()
        viewerRef.current = null
      }
    }
  }, [xml, activeActivityIds, completedActivityIds])

  if (loading) {
    return (
      <Box sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#fafafa' }}>
        <CircularProgress />
      </Box>
    )
  }

  if (!xml) {
    return (
      <Box sx={{ height: '100%', display: 'flex', alignItems: 'center', justifyContent: 'center', bgcolor: '#fafafa' }}>
        <Typography color="text.secondary">No diagram available</Typography>
      </Box>
    )
  }

  return (
    <Box
      ref={containerRef}
      sx={{
        width: '100%',
        height: '100%',
        bgcolor: '#fafafa',
        borderRadius: 1,
        '& .djs-container': {
          background: '#fafafa !important',
        },
        // Hide palette
        '& .djs-palette': {
          display: 'none !important',
        },
        // Active tasks - BLUE highlight (like in Flowable UI)
        '& .active .djs-visual > rect': {
          stroke: '#1e88e5 !important',
          strokeWidth: '3px !important',
          fill: 'rgba(30, 136, 229, 0.1) !important',
        },
        '& .active .djs-visual > circle': {
          stroke: '#1e88e5 !important',
          strokeWidth: '3px !important',
        },
        '& .active .djs-visual > polygon': {
          stroke: '#1e88e5 !important',
          strokeWidth: '2px !important',
        },
        // Completed tasks - GREEN highlight
        '& .completed .djs-visual > rect': {
          stroke: '#43a047 !important',
          strokeWidth: '3px !important',
          fill: 'rgba(67, 160, 71, 0.1) !important',
        },
        '& .completed .djs-visual > circle': {
          stroke: '#43a047 !important',
          strokeWidth: '3px !important',
        },
        '& .completed .djs-visual > polygon': {
          stroke: '#43a047 !important',
          strokeWidth: '2px !important',
        },
      }}
    />
  )
}

export default BpmnViewer
