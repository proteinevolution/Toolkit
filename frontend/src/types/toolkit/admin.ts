interface SingleToolStats {
    toolName: string,
    count: number,
    failedCount: number,
    internalCount: number,
    deletedCount: number
}

interface ToolStatsCollection {
    summary: SingleToolStats,
    singleToolStats: SingleToolStats[],
}

interface WeeklyToolStats {
    week: number,
    year: number,
    toolStats: ToolStatsCollection
}

interface MonthlyToolStats {
    month: number,
    year: number,
    toolStats: ToolStatsCollection
}

export interface Statistics {
    totalToolStats: ToolStatsCollection,
    weeklyToolStats: WeeklyToolStats[],
    monthlyToolStats: MonthlyToolStats[]
}
