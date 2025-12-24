declare module 'bpmn-js/lib/NavigatedViewer' {
  interface BpmnViewerOptions {
    container: HTMLElement
  }

  class BpmnJS {
    constructor(options: BpmnViewerOptions)
    importXML(xml: string): Promise<{ warnings: string[] }>
    get(name: string): unknown
    destroy(): void
  }

  export default BpmnJS
}
