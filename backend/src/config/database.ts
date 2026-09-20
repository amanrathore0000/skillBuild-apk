import { PrismaClient } from '@prisma/client';

export const prisma = new PrismaClient({
  log: process.env.NODE_ENV === 'development' ? ['query', 'error', 'warn'] : ['error'],
});

export async function connectDatabase() {
  try {
    await prisma.$connect();
    console.log('✅ Connected to MySQL database successfully via Prisma.');
  } catch (error) {
    console.error('❌ Failed to connect to MySQL database:', error);
    // Don't crash immediately so server can run or show connection instructions
  }
}
