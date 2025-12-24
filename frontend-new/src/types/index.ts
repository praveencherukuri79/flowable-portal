// Process Definition
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

// Process Instance - matches backend ProcessInstanceDto
export interface ProcessInstance {
  id: string
  processDefinitionId: string
  processDefinitionKey: string
  name?: string
  description?: string
  businessKey: string
  startUserId: string
  startTime: string
  endTime: string | null
  status: string // 'ACTIVE' | 'COMPLETED' | 'SUSPENDED' | 'TERMINATED'
  tenantId: string
  variables: Record<string, unknown>
  activeActivityIds?: string[]
  completedActivityIds?: string[]
  diagramUrl?: string
  suspended: boolean
}

// Task - matches backend TaskDto
export interface Task {
  id: string
  name: string
  description?: string
  assignee: string | null
  owner: string | null
  delegationState?: string
  createTime: string
  endTime?: string
  durationInMillis?: number
  dueDate: string | null
  priority?: number
  category?: string
  processInstanceId: string
  processDefinitionId: string
  executionId?: string
  taskDefinitionKey: string
  formKey?: string
  state: string // 'CLAIMABLE' | 'ASSIGNED' | etc
  candidateGroups?: string[]
  candidateUsers?: string[]
  variables?: Record<string, unknown>
  comments?: string[]
  attachments?: string[]
  suspended?: string
  tenantId?: string
}

// Event Log - matches backend EventLogDto
export interface EventLog {
  id: string
  timestamp: string
  type: string
  processDefinitionId: string
  processInstanceId: string
  executionId: string
  data: string | null
}

// Dashboard Metrics - matches backend MetricsDto
export interface Metrics {
  runningInstances: number
  completedInstances: number
  totalTasks: number
  instancesByDay: DailyCount[]
  tasksByState: StateCount[]
  avgDurationByDefinition: DurationMetric[]
}

export interface DailyCount {
  day: string
  count: number
}

export interface StateCount {
  state: string
  count: number
}

export interface DurationMetric {
  definitionKey: string
  minutes: number
}

// Paginated Response
export interface PagedResponse<T> {
  content: T[]
  total: number
  page?: number
  size?: number
}

// Variable
export interface ProcessVariable {
  name: string
  type: string
  value: unknown
  scope?: 'local' | 'global'
}

// Activity Instance (for history/audit)
export interface ActivityInstance {
  id: string
  activityId: string
  activityName: string
  activityType: string
  processInstanceId: string
  executionId: string
  startTime: string
  endTime: string | null
  durationInMillis: number | null
  assignee: string | null
  taskId: string | null
  calledProcessInstanceId: string | null
}

// Instance Details (comprehensive view)
export interface InstanceDetails {
  instance: ProcessInstance
  tasks: Task[]
  activities: ActivityInstance[]
  variables: ProcessVariable[]
  events: EventLog[]
  subProcesses?: ProcessInstance[]
}

// API Response
export interface ApiResponse<T = unknown> {
  success: boolean
  message: string
  data?: T
  timestamp?: string
}

// Task Action Response
export interface TaskActionResponse {
  taskId: string
  action: string
  performedBy: string
  message: string
  taskState: string
}

// User
export interface User {
  id: string
  username: string
  fullName: string
  email: string
  role: 'ADMIN' | 'MAKER' | 'CHECKER'
}

// Search Filters
export interface InstanceSearchFilters {
  definitionKey?: string
  state?: string
  businessKey?: string
  startDateFrom?: string
  startDateTo?: string
  initiator?: string
}

export interface TaskSearchFilters {
  assignee?: string
  candidateGroup?: string
  state?: string
  processInstanceId?: string
  dueDate?: string
}

// Process Statistics - from /flowable/history/process/statistics
export interface ProcessStatistics {
  totalCompleted: number
  totalActive: number
  byDefinition: Record<string, number>
  averageDurationMs?: number
}

// Task Statistics - from /flowable/history/task/statistics
export interface TaskStatistics {
  totalCompleted: number
  totalActive: number
  byAssignee?: Record<string, number>
  averageDurationMs?: number
}
