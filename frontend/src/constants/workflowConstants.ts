/**
 * Workflow decision variable names
 * Using entity-based names for clarity and maintainability
 */
export enum WorkflowDecision {
  ITEM = 'itemDecision',
  PLAN = 'planDecision',
  PRODUCT = 'productDecision'
}

/**
 * Workflow decision values
 */
export enum WorkflowDecisionValue {
  APPROVE = 'APPROVE',
  REJECT = 'REJECT',
  BACK = 'BACK',
  FORWARD = 'FORWARD'
}

/**
 * Entity types used in the workflow
 */
export enum EntityType {
  ITEM = 'item',
  PLAN = 'plan',
  PRODUCT = 'product'
}

/**
 * Helper function to get decision variable name for an entity type
 */
export function getDecisionVariable(entityType: EntityType): WorkflowDecision {
  switch (entityType) {
    case EntityType.ITEM:
      return WorkflowDecision.ITEM
    case EntityType.PLAN:
      return WorkflowDecision.PLAN
    case EntityType.PRODUCT:
      return WorkflowDecision.PRODUCT
    default:
      throw new Error(`Unknown entity type: ${entityType}`)
  }
}

/**
 * Helper function to get decision variable name from task definition key
 */
export function getDecisionVariableFromTaskKey(taskDefinitionKey: string): WorkflowDecision | null {
  if (taskDefinitionKey.includes('item')) {
    return WorkflowDecision.ITEM
  } else if (taskDefinitionKey.includes('plan')) {
    return WorkflowDecision.PLAN
  } else if (taskDefinitionKey.includes('product')) {
    return WorkflowDecision.PRODUCT
  }
  return null
}

