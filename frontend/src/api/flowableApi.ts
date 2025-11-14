import axios from 'axios'

// Generic Flowable API - handles ALL workflow operations

export interface ProcessStartResponse {
  processInstanceId: string
  processDefinitionId: string
  processKey: string
  businessKey: string
  message: string
}

export interface TaskDto {
  id: string
  name: string
  assignee: string | null
  createTime: string
  processInstanceId: string
  processDefinitionId: string
  taskDefinitionKey: string
  formKey?: string
  description?: string
  priority?: number
  dueDate?: string
  state?: string
}

export interface TaskActionResponse {
  taskId: string
  action: string
  performedBy: string
  message: string
  taskState: string
}

export const flowableApi = {
  // ============ RUNTIME (Process Management) ============
  
  startProcess: async (processKey: string, variables?: Record<string, unknown>): Promise<ProcessStartResponse> => {
    const response = await axios.post<ProcessStartResponse>(
      `/api/flowable/runtime/start/${processKey}`,
      variables || {}
    )
    return response.data
  },

  getProcessVariables: async (processInstanceId: string): Promise<Record<string, unknown>> => {
    const response = await axios.get(`/api/flowable/runtime/${processInstanceId}/variables`)
    return response.data
  },

  setProcessVariable: async (processInstanceId: string, variableName: string, value: unknown) => {
    await axios.put(`/api/flowable/runtime/${processInstanceId}/variables/${variableName}`, value)
  },

  suspendProcess: async (processInstanceId: string) => {
    await axios.post(`/api/flowable/runtime/suspend/${processInstanceId}`)
  },

  activateProcess: async (processInstanceId: string) => {
    await axios.post(`/api/flowable/runtime/activate/${processInstanceId}`)
  },

  deleteProcess: async (processInstanceId: string, reason?: string) => {
    await axios.delete(`/api/flowable/runtime/${processInstanceId}`, {
      params: { reason }
    })
  },

  // ============ TASK (Task Management) ============

  getMyTasks: async (): Promise<TaskDto[]> => {
    const response = await axios.get<TaskDto[]>('/api/flowable/task/my-tasks')
    return response.data
  },

  getTasksByCandidateGroup: async (group: string): Promise<TaskDto[]> => {
    const response = await axios.get<TaskDto[]>(`/api/flowable/task/candidate-group/${group}`)
    return response.data
  },

  getTasksByGroupOrAssigned: async (group: string): Promise<TaskDto[]> => {
    const response = await axios.get<TaskDto[]>(`/api/flowable/task/candidate-group-or-assigned/${group}`)
    return response.data
  },

  getTaskVariables: async (taskId: string): Promise<Record<string, unknown>> => {
    const response = await axios.get<Record<string, unknown>>(`/api/flowable/task/${taskId}/variables`)
    return response.data
  },

  claimTask: async (taskId: string): Promise<TaskActionResponse> => {
    const response = await axios.post<TaskActionResponse>(`/api/flowable/task/${taskId}/claim`)
    return response.data
  },

  completeTask: async (taskId: string, variables?: Record<string, unknown>): Promise<TaskActionResponse> => {
    const response = await axios.post<TaskActionResponse>(
      `/api/flowable/task/complete/${taskId}`,
      variables || {}
    )
    return response.data
  },

  assignTask: async (taskId: string, user: string): Promise<TaskActionResponse> => {
    const response = await axios.post<TaskActionResponse>(
      `/api/flowable/task/assign/${taskId}`,
      null,
      { params: { user } }
    )
    return response.data
  },

  reassignTask: async (taskId: string, newUser: string): Promise<TaskActionResponse> => {
    const response = await axios.post<TaskActionResponse>(
      `/api/flowable/task/reassign/${taskId}`,
      null,
      { params: { newUser } }
    )
    return response.data
  },

  delegateTask: async (taskId: string, delegateUser: string): Promise<TaskActionResponse> => {
    const response = await axios.post<TaskActionResponse>(
      `/api/flowable/task/delegate/${taskId}`,
      null,
      { params: { delegateUser } }
    )
    return response.data
  }
}

// ============ DATA QUERY (Read-only business data) ============

export interface Product {
  id?: number
  sheetId: string
  productName: string
  rate: number
  api: string
  effectiveDate: string
  status: string
  editedBy?: string
  editedAt?: string
  approvedBy?: string
  approvedAt?: string
}

export interface Plan {
  id?: number
  sheetId: string
  planCode: string
  planName: string
  description?: string
  status: string
  editedBy?: string
  editedAt?: string
  approvedBy?: string
  approvedAt?: string
}

export interface Item {
  id?: number
  sheetId: string
  itemCode: string
  itemName: string
  description?: string
  status: string
  editedBy?: string
  editedAt?: string
  approvedBy?: string
  approvedAt?: string
}

export const dataQueryApi = {
  getProductsBySheet: async (sheetId: string): Promise<Product[]> => {
    const response = await axios.get<Product[]>(`/api/data/products/sheet/${sheetId}`)
    return response.data
  },

  getPlansBySheet: async (sheetId: string): Promise<Plan[]> => {
    const response = await axios.get<Plan[]>(`/api/data/plans/sheet/${sheetId}`)
    return response.data
  },

  getItemsBySheet: async (sheetId: string): Promise<Item[]> => {
    const response = await axios.get<Item[]>(`/api/data/items/sheet/${sheetId}`)
    return response.data
  }
}

