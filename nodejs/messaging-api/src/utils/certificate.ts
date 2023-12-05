import * as fs from 'fs';
import forge, { pki } from 'node-forge';

export function base64UrlEncode(str: string): string {
  return str.replace(/\+/g, '-').replace(/\//g, '_').replace(/=/g, '');
}

export function getPrivateKeyFromFile(): Buffer {
  return fs.readFileSync(process.env.PRIVATE_KEY as string);
}

export function getCertificateFromFile(): Buffer {
  return fs.readFileSync(process.env.CERTIFICATE as string);
}

export function getSwiftCaFromFile(): Buffer {
  return fs.readFileSync(process.env.SWIFT_CA as string);
}

export function getX5c(certificate: Buffer): string {
  return certificate
    .toString()
    .replace(/-----BEGIN CERTIFICATE-----/, '')
    .replace(/-----END CERTIFICATE-----/, '')
    .replace(/\n/g, '');
}

export function getSubjectDnFromCertificate(certificate: Buffer): string {
  const cert = forge.pki.certificateFromPem(certificate.toString());
  return cert.subject.attributes
    .slice(0)
    .reverse()
    .map((attr: pki.CertificateField) => [attr.shortName, attr.value].join('='))
    .join(', ');
}
