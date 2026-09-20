import { Router } from 'express';
import { AuthController } from './auth.controller.js';
import { validate } from '../../middlewares/validate.middleware.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
import { GoogleLoginSchema, SignupSchema, LoginSchema, RefreshTokenSchema } from './auth.validation.js';
export const authRouter = Router();
authRouter.post('/google', validate(GoogleLoginSchema), AuthController.googleAuth);
authRouter.post('/signup', validate(SignupSchema), AuthController.signup);
authRouter.post('/login', validate(LoginSchema), AuthController.login);
authRouter.post('/refresh', validate(RefreshTokenSchema), AuthController.refresh);
authRouter.get('/me', requireAuth, AuthController.me);
//# sourceMappingURL=auth.routes.js.map