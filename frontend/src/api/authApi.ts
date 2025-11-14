import axios from 'axios'

const API_URL = '/api/auth'

export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
  email: string
  fullName: string
  role: string
}

export interface AuthResponse {
  token: string
  username: string
  role: string
  fullName: string
}

export const authApi = {
  login: async (data: LoginRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(`${API_URL}/login`, data)
    return response.data
  },

  register: async (data: RegisterRequest): Promise<AuthResponse> => {
    const response = await axios.post<AuthResponse>(`${API_URL}/register`, data)
    return response.data
  }
}

// Axios interceptor for adding auth token to requests
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// Axios interceptor for handling auth errors
axios.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Clear auth state and redirect to login
      localStorage.clear()
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

