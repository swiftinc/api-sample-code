import express, { Request, Response } from 'express';
import api from '../api/api';

const router = express.Router();
router.use(express.json());

/**
 * @openapi
 * /preval/verifyBeneficiaryAccount:
 *   post:
 *     summary: Send FIN message
 *     requestBody:
 *       description: FIN message
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *           example: { "correlation_identifier": "112211221122", "context": "BENR", "uetr": "97ed4827-7b6f-4491-a06f-b548d5a7512d", "creditor_account": "7892368367", "creditor_name": "DEF Electronics", "creditor_agent": { "bicfi": "AAAAUS2L" }}
 *     responses:
 *       200:
 *         description: Returns a FIN message
 *         application/json:
 *           schema:
 *             type: object
 */
router.post('/verifyBeneficiaryAccount', (req: Request, res: Response) => {
  api.PaymentPreValidationApi.verifyBeneficiaryAccount('swhqbebb', undefined, undefined, req.body)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.response ? error.response.data : error.message);
    });
});

export default router;
