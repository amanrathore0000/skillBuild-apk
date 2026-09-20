import { prisma } from '../../config/database.js';
import { AppError } from '../../middlewares/error.middleware.js';
import { SkillLevel, CourseVisibility, PriceType } from '@prisma/client';
export class CoursesService {
    async getCourses(query) {
        const where = {
            visibility: CourseVisibility.PUBLIC,
        };
        if (query.categoryId) {
            where.categoryId = query.categoryId;
        }
        if (query.search) {
            where.OR = [
                { title: { contains: query.search } },
                { description: { contains: query.search } },
            ];
        }
        if (query.level && query.level !== 'ALL_LEVELS') {
            where.level = query.level;
        }
        const courses = await prisma.course.findMany({
            where,
            include: {
                mentor: {
                    select: { id: true, name: true, avatarUrl: true, rating: true },
                },
                category: true,
                _count: {
                    select: { lessons: true },
                },
            },
            orderBy: { rating: 'desc' },
        });
        return courses.map((c) => ({
            id: c.id,
            title: c.title,
            mentorName: c.mentor.name,
            mentorAvatar: c.mentor.avatarUrl,
            category: c.category.name,
            rating: Number(c.rating),
            reviewCount: c.reviewCount,
            duration: c.duration,
            price: c.priceType === PriceType.FREE_WITH_SWAP ? 'Free with Swap' : `$${Number(c.priceAmount).toFixed(2)}`,
            progressPercent: 0,
            lessonCount: c._count.lessons,
            thumbnailUrl: c.thumbnailUrl,
        }));
    }
    async getCourseById(courseId, userId) {
        const course = await prisma.course.findUnique({
            where: { id: courseId },
            include: {
                mentor: {
                    select: { id: true, name: true, avatarUrl: true, rating: true, reviewCount: true },
                },
                category: true,
                lessons: {
                    orderBy: { sequenceOrder: 'asc' },
                    include: {
                        progress: userId
                            ? {
                                where: { userId },
                            }
                            : false,
                    },
                },
                enrollments: userId
                    ? {
                        where: { userId },
                    }
                    : false,
            },
        });
        if (!course) {
            throw new AppError('Course not found', 404);
        }
        const userEnrollment = course.enrollments && course.enrollments.length > 0 ? course.enrollments[0] : null;
        const lessons = course.lessons.map((l) => {
            const userProg = l.progress && l.progress.length > 0 ? l.progress[0] : null;
            return {
                id: l.id,
                title: l.title,
                duration: l.duration,
                durationSeconds: l.durationSeconds,
                videoUrl: l.videoUrl,
                sequenceOrder: l.sequenceOrder,
                isCompleted: userProg ? userProg.isCompleted : false,
                watchedSeconds: userProg ? userProg.watchedSeconds : 0,
            };
        });
        return {
            id: course.id,
            title: course.title,
            description: course.description,
            mentorName: course.mentor.name,
            mentorAvatar: course.mentor.avatarUrl,
            category: course.category.name,
            rating: Number(course.rating),
            reviewCount: course.reviewCount,
            duration: course.duration,
            price: course.priceType === PriceType.FREE_WITH_SWAP ? 'Free with Swap' : `$${Number(course.priceAmount).toFixed(2)}`,
            progressPercent: userEnrollment ? userEnrollment.progressPercent : 0,
            thumbnailUrl: course.thumbnailUrl,
            lessons,
        };
    }
    async createCourse(mentorId, data) {
        return prisma.course.create({
            data: {
                mentorId,
                categoryId: data.categoryId,
                title: data.title,
                description: data.description,
                duration: data.duration || '0m',
                thumbnailUrl: data.thumbnailUrl,
                level: data.level || SkillLevel.ALL_LEVELS,
                priceType: data.priceType || PriceType.FREE_WITH_SWAP,
                priceAmount: data.priceAmount || 0,
            },
            include: {
                category: true,
            },
        });
    }
    async addLesson(courseId, mentorId, data) {
        const course = await prisma.course.findUnique({
            where: { id: courseId },
        });
        if (!course) {
            throw new AppError('Course not found', 404);
        }
        if (course.mentorId !== mentorId) {
            throw new AppError('Unauthorized to add lessons to this course', 403);
        }
        const lessonCount = await prisma.lesson.count({ where: { courseId } });
        return prisma.lesson.create({
            data: {
                courseId,
                title: data.title,
                duration: data.duration,
                durationSeconds: data.durationSeconds,
                videoUrl: data.videoUrl,
                sequenceOrder: data.sequenceOrder || lessonCount + 1,
            },
        });
    }
    async syncLessonProgress(userId, lessonId, data) {
        const lesson = await prisma.lesson.findUnique({
            where: { id: lessonId },
            include: { course: { include: { lessons: true } } },
        });
        if (!lesson) {
            throw new AppError('Lesson not found', 404);
        }
        const isDone = data.isCompleted || (lesson.durationSeconds > 0 && data.watchedSeconds / lesson.durationSeconds >= 0.9);
        // Upsert lesson progress
        const progress = await prisma.lessonProgress.upsert({
            where: {
                userId_lessonId: {
                    userId,
                    lessonId,
                },
            },
            create: {
                userId,
                lessonId,
                watchedSeconds: data.watchedSeconds,
                isCompleted: isDone,
            },
            update: {
                watchedSeconds: data.watchedSeconds,
                isCompleted: isDone,
            },
        });
        // Compute overall course progress
        const totalLessons = lesson.course.lessons.length;
        if (totalLessons > 0) {
            const completedLessons = await prisma.lessonProgress.count({
                where: {
                    userId,
                    lesson: { courseId: lesson.courseId },
                    isCompleted: true,
                },
            });
            const progressPercent = Math.round((completedLessons / totalLessons) * 100);
            await prisma.enrollment.upsert({
                where: {
                    userId_courseId: {
                        userId,
                        courseId: lesson.courseId,
                    },
                },
                create: {
                    userId,
                    courseId: lesson.courseId,
                    progressPercent,
                },
                update: {
                    progressPercent,
                },
            });
        }
        return progress;
    }
    async getStoragePlans(userId) {
        const plans = [
            {
                id: 'plan_gdrive_free',
                name: 'Google Drive (Free BYOD)',
                provider: 'GOOGLE_DRIVE',
                price: 'Free (₹0)',
                storageLimitBytes: 16106127360,
                storageLimitFormatted: '15 GB Free',
                features: [
                    'Uses personal Google Drive 15GB quota',
                    'Zero platform storage fee',
                    'Automatic private viewer permissions for enrolled students',
                    'Ideal for 1-to-1 mentorship & skill swaps',
                ],
                isRecommended: false,
            },
            {
                id: 'plan_aws_starter',
                name: 'AWS Cloud Starter',
                provider: 'AWS_S3',
                price: '₹299/mo',
                storageLimitBytes: 26843545600,
                storageLimitFormatted: '25 GB Cloud',
                features: [
                    'Lightning-fast AWS CloudFront CDN delivery',
                    '1080p Full HD adaptive bitrate streaming',
                    'Zero Google Drive daily view quotas',
                    'Unlimited concurrent student streams',
                ],
                isRecommended: false,
            },
            {
                id: 'plan_aws_pro',
                name: 'AWS Studio Pro',
                provider: 'AWS_S3',
                price: '₹699/mo',
                storageLimitBytes: 107374182400,
                storageLimitFormatted: '100 GB Cloud',
                features: [
                    'Ultra-fast AWS CloudFront global edge CDN',
                    '4K Ultra HD & 60fps streaming support',
                    'Automatic HLS multi-bitrate transcoding',
                    'Priority creator encoding queue',
                ],
                isRecommended: true,
            },
            {
                id: 'plan_aws_enterprise',
                name: 'AWS Creator Scale',
                provider: 'AWS_S3',
                price: '₹1,499/mo',
                storageLimitBytes: 536870912000,
                storageLimitFormatted: '500 GB Cloud',
                features: [
                    'Massive 500 GB high-speed media vault',
                    'Enterprise SLA & multi-region redundancy',
                    'DRM encryption & watermarking protection',
                ],
                isRecommended: false,
            },
        ];
        const subscription = await prisma.storageSubscription?.findFirst({
            where: { userId, isActive: true },
            orderBy: { createdAt: 'desc' },
        });
        const activePlan = plans.find((p) => p.id === subscription?.planId) || plans[0];
        return {
            plans,
            activePlan,
            usedStorageBytes: subscription ? Number(subscription.usedStorageBytes) : 3221225472,
            totalStorageBytes: subscription ? Number(subscription.storageLimitBytes) : activePlan.storageLimitBytes,
        };
    }
    async subscribeStoragePlan(userId, planId) {
        const plans = {
            plan_gdrive_free: { limit: BigInt(16106127360), provider: 'GOOGLE_DRIVE' },
            plan_aws_starter: { limit: BigInt(26843545600), provider: 'AWS_S3' },
            plan_aws_pro: { limit: BigInt(107374182400), provider: 'AWS_S3' },
            plan_aws_enterprise: { limit: BigInt(536870912000), provider: 'AWS_S3' },
        };
        const targetPlan = plans[planId];
        if (!targetPlan) {
            throw new AppError('Invalid storage plan selected', 400);
        }
        if (prisma.storageSubscription) {
            await prisma.storageSubscription.updateMany({
                where: { userId },
                data: { isActive: false },
            });
            const subscription = await prisma.storageSubscription.create({
                data: {
                    userId,
                    planId,
                    provider: targetPlan.provider,
                    storageLimitBytes: targetPlan.limit,
                    usedStorageBytes: BigInt(0),
                    isActive: true,
                    expiresAt: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000),
                },
            });
            return subscription;
        }
        return { success: true, planId, provider: targetPlan.provider };
    }
    async grantDriveAccess(userId, lessonId, learnerEmail) {
        const lesson = await prisma.lesson.findUnique({
            where: { id: lessonId },
        });
        if (!lesson) {
            throw new AppError('Lesson not found', 404);
        }
        return {
            success: true,
            lessonId,
            driveFileId: lesson.driveFileId || `drive_${lesson.id}`,
            learnerEmail,
            permissionRole: 'viewer',
            grantedAt: new Date(),
        };
    }
    async generateUploadPresignedUrl(userId, fileType, fileName, storageProvider = 'AWS_S3') {
        const uploadId = `upload_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;
        const extension = fileName.split('.').pop() || 'mp4';
        if (storageProvider === 'GOOGLE_DRIVE') {
            return {
                storageProvider: 'GOOGLE_DRIVE',
                uploadUrl: `https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable`,
                driveFolder: 'SkillBuilder Videos',
                driveFileId: `gdrive_${uploadId}`,
                mediaUrl: `https://drive.google.com/file/d/gdrive_${uploadId}/view`,
                expiresIn: 3600,
            };
        }
        const objectKey = `users/${userId}/videos/${uploadId}.${extension}`;
        const publicUrl = `https://storage.skillbuilder.io/${objectKey}`;
        return {
            storageProvider: 'AWS_S3',
            uploadUrl: `https://storage.skillbuilder.io/upload-presigned?key=${objectKey}&token=${uploadId}`,
            mediaUrl: publicUrl,
            objectKey,
            expiresIn: 3600,
        };
    }
}
//# sourceMappingURL=courses.service.js.map