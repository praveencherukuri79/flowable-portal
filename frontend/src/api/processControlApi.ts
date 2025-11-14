import axios from 'axios'

const api = axios.create({ baseURL: '/api/admin' })

// Add auth interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
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
  processDefinitionId: string
  processDefinitionKey: string
  name: string
  businessKey: string
  status: string
  startTime: string
  endTime?: string
  startUserId: string
  suspended: boolean
}

export interface TaskInfo {
  id: string
  name: string
  description: string
  assignee: string
  owner: string
  processInstanceId: string
  processDefinitionId: string
  taskDefinitionKey: string
  createTime: string
  dueDate?: string
  priority: number
  suspended: boolean
}

export const processControlApi = {
  // Process Definitions
  getAllDefinitions: async (): Promise<ProcessDefinition[]> => {
    const response = await api.get<ProcessDefinition[]>('/definitions')
    return response.data
  },

  startProcessByKey: async (processKey: string, businessKey?: string, variables?: Record<string, unknown>) => {
    const response = await axios.post(`/api/admin/process/start/${processKey}`, {
      businessKey,
      variables
    }, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  // Process Instances
  getAllRunningInstances: async (): Promise<ProcessInstance[]> => {
    const response = await axios.get('/api/admin/process/instances/running', {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  getInstanceDetails: async (processInstanceId: string) => {
    const response = await axios.get(`/api/admin/process/instances/${processInstanceId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  getInstanceVariables: async (processInstanceId: string): Promise<Record<string, unknown>> => {
    const response = await axios.get(`/api/admin/process/instances/${processInstanceId}/variables`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  updateInstanceVariables: async (processInstanceId: string, variables: Record<string, unknown>) => {
    const response = await axios.put(`/api/admin/process/instances/${processInstanceId}/variables`, variables, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  deleteInstance: async (processInstanceId: string, deleteReason?: string) => {
    const response = await axios.delete(`/api/admin/process/instances/${processInstanceId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      params: { deleteReason }
    })
    return response.data
  },

  suspendInstance: async (processInstanceId: string) => {
    const response = await axios.post(`/api/admin/process/instances/${processInstanceId}/suspend`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  activateInstance: async (processInstanceId: string) => {
    const response = await axios.post(`/api/admin/process/instances/${processInstanceId}/activate`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  // Tasks
  getTasksByProcessInstance: async (processInstanceId: string): Promise<TaskInfo[]> => {
    const response = await axios.get(`/api/admin/process/instances/${processInstanceId}/tasks`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  assignTask: async (taskId: string, userId: string) => {
    const response = await axios.post(`/api/admin/tasks/${taskId}/assign`, null, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      params: { userId }
    })
    return response.data
  },

  unclaimTask: async (taskId: string) => {
    const response = await axios.post(`/api/admin/tasks/${taskId}/unclaim`, {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  completeTask: async (taskId: string, variables?: Record<string, unknown>) => {
    const response = await axios.post(`/api/admin/tasks/${taskId}/complete`, variables || {}, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  },

  deleteTask: async (taskId: string, deleteReason?: string) => {
    const response = await axios.delete(`/api/admin/tasks/${taskId}`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` },
      params: { deleteReason }
    })
    return response.data
  },

  getTaskVariables: async (taskId: string): Promise<Record<string, unknown>> => {
    const response = await axios.get(`/api/admin/tasks/${taskId}/variables`, {
      headers: { Authorization: `Bearer ${localStorage.getItem('token')}` }
    })
    return response.data
  }
}

