import { atom } from 'recoil'

export interface AuthState {
  token: string | null
  username: string | null
  role: string | null
  fullName: string | null
  isAuthenticated: boolean
}

export const authState = atom<AuthState>({
  key: 'authState',
  default: {
    token: localStorage.getItem('token'),
    username: localStorage.getItem('username'),
    role: localStorage.getItem('role'),
    fullName: localStorage.getItem('fullName'),
    isAuthenticated: !!localStorage.getItem('token')
  }
})

