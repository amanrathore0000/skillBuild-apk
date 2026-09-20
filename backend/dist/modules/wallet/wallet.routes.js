import { Router } from 'express';
import { WalletController } from './wallet.controller.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
export const walletRouter = Router();
walletRouter.get('/', requireAuth, WalletController.getWallet);
walletRouter.post('/payout', requireAuth, WalletController.requestPayout);
//# sourceMappingURL=wallet.routes.js.map