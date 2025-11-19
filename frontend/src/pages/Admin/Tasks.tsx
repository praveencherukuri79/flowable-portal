import React from 'react'
import { Card, CardContent, Typography, Chip, Button, Snackbar, Alert, Divider, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Paper, TablePagination } from '@mui/material'
import { DataGrid, GridColDef, GridRenderCellParams } from '@mui/x-data-grid'
import { useNavigate } from 'react-router-dom'
import { flowableApi, TaskDto } from '../../api/flowableApi'
import { adminApi, Task } from '../../api/adminApi'
import dayjs from '../../utils/dayjs'
import PlayArrowIcon from '@mui/icons-material/PlayArrow'
import LockOpenIcon from '@mui/icons-material/LockOpen'

export default function TasksPage() {
  // Admin Tasks (claimable/actionable)
  const [adminTasks, setAdminTasks] = React.useState<TaskDto[]>([])
  
  // All Tasks (readonly view)
  const [allTasks, setAllTasks] = React.useState<Task[]>([])
  const [allTotal, setAllTotal] = React.useState(0)
  const [allPage, setAllPage] = React.useState(0)
  const [allPageSize, setAllPageSize] = React.useState(25)
  
  // Table pagination states
  const [adminTablePage, setAdminTablePage] = React.useState(0)
  const [adminTableRowsPerPage, setAdminTableRowsPerPage] = React.useState(5)
  
  const [error, setError] = React.useState('')
  const [success, setSuccess] = React.useState('')
  const navigate = useNavigate()

  const loadAdminTasks = async () => {
    try {
      const data = await flowableApi.getTasksByGroupOrAssigned('ADMIN')
      setAdminTasks(data)
    } catch (err) {
      setError('Failed to load admin tasks')
      console.error('Load admin tasks error:', err)
    }
  }

  const loadAllTasks = async () => {
    try {
      const res = await adminApi.searchTasks({ page: allPage, size: allPageSize })
      setAllTasks(res.data.content)
      setAllTotal(res.data.total)
    } catch (err) {
      setError('Failed to load all tasks')
      console.error('Load all tasks error:', err)
    }
  }

  React.useEffect(() => {
    loadAdminTasks()
    loadAllTasks()
  }, [allPage, allPageSize])

  const handleClaim = async (taskId: string) => {
    try {
      await flowableApi.claimTask(taskId)
      setSuccess('Task claimed successfully')
      loadAdminTasks()
    } catch (err) {
      setError('Failed to claim task')
      console.error('Claim task error:', err)
    }
  }

  const handleOpen = (task: TaskDto) => {
    if (task.formKey) {
      // Navigate using formKey - pass formKey in state for backend API calls
      navigate(task.formKey, { state: { taskId: task.id, processInstanceId: task.processInstanceId, formKey: task.formKey } })
    }
  }

  // Admin Tasks columns (with actions)
  const adminColumns: GridColDef[] = [
    { field: 'id', headerName: 'Task ID', flex: 1 },
    { field: 'name', headerName: 'Task Name', flex: 1.5 },
    {
      field: 'assignee',
      headerName: 'Status',
      width: 150,
      renderCell: (params: GridRenderCellParams) => (
        params.value ? (
          <Chip label="Assigned" color="success" size="small" />
        ) : (
          <Chip label="Claimable" color="default" size="small" />
        )
      )
    },
    { field: 'processInstanceId', headerName: 'Process Instance', flex: 1 },
    {
      field: 'createTime',
      headerName: 'Created',
      width: 180,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm') : '',
    },
    {
      field: 'actions',
      headerName: 'Actions',
      width: 200,
      renderCell: (params: GridRenderCellParams) => {
        const task = params.row as TaskDto
        if (!task.assignee) {
          return (
            <Button
              size="small"
              variant="outlined"
              startIcon={<LockOpenIcon />}
              onClick={() => handleClaim(task.id)}
            >
              Claim
            </Button>
          )
        }
        return (
          <Button
            size="small"
            variant="contained"
            startIcon={<PlayArrowIcon />}
            onClick={() => handleOpen(task)}
            disabled={!task.formKey}
          >
            Open
          </Button>
        )
      }
    },
  ]

  // All Tasks columns (readonly)
  const allColumns: GridColDef[] = [
    { field: 'id', headerName: 'Task ID', flex: 1 },
    { field: 'name', headerName: 'Name', flex: 1.5 },
    { field: 'assignee', headerName: 'Assignee', width: 180 },
    { field: 'processInstanceId', headerName: 'Process Instance', flex: 1 },
    {
      field: 'createTime',
      headerName: 'Created',
      width: 180,
      valueFormatter: (params) =>
        params.value ? dayjs(params.value).format('YYYY-MM-DD HH:mm') : '',
    },
    {
      field: 'state',
      headerName: 'State',
      width: 140,
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
    <>
      {/* Section 1: Admin Tasks (Claimable/Actionable) */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            My Admin Tasks
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Tasks assigned to ADMIN group that you can claim and complete
          </Typography>
          
          {/* ===== DATA GRID VERSION (Comment out to use Table) ===== */}
          {/* <div style={{ height: 400 }}>
            <DataGrid
              rows={adminTasks}
              columns={adminColumns}
              getRowId={(r) => r.id}
              disableRowSelectionOnClick
              pagination
              paginationMode="client"
              pageSizeOptions={[5, 10, 25]}
              initialState={{
                pagination: { paginationModel: { pageSize: 5 } },
              }}
            />
          </div> */}
          
          {/* ===== MUI TABLE VERSION (Comment out to use DataGrid) ===== */}
          <TableContainer component={Paper} sx={{ maxHeight: 400 }}>
            <Table stickyHeader>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Task ID</strong></TableCell>
                  <TableCell><strong>Task Name</strong></TableCell>
                  <TableCell><strong>Status</strong></TableCell>
                  <TableCell><strong>Process Instance</strong></TableCell>
                  <TableCell><strong>Created</strong></TableCell>
                  <TableCell><strong>Actions</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {adminTasks.slice(adminTablePage * adminTableRowsPerPage, adminTablePage * adminTableRowsPerPage + adminTableRowsPerPage).map((task) => (
                  <TableRow key={task.id} hover>
                    <TableCell>{task.id}</TableCell>
                    <TableCell>{task.name}</TableCell>
                    <TableCell>
                      {task.assignee ? (
                        <Chip label="Assigned" color="success" size="small" />
                      ) : (
                        <Chip label="Claimable" color="default" size="small" />
                      )}
                    </TableCell>
                    <TableCell>{task.processInstanceId}</TableCell>
                    <TableCell>
                      {task.createTime ? dayjs(task.createTime).format('YYYY-MM-DD HH:mm') : ''}
                    </TableCell>
                    <TableCell>
                      {!task.assignee ? (
                        <Button
                          size="small"
                          variant="outlined"
                          startIcon={<LockOpenIcon />}
                          onClick={() => handleClaim(task.id)}
                        >
                          Claim
                        </Button>
                      ) : (
                        <Button
                          size="small"
                          variant="contained"
                          startIcon={<PlayArrowIcon />}
                          onClick={() => handleOpen(task)}
                          disabled={!task.formKey}
                        >
                          Open
                        </Button>
                      )}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={adminTasks.length}
              page={adminTablePage}
              onPageChange={(_, newPage) => setAdminTablePage(newPage)}
              rowsPerPage={adminTableRowsPerPage}
              onRowsPerPageChange={(e) => {
                setAdminTableRowsPerPage(parseInt(e.target.value, 10))
                setAdminTablePage(0)
              }}
              rowsPerPageOptions={[5, 10, 25]}
            />
          </TableContainer>
        </CardContent>
      </Card>

      <Divider sx={{ my: 3 }} />

      {/* Section 2: All Tasks (Readonly) */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            All User Tasks (All Groups)
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Complete view of all tasks across MAKER, CHECKER, and ADMIN groups
          </Typography>
          
          {/* ===== DATA GRID VERSION (Comment out to use Table) ===== */}
          {/* <div style={{ height: 500 }}>
            <DataGrid
              rows={allTasks}
              columns={allColumns}
              getRowId={(r) => r.id}
              disableRowSelectionOnClick
              pagination
              paginationMode="server"
              rowCount={allTotal}
              paginationModel={{ page: allPage, pageSize: allPageSize }}
              onPaginationModelChange={(model) => {
                setAllPage(model.page)
                setAllPageSize(model.pageSize)
              }}
              pageSizeOptions={[10, 25, 50, 100]}
            />
          </div> */}
          
          {/* ===== MUI TABLE VERSION (Comment out to use DataGrid) ===== */}
          <TableContainer component={Paper}>
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell><strong>Task ID</strong></TableCell>
                  <TableCell><strong>Name</strong></TableCell>
                  <TableCell><strong>Assignee</strong></TableCell>
                  <TableCell><strong>Process Instance</strong></TableCell>
                  <TableCell><strong>Created</strong></TableCell>
                  <TableCell><strong>State</strong></TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {allTasks.map((task) => (
                  <TableRow key={task.id} hover>
                    <TableCell>{task.id}</TableCell>
                    <TableCell>{task.name}</TableCell>
                    <TableCell>{task.assignee}</TableCell>
                    <TableCell>{task.processInstanceId}</TableCell>
                    <TableCell>
                      {task.createTime ? dayjs(task.createTime).format('YYYY-MM-DD HH:mm') : ''}
                    </TableCell>
                    <TableCell>
                      <Chip
                        label={task.state}
                        color={task.state === 'CLAIMABLE' ? 'info' : 'success'}
                        size="small"
                      />
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={allTotal}
              page={allPage}
              onPageChange={(_, newPage) => setAllPage(newPage)}
              rowsPerPage={allPageSize}
              onRowsPerPageChange={(e) => {
                setAllPageSize(parseInt(e.target.value, 10))
                setAllPage(0)
              }}
              rowsPerPageOptions={[10, 25, 50, 100]}
            />
          </TableContainer>
        </CardContent>
      </Card>
      
      <Snackbar
        open={!!error}
        autoHideDuration={6000}
        onClose={() => setError('')}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="error" onClose={() => setError('')}>
          {error}
        </Alert>
      </Snackbar>
      
      <Snackbar
        open={!!success}
        autoHideDuration={3000}
        onClose={() => setSuccess('')}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert severity="success" onClose={() => setSuccess('')}>
          {success}
        </Alert>
      </Snackbar>
    </>
  )
}

