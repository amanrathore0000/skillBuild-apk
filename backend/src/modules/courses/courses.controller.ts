import { Request, Response, NextFunction } from 'express';
import { CoursesService } from './courses.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';

const coursesService = new CoursesService();

export class CoursesController {
  static async getCourses(req: Request, res: Response, next: NextFunction) {
    try {
      const { categoryId, search, level } = req.query;
      const courses = await coursesService.getCourses({
        categoryId: categoryId as string,
        search: search as string,
        level: level as string,
      });
      return ApiResponse.success(res, courses, 'Courses fetched successfully');
    } catch (error) {
      next(error);
    }
  }

  static async getCourseById(req: Request, res: Response, next: NextFunction) {
    try {
      const courseId = req.params.id as string;
      const course = await coursesService.getCourseById(courseId, req.user?.userId);
      return ApiResponse.success(res, course, 'Course details fetched');
    } catch (error) {
      next(error);
    }
  }

  static async createCourse(req: Request, res: Response, next: NextFunction) {
    try {
      const course = await coursesService.createCourse(req.user!.userId, req.body);
      return ApiResponse.created(res, course, 'Course created successfully');
    } catch (error) {
      next(error);
    }
  }

  static async addLesson(req: Request, res: Response, next: NextFunction) {
    try {
      const courseId = req.params.id as string;
      const lesson = await coursesService.addLesson(courseId, req.user!.userId, req.body);
      return ApiResponse.created(res, lesson, 'Lesson added successfully');
    } catch (error) {
      next(error);
    }
  }

  static async syncProgress(req: Request, res: Response, next: NextFunction) {
    try {
      const lessonId = req.params.lessonId as string;
      const progress = await coursesService.syncLessonProgress(req.user!.userId, lessonId, req.body);
      return ApiResponse.success(res, progress, 'Progress updated successfully');
    } catch (error) {
      next(error);
    }
  }

  static async getUploadUrl(req: Request, res: Response, next: NextFunction) {
    try {
      const { fileType, fileName, storageProvider } = req.body;
      const presigned = await coursesService.generateUploadPresignedUrl(req.user!.userId, fileType, fileName, storageProvider);
      return ApiResponse.success(res, presigned, 'Presigned upload URL generated');
    } catch (error) {
      next(error);
    }
  }

  static async getStoragePlans(req: Request, res: Response, next: NextFunction) {
    try {
      const storageInfo = await coursesService.getStoragePlans(req.user!.userId);
      return ApiResponse.success(res, storageInfo, 'Storage plans retrieved successfully');
    } catch (error) {
      next(error);
    }
  }

  static async subscribeStoragePlan(req: Request, res: Response, next: NextFunction) {
    try {
      const { planId } = req.body;
      const result = await coursesService.subscribeStoragePlan(req.user!.userId, planId);
      return ApiResponse.success(res, result, 'Subscribed to storage plan successfully');
    } catch (error) {
      next(error);
    }
  }

  static async grantDriveAccess(req: Request, res: Response, next: NextFunction) {
    try {
      const { lessonId, learnerEmail } = req.body;
      const result = await coursesService.grantDriveAccess(req.user!.userId, lessonId, learnerEmail);
      return ApiResponse.success(res, result, 'Google Drive access granted successfully');
    } catch (error) {
      next(error);
    }
  }
}
