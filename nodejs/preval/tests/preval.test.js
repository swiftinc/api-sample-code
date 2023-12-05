const request = require('supertest');
const server = require('../dist/app');

afterAll(() => {
  server.close();
});

describe('POST /preval/verifyBeneficiaryAccount', () => {
  it('Beneficiary data should be verified', (done) => {
    const data = {
      correlation_identifier: '112211221122',
      context: 'BENR',
      uetr: '97ed4827-7b6f-4491-a06f-b548d5a7512d',
      creditor_account: '7892368367',
      creditor_name: 'DEF Electronics',
      creditor_agent: { bicfi: 'AAAAUS2L' },
    };

    request(server)
      .post('/preval/verifyBeneficiaryAccount')
      .send(data)
      .expect('Content-Type', /json/)
      .expect(200, done);
  });
});
