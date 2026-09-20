import { CoursesService } from './courses.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';
const coursesService = new CoursesService();
export class CoursesController {
    static async getCourses(req, res, next) {
        try {
            const { categoryId, search, level } = req.query;
            const courses = await coursesService.getCourses({
                categoryId: categoryId,
                search: search,
                level: level,
            });
            return ApiResponse.success(res, courses, 'Courses fetched successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async getCourseById(req, res, next) {
        try {
            const courseId = req.params.id;
            const course = await coursesService.getCourseById(courseId, req.user?.userId);
            return ApiResponse.success(res, course, 'Course details fetched');
        }
        catch (error) {
            next(error);
        }
    }
    static async createCourse(req, res, next) {
        try {
            const course = await coursesService.createCourse(req.user.userId, req.body);
            return ApiResponse.created(res, course, 'Course created successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async addLesson(req, res, next) {
        try {
            const courseId = req.params.id;
            const lesson = await coursesService.addLesson(courseId, req.user.userId, req.body);
            return ApiResponse.created(res, lesson, 'Lesson added successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async syncProgress(req, res, next) {
        try {
            const lessonId = req.params.lessonId;
            const progress = await coursesService.syncLessonProgress(req.user.userId, lessonId, req.body);
            return ApiResponse.success(res, progress, 'Progress updated successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async getUploadUrl(req, res, next) {
        try {
            const { fileType, fileName, storageProvider } = req.body;
            const presigned = await coursesService.generateUploadPresignedUrl(req.user.userId, fileType, fileName, storageProvider);
            return ApiResponse.success(res, presigned, 'Presigned upload URL generated');
        }
        catch (error) {
            next(error);
        }
    }
    static async getStoragePlans(req, res, next) {
        try {
            const storageInfo = await coursesService.getStoragePlans(req.user.userId);
            return ApiResponse.success(res, storageInfo, 'Storage plans retrieved successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async subscribeStoragePlan(req, res, next) {
        try {
            const { planId } = req.body;
            const result = await coursesService.subscribeStoragePlan(req.user.userId, planId);
            return ApiResponse.success(res, result, 'Subscribed to storage plan successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async grantDriveAccess(req, res, next) {
        try {
            const { lessonId, learnerEmail } = req.body;
            const result = await coursesService.grantDriveAccess(req.user.userId, lessonId, learnerEmail);
            return ApiResponse.success(res, result, 'Google Drive access granted successfully');
        }
        catch (error) {
            next(error);
        }
    }
}
//# sourceMappingURL=courses.controller.js.map