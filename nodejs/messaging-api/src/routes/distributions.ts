import express, { Request, Response } from 'express';
import api from '../api/api';

const router = express.Router();

/**
 * @openapi
 * /distributions/list:
 *   get:
 *     summary: Get distribution list
 *     responses:
 *       200:
 *         description: Returns distribution list
 */
router.get('/list', (req: Request, res: Response) => {
  api.DistributionsApi.listDistributions(100, 0)
    .then((response) => {
      res.send(response.data);
    })
    .catch((error) => {
      res.status(500).send(error.response ? error.response.data : error.message);
    });
});

export default router;
