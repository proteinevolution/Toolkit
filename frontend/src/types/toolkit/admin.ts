interface singleToolStats {
    toolName: string,
    count: number,
    failedCount: number,
    internalCount: number,
    deletedCount: number
}

interface toolStatsCollection {
    singleToolStats: singleToolStats[]
}

export interface Statistics {
    totalToolStats: toolStatsCollection;
}