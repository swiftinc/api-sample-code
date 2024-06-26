const request = require('supertest');
const server = require('swiftref/app');

afterAll(() => {
  server.close();
});

describe('GET /swiftref/bic/swhqbebb', () => {
  it('should return BIC details', (done) => {
    request(server)
      .get('/swiftref/bic/swhqbebb')
      .end((err, res) => {
        try {
          expect(res.status).toBe(200);
        } catch (err) {
          console.error(`${JSON.stringify(res.body)}`);
          throw err;
        }
        done();
      })
  });
});

describe('GET /swiftref/iban/IT60X0542811101000000123456', () => {
  it('should return IBAN details', (done) => {
    request(server)
      .get('/swiftref/iban/IT60X0542811101000000123456')
      .expect('Content-Type', /json/)
      .expect(200, done);
  });
});
