import { defineConfig } from 'cypress';

export default defineConfig({
  e2e: {
    baseUrl: 'http://localhost:1420',
    viewportWidth: 1280,
    viewportHeight: 720,
    defaultCommandTimeout: 10000,
    requestTimeout: 10000,
    responseTimeout: 30000,
    video: false,
    screenshotOnRunFailure: true,
    chromeWebSecurity: false,
    experimentalStudio: true,
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
  env: {
    apiUrl: 'http://localhost:8080',
  },
});
