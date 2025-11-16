import { Role } from "../middlewares/rbac";

export const Policy = {
  classes: {
    list:       ["member","trainer","admin","receptionist"] as Role[],
    create:     ["trainer","admin"] as Role[],
    update:     ["trainer","admin"] as Role[],
    delete:     ["admin"] as Role[],
  },
  bookings: {
    create:     ["member","admin","receptionist"] as Role[], // receptionist tạo hộ
    cancel:     ["member","admin"] as Role[],
    listMy:     ["member","trainer","admin","receptionist"] as Role[],
  },
  checkins: {
    qr:         ["admin","receptionist","member"] as Role[], // tuỳ yêu cầu, thường quầy dùng
  },
  notifications: {
    send:       ["trainer","admin"] as Role[],
    listMy:     ["member","trainer","admin","receptionist"] as Role[],
  },
  plans: {
    crud:       ["admin"] as Role[],
  },
  users: {
    list:       ["admin"] as Role[],
    updateSelf: ["member","trainer","admin","receptionist"] as Role[],
  }
} as const;
