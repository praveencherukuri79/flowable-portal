import React from 'react'
import { Card, CardContent, Typography } from '@mui/material'
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

