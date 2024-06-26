import { Configuration, BicsApi, IbansApi } from './generated';
import { getAccessToken } from '../../common/password-token';
import defaultAxiosConfig from '../../common/axios-config';
import axios from 'axios';

const configuration: Configuration = new Configuration({
  basePath: `${process.env.URL}${process.env.ENDPOINT}`,
  accessToken: getAccessToken // Can be also implemented using axiosInstance.interceptors.request.use
});

const axiosInstance = axios.create(defaultAxiosConfig);

export default {
  BicsApi: new BicsApi(configuration, undefined, axiosInstance),
  IbansApi: new IbansApi(configuration, undefined, axiosInstance)
};
