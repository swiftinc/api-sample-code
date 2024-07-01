import { CreateAxiosDefaults } from 'axios';
import * as fs from 'fs';
import { HttpsProxyAgent } from 'https-proxy-agent';
import https from 'https';

let caContent: Buffer | undefined;
if (process.env.SWIFT_CA) {
  caContent = fs.readFileSync(process.env.SWIFT_CA as string);
}

const options: https.AgentOptions = { ca: caContent };

const defaultAxiosConfig: CreateAxiosDefaults = {
  headers: {
    Accept: 'application/json'
  }
};

// Set proxy
if (process.env.PROXY) {
  defaultAxiosConfig.httpsAgent = new HttpsProxyAgent(process.env.PROXY, options);
} else {
  defaultAxiosConfig.httpsAgent = new https.Agent(options);
}

export default defaultAxiosConfig;
