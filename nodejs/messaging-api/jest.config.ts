import type { Config } from 'jest';

const config: Config = {
  roots: ['<rootDir>/tests'],
  modulePaths: ['<rootDir>/dist/src']
};

export default config;
