import axios, { CreateAxiosDefaults } from 'axios';
import { Configuration, PaymentPreValidationApi } from './generated';
import { getAccessToken } from '../../common/bearertoken';
import { HttpsProxyAgent } from 'https-proxy-agent';
import { getSwiftCaFromFile } from '../../common/certificate';
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

const axiosInstance = axios.create(axiosConfig);

const configuration: Configuration = new Configuration({
  basePath: `${process.env.URL}${process.env.ENDPOINT}`,
  accessToken: getAccessToken // Can be also implemented using axiosInstance.interceptors.request.use
});

export default {
  PaymentPreValidationApi: new PaymentPreValidationApi(configuration, undefined, axiosInstance)
};
