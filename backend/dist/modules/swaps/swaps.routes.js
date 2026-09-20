import { Router } from 'express';
import { SwapsController } from './swaps.controller.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
export const swapsRouter = Router();
swapsRouter.get('/matches', requireAuth, SwapsController.getMatches);
swapsRouter.post('/', requireAuth, SwapsController.createProposal);
swapsRouter.get('/', requireAuth, SwapsController.getProposals);
swapsRouter.patch('/:id/status', requireAuth, SwapsController.updateStatus);
//# sourceMappingURL=swaps.routes.js.map