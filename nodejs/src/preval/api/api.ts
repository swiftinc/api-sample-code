import axios from 'axios';
import { Configuration, PaymentPreValidationApi } from './generated';
import { getAccessToken } from '../../common/bearer-token';
import defaultAxiosConfig from '../../common/axios-config';

const axiosInstance = axios.create(defaultAxiosConfig);

const configuration: Configuration = new Configuration({
  basePath: `${process.env.URL}${process.env.ENDPOINT}`,
  accessToken: getAccessToken // Can be also implemented using axiosInstance.interceptors.request.use
});

export default {
  PaymentPreValidationApi: new PaymentPreValidationApi(configuration, undefined, axiosInstance)
};
