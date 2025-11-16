import { PrismaClient } from "@prisma/client";
const db = new PrismaClient();
async function main() {
  const plan = await db.plan.upsert({
    where: { id: 1n },
    update: {},
    create: { name: "12 buổi/30 ngày", price: 800000 as any, sessions: 12, durationDays: 30 }
  });

  const user = await db.user.upsert({
    where: { email: "a@example.com" },
    update: {},
    create: {
      email: "a@example.com",
      passwordHash: "$2b$10$8wIqiiWMdnGN4b.BOqvkSe4MYcpSXKsWsVfUTrhzZM2xRnaNyRdi6", // "123456" (sample)
      fullName: "Nguyễn A",
      role: "member",
    } as any
  });

  await db.membership.create({
    data: {
      userId: user.id,
      planId: plan.id,
      startDate: new Date(),
      endDate: new Date(Date.now() + 30*24*3600*1000),
      remainingSessions: 12,
    }
  });

  console.log("Seed OK");
}
main().finally(()=>db.$disconnect());
