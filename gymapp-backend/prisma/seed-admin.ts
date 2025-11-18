import { PrismaClient } from "@prisma/client";
import bcrypt from "bcrypt";

const db = new PrismaClient();

async function main() {
  console.log("🔧 Creating admin user...");

  // Hash password
  const passwordHash = await bcrypt.hash("admin123", 10);

  // Create or update admin user
  const admin = await db.user.upsert({
    where: { email: "admin@gymapp.com" },
    update: {
      role: "admin",
      status: "active",
      passwordHash,
    },
    create: {
      email: "admin@gymapp.com",
      passwordHash,
      fullName: "Admin User",
      role: "admin",
      status: "active",
    },
  });

  console.log("✅ Admin user created/updated:");
  console.log(`   Email: ${admin.email}`);
  console.log(`   Role: ${admin.role}`);
  console.log(`   Password: admin123`);

  // Create trainer user for testing
  const trainerHash = await bcrypt.hash("trainer123", 10);
  const trainer = await db.user.upsert({
    where: { email: "trainer@gymapp.com" },
    update: {
      role: "trainer",
      status: "active",
      passwordHash: trainerHash,
    },
    create: {
      email: "trainer@gymapp.com",
      passwordHash: trainerHash,
      fullName: "Trainer User",
      role: "trainer",
      status: "active",
    },
  });

  console.log("✅ Trainer user created/updated:");
  console.log(`   Email: ${trainer.email}`);
  console.log(`   Role: ${trainer.role}`);
  console.log(`   Password: trainer123`);

  // Create receptionist user
  const receptionistHash = await bcrypt.hash("receptionist123", 10);
  const receptionist = await db.user.upsert({
    where: { email: "receptionist@gymapp.com" },
    update: {
      role: "receptionist",
      status: "active",
      passwordHash: receptionistHash,
    },
    create: {
      email: "receptionist@gymapp.com",
      passwordHash: receptionistHash,
      fullName: "Receptionist User",
      role: "receptionist",
      status: "active",
    },
  });

  console.log("✅ Receptionist user created/updated:");
  console.log(`   Email: ${receptionist.email}`);
  console.log(`   Role: ${receptionist.role}`);
  console.log(`   Password: receptionist123`);

  console.log("\n🎉 All test users ready!");
}

main()
  .catch((e) => {
    console.error("❌ Error:", e);
    process.exit(1);
  })
  .finally(() => db.$disconnect());
