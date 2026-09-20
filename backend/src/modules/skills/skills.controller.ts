import { Request, Response, NextFunction } from 'express';
import { SkillsService } from './skills.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';

const skillsService = new SkillsService();

export class SkillsController {
  static async getCategories(req: Request, res: Response, next: NextFunction) {
    try {
      const categories = await skillsService.getCategories();
      return ApiResponse.success(res, categories, 'Categories retrieved');
    } catch (error) {
      next(error);
    }
  }

  static async getSkills(req: Request, res: Response, next: NextFunction) {
    try {
      const { categoryId, search, level } = req.query;
      const skills = await skillsService.getSkills({
        categoryId: categoryId as string,
        search: search as string,
        level: level as string,
      });
      return ApiResponse.success(res, skills, 'Skills retrieved');
    } catch (error) {
      next(error);
    }
  }

  static async createSkill(req: Request, res: Response, next: NextFunction) {
    try {
      const skill = await skillsService.createSkill(req.body);
      return ApiResponse.created(res, skill, 'Skill created successfully');
    } catch (error) {
      next(error);
    }
  }
}
