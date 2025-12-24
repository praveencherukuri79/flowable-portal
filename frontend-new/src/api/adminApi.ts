import axios from 'axios'
import type {
  ProcessDefinition,
  ProcessInstance,
  Task,
  EventLog,
  Metrics,
  PagedResponse,
  ProcessStatistics,
  TaskStatistics,
} from '../types'

const api = axios.create({ baseURL: '/api' })

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

export const adminApi = {
  // ============ PROCESS DEFINITIONS ============
  
  /**
   * Get all deployed process definitions
   * GET /api/admin/definitions
   */
  getDefinitions: async (): Promise<ProcessDefinition[]> => {
    const response = await api.get<ProcessDefinition[]>('/admin/definitions')
    return response.data
  },

  // ============ PROCESS INSTANCES ============

  /**
   * Search process instances with filters
   * GET /api/admin/instances/search
   */
  searchInstances: async (params: {
    definitionKey?: string
    state?: string
    page?: number
    size?: number
  }): Promise<PagedResponse<ProcessInstance>> => {
    const response = await api.get<PagedResponse<ProcessInstance>>('/admin/instances/search', { params })
    return response.data
  },

  /**
   * Get running instances (convenience method)
   */
  getRunningInstances: async (page = 0, size = 25): Promise<PagedResponse<ProcessInstance>> => {
    return adminApi.searchInstances({ state: 'RUNNING', page, size })
  },

  /**
   * Get completed instances (convenience method)
   */
  getCompletedInstances: async (page = 0, size = 25): Promise<PagedResponse<ProcessInstance>> => {
    return adminApi.searchInstances({ state: 'COMPLETED', page, size })
  },

  /**
   * Get specific historic process instance
   * GET /api/flowable/history/process/instance/{processInstanceId}
   */
  getInstanceById: async (processInstanceId: string): Promise<ProcessInstance> => {
    const response = await api.get<ProcessInstance>(`/flowable/history/process/instance/${processInstanceId}`)
    return response.data
  },

  /**
   * Get process variables for an instance
   * For running instances: GET /api/flowable/runtime/{processInstanceId}/variables
   * For completed instances: falls back to variables embedded in the instance DTO
   */
  getInstanceVariables: async (processInstanceId: string): Promise<Record<string, unknown>> => {
    try {
      const response = await api.get<Record<string, unknown>>(`/flowable/runtime/${processInstanceId}/variables`)
      return response.data
    } catch {
      // For completed instances, runtime variables aren't available
      // The instance DTO itself contains the historic variables
      return {}
    }
  },

  /**
   * Set a variable on a process instance
   * PUT /api/flowable/runtime/{processInstanceId}/variables/{variableName}
   */
  setInstanceVariable: async (processInstanceId: string, name: string, value: unknown): Promise<void> => {
    await api.put(`/flowable/runtime/${processInstanceId}/variables/${name}`, value)
  },

  /**
   * Suspend a process instance
   * POST /api/flowable/runtime/suspend/{processInstanceId}
   */
  suspendInstance: async (processInstanceId: string): Promise<void> => {
    await api.post(`/flowable/runtime/suspend/${processInstanceId}`)
  },

  /**
   * Activate a suspended process instance
   * POST /api/flowable/runtime/activate/{processInstanceId}
   */
  activateInstance: async (processInstanceId: string): Promise<void> => {
    await api.post(`/flowable/runtime/activate/${processInstanceId}`)
  },

  /**
   * Delete a process instance
   * DELETE /api/flowable/runtime/{processInstanceId}
   */
  deleteInstance: async (processInstanceId: string, reason?: string): Promise<void> => {
    await api.delete(`/flowable/runtime/${processInstanceId}`, { params: { reason } })
  },

  // ============ TASKS ============

  /**
   * Search tasks with filters
   * GET /api/admin/tasks/search
   */
  searchTasks: async (params: {
    candidateGroup?: string
    state?: string
    page?: number
    size?: number
  }): Promise<PagedResponse<Task>> => {
    const response = await api.get<PagedResponse<Task>>('/admin/tasks/search', { params })
    return response.data
  },

  /**
   * Get task history for a process instance
   * GET /api/flowable/history/task/process/{processInstanceId}
   */
  getTasksByInstance: async (processInstanceId: string): Promise<Task[]> => {
    const response = await api.get<Task[]>(`/flowable/history/task/process/${processInstanceId}`)
    return response.data
  },

  /**
   * Claim a task for the current user
   * POST /api/flowable/task/{taskId}/claim
   */
  claimTask: async (taskId: string): Promise<void> => {
    await api.post(`/flowable/task/${taskId}/claim`)
  },

  /**
   * Assign a task to a specific user
   * POST /api/flowable/task/assign/{taskId}
   */
  assignTask: async (taskId: string, user: string): Promise<void> => {
    await api.post(`/flowable/task/assign/${taskId}`, null, { params: { user } })
  },

  /**
   * Complete a task with optional variables
   * POST /api/flowable/task/complete/{taskId}
   */
  completeTask: async (taskId: string, variables?: Record<string, unknown>): Promise<void> => {
    await api.post(`/flowable/task/complete/${taskId}`, variables || {})
  },

  // ============ EVENTS ============

  /**
   * Get event logs
   * GET /api/admin/events/search
   */
  getEvents: async (limit = 100): Promise<EventLog[]> => {
    const response = await api.get<EventLog[]>('/admin/events/search', { params: { limit } })
    return response.data
  },

  /**
   * Get events for a specific process instance
   * Filters from the full event list
   */
  getEventsByInstance: async (processInstanceId: string): Promise<EventLog[]> => {
    const allEvents = await adminApi.getEvents(500)
    return allEvents.filter(e => e.processInstanceId === processInstanceId)
  },

  // ============ METRICS ============

  /**
   * Get dashboard metrics
   * GET /api/admin/metrics
   */
  getMetrics: async (): Promise<Metrics> => {
    const response = await api.get<Metrics>('/admin/metrics')
    return response.data
  },

  // ============ DIAGRAM ============

  /**
   * Get BPMN XML for a process definition
   * GET /api/flowable/diagram/{processDefinitionKey}/xml
   */
  getBpmnXml: async (processDefinitionKey: string): Promise<string> => {
    const response = await api.get<string>(`/flowable/diagram/${processDefinitionKey}/xml`, {
      headers: { Accept: 'application/xml' }
    })
    return response.data
  },

  /**
   * Get BPMN XML by process instance ID (fetches via process definition key)
   */
  getBpmnXmlByInstance: async (processInstanceId: string): Promise<string> => {
    // First get the instance to find the process definition key
    const instance = await adminApi.getInstanceById(processInstanceId)
    if (!instance.processDefinitionKey) {
      throw new Error('Process definition key not found')
    }
    return adminApi.getBpmnXml(instance.processDefinitionKey)
  },

  // ============ HISTORY & STATISTICS ============

  /**
   * Get all process history
   * GET /api/flowable/history/process
   */
  getAllProcessHistory: async (): Promise<ProcessInstance[]> => {
    const response = await api.get<ProcessInstance[]>('/flowable/history/process')
    return response.data
  },

  /**
   * Get process history by definition key
   * GET /api/flowable/history/process/{processKey}
   */
  getProcessHistory: async (processKey: string): Promise<ProcessInstance[]> => {
    const response = await api.get<ProcessInstance[]>(`/flowable/history/process/${processKey}`)
    return response.data
  },

  /**
   * Get process statistics
   * GET /api/flowable/history/process/statistics
   */
  getProcessStatistics: async (): Promise<ProcessStatistics> => {
    const response = await api.get<ProcessStatistics>('/flowable/history/process/statistics')
    return response.data
  },

  /**
   * Get task statistics
   * GET /api/flowable/history/task/statistics
   */
  getTaskStatistics: async (): Promise<TaskStatistics> => {
    const response = await api.get<TaskStatistics>('/flowable/history/task/statistics')
    return response.data
  },

  /**
   * Get completed tasks
   * GET /api/flowable/history/task/completed
   */
  getCompletedTasks: async (): Promise<Task[]> => {
    const response = await api.get<Task[]>('/flowable/history/task/completed')
    return response.data
  },

  /**
   * Get process history by date range
   * GET /api/flowable/history/process/date-range
   */
  getProcessHistoryByDateRange: async (startDate: string, endDate: string): Promise<ProcessInstance[]> => {
    const response = await api.get<ProcessInstance[]>('/flowable/history/process/date-range', {
      params: { startDate, endDate }
    })
    return response.data
  },
}

export default adminApi
