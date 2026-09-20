import { Router } from 'express';
import { UsersController } from './users.controller.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
export const usersRouter = Router();
usersRouter.get('/me/mentor-stats', requireAuth, UsersController.getMentorStats);
usersRouter.patch('/me', requireAuth, UsersController.updateProfile);
usersRouter.put('/me/skills', requireAuth, UsersController.updateSkills);
usersRouter.get('/:id', UsersController.getProfile);
//# sourceMappingURL=users.routes.js.map