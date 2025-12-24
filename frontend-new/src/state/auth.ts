import { atom } from 'recoil'

export interface AuthState {
  token: string | null
  username: string | null
  role: string | null
  fullName: string | null
  isAuthenticated: boolean
}

const getInitialAuthState = (): AuthState => {
  const token = localStorage.getItem('token')
  const username = localStorage.getItem('username')
  const role = localStorage.getItem('role')
  const fullName = localStorage.getItem('fullName')
  
  return {
    token,
    username,
    role,
    fullName,
    isAuthenticated: !!token
  }
}

export const authState = atom<AuthState>({
  key: 'authState',
  default: getInitialAuthState()
})

