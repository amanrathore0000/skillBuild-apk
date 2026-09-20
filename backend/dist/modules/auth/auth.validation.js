import { z } from 'zod';
export const GoogleLoginSchema = z.object({
    idToken: z.string().min(1, 'Google ID token is required'),
});
export const SignupSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    name: z.string().min(2, 'Name must be at least 2 characters'),
    isMentor: z.boolean().optional().default(false),
    location: z.string().optional(),
    phone: z.string().optional(),
    dob: z.string().optional(),
});
export const LoginSchema = z.object({
    email: z.string().email('Invalid email address'),
    password: z.string().min(1, 'Password is required'),
});
export const RefreshTokenSchema = z.object({
    refreshToken: z.string().min(1, 'Refresh token is required'),
});
//# sourceMappingURL=auth.validation.js.map