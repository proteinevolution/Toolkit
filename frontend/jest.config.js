module.exports = {
    moduleFileExtensions: ['vue', 'js', 'json', 'ts'],
    transform: {
        '^.+\\.ts?$': 'ts-jest',
        '^.+\\.vue$': '@vue/vue3-jest',
    },
    // necessary to allow transforming vue-switches Vue SFCs
    transformIgnorePatterns: ['/node_modules/(?!vue-switches)'],
    // TODO: currently one test fails because of https://github.com/vuejs/vue-jest/issues/435
    testPathIgnorePatterns: ['<rootDir>/tests/unit/components/tools/parameters/BooleanParameter.spec.ts'],
    moduleNameMapper: {
        '^@/(.*)': '<rootDir>/src/$1',
        '^lodash-es$': 'lodash',
    },
    testEnvironment: 'jsdom',
    testEnvironmentOptions: {
        customExportConditions: ['node', 'node-addons'],
    },
};
