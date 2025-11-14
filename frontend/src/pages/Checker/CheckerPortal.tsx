import React from 'react'
import { Typography, Container } from '@mui/material'
import { PendingApprovals } from './PendingApprovals'

export const CheckerPortal: React.FC = () => {
  return (
    <Container maxWidth="xl">
      <Typography variant="h4" sx={{ mb: 3 }}>
        Checker Portal
      </Typography>
      <PendingApprovals />
    </Container>
  )
}

