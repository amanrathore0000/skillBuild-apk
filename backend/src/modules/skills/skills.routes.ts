import { Router } from 'express';
import { SkillsController } from './skills.controller.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';

export const skillsRouter = Router();

skillsRouter.get('/categories', SkillsController.getCategories);
skillsRouter.get('/', SkillsController.getSkills);
skillsRouter.post('/', requireAuth, SkillsController.createSkill);
