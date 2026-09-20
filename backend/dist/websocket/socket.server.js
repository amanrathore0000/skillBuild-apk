import { Server as SocketIOServer } from 'socket.io';
import { verifyAccessToken } from '../utils/jwt.js';
import { ChatService } from '../modules/chat/chat.service.js';
const chatService = new ChatService();
export function setupWebSocket(httpServer) {
    const io = new SocketIOServer(httpServer, {
        cors: {
            origin: '*',
            methods: ['GET', 'POST'],
        },
    });
    // JWT Authentication Middleware for WebSockets
    io.use((socket, next) => {
        const token = socket.handshake.auth.token || socket.handshake.headers.authorization?.split(' ')[1];
        if (!token) {
            return next(new Error('Authentication error: Missing token'));
        }
        try {
            const payload = verifyAccessToken(token);
            socket.user = payload;
            next();
        }
        catch {
            next(new Error('Authentication error: Invalid or expired token'));
        }
    });
    io.on('connection', (socket) => {
        const userId = socket.user?.userId;
        console.log(`🔌 User connected to WebSocket: ${userId} (${socket.id})`);
        // Join personal user notification room
        if (userId) {
            socket.join(`user:${userId}`);
        }
        // Join a conversation room
        socket.on('join_conversation', (conversationId) => {
            socket.join(`conversation:${conversationId}`);
            console.log(`💬 User ${userId} joined room conversation:${conversationId}`);
        });
        // Leave a conversation room
        socket.on('leave_conversation', (conversationId) => {
            socket.leave(`conversation:${conversationId}`);
            console.log(`🚪 User ${userId} left room conversation:${conversationId}`);
        });
        // Real-time message sending
        socket.on('send_message', async (data) => {
            try {
                if (!userId)
                    return;
                // Persist message to MySQL
                const message = await chatService.sendMessage(userId, data);
                // Broadcast to everyone in conversation room
                io.to(`conversation:${data.conversationId}`).emit('new_message', message);
                // Also notify recipient directly if they are outside this conversation screen
                io.to(`user:${message.recipientId}`).emit('message_notification', {
                    conversationId: data.conversationId,
                    senderName: message.senderName,
                    text: message.text,
                    type: message.type,
                });
            }
            catch (err) {
                socket.emit('error', { message: err.message });
            }
        });
        // Ephemeral typing indicators
        socket.on('typing_start', (conversationId) => {
            socket.to(`conversation:${conversationId}`).emit('user_typing', {
                userId,
                isTyping: true,
            });
        });
        socket.on('typing_stop', (conversationId) => {
            socket.to(`conversation:${conversationId}`).emit('user_typing', {
                userId,
                isTyping: false,
            });
        });
        // Doubt query resolution
        socket.on('resolve_doubt', async (data) => {
            try {
                if (!userId)
                    return;
                const updated = await chatService.resolveDoubt(data.messageId, userId);
                io.to(`conversation:${data.conversationId}`).emit('doubt_resolved', {
                    messageId: data.messageId,
                    isResolved: true,
                });
            }
            catch (err) {
                socket.emit('error', { message: err.message });
            }
        });
        // Video demand upvote
        socket.on('upvote_demand', async (data) => {
            try {
                const updated = await chatService.upvoteVideoDemand(data.messageId);
                io.to(`conversation:${data.conversationId}`).emit('demand_upvoted', {
                    messageId: data.messageId,
                    votes: updated.demandVotes,
                });
            }
            catch (err) {
                socket.emit('error', { message: err.message });
            }
        });
        socket.on('disconnect', () => {
            console.log(`🔌 User disconnected: ${userId} (${socket.id})`);
        });
    });
    return io;
}
//# sourceMappingURL=socket.server.js.map