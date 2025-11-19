import React from 'react'
import {
  Card,
  CardContent,
  TextField,
  Typography,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
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
        
        {/* ===== DATA GRID VERSION (Comment out to use Table) ===== */}
        {/* <div style={{ height: 520 }}>
          <DataGrid
            rows={filtered}
            columns={columns}
            getRowId={(r) => r.id}
            disableRowSelectionOnClick
          />
        </div> */}
        
        {/* ===== MUI TABLE VERSION (Comment out to use DataGrid) ===== */}
        <TableContainer component={Paper} sx={{ maxHeight: 520 }}>
          <Table stickyHeader>
            <TableHead>
              <TableRow>
                <TableCell><strong>Key</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Version</strong></TableCell>
                <TableCell><strong>Deployment</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filtered.map((row) => (
                <TableRow key={row.id} hover>
                  <TableCell>{row.key}</TableCell>
                  <TableCell>{row.name}</TableCell>
                  <TableCell>{row.version}</TableCell>
                  <TableCell>{row.deploymentId}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  )
}

