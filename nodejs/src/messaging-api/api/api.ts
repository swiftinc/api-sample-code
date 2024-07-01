import { Configuration, DistributionsApi, FinApi } from './generated';
import { getAccessToken } from '../../common/bearer-token';
import swiftSignature from '../../common/signature';
import defaultAxiosConfig from '../../common/axios-config';
import axios from 'axios';

const swiftSignatureHeader = 'X-SWIFT-Signature';

const axiosInstance = axios.create(defaultAxiosConfig);

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
    console.log(err);
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
