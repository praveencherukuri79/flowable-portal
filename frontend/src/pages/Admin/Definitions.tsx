import React from 'react'
import {
  Card,
  CardContent,
  TextField,
  Typography,
} from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { adminApi, ProcessDefinition } from '../../api/adminApi'

export default function DefinitionsPage() {
  const [rows, setRows] = React.useState<ProcessDefinition[]>([])
  const [q, setQ] = React.useState('')

  React.useEffect(() => {
    adminApi.getDefinitions().then((res) => setRows(res.data))
  }, [])

  const filtered = q
    ? rows.filter((r) => r.key.includes(q) || r.name?.includes(q))
    : rows

  const columns: GridColDef[] = [
    { field: 'key', headerName: 'Key', flex: 1 },
    { field: 'name', headerName: 'Name', flex: 1.5 },
    { field: 'version', headerName: 'Version', width: 120 },
    { field: 'deploymentId', headerName: 'Deployment', flex: 1.2 },
  ]

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Process Definitions
        </Typography>
        <TextField
          size="small"
          placeholder="Search..."
          value={q}
          onChange={(e) => setQ(e.target.value)}
          sx={{ mb: 2 }}
        />
        <div style={{ height: 520 }}>
          <DataGrid
            rows={filtered}
            columns={columns}
            getRowId={(r) => r.id}
            disableRowSelectionOnClick
          />
        </div>
      </CardContent>
    </Card>
  )
}

