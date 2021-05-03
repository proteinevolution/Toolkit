interface singleToolStats {
    toolName: string,
    count: number,
    failedCount: number,
    internalCount: number,
    deletedCount: number
}

interface toolStatsCollection {
    summary: singleToolStats,
    singleToolStats: singleToolStats[],
}

interface weeklyToolStats {
    week: number,
    year: number,
    toolStats: toolStatsCollection
}

interface monthlyToolStats {
    month: number,
    year: number,
    toolStats: toolStatsCollection
}

export interface Statistics {
    totalToolStats: toolStatsCollection,
    weeklyToolStats: weeklyToolStats[],
    monthlyToolStats: monthlyToolStats[]
}