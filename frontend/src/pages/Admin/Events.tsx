import React from 'react'
import { Card, CardContent, Typography } from '@mui/material'
import { DataGrid, GridColDef } from '@mui/x-data-grid'
import { adminApi, EventLog } from '../../api/adminApi'
import dayjs from '../../utils/dayjs'

export default function EventsPage() {
  const [rows, setRows] = React.useState<EventLog[]>([])

  React.useEffect(() => {
    adminApi.searchEvents({ limit: 200 }).then((res) => setRows(res.data))
  }, [])

  const columns: GridColDef[] = [
    {
      field: 'timestamp',
      headerName: 'Time',
      flex: 1,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm:ss') : '',
    },
    { field: 'type', headerName: 'Type', flex: 1 },
    { field: 'processInstanceId', headerName: 'Process Instance', flex: 1 },
    { field: 'executionId', headerName: 'Execution ID', flex: 1 },
  ]

  return (
    <Card>
      <CardContent>
        <Typography variant="h6" gutterBottom>
          Event Logs
        </Typography>
        <div style={{ height: 560 }}>
          <DataGrid
            rows={rows}
            columns={columns}
            getRowId={(r) => r.id}
            disableRowSelectionOnClick
          />
        </div>
      </CardContent>
    </Card>
  )
}

