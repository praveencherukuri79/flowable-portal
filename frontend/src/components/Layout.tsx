import React from 'react'
import { useNavigate } from 'react-router-dom'
import { useRecoilValue, useSetRecoilState } from 'recoil'
import {
  AppBar,
  Box,
  Toolbar,
  Typography,
  IconButton,
  Menu,
  MenuItem,
  Chip
} from '@mui/material'
import AccountCircle from '@mui/icons-material/AccountCircle'
import { authState } from '../state/auth'

interface LayoutProps {
  children: React.ReactNode
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null)
  const auth = useRecoilValue(authState)
  const setAuth = useSetRecoilState(authState)
  const navigate = useNavigate()

  const handleMenu = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget)
  }

  const handleClose = () => {
    setAnchorEl(null)
  }

  const handleLogout = () => {
    localStorage.clear()
    setAuth({
      token: null,
      username: null,
      role: null,
      fullName: null,
      isAuthenticated: false
    })
    navigate('/login')
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', minHeight: '100vh' }}>
      <AppBar position="static">
        <Toolbar>
          <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
            Flowable Portal
          </Typography>

          {auth.isAuthenticated && (
            <>
              <Chip
                label={auth.role}
                color="secondary"
                size="small"
                sx={{ mr: 2 }}
              />
              <Typography variant="body2" sx={{ mr: 2 }}>
                {auth.fullName}
              </Typography>
              <IconButton
                size="large"
                onClick={handleMenu}
                color="inherit"
              >
                <AccountCircle />
              </IconButton>
              <Menu
                anchorEl={anchorEl}
                anchorOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                keepMounted
                transformOrigin={{
                  vertical: 'top',
                  horizontal: 'right',
                }}
                open={Boolean(anchorEl)}
                onClose={handleClose}
              >
                <MenuItem onClick={handleLogout}>Logout</MenuItem>
              </Menu>
            </>
          )}
        </Toolbar>
      </AppBar>

      <Box component="main" sx={{ flexGrow: 1, bgcolor: 'background.default', p: 3 }}>
        {children}
      </Box>
    </Box>
  )
}

