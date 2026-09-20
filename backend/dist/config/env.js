import dotenv from 'dotenv';
dotenv.config();
export const ENV = {
    PORT: process.env.PORT || '5000',
    NODE_ENV: process.env.NODE_ENV || 'development',
    DATABASE_URL: process.env.DATABASE_URL || 'mysql://root:password@localhost:3306/skillbuilder',
    JWT_SECRET: process.env.JWT_SECRET || 'skillbuilder_default_jwt_secret_dev',
    JWT_REFRESH_SECRET: process.env.JWT_REFRESH_SECRET || 'skillbuilder_default_refresh_secret_dev',
    JWT_EXPIRES_IN: process.env.JWT_EXPIRES_IN || '7d',
    JWT_REFRESH_EXPIRES_IN: process.env.JWT_REFRESH_EXPIRES_IN || '30d',
    GOOGLE_CLIENT_ID: process.env.GOOGLE_CLIENT_ID || '',
    CORS_ORIGIN: process.env.CORS_ORIGIN || '*',
};
//# sourceMappingURL=env.js.map