import type { Config } from 'jest';

const config: Config = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/tests'],
  modulePaths: ['<rootDir>/src'],
  testTimeout: 5000,
  detectOpenHandles: true
};

export default config;
