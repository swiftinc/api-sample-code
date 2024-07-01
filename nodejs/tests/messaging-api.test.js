const request = require('supertest');
const server = require('messaging-api/app');

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

describe('GET /fin/44984189500/download', () => {
  it('should return FIN message', (done) => {
    request(server)
      .get('/fin/44984189500/download')
      .expect('Content-Type', /json/)
      .expect(200, done);
  });
});

describe('POST /fin/send', () => {
  it('FIN message should be sent', (done) => {
    const finMessage = {
      sender_reference: '1234',
      message_type: 'fin.999',
      sender: 'ABCD1234XXXX',
      receiver: 'ABCD1234XXXX',
      payload: 'OjIwOjEyMzRcclxuOjc5OlRlc3Q=',
      network_info: {
        network_priority: 'Normal',
        uetr: '099d4eb3-3d68-45fa-9afd-30b19af48728',
        delivery_monitoring: 'NonDelivery',
        possible_duplicate: false,
      },
    };

    request(server)
      .post('/fin/send')
      .send(finMessage)
      .expect('Content-Type', /json/)
      .expect(200, done);
  });
});
