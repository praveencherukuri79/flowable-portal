import React from 'react'
import { Box, Tabs, Tab, Toolbar, AppBar, Container, Typography } from '@mui/material'
import Dashboard from './Dashboard'
import Definitions from './Definitions'
import Instances from './Instances'
import Tasks from './Tasks'
import Events from './Events'
import { UserManagement } from './UserManagement'
import { ProcessControl } from './ProcessControl'

export function AdminPortal() {
  const [tab, setTab] = React.useState(0)

  return (
    <Box sx={{ bgcolor: 'background.default', minHeight: '100vh' }}>
      <AppBar position="sticky" color="default" elevation={0}>
        <Toolbar>
          <Typography variant="h6" fontWeight={600}>
            Flowable Admin Portal
          </Typography>
          <Box sx={{ flex: 1 }} />
          <Tabs
            value={tab}
            onChange={(_: React.SyntheticEvent, v: number) => setTab(v)}
            textColor="primary"
            indicatorColor="primary"
          >
            <Tab label="Dashboard" />
            <Tab label="Definitions" />
            <Tab label="Instances" />
            <Tab label="Tasks" />
            <Tab label="Events" />
            <Tab label="Users" />
            <Tab label="Process Control" />
          </Tabs>
        </Toolbar>
      </AppBar>

      <Container maxWidth="xl" sx={{ py: 3 }}>
        {tab === 0 && <Dashboard />}
        {tab === 1 && <Definitions />}
        {tab === 2 && <Instances />}
        {tab === 3 && <Tasks />}
        {tab === 4 && <Events />}
        {tab === 5 && <UserManagement />}
        {tab === 6 && <ProcessControl />}
      </Container>
    </Box>
  )
}

