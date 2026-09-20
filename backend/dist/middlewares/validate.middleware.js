import { ZodError } from 'zod';
import { AppError } from './error.middleware.js';
export function validate(schema) {
    return async (req, res, next) => {
        try {
            req.body = await schema.parseAsync(req.body);
            next();
        }
        catch (error) {
            if (error instanceof ZodError) {
                const issues = error.errors.map((e) => ({
                    field: e.path.join('.'),
                    message: e.message,
                }));
                return next(new AppError('Validation failed', 422, issues));
            }
            next(error);
        }
    };
}
//# sourceMappingURL=validate.middleware.js.map