export interface Tool {
    name: string;
    longname: string;
    title?: string;
    section: string;
}

export interface ToolGroup {
    name: string;
    tools: Tool[];
}
