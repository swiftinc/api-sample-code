import 'dotenv/config';

import express from 'express';
import distributionsRoutes from './routes/distributions';
import finRoutes from './routes/fin';

import swaggerJsdoc from 'swagger-jsdoc';
import swaggerUi from 'swagger-ui-express';

const app = express();
const port = process.env.PORT || 8080;

// Messaging API routes
app.use('/distributions', distributionsRoutes);
app.use('/fin', finRoutes);

// Swagger UI
const options = {
  definition: {
    openapi: '3.0.0',
    info: {
      title: 'Swift Messaging API',
      version: '1.0.0'
    },
    servers: [
      {
        url: '/'
      }
    ]
  },
  apis: ['./routes/*.js']
};
const specs = swaggerJsdoc(options);
app.use('/test', swaggerUi.serve, swaggerUi.setup(specs));

// Start listening
const server = app.listen(port, () => {
  console.log(`Swagger UI available at http://127.0.0.1:${port}/test`);
});

module.exports = server;
