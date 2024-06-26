import axios, { AxiosError, CreateAxiosDefaults } from 'axios';
import querystring from 'querystring';
import { HttpsProxyAgent } from 'https-proxy-agent';
import https from 'https';
import { getSwiftCaFromFile } from '../swiftref/api/api';

type TokenHolder = {
  token_type: string;
  access_token: string;
  expires_in: number;
  refresh_token: string;
  refreshToken_expires_in: number;
};

const token: TokenHolder = {
  expires_in: 0
} as TokenHolder;

export async function getAccessToken(): Promise<string> {
  if (isAccessTokenExpired()) {
    try {
      console.log('OAuth2 access token expired, retrieving a new one...');
      const newToken = await requestAccessToken();
      token.expires_in = Date.now() + newToken.expires_in * 1000 - 10000; // clock skew of 10 seconds
      token.access_token = newToken.access_token;
      console.log('New OAuth token retrieved.');
    } catch (err) {
      if (axios.isAxiosError(err)) {
        const axiosError = err as AxiosError;
        if (axiosError.response) {
          throw new Error(JSON.stringify(axiosError.response.data));
        }
      }
      const error = {
        error: 'invalid_request',
        error_description: (err as Error).message
      };
      console.error(error);
      throw new Error(JSON.stringify(error));
    }
  }
  return token.access_token;
}

async function requestAccessToken(): Promise<TokenHolder> {
  const data = {
    grant_type: 'password',
    username: process.env.OAUTH2_USERNAME as string,
    password: process.env.OAUTH2_PASSWORD as string
  };

  const axiosConfig: CreateAxiosDefaults = {
    baseURL: process.env.URL,
    auth: {
      username: process.env.CONSUMER_KEY as string,
      password: process.env.CONSUMER_SECRET as string
    },
    headers: {
      'Content-Type': 'application/x-www-form-urlencoded'
    }
  };

  // Set proxy to the axios OAuth client
  if (process.env.PROXY) {
    axiosConfig.httpsAgent = new HttpsProxyAgent(process.env.PROXY);
  } else {
    axiosConfig.httpsAgent = new https.Agent();
  }

  // Set Swift Root CA
  if (process.env.SWIFT_CA) {
    axiosConfig.httpsAgent.options = { ca: getSwiftCaFromFile() };
  }

  const oauthClient = axios.create(axiosConfig);
  const res = await oauthClient.post(process.env.TOKEN_URI as string, querystring.stringify(data));
  return res.data;
}

function isAccessTokenExpired() {
  return Date.now() > token.expires_in;
}
