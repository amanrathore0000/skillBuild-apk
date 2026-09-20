import { Router } from 'express';
import { CoursesController } from './courses.controller.js';
import { requireAuth, optionalAuth } from '../../middlewares/auth.middleware.js';
export const coursesRouter = Router();
coursesRouter.get('/', CoursesController.getCourses);
coursesRouter.get('/:id', optionalAuth, CoursesController.getCourseById);
coursesRouter.post('/', requireAuth, CoursesController.createCourse);
coursesRouter.post('/:id/lessons', requireAuth, CoursesController.addLesson);
coursesRouter.post('/lessons/:lessonId/progress', requireAuth, CoursesController.syncProgress);
coursesRouter.post('/upload-url', requireAuth, CoursesController.getUploadUrl);
coursesRouter.get('/storage/plans', requireAuth, CoursesController.getStoragePlans);
coursesRouter.post('/storage/subscribe', requireAuth, CoursesController.subscribeStoragePlan);
coursesRouter.post('/storage/grant-drive-access', requireAuth, CoursesController.grantDriveAccess);
//# sourceMappingURL=courses.routes.js.map