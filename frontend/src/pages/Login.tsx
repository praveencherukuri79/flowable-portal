import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useSetRecoilState } from 'recoil'
import {
  Box,
  Button,
  Container,
  Paper,
  TextField,
  Typography,
  Alert,
  CircularProgress
} from '@mui/material'
import { authState } from '../state/auth'
import { authApi } from '../api/authApi'

export const Login: React.FC = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const setAuth = useSetRecoilState(authState)

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault()
    setError('')
    setLoading(true)

    try {
      // Trim username to prevent whitespace issues
      const trimmedUsername = username.trim()
      if (!trimmedUsername) {
        setError('Username is required')
        setLoading(false)
        return
      }
      
      const response = await authApi.login({ username: trimmedUsername, password })
      
      // Store in localStorage
      localStorage.setItem('token', response.token)
      localStorage.setItem('username', response.username)
      localStorage.setItem('role', response.role)
      localStorage.setItem('fullName', response.fullName)

      // Update Recoil state
      setAuth({
        token: response.token,
        username: response.username,
        role: response.role,
        fullName: response.fullName,
        isAuthenticated: true
      })

      // Redirect based on role
      const path = response.role === 'ADMIN' ? '/admin' 
                 : response.role === 'MAKER' ? '/maker' 
                 : '/checker'
      navigate(path)
    } catch (err: unknown) {
      setError('Invalid username or password')
      console.error('Login error:', err)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Container maxWidth="sm">
      <Box
        sx={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}
      >
        <Paper elevation={3} sx={{ p: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" gutterBottom align="center">
            Flowable Portal
          </Typography>
          <Typography variant="body2" color="text.secondary" align="center" sx={{ mb: 3 }}>
            Sign in to continue
          </Typography>

          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          <form onSubmit={handleLogin}>
            <TextField
              fullWidth
              label="Username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              margin="normal"
              required
              autoFocus
              disabled={loading}
            />
            <TextField
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
              disabled={loading}
            />
            <Button
              type="submit"
              fullWidth
              variant="contained"
              size="large"
              sx={{ mt: 3 }}
              disabled={loading}
            >
              {loading ? <CircularProgress size={24} /> : 'Sign In'}
            </Button>
          </form>

          <Box sx={{ mt: 3 }}>
            <Typography variant="caption" color="text.secondary">
              Demo Users:
            </Typography>
            <Typography variant="caption" display="block" color="text.secondary">
              • Maker: maker1 / password123
            </Typography>
            <Typography variant="caption" display="block" color="text.secondary">
              • Checker: checker1 / password123
            </Typography>
            <Typography variant="caption" display="block" color="text.secondary">
              • Admin: admin / admin123
            </Typography>
          </Box>
        </Paper>
      </Box>
    </Container>
  )
}

