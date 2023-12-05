const request = require('supertest');
const server = require('../dist/app');

afterAll(() => {
  server.close();
});

describe('GET /distributions/list', () => {
  it('should return distribution list', (done) => {
    request(server)
      .get('/distributions/list')
      .expect('Content-Type', /json/)
      .expect(200, done);
  });
});
