import express, { Request, Response } from 'express';
import api from '../api/api';

const router = express.Router();
router.use(express.json());

/**
 * @openapi
 * /fin/send:
 *   post:
 *     summary: Send FIN message
 *     requestBody:
 *       description: FIN message
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *           example: { "sender_reference": "1234", "message_type": "fin.999", "sender": "ABCD1234XXXX", "receiver": "ABCD1234XXXX", "payload": "OjIwOjEyMzRcclxuOjc5OlRlc3Q=", "network_info": { "network_priority": "Normal", "uetr": "099d4eb3-3d68-45fa-9afd-30b19af48728", "delivery_monitoring": "NonDelivery", "possible_duplicate": false } }
 *     responses:
 *       200:
 *         description: Returns a FIN message
 *         application/json:
 *           schema:
 *             type: object
 */
router.post('/send', (req: Request, res: Response) => {
  // Keep the signature empty. The signature is calculated by the axios interceptor.
  api.FinApi.sendFinMessage('', req.body)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.message);
    });
});

/**
 * @openapi
 * /fin/{distributionId}/download:
 *   get:
 *     description: Download FIN message
 *     parameters:
 *       - in: path
 *         name: distributionId
 *         schema:
 *           type: integer
 *           default: 44984189500
 *         required: true
 *         description: Distribution id
 *     responses:
 *       200:
 *         description: Returns a FIN message
 *         application/json:
 *           schema:
 *             type: object
 */
router.get('/:distributionId/download', (req: Request, res: Response) => {
  const distributionId = parseInt(req.params.distributionId, 10);
  api.FinApi.downloadFinMessage(distributionId)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.response ? error.response.data : error.message);
    });
});

export default router;
