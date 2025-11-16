import { PrismaClient } from "@prisma/client";
const db = new PrismaClient();

async function main() {
  // 1) Branch
  const branch = await db.branch.upsert({
    where: { id: 1n }, update: {},
    create: { name: "CN Quận 1", address: "123 Lê Lợi, Q1" }
  });

  // 2) Trainer
  const trainer = await db.user.upsert({
    where: { email: "trainer@example.com" }, update: {},
    create: {
      email: "trainer@example.com",
      passwordHash: "$2b$10$8wIqiiWMdnGN4b.BOqvkSe4MYcpSXKsWsVfUTrhzZM2xRnaNyRdi6", // "123456"
      fullName: "Trần Trainer",
      role: "trainer",
    } as any
  });

  // 3) 2 lớp mẫu hôm nay
  const now = new Date();
  const start1 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 18, 0, 0);
  const start2 = new Date(now.getFullYear(), now.getMonth(), now.getDate(), 19, 30, 0);

  await db.class.createMany({
    data: [
      { title: "HIIT 45'", description: "Luyện cường độ cao", trainerId: trainer.id, branchId: branch.id, startTime: start1, endTime: new Date(start1.getTime()+45*60000), capacity: 20 },
      { title: "Yoga cơ bản", description: "Kéo giãn - thở",     trainerId: trainer.id, branchId: branch.id, startTime: start2, endTime: new Date(start2.getTime()+60*60000), capacity: 15 },
    ]
  });

  console.log("Seed classes OK");
}

main().finally(()=>db.$disconnect());
