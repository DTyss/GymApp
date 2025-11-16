export type Paging = { page: number; pageSize: number };
export function parsePaging(q: any): Paging {
  const page = Math.max(1, Number(q.page ?? 1));
  const pageSize = Math.min(100, Math.max(1, Number(q.pageSize ?? 20))); // cap 100
  return { page, pageSize };
}
export function toSkipTake({ page, pageSize }: Paging) {
  return { skip: (page - 1) * pageSize, take: pageSize };
}
