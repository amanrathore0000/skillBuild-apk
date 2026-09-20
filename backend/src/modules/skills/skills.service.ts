import { prisma } from '../../config/database.js';
import { SkillLevel } from '@prisma/client';

export class SkillsService {
  async getCategories() {
    return prisma.category.findMany({
      include: {
        _count: {
          select: { skills: true, courses: true },
        },
      },
      orderBy: { name: 'asc' },
    });
  }

  async getSkills(query: { categoryId?: string; search?: string; level?: string }) {
    const where: any = {};

    if (query.categoryId) {
      where.categoryId = query.categoryId;
    }

    if (query.search) {
      where.OR = [
        { title: { contains: query.search } },
        { description: { contains: query.search } },
      ];
    }

    if (query.level && query.level !== 'ALL_LEVELS') {
      where.level = query.level as SkillLevel;
    }

    return prisma.skill.findMany({
      where,
      include: {
        category: true,
      },
      orderBy: { mentorCount: 'desc' },
    });
  }

  async createSkill(data: { categoryId: string; title: string; description?: string; level?: SkillLevel }) {
    return prisma.skill.create({
      data: {
        categoryId: data.categoryId,
        title: data.title,
        description: data.description,
        level: data.level || SkillLevel.ALL_LEVELS,
      },
      include: { category: true },
    });
  }
}
