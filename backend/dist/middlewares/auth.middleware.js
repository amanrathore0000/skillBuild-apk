import { verifyAccessToken } from '../utils/jwt.js';
import { AppError } from './error.middleware.js';
export function requireAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return next(new AppError('Authentication required. Missing Bearer token.', 401));
    }
    const token = authHeader.split(' ')[1];
    try {
        const payload = verifyAccessToken(token);
        req.user = payload;
        next();
    }
    catch {
        return next(new AppError('Invalid or expired authentication token.', 401));
    }
}
export function optionalAuth(req, res, next) {
    const authHeader = req.headers.authorization;
    if (authHeader && authHeader.startsWith('Bearer ')) {
        const token = authHeader.split(' ')[1];
        try {
            req.user = verifyAccessToken(token);
        }
        catch {
            // ignore invalid token for optional auth
        }
    }
    next();
}
//# sourceMappingURL=auth.middleware.js.map