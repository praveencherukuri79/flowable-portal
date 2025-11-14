import React from 'react'
import { Card, CardContent, Typography, Chip } from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { adminApi, Task } from '../../api/adminApi'
import dayjs from '../../utils/dayjs'

export default function TasksPage() {
  const [rows, setRows] = React.useState<Task[]>([])
  const [total, setTotal] = React.useState(0)
  const [page, setPage] = React.useState(0)
  const [pageSize, setPageSize] = React.useState(25)

  React.useEffect(() => {
    adminApi
      .searchTasks({ page, size: pageSize })
      .then((res) => {
        setRows(res.data.content)
        setTotal(res.data.total)
      })
  }, [page, pageSize])

  const columns: GridColDef[] = [
    { field: 'id', headerName: 'Task ID', flex: 1.2 },
    { field: 'name', headerName: 'Name', flex: 1.5 },
    { field: 'assignee', headerName: 'Assignee', width: 180 },
    { field: 'processInstanceId', headerName: 'Process Instance', flex: 1 },
    {
      field: 'createTime',
      headerName: 'Created',
      flex: 1,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm:ss') : '',
    },
    {
      field: 'state',
      headerName: 'State',
      width: 160,
      renderCell: (params) => (
        <Chip
          label={params.value}
          color={params.value === 'CLAIMABLE' ? 'info' : 'success'}
          size="small"
        />
      ),
    },
  ]

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          User Tasks
        </Typography>
        <div style={{ height: 560 }}>
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
        </div>
      </CardContent>
    </Card>
  )
}

