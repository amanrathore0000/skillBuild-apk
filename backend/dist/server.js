import express from 'express';
import http from 'http';
import cors from 'cors';
import morgan from 'morgan';
import path from 'path';
import fs from 'fs';
import { fileURLToPath } from 'url';
import { ENV } from './config/env.js';
import { connectDatabase } from './config/database.js';
import { errorHandler } from './middlewares/error.middleware.js';
import { setupWebSocket } from './websocket/socket.server.js';
import { toNodeHandler } from 'better-auth/node';
import { auth } from './auth.js';
// Import Route Handlers
import { authRouter } from './modules/auth/auth.routes.js';
import { usersRouter } from './modules/users/users.routes.js';
import { skillsRouter } from './modules/skills/skills.routes.js';
import { swapsRouter } from './modules/swaps/swaps.routes.js';
import { coursesRouter } from './modules/courses/courses.routes.js';
import { chatRouter } from './modules/chat/chat.routes.js';
import { walletRouter } from './modules/wallet/wallet.routes.js';
import { supportRouter } from './modules/support/support.routes.js';
const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const adminDistPath = path.resolve(__dirname, '../../admin-dashboard/dist');
const app = express();
const server = http.createServer(app);
// Global Middlewares
app.use(cors({ origin: ENV.CORS_ORIGIN, credentials: true }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use(morgan(ENV.NODE_ENV === 'development' ? 'dev' : 'combined'));
// Health check endpoint
app.get('/health', (req, res) => {
    res.status(200).json({
        status: 'healthy',
        timestamp: new Date().toISOString(),
        environment: ENV.NODE_ENV,
        service: 'SkillBuilder Backend API (Node.js / Express / Neon PostgreSQL)',
        adminPortal: '/admin',
    });
});
// Mount Better Auth Route Handler (/api/auth/*)
app.all('/api/auth/*', toNodeHandler(auth));
// API Routes Mounting
app.use('/api/v1/auth', authRouter);
app.use('/api/v1/users', usersRouter);
app.use('/api/v1/skills', skillsRouter);
app.use('/api/v1/swaps', swapsRouter);
app.use('/api/v1/courses', coursesRouter);
app.use('/api/v1/chat', chatRouter);
app.use('/api/v1/wallet', walletRouter);
app.use('/api/v1/support', supportRouter);
// Serve Admin Dashboard Web App
if (fs.existsSync(adminDistPath)) {
    app.use('/admin', express.static(adminDistPath));
    app.get('/admin', (req, res) => {
        res.sendFile(path.join(adminDistPath, 'index.html'));
    });
    app.get('/admin/*', (req, res) => {
        res.sendFile(path.join(adminDistPath, 'index.html'));
    });
    app.get('/', (req, res) => {
        res.redirect('/admin');
    });
}
// 404 Catch-all handler for unmatched routes
app.use('*', (req, res) => {
    res.status(404).json({
        success: false,
        message: `API Route not found: [${req.method}] ${req.originalUrl}`,
    });
});
// Global Error Handler
app.use(errorHandler);
// Setup Real-time WebSockets with Socket.io
setupWebSocket(server);
// Start Server
const PORT = Number(ENV.PORT) || 5000;
server.listen(PORT, async () => {
    console.log(`=======================================================`);
    console.log(`🚀 SkillBuilder Backend API Server running on port ${PORT}`);
    console.log(`🌐 Environment: ${ENV.NODE_ENV}`);
    console.log(`📡 WebSocket Gateway: Ready for connections`);
    console.log(`🛡️ Admin Portal: http://localhost:${PORT}/admin`);
    console.log(`📚 Health Check: http://localhost:${PORT}/health`);
    console.log(`=======================================================`);
    // Attempt database connection check
    await connectDatabase();
});
export { app, server };
//# sourceMappingURL=server.js.map