import axios from 'axios'

interface LoginRequest {
  username: string
  password: string
}

interface LoginResponse {
  token: string
  username: string
  role: string
  fullName: string
  expiresIn?: number
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await axios.post<LoginResponse>('/api/auth/login', credentials)
    return response.data
  },

  logout: async (): Promise<void> => {
    try {
      await axios.post('/api/auth/logout')
    } catch {
      // Ignore errors on logout
    }
  },

  validateToken: async (): Promise<boolean> => {
    try {
      const token = localStorage.getItem('token')
      if (!token) return false
      await axios.get('/api/auth/validate', {
        headers: { Authorization: `Bearer ${token}` }
      })
      return true
    } catch {
      return false
    }
  },
}

export default authApi

