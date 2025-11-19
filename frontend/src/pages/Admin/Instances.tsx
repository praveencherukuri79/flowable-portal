import React from 'react'
import { Card, CardContent, Typography, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TablePagination } from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { adminApi, ProcessInstance } from '../../api/adminApi'
import dayjs from '../../utils/dayjs'

export default function InstancesPage() {
  const [rows, setRows] = React.useState<ProcessInstance[]>([])
  const [total, setTotal] = React.useState(0)
  const [page, setPage] = React.useState(0)
  const [pageSize, setPageSize] = React.useState(25)

  React.useEffect(() => {
    adminApi
      .searchInstances({ page, size: pageSize })
      .then((res) => {
        setRows(res.data.content)
        setTotal(res.data.total)
      })
  }, [page, pageSize])

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'Instance ID', flex: 1.3 },
    { field: 'definitionKey', headerName: 'Definition', flex: 1 },
    { field: 'businessKey', headerName: 'Business Key', flex: 1 },
    { field: 'state', headerName: 'State', width: 140 },
    {
      field: 'startTime',
      headerName: 'Start Time',
      flex: 1,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm:ss') : '',
    },
    {
      field: 'endTime',
      headerName: 'End Time',
      flex: 1,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm:ss') : '',
    },
  ]

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Process Instances
        </Typography>
        
        {/* ===== DATA GRID VERSION (Comment out to use Table) ===== */}
        {/* <div style={{ height: 560 }}>
          <DataGrid
            rows={rows}
            columns={columns}
            getRowId={(r) => r.id}
            disableRowSelectionOnClick
            pagination
            paginationMode="server"
            rowCount={total}
            paginationModel={{ page, pageSize }}
            onPaginationModelChange={(model) => {
              setPage(model.page)
              setPageSize(model.pageSize)
            }}
            pageSizeOptions={[10, 25, 50, 100]}
          />
        </div> */}
        
        {/* ===== MUI TABLE VERSION (Comment out to use DataGrid) ===== */}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell><strong>Instance ID</strong></TableCell>
                <TableCell><strong>Definition</strong></TableCell>
                <TableCell><strong>Business Key</strong></TableCell>
                <TableCell><strong>State</strong></TableCell>
                <TableCell><strong>Start Time</strong></TableCell>
                <TableCell><strong>End Time</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {rows.map((row) => (
                <TableRow key={row.id} hover>
                  <TableCell>{row.id}</TableCell>
                  <TableCell>{row.definitionKey}</TableCell>
                  <TableCell>{row.businessKey}</TableCell>
                  <TableCell>{row.state}</TableCell>
                  <TableCell>
                    {row.startTime ? dayjs(row.startTime).format('YYYY-MM-DD HH:mm:ss') : ''}
                  </TableCell>
                  <TableCell>
                    {row.endTime ? dayjs(row.endTime).format('YYYY-MM-DD HH:mm:ss') : ''}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <TablePagination
            component="div"
            count={total}
            page={page}
            onPageChange={(_, newPage) => setPage(newPage)}
            rowsPerPage={pageSize}
            onRowsPerPageChange={(e) => setPageSize(parseInt(e.target.value, 10))}
            rowsPerPageOptions={[10, 25, 50, 100]}
          />
        </TableContainer>
      </CardContent>
    </Card>
  )
}

