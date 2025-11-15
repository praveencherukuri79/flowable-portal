import axios from 'axios'

// Staging data types
export interface ProductStaging {
  id?: number
  sheetId: string
  productName: string
  rate: number
  api: string
  effectiveDate: string
  approved?: boolean
  approvedBy?: string
  approvedAt?: string
  status?: string
  editedBy?: string
  editedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface PlanStaging {
  id?: number
  sheetId: string
  planName: string
  planType: string
  premium: number
  coverageAmount: number
  effectiveDate: string
  approved?: boolean
  approvedBy?: string
  approvedAt?: string
  status?: string
  editedBy?: string
  editedAt?: string
  createdAt?: string
  updatedAt?: string
}

export interface ItemStaging {
  id?: number
  sheetId: string
  itemName: string
  itemCategory: string
  price: number
  quantity: number
  effectiveDate: string
  approved?: boolean
  approvedBy?: string
  approvedAt?: string
  status?: string
  editedBy?: string
  editedAt?: string
  createdAt?: string
  updatedAt?: string
}

export const stagingApi = {
  // Product Staging
  getProductsStaging: async (sheetId: string): Promise<ProductStaging[]> => {
    const response = await axios.get(`/api/data-query/products/staging/${sheetId}`)
    return response.data
  },

  approveProductIndividual: async (id: number, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/products/staging/approve-individual/${id}`, {
      approverUsername
    })
  },

  approveProductsBulk: async (sheetId: string, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/products/staging/approve-bulk/${sheetId}`, {
      approverUsername
    })
  },

  // Plan Staging
  getPlansStaging: async (sheetId: string): Promise<PlanStaging[]> => {
    const response = await axios.get(`/api/data-query/plans/staging/${sheetId}`)
    return response.data
  },

  approvePlanIndividual: async (id: number, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/plans/staging/approve-individual/${id}`, {
      approverUsername
    })
  },

  approvePlansBulk: async (sheetId: string, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/plans/staging/approve-bulk/${sheetId}`, {
      approverUsername
    })
  },

  // Item Staging
  getItemsStaging: async (sheetId: string): Promise<ItemStaging[]> => {
    const response = await axios.get(`/api/data-query/items/staging/${sheetId}`)
    return response.data
  },

  approveItemIndividual: async (id: number, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/items/staging/approve-individual/${id}`, {
      approverUsername
    })
  },

  approveItemsBulk: async (sheetId: string, approverUsername: string): Promise<void> => {
    await axios.post(`/api/data-query/items/staging/approve-bulk/${sheetId}`, {
      approverUsername
    })
  },

  // Sheet Management
  getSheet: async (sheetId: string): Promise<any> => {
    const response = await axios.get(`/api/data-query/sheets/${sheetId}`)
    return response.data
  },

  approveSheet: async (sheetId: string, approvedBy: string, taskId: string, decision: string): Promise<void> => {
    await axios.post(`/api/data-query/sheets/${sheetId}/approve`, {
      approvedBy,
      taskId,
      decision
    })
  },

  checkRowsApproved: async (sheetId: string, entityType: string): Promise<boolean> => {
    const response = await axios.get(`/api/data-query/sheets/check-rows-approved/${sheetId}`, {
      params: { entityType }
    })
    return response.data
  }
}
