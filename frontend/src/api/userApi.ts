import axios from 'axios'

const API_URL = '/api/admin/users'

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  role: string
  enabled: boolean
  createdAt?: string
  updatedAt?: string
}

export interface CreateUserRequest {
  username: string
  password: string
  email: string
  fullName: string
  role: string
}

export const userApi = {
  getAllUsers: async (): Promise<User[]> => {
    const response = await axios.get<User[]>(API_URL)
    return response.data
  },

  getUserByUsername: async (username: string): Promise<User> => {
    const response = await axios.get<User>(`${API_URL}/${username}`)
    return response.data
  },

  createUser: async (data: CreateUserRequest) => {
    const response = await axios.post(API_URL, data)
    return response.data
  },

  updateUser: async (id: number, user: User) => {
    const response = await axios.put(`${API_URL}/${id}`, user)
    return response.data
  },

  deleteUser: async (id: number) => {
    const response = await axios.delete(`${API_URL}/${id}`)
    return response.data
  }
}

