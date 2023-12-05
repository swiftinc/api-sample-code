import express, { Request, Response } from 'express';
import api from '../api/api';

const router = express.Router();

/**
 * @openapi
 * /swiftref/bic/{bic}:
 *   get:
 *     description: BIC details
 *     parameters:
 *       - in: path
 *         name: bic
 *         schema:
 *           type: string
 *           default: swhqbebb
 *         required: true
 *         description: BIC
 *     responses:
 *       200:
 *         description: Returns BIC details
 *         application/json:
 *           schema:
 *             type: object
 */
router.get('/bic/:bic', (req: Request, res: Response) => {
  api.BicsApi.getBicDetails(req.params.bic)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.response ? error.response.data : error.message);
    });
});

/**
 * @openapi
 * /swiftref/iban/{iban}:
 *   get:
 *     description: IBAN details
 *     parameters:
 *       - in: path
 *         name: iban
 *         schema:
 *           type: string
 *           default: IT60X0542811101000000123456
 *         required: true
 *         description: IBAN
 *     responses:
 *       200:
 *         description: Returns IBAN details
 *         application/json:
 *           schema:
 *             type: object
 */
router.get('/iban/:iban', (req: Request, res: Response) => {
  api.IbansApi.getIbanDetails(req.params.iban)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.response ? error.response.data : error.message);
    });
});

export default router;
