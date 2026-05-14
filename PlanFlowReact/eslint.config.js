import js from '@eslint/js';
import globals from 'globals';
import reactHooks from 'eslint-plugin-react-hooks';
import reactRefresh from 'eslint-plugin-react-refresh';
import importPlugin from 'eslint-plugin-import';
import tseslint from 'typescript-eslint';

export default tseslint.config(
  { ignores: ['dist', 'node_modules', 'coverage'] },
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      ...tseslint.configs.recommended,
      reactHooks.configs['recommended-latest'],
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2022,
      sourceType: 'module',
      globals: { ...globals.browser, ...globals.node },
    },
    plugins: {
      import: importPlugin,
    },
    settings: {
      'import/resolver': {
        typescript: { project: './tsconfig.app.json' },
      },
    },
    rules: {
      'no-console': ['warn', { allow: ['warn', 'error'] }],
      '@typescript-eslint/consistent-type-imports': ['error', { fixStyle: 'inline-type-imports' }],
      '@typescript-eslint/no-unused-vars': [
        'error',
        { argsIgnorePattern: '^_', varsIgnorePattern: '^_' },
      ],
      'import/no-restricted-paths': [
        'error',
        {
          zones: [
            {
              target: './src/components',
              from: './src/store',
              message: 'components/** must not depend on store/**',
            },
            {
              target: './src/components',
              from: './src/api',
              message: 'components/** must not depend on api/**',
            },
            {
              target: './src/components',
              from: './src/pages',
              message: 'components/** must not depend on pages/**',
            },
            {
              target: './src/api',
              from: './src/store',
              message: 'api/** must not depend on store/**',
            },
            {
              target: './src/api',
              from: './src/components',
              message: 'api/** must not depend on components/**',
            },
            {
              target: './src/api',
              from: './src/pages',
              message: 'api/** must not depend on pages/**',
            },
            {
              target: './src/types',
              from: './src/api',
              message: 'types/** must not depend on api/**',
            },
            {
              target: './src/types',
              from: './src/store',
              message: 'types/** must not depend on store/**',
            },
            {
              target: './src/types',
              from: './src/components',
              message: 'types/** must not depend on components/**',
            },
            {
              target: './src/types',
              from: './src/pages',
              message: 'types/** must not depend on pages/**',
            },
            {
              target: './src/store',
              from: './src/components',
              message: 'store/** must not depend on components/**',
            },
            {
              target: './src/store',
              from: './src/pages',
              message: 'store/** must not depend on pages/**',
            },
          ],
        },
      ],
    },
  },
);
