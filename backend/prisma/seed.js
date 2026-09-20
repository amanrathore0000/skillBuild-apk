"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const client_1 = require("@prisma/client");
const bcryptjs_1 = __importDefault(require("bcryptjs"));
const prisma = new client_1.PrismaClient();
async function main() {
    console.log('🌱 Seeding SkillBuilder MySQL database...');
    // 1. Clean existing records in reverse dependency order
    await prisma.chatMessage.deleteMany();
    await prisma.conversation.deleteMany();
    await prisma.walletTransaction.deleteMany();
    await prisma.mentorWallet.deleteMany();
    await prisma.lessonProgress.deleteMany();
    await prisma.enrollment.deleteMany();
    await prisma.lesson.deleteMany();
    await prisma.course.deleteMany();
    await prisma.swapProposal.deleteMany();
    await prisma.userSkill.deleteMany();
    await prisma.skill.deleteMany();
    await prisma.category.deleteMany();
    await prisma.review.deleteMany();
    await prisma.user.deleteMany();
    // 2. Seed Categories
    const devCategory = await prisma.category.create({
        data: { id: 'cat-dev', name: 'Development', iconName: 'code' },
    });
    const musicCategory = await prisma.category.create({
        data: { id: 'cat-music', name: 'Music', iconName: 'musical_note' },
    });
    const designCategory = await prisma.category.create({
        data: { id: 'cat-design', name: 'Design', iconName: 'palette' },
    });
    const cookingCategory = await prisma.category.create({
        data: { id: 'cat-cooking', name: 'Cooking', iconName: 'utensils' },
    });
    const langCategory = await prisma.category.create({
        data: { id: 'cat-lang', name: 'Language', iconName: 'globe' },
    });
    const fitnessCategory = await prisma.category.create({
        data: { id: 'cat-fitness', name: 'Fitness', iconName: 'fitness' },
    });
    // 3. Seed Skills
    const guitarSkill = await prisma.skill.create({
        data: {
            id: 'skill-guitar',
            categoryId: musicCategory.id,
            title: 'Acoustic Guitar',
            description: 'Fingerstyle technique, chord progressions, and rhythm.',
            level: client_1.SkillLevel.ALL_LEVELS,
            mentorCount: 12,
        },
    });
    const kotlinSkill = await prisma.skill.create({
        data: {
            id: 'skill-kotlin',
            categoryId: devCategory.id,
            title: 'Kotlin & Jetpack Compose',
            description: 'Modern Android UI, Coroutines, StateFlow, and Clean Architecture.',
            level: client_1.SkillLevel.INTERMEDIATE,
            mentorCount: 24,
        },
    });
    const nodeSkill = await prisma.skill.create({
        data: {
            id: 'skill-node',
            categoryId: devCategory.id,
            title: 'Node.js & MySQL',
            description: 'Scalable REST APIs, relational database modeling, and WebSockets.',
            level: client_1.SkillLevel.ADVANCED,
            mentorCount: 18,
        },
    });
    const uiDesignSkill = await prisma.skill.create({
        data: {
            id: 'skill-design',
            categoryId: designCategory.id,
            title: 'UI/UX Design in Figma',
            description: 'Design systems, auto-layout, wireframing, and interactive prototypes.',
            level: client_1.SkillLevel.BEGINNER,
            mentorCount: 15,
        },
    });
    const spanishSkill = await prisma.skill.create({
        data: {
            id: 'skill-spanish',
            categoryId: langCategory.id,
            title: 'Conversational Spanish',
            description: 'Grammar essentials, pronunciation, and everyday dialogues.',
            level: client_1.SkillLevel.ALL_LEVELS,
            mentorCount: 8,
        },
    });
    // 4. Seed Demo Users
    const passwordHash = await bcryptjs_1.default.hash('password123', 10);
    const mentorUser = await prisma.user.create({
        data: {
            id: 'usr-mentor-1',
            email: 'mentor.alex@skillbuilder.com',
            passwordHash,
            name: 'Alex Rivera',
            avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=400',
            location: 'San Francisco, CA',
            bio: 'Professional guitarist and music producer with 10+ years teaching experience.',
            rating: 4.95,
            reviewCount: 42,
            isVerified: true,
            isMentor: true,
        },
    });
    const learnerUser = await prisma.user.create({
        data: {
            id: 'usr-learner-1',
            email: 'learner.sophia@skillbuilder.com',
            passwordHash,
            name: 'Sophia Chen',
            avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=400',
            location: 'Seattle, WA',
            bio: 'Senior Android Engineer passionate about learning music and sharing Jetpack Compose skills.',
            rating: 4.88,
            reviewCount: 18,
            isVerified: true,
            isMentor: true,
        },
    });
    // 5. Seed User Skills (Setup Reciprocal Match!)
    // Alex: Teaches Acoustic Guitar, Wants Kotlin & Jetpack Compose
    await prisma.userSkill.createMany({
        data: [
            { userId: mentorUser.id, skillId: guitarSkill.id, skillType: client_1.SkillType.TAUGHT },
            { userId: mentorUser.id, skillId: kotlinSkill.id, skillType: client_1.SkillType.WANTED },
        ],
    });
    // Sophia: Teaches Kotlin & Jetpack Compose, Wants Acoustic Guitar
    await prisma.userSkill.createMany({
        data: [
            { userId: learnerUser.id, skillId: kotlinSkill.id, skillType: client_1.SkillType.TAUGHT },
            { userId: learnerUser.id, skillId: guitarSkill.id, skillType: client_1.SkillType.WANTED },
        ],
    });
    // 6. Seed Wallets
    await prisma.mentorWallet.create({
        data: {
            userId: mentorUser.id,
            balance: 1250.00,
            pendingPayout: 320.00,
            totalEarned: 4890.00,
            monthlyRevenue: 980.00,
        },
    });
    await prisma.walletTransaction.create({
        data: {
            userId: mentorUser.id,
            title: 'Course Purchase: Fingerstyle Mastery',
            subtitle: 'Enrolled student payment',
            amount: 49.99,
            isCredit: true,
        },
    });
    // 7. Seed Sample Course & Lessons
    const course = await prisma.course.create({
        data: {
            id: 'course-guitar-101',
            mentorId: mentorUser.id,
            categoryId: musicCategory.id,
            title: 'Fingerstyle Acoustic Guitar: From Zero to Flow',
            description: 'Master right-hand thumb independence, percussive tapping, and acoustic harmonics in 6 structured lessons.',
            thumbnailUrl: 'https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800',
            level: client_1.SkillLevel.ALL_LEVELS,
            visibility: client_1.CourseVisibility.PUBLIC,
            priceType: client_1.PriceType.FREE_WITH_SWAP,
            priceAmount: 0.00,
            duration: '2h 15m',
            rating: 4.95,
            reviewCount: 38,
        },
    });
    await prisma.lesson.createMany({
        data: [
            {
                courseId: course.id,
                title: 'Lesson 1: Acoustic Anatomy & Right Hand Ergonomics',
                duration: '14:20',
                durationSeconds: 860,
                videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4',
                sequenceOrder: 1,
            },
            {
                courseId: course.id,
                title: 'Lesson 2: Travis Picking & Independent Thumb Patterns',
                duration: '22:15',
                durationSeconds: 1335,
                videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4',
                sequenceOrder: 2,
            },
            {
                courseId: course.id,
                title: 'Lesson 3: Harmonic Tapping & Modern Flow Techniques',
                duration: '18:40',
                durationSeconds: 1120,
                videoUrl: 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4',
                sequenceOrder: 3,
            },
        ],
    });
    // 8. Seed Swap Proposal
    const swap = await prisma.swapProposal.create({
        data: {
            id: 'swap-alex-sophia',
            senderId: learnerUser.id,
            receiverId: mentorUser.id,
            senderSkillId: kotlinSkill.id,
            receiverSkillId: guitarSkill.id,
            status: client_1.SwapStatus.ACTIVE,
            proposedDate: new Date(Date.now() + 86400000 * 2),
            message: 'Hey Alex! I would love to learn fingerstyle guitar with you in exchange for Kotlin & Compose mentoring.',
        },
    });
    // 9. Seed Conversation & Messages
    const conversation = await prisma.conversation.create({
        data: {
            id: 'conv-alex-sophia',
            mentorId: mentorUser.id,
            learnerId: learnerUser.id,
            swapId: swap.id,
            lastMessage: 'Sounds like a great swap! Let us schedule our first session.',
            hasDoubtPending: false,
        },
    });
    await prisma.chatMessage.createMany({
        data: [
            {
                conversationId: conversation.id,
                senderId: learnerUser.id,
                recipientId: mentorUser.id,
                text: 'Hi Alex! Looking forward to our reciprocal skill swap sessions.',
                type: client_1.ChatMessageType.STANDARD,
            },
            {
                conversationId: conversation.id,
                senderId: mentorUser.id,
                recipientId: learnerUser.id,
                text: 'Sounds like a great swap! Let us schedule our first session.',
                type: client_1.ChatMessageType.STANDARD,
            },
        ],
    });
    console.log('✅ Seed completed successfully with demo users, skills, course, swap, and conversation!');
}
main()
    .catch((e) => {
    console.error(e);
    process.exit(1);
})
    .finally(async () => {
    await prisma.$disconnect();
});
//# sourceMappingURL=seed.js.map