import { Request, Response } from "express";
import { prisma } from "../../libs/prisma";
import { asyncHandler } from "../../utils/errors";

/**
 * GET /stats/dashboard - Tổng quan hệ thống
 * Trả về các số liệu tổng quan cho admin dashboard
 */
export const dashboard = asyncHandler(async (req: Request, res: Response) => {
  const now = new Date();
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
  const endOfToday = new Date(startOfToday.getTime() + 24 * 60 * 60 * 1000);

  // Thống kê song song để tối ưu performance
  const [
    totalUsers,
    totalMembers,
    activeMembers,
    totalClasses,
    todayClasses,
    totalCheckins,
    todayCheckins,
    monthCheckins,
    totalBookings,
    activeMemberships
  ] = await Promise.all([
    // Tổng số users
    prisma.user.count(),
    
    // Tổng số members
    prisma.user.count({ 
      where: { role: "member" } 
    }),
    
    // Members đang active
    prisma.user.count({ 
      where: { 
        role: "member", 
        status: "active" 
      } 
    }),
    
    // Tổng số lớp học
    prisma.class.count(),
    
    // Lớp học hôm nay
    prisma.class.count({
      where: {
        startTime: {
          gte: startOfToday,
          lt: endOfToday
        }
      }
    }),
    
    // Tổng lượt check-in
    prisma.checkin.count(),
    
    // Check-in hôm nay
    prisma.checkin.count({
      where: { 
        checkedAt: { gte: startOfToday } 
      }
    }),
    
    // Check-in tháng này
    prisma.checkin.count({
      where: { 
        checkedAt: { gte: startOfMonth } 
      }
    }),
    
    // Tổng bookings
    prisma.booking.count(),
    
    // Memberships đang active
    prisma.membership.count({
      where: {
        status: "active",
        endDate: { gte: now }
      }
    })
  ]);

  res.json({
    users: {
      total: totalUsers,
      members: totalMembers,
      activeMembers
    },
    classes: {
      total: totalClasses,
      today: todayClasses
    },
    checkins: {
      total: totalCheckins,
      today: todayCheckins,
      thisMonth: monthCheckins
    },
    bookings: {
      total: totalBookings
    },
    memberships: {
      active: activeMemberships
    }
  });
});

/**
 * GET /stats/members - Thống kê hội viên
 * Query: from, to (date range)
 */
export const memberStats = asyncHandler(async (req: Request, res: Response) => {
  const { from, to } = req.query;
  
  const where: any = {};
  if (from || to) {
    where.createdAt = {};
    if (from) where.createdAt.gte = new Date(String(from));
    if (to) where.createdAt.lte = new Date(String(to));
  }

  const [
    byRole,
    byStatus,
    newMembers,
    withActiveMembership
  ] = await Promise.all([
    // Phân bố theo role
    prisma.user.groupBy({
      by: ["role"],
      _count: { id: true },
      where
    }),
    
    // Phân bố theo status
    prisma.user.groupBy({
      by: ["status"],
      _count: { id: true },
      where
    }),
    
    // Hội viên mới
    prisma.user.count({
      where: {
        ...where,
        role: "member"
      }
    }),
    
    // Có membership active
    prisma.user.count({
      where: {
        role: "member",
        memberships: {
          some: {
            status: "active",
            endDate: { gte: new Date() }
          }
        }
      }
    })
  ]);

  res.json({
    byRole,
    byStatus,
    newMembers,
    withActiveMembership
  });
});

/**
 * GET /stats/checkins - Thống kê check-in
 * Query: from, to, branchId
 */
export const checkinStats = asyncHandler(async (req: Request, res: Response) => {
  const { from, to, branchId } = req.query;
  
  const where: any = {};
  if (from || to) {
    where.checkedAt = {};
    if (from) where.checkedAt.gte = new Date(String(from));
    if (to) where.checkedAt.lte = new Date(String(to));
  }
  if (branchId) where.branchId = BigInt(String(branchId));

  const [
    total,
    byBranch,
    byMethod,
    byDay
  ] = await Promise.all([
    // Tổng số check-in
    prisma.checkin.count({ where }),
    
    // Theo chi nhánh
    prisma.checkin.groupBy({
      by: ["branchId"],
      _count: { id: true },
      where
    }),
    
    // Theo phương thức
    prisma.checkin.groupBy({
      by: ["method"],
      _count: { id: true },
      where
    }),
    
    // Theo ngày (7 ngày gần nhất) - sử dụng raw query
    prisma.$queryRaw`
      SELECT DATE(checkedAt) as date, COUNT(*) as count
      FROM Checkin
      WHERE checkedAt >= DATE_SUB(NOW(), INTERVAL 7 DAY)
      GROUP BY DATE(checkedAt)
      ORDER BY date DESC
    `
  ]);

  res.json({
    total,
    byBranch,
    byMethod,
    byDay
  });
});

/**
 * GET /stats/revenue - Thống kê doanh thu (giả định)
 * Tính dựa trên số memberships được tạo
 * Query: from, to (date range)
 */
export const revenueStats = asyncHandler(async (req: Request, res: Response) => {
  const { from, to } = req.query;
  
  const where: any = {};
  if (from || to) {
    where.createdAt = {};
    if (from) where.createdAt.gte = new Date(String(from));
    if (to) where.createdAt.lte = new Date(String(to));
  }

  // Lấy tất cả memberships kèm plan để tính doanh thu
  const memberships = await prisma.membership.findMany({
    where,
    include: { plan: true }
  });

  // Tính tổng doanh thu
  const totalRevenue = memberships.reduce((sum, m) => {
    return sum + Number(m.plan.price);
  }, 0);

  // Doanh thu theo plan
  const byPlanMap = memberships.reduce((acc: any, m) => {
    const planName = m.plan.name;
    if (!acc[planName]) {
      acc[planName] = { count: 0, revenue: 0 };
    }
    acc[planName].count += 1;
    acc[planName].revenue += Number(m.plan.price);
    return acc;
  }, {});

  // Convert Map to Array
  const byPlan = Object.entries(byPlanMap).map(([name, data]: [string, any]) => ({
    plan: name,
    count: data.count,
    revenue: data.revenue
  }));

  res.json({
    totalRevenue,
    totalMemberships: memberships.length,
    byPlan
  });
});