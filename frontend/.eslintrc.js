module.exports = {
    root: true,
    env: {
        browser: true,
        es6: true,
        node: true,
    },
    extends: [
        'plugin:vue/recommended',
        'eslint:recommended',
        '@vue/typescript/recommended',
    ],
    parserOptions: {
        ecmaVersion: 2020,
    },
    rules: {
        'vue/html-indent': ['warn', 4],
        'vue/no-v-html': 'off',
        'vue/max-attributes-per-line': [
            'warn',
            {
                singleline: 1,
                multiline: {
                    max: 1,
                    allowFirstLine: true,
                },
            },
        ],
        'vue/html-self-closing': [
            'warn',
            {
                html: {
                    normal: 'never',
                    component: 'always',
                },
            },
        ],
        'vue/html-closing-bracket-newline': [
            'warn',
            {
                singleline: 'never',
                multiline: 'never',
            },
        ],
        // TODO get rid of these rules for better code style
        'vue/require-default-prop': 'off',
        '@typescript-eslint/no-explicit-any': 'off',
        '@typescript-eslint/no-inferrable-types': 'off',
    },
};
