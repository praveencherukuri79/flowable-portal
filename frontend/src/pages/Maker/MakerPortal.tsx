import React from 'react'
import { Typography, Container } from '@mui/material'
import { MyTasks } from './MyTasks'

export const MakerPortal: React.FC = () => {
  return (
    <Container maxWidth="xl">
      <Typography variant="h4" sx={{ mb: 3 }}>
        Maker Portal
      </Typography>
      <MyTasks />
    </Container>
  )
}

