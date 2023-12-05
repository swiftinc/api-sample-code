import axios, { CreateAxiosDefaults } from 'axios';
import { Configuration, BicsApi, IbansApi } from './generated';
import { getAccessToken } from '../services/oauthtoken';
import { HttpsProxyAgent } from 'https-proxy-agent';
import * as fs from 'fs';
import https from 'https';

const axiosConfig: CreateAxiosDefaults = {};

// Set proxy
if (process.env.PROXY) {
  axiosConfig.httpsAgent = new HttpsProxyAgent(process.env.PROXY);
} else {
  axiosConfig.httpsAgent = new https.Agent();
}

// Set Swift Root CA
if (process.env.SWIFT_CA) {
  axiosConfig.httpsAgent.options = { ca: getSwiftCaFromFile() };
}

// Only Accept JSON
axiosConfig.headers = {
  Accept: 'application/json'
};

const axiosInstance = axios.create(axiosConfig);

const configuration: Configuration = new Configuration({
  basePath: `${process.env.URL}${process.env.ENDPOINT}`,
  accessToken: getAccessToken // Can be also implemented using axiosInstance.interceptors.request.use
});

export function getSwiftCaFromFile(): Buffer {
  return fs.readFileSync(process.env.SWIFT_CA as string);
}

export default {
  BicsApi: new BicsApi(configuration, undefined, axiosInstance),
  IbansApi: new IbansApi(configuration, undefined, axiosInstance)
};
