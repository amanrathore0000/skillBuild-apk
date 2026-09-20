import { UsersService } from './users.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';
const usersService = new UsersService();
export class UsersController {
    static async getProfile(req, res, next) {
        try {
            const id = req.params.id;
            const profile = await usersService.getProfile(id);
            return ApiResponse.success(res, profile, 'User profile fetched');
        }
        catch (error) {
            next(error);
        }
    }
    static async updateProfile(req, res, next) {
        try {
            const updated = await usersService.updateProfile(req.user.userId, req.body);
            return ApiResponse.success(res, updated, 'Profile updated successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async updateSkills(req, res, next) {
        try {
            const updated = await usersService.updateUserSkills(req.user.userId, req.body);
            return ApiResponse.success(res, updated, 'User skills updated successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async getMentorStats(req, res, next) {
        try {
            const stats = await usersService.getMentorStats(req.user.userId);
            return ApiResponse.success(res, stats, 'Mentor stats fetched');
        }
        catch (error) {
            next(error);
        }
    }
}
//# sourceMappingURL=users.controller.js.map