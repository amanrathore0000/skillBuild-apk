import { prisma } from '../../config/database.js';
import { AppError } from '../../middlewares/error.middleware.js';

export class WalletService {
  async getWallet(userId: string) {
    let wallet = await prisma.mentorWallet.findUnique({
      where: { userId },
    });

    if (!wallet) {
      wallet = await prisma.mentorWallet.create({
        data: { userId },
      });
    }

    const transactions = await prisma.walletTransaction.findMany({
      where: { userId },
      orderBy: { createdAt: 'desc' },
      take: 50,
    });

    return {
      balance: `$${Number(wallet.balance).toFixed(2)}`,
      pendingPayout: `$${Number(wallet.pendingPayout).toFixed(2)}`,
      totalEarned: `$${Number(wallet.totalEarned).toFixed(2)}`,
      monthlyRevenue: `$${Number(wallet.monthlyRevenue).toFixed(2)}`,
      transactions: transactions.map((t) => ({
        id: t.id,
        title: t.title,
        subtitle: t.subtitle,
        amount: `${t.isCredit ? '+' : '-'}$${Number(t.amount).toFixed(2)}`,
        date: t.createdAt.toISOString().split('T')[0],
        isCredit: t.isCredit,
      })),
    };
  }

  async requestPayout(userId: string, amount: number, payoutMethod: string) {
    const wallet = await prisma.mentorWallet.findUnique({
      where: { userId },
    });

    if (!wallet || Number(wallet.balance) < amount) {
      throw new AppError('Insufficient wallet balance for this payout request', 400);
    }

    return prisma.$transaction(async (tx) => {
      // Deduct balance and add to pending payout
      const updatedWallet = await tx.mentorWallet.update({
        where: { userId },
        data: {
          balance: { decrement: amount },
          pendingPayout: { increment: amount },
        },
      });

      // Record transaction
      await tx.walletTransaction.create({
        data: {
          userId,
          title: 'Payout Request',
          subtitle: `Transfer to ${payoutMethod}`,
          amount,
          isCredit: false,
        },
      });

      return updatedWallet;
    });
  }
}
