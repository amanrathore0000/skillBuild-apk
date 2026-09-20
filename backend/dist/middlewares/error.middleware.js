export class AppError extends Error {
    statusCode;
    errors;
    constructor(message, statusCode = 400, errors) {
        super(message);
        this.statusCode = statusCode;
        this.errors = errors;
        Object.setPrototypeOf(this, AppError.prototype);
    }
}
export function errorHandler(err, req, res, next) {
    console.error('💥 Error caught in handler:', err);
    if (err instanceof AppError) {
        return res.status(err.statusCode).json({
            success: false,
            message: err.message,
            ...(err.errors ? { errors: err.errors } : {}),
        });
    }
    return res.status(500).json({
        success: false,
        message: process.env.NODE_ENV === 'production' ? 'Internal server error' : err.message,
    });
}
//# sourceMappingURL=error.middleware.js.map