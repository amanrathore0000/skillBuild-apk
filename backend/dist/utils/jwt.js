import jwt from 'jsonwebtoken';
import { ENV } from '../config/env.js';
export function generateTokens(payload) {
    const accessToken = jwt.sign(payload, ENV.JWT_SECRET, {
        expiresIn: ENV.JWT_EXPIRES_IN,
    });
    const refreshToken = jwt.sign(payload, ENV.JWT_REFRESH_SECRET, {
        expiresIn: ENV.JWT_REFRESH_EXPIRES_IN,
    });
    return { accessToken, refreshToken };
}
export function verifyAccessToken(token) {
    return jwt.verify(token, ENV.JWT_SECRET);
}
export function verifyRefreshToken(token) {
    return jwt.verify(token, ENV.JWT_REFRESH_SECRET);
}
//# sourceMappingURL=jwt.js.map