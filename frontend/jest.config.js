module.exports = {
    moduleFileExtensions: ["vue", "js", "json", "ts"],
    transform: {
        "^.+\\.ts?$": "ts-jest",
        "^.+\\.vue$": "@vue/vue2-jest",
    },
    // necessary to allow transforming vue-switches Vue SFCs
    // TODO: currently one test fails because of https://github.com/vuejs/vue-jest/issues/435
    transformIgnorePatterns: ["/node_modules/(?!vue-switches)"],
    moduleNameMapper: {
        "^@/(.*)": "<rootDir>/src/$1",
        "^lodash-es$": "lodash",
    },
    testEnvironment: "jsdom",
    testEnvironmentOptions: {
        customExportConditions: ["node", "node-addons"]
    },
}
