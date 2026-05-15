export async function authFetch(input: RequestInfo, init?: RequestInit) {
  const token = localStorage.getItem("token");
  const headers = new Headers(init?.headers || {});
  if (token) {
    headers.set("Authorization", `Bearer ${token}`);
  }
  const resp = await fetch(input, { ...init, headers });
  return resp;
}
