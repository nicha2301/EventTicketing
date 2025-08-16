import { defineConfig } from 'orval';

const OPENAPI_URL = process.env.OPENAPI_URL || `${process.env.NEXT_PUBLIC_API_BASE_URL || 'http://localhost:8080'}/v3/api-docs`;

export default defineConfig({
  webApp: {
    input: OPENAPI_URL,
    output: {
      target: './src/lib/api/generated/client.ts',
      client: 'react-query',
      prettier: true,
      override: {
        mutator: {
          path: './src/lib/api/http.ts',
          name: 'http',
        },
        query: {
          useSuspenseQuery: false,
        },
      },
    },
  },
});


