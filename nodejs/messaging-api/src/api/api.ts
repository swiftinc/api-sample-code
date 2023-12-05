import axios, { CreateAxiosDefaults } from 'axios';
import { Configuration, DistributionsApi, FinApi } from './generated';
import { getAccessToken } from '../services/oauthtoken';
import { HttpsProxyAgent } from 'https-proxy-agent';
import { getSwiftCaFromFile } from '../utils/certificate';
import https from 'https';
import swiftSignature from '../services/signature';

const swiftSignatureHeader = 'X-SWIFT-Signature';

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

const axiosInstance = axios.create(axiosConfig);

// Signature interceptor
axiosInstance.interceptors.request.use(
  (config) => {
    if (config.headers[swiftSignatureHeader] !== undefined) {
      const signature = swiftSignature(config.url as string, config.data as string);
      config.headers[swiftSignatureHeader] = signature;
    }
    return config;
  },
  (err) => {
    console.error(err);
    Promise.reject(err);
  }
);

const configuration: Configuration = new Configuration({
  basePath: `${process.env.URL}${process.env.ENDPOINT}`,
  accessToken: getAccessToken // Can be also implemented using axiosInstance.interceptors.request.use
});

export default {
  DistributionsApi: new DistributionsApi(configuration, undefined, axiosInstance),
  FinApi: new FinApi(configuration, undefined, axiosInstance)
};
