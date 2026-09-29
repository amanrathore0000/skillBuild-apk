import { AuthService } from './auth.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';
const authService = new AuthService();
export class AuthController {
    static async googleAuth(req, res, next) {
        try {
            const { idToken, isMentor, name, picture } = req.body;
            const result = await authService.authenticateWithGoogle(idToken, {
                isMentor: Boolean(isMentor),
                ipAddress: req.ip || req.socket.remoteAddress,
                userAgent: req.headers['user-agent'],
                fallbackName: name,
                fallbackPicture: picture,
            });
            return ApiResponse.success(res, result, 'Google authentication successful');
        }
        catch (error) {
            next(error);
        }
    }
    static async signup(req, res, next) {
        try {
            const result = await authService.signup(req.body);
            return ApiResponse.created(res, result, 'Account created successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async login(req, res, next) {
        try {
            const result = await authService.login(req.body);
            return ApiResponse.success(res, result, 'Login successful');
        }
        catch (error) {
            next(error);
        }
    }
    static async logout(req, res, next) {
        try {
            const sessionToken = req.headers['x-session-token'];
            const result = await authService.logout(req.user.userId, sessionToken);
            return ApiResponse.success(res, result, 'Logged out successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async refresh(req, res, next) {
        try {
            const { refreshToken } = req.body;
            const result = await authService.refreshToken(refreshToken);
            return ApiResponse.success(res, result, 'Token refreshed successfully');
        }
        catch (error) {
            next(error);
        }
    }
    static async me(req, res, next) {
        try {
            const result = await authService.getCurrentUser(req.user.userId);
            return ApiResponse.success(res, result, 'User profile fetched');
        }
        catch (error) {
            next(error);
        }
    }
}
//# sourceMappingURL=auth.controller.js.map