import { PrismaClient } from "@prisma/client";
import { hashPassword } from "../src/libs/auth";
const db = new PrismaClient();

async function main() {
  const adminPass = await hashPassword("admin123");
  const memberPass = await hashPassword("123456");

  await db.user.upsert({
    where: { email: "admin@gymapp.local" },
    update: {},
    create: {
      email: "admin@gymapp.local",
      fullName: "Admin",
      passwordHash: adminPass,
      role: "admin"
    }
  });

  await db.user.upsert({
    where: { email: "a@example.com" },
    update: {},
    create: {
      email: "a@example.com",
      fullName: "Nguyễn A",
      passwordHash: memberPass,
      role: "member"
    }
  });

  console.log("Seed users OK");
}

main().finally(() => db.$disconnect());
