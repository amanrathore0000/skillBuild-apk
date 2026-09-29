import { AuthService } from './src/modules/auth/auth.service.js';
import { prisma } from './src/config/database.js';

async function runAcceptanceTests() {
  console.log('====================================================');
  console.log('🚀 Running SkillBuilder OAuth Backend Acceptance Tests');
  console.log('====================================================');

  process.env.NODE_ENV = 'test';
  const authService = new AuthService();

  const testEmailA = `oauth_user_a_${Date.now()}@example.com`;
  const testSubA = `google_sub_a_${Date.now()}`;
  const testTokenA = `test_google_token:${testEmailA}:${testSubA}`;

  try {
    // -------------------------------------------------------------
    // Test A: New Google User
    // -------------------------------------------------------------
    console.log('\n[Test A] New Google User Registration & Session...');
    const resultA = await authService.authenticateWithGoogle(testTokenA, {
      fallbackName: 'Alice Vance',
      isMentor: true,
    });

    console.log(`  ✓ Result user: ${resultA.user.name} (${resultA.user.email})`);
    console.log(`  ✓ Session token issued: ${resultA.sessionToken.slice(0, 12)}...`);
    console.log(`  ✓ JWT access token issued: ${resultA.accessToken.slice(0, 20)}...`);

    if (resultA.user.email !== testEmailA) {
      throw new Error(`Test A Failed: email ${resultA.user.email} does not match expected ${testEmailA}`);
    }

    // Verify database records
    const usersInDb = await prisma.user.findMany({ where: { email: testEmailA } });
    if (usersInDb.length !== 1) {
      throw new Error(`Test A Failed: expected 1 User record in DB, found ${usersInDb.length}`);
    }
    const accountsInDb = await prisma.account.findMany({ where: { userId: usersInDb[0].id } });
    if (accountsInDb.length !== 1 || accountsInDb[0].accountId !== testSubA || accountsInDb[0].providerId !== 'google') {
      throw new Error(`Test A Failed: Account relationship incorrect: ${JSON.stringify(accountsInDb)}`);
    }
    const sessionsInDb = await prisma.session.findMany({ where: { userId: usersInDb[0].id } });
    if (sessionsInDb.length !== 1 || sessionsInDb[0].token !== resultA.sessionToken) {
      throw new Error(`Test A Failed: Session record incorrect: ${JSON.stringify(sessionsInDb)}`);
    }
    console.log('  ✅ Test A Passed: Exactly 1 User, 1 Account (providerId=google, accountId=sub), and 1 Session created.');

    // -------------------------------------------------------------
    // Test B: Existing Google User Logs In Again
    // -------------------------------------------------------------
    console.log('\n[Test B] Existing Google User Logs In Again...');
    const resultB = await authService.authenticateWithGoogle(testTokenA);

    console.log(`  ✓ Result user: ${resultB.user.name} (${resultB.user.email})`);
    console.log(`  ✓ New Session token issued: ${resultB.sessionToken.slice(0, 12)}...`);

    if (resultB.user.id !== usersInDb[0].id) {
      throw new Error(`Test B Failed: User ID changed from ${usersInDb[0].id} to ${resultB.user.id}`);
    }

    const totalUsersAfterB = await prisma.user.findMany({ where: { email: testEmailA } });
    if (totalUsersAfterB.length !== 1) {
      throw new Error(`Test B Failed: Duplicate User created! Found ${totalUsersAfterB.length} users`);
    }
    const accountsAfterB = await prisma.account.findMany({ where: { userId: usersInDb[0].id } });
    if (accountsAfterB.length !== 1) {
      throw new Error(`Test B Failed: Duplicate Account created! Found ${accountsAfterB.length} accounts`);
    }
    console.log('  ✅ Test B Passed: Existing Account found, same User reused, NO duplicate user or account created.');

    // -------------------------------------------------------------
    // Test C: Account Linking (Existing Email, New Google sub)
    // -------------------------------------------------------------
    console.log('\n[Test C] Account Linking (Pre-existing email links Google Account)...');
    const manualEmail = `manual_user_${Date.now()}@example.com`;
    const manualUser = await prisma.user.create({
      data: {
        email: manualEmail,
        name: 'Manual Account',
        passwordHash: 'dummy_hash',
      },
    });

    const manualSub = `google_sub_manual_${Date.now()}`;
    const manualToken = `test_google_token:${manualEmail}:${manualSub}`;

    const resultC = await authService.authenticateWithGoogle(manualToken);

    if (resultC.user.id !== manualUser.id) {
      throw new Error(`Test C Failed: Linked user ID ${resultC.user.id} does not match manual user ${manualUser.id}`);
    }

    const totalUsersAfterC = await prisma.user.findMany({ where: { email: manualEmail } });
    if (totalUsersAfterC.length !== 1) {
      throw new Error(`Test C Failed: Duplicate User created during linking! Found ${totalUsersAfterC.length} users`);
    }
    const linkedAccount = await prisma.account.findUnique({
      where: {
        providerId_accountId: {
          providerId: 'google',
          accountId: manualSub,
        },
      },
    });
    if (!linkedAccount || linkedAccount.userId !== manualUser.id) {
      throw new Error(`Test C Failed: Account was not linked properly to manual user ID`);
    }
    console.log('  ✅ Test C Passed: Pre-existing user account safely linked to Google provider identity without duplicates.');

    // -------------------------------------------------------------
    // Test D: Logout (Session Invalidation)
    // -------------------------------------------------------------
    console.log('\n[Test D] Logout / Session Invalidation...');
    await authService.logout(resultA.user.id, resultA.sessionToken);

    const checkSession = await prisma.session.findUnique({ where: { token: resultA.sessionToken } });
    if (checkSession) {
      throw new Error(`Test D Failed: Session ${resultA.sessionToken} still exists in DB after logout`);
    }
    console.log('  ✅ Test D Passed: Session successfully invalidated from database.');

    // -------------------------------------------------------------
    // Test E: Invalid / Tampered Token
    // -------------------------------------------------------------
    console.log('\n[Test E] Invalid / Tampered Token Rejection...');
    let errorCaught = false;
    try {
      await authService.authenticateWithGoogle('invalid_random_garbage_token');
    } catch (err: any) {
      errorCaught = true;
      console.log(`  ✓ Expected 401 error caught: ${err.message}`);
    }
    if (!errorCaught) {
      throw new Error('Test E Failed: Invalid token was not rejected with 401 error');
    }
    console.log('  ✅ Test E Passed: Invalid token rejected properly.');

    // Cleanup test records
    await prisma.session.deleteMany({ where: { userId: { in: [usersInDb[0].id, manualUser.id] } } });
    await prisma.account.deleteMany({ where: { userId: { in: [usersInDb[0].id, manualUser.id] } } });
    await prisma.user.deleteMany({ where: { id: { in: [usersInDb[0].id, manualUser.id] } } });

    console.log('\n====================================================');
    console.log('🎉 ALL ACCEPTANCE TESTS PASSED SUCCESSFULLY! (5/5)');
    console.log('====================================================\n');
  } catch (err: any) {
    console.error('\n❌ Acceptance Test Failed:', err);
    process.exit(1);
  } finally {
    await prisma.$disconnect();
  }
}

runAcceptanceTests();
