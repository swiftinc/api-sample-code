import crypto from 'crypto';
import {
  base64UrlEncode,
  getCertificateFromFile,
  getPrivateKeyFromFile,
  getSubjectDnFromCertificate,
  getX5c
} from '../utils/certificate';

export default function swiftSignature(url: string, data: string): string {
  const certificate = getCertificateFromFile();

  const header = { alg: 'RS256', typ: 'JWT', x5c: [getX5c(certificate)] };
  const payload = {
    sub: getSubjectDnFromCertificate(certificate),
    aud: url.replace('https://', ''),
    exp: Math.floor(Date.now() / 1000) + 15,
    iat: Math.floor(Date.now() / 1000),
    nbf: Math.floor(Date.now() / 1000),
    jti: Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15),
    digest: calculateDigestSha256(data)
  };

  const base64Header = base64UrlEncode(btoa(JSON.stringify(header)));
  const base64Payload = base64UrlEncode(btoa(JSON.stringify(payload)));

  const signature = crypto
    .createSign('RSA-SHA256')
    .update(`${base64Header}.${base64Payload}`)
    .sign({
      key: crypto.createPrivateKey({
        key: getPrivateKeyFromFile(),
        passphrase: process.env.KEY_PASSWORD
      })
    });

  const base64Signature = base64UrlEncode(signature.toString('base64'));
  return `${base64Header}.${base64Payload}.${base64Signature}`;
}

function calculateDigestSha256(data: string) {
  const dataBase64 = Buffer.from(data).toString('base64');
  const hash = crypto.createHash('sha256');
  hash.update(dataBase64);
  return hash.digest('base64');
}
