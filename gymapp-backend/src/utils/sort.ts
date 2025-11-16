export type Sort = { sortBy?: string; sortDir?: "asc"|"desc" };
export function parseSort(q: any, allow: string[], def: Sort): Sort {
  const sortBy = allow.includes(String(q.sortBy)) ? String(q.sortBy) : def.sortBy;
  const sortDir = (String(q.sortDir).toLowerCase()==="desc" ? "desc" : "asc") as "asc"|"desc";
  return { sortBy, sortDir };
}
