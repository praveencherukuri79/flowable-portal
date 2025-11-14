import axios from 'axios'

const api = axios.create({ baseURL: '/api/admin' })

// Add auth interceptor to this instance
api.interceptors.request.use(
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

export interface ProcessDefinition {
  id: string
  key: string
  name: string
  category: string
  description: string
  version: number
  deploymentId: string
  resourceName: string
  diagramResourceName: string
  suspended: boolean
}

export interface ProcessInstance {
  id: string
  definitionId: string
  definitionKey: string
  businessKey: string
  startUserId: string
  startTime: string
  endTime: string | null
  state: string
  tenantId: string
  variables: Record<string, any>
}

export interface Task {
  id: string
  name: string
  assignee: string | null
  owner: string | null
  createTime: string
  dueDate: string | null
  processInstanceId: string
  processDefinitionId: string
  state: string
  variables: Record<string, any>
}

export interface EventLog {
  id: string
  timestamp: string
  type: string
  processDefinitionId: string
  processInstanceId: string
  executionId: string
  data: string | null
}

export interface Metrics {
  runningInstances: number
  completedInstances: number
  totalTasks: number
  instancesByDay: { day: string; count: number }[]
  tasksByState: { state: string; count: number }[]
  avgDurationByDefinition: { definitionKey: string; minutes: number }[]
}

export interface PagedResponse<T> {
  content: T[]
  total: number
}

export const adminApi = {
  // Process Definitions
  getDefinitions: () => api.get<ProcessDefinition[]>('/definitions'),
  getAllDefinitions: async () => {
    const response = await api.get<ProcessDefinition[]>('/definitions')
    return response.data
  },
  
  // Process Instances
  searchInstances: (params: {
    definitionKey?: string
    state?: string
    page?: number
    size?: number
  }) => api.get<PagedResponse<ProcessInstance>>('/instances/search', { params }),
  searchProcessInstances: async (params: {
    definitionKey?: string
    state?: string
    page?: number
    size?: number
  }) => {
    const response = await api.get<PagedResponse<ProcessInstance>>('/instances/search', { params })
    return response.data
  },
  
  // Tasks
  searchTasks: (params: {
    candidateGroup?: string
    state?: string
    page?: number
    size?: number
  }) => api.get<PagedResponse<Task>>('/tasks/search', { params }),
  
  // Events
  searchEvents: (params: { limit?: number }) => api.get<EventLog[]>('/events/search', { params }),
  
  // Metrics
  getMetrics: () => api.get<Metrics>('/metrics'),
  
  // Diagrams
  getDiagramSvg: (procInstId: string) =>
    api.get<string>(`/diagram/${procInstId}`, { headers: { Accept: 'image/svg+xml' } }),
}

