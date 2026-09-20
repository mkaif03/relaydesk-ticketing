import Link from "next/link";
import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import AuditFilterControls from "@/components/AuditFilterControls";

export default async function AuditLogsPage({
  searchParams,
}: {
  searchParams: Promise<{ [key: string]: string | undefined }>;
}) {
  const params = await searchParams;
  const page = params.page || "0";
  const action = params.action || "";
  const entityType = params.entityType || "";

  const apiUrl = process.env.API_URL || "http://localhost:8080";
  const cookieStore = await cookies();
  const jsessionid = cookieStore.get("rd_at");
  
  const headers: Record<string, string> = jsessionid ? { Cookie: `rd_at=${jsessionid.value}` } : {};

  const queryParams = new URLSearchParams();
  queryParams.append("page", page);
  queryParams.append("size", "20");
  if (action) queryParams.append("action", action);
  if (entityType) queryParams.append("entityType", entityType);

  const res = await fetch(`${apiUrl}/api/v1/audit?${queryParams.toString()}`, {
    headers,
    cache: "no-store",
  });

  if (res.status === 401 || res.status === 403) {
    redirect("/");
  }

  const data = res.ok ? await res.json() : null;

  return (
    <div className="min-h-screen p-8 max-w-7xl mx-auto">
      <header className="flex justify-between items-center mb-8">
        <div>
          <h1 className="text-3xl font-bold tracking-tight text-white mb-2">Audit Logs</h1>
          <p className="text-gray-400 text-sm">System-wide event tracking and security logging.</p>
        </div>
        <Link
          href="/"
          className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white glass-panel rounded-lg transition-colors border border-gray-500/30"
        >
          Back to Dashboard
        </Link>
      </header>

      <AuditFilterControls filterAction={action} filterEntityType={entityType} />

      <div className="glass-panel rounded-2xl overflow-hidden mb-6">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-gray-800/50 border-b border-gray-700/50">
                <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">Timestamp</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">Actor</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">Action</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">Entity</th>
                <th className="px-6 py-4 text-xs font-semibold text-gray-400 uppercase tracking-wider">Payload</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-700/50">
              {data?.content?.map((log: any) => (
                <tr key={log.id} className="hover:bg-white/[0.02] transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-300">
                    {new Date(log.createdAt).toLocaleString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-white font-medium">
                    {log.actorEmail}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="px-2.5 py-1 text-xs font-semibold rounded-full bg-blue-500/10 text-blue-400 border border-blue-500/20">
                      {log.action}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-400">
                    {log.entityType} <br/>
                    <span className="text-xs text-gray-500 font-mono" title={log.entityId}>{log.entityId.substring(0, 8)}...</span>
                  </td>
                  <td className="px-6 py-4 text-sm text-gray-400 max-w-xs truncate">
                    <div className="font-mono text-xs bg-black/30 p-2 rounded border border-white/5 overflow-hidden text-ellipsis whitespace-nowrap" title={log.payload}>
                      {log.payload}
                    </div>
                  </td>
                </tr>
              ))}
              {(!data?.content || data.content.length === 0) && (
                <tr>
                  <td colSpan={5} className="px-6 py-12 text-center text-gray-400">
                    No audit logs found matching the criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {data && data.totalPages > 1 && (
        <div className="flex justify-between items-center glass-panel px-6 py-4 rounded-xl">
          <Link
            href={`/admin/audit?${new URLSearchParams({ ...params, page: Math.max(0, parseInt(page, 10) - 1).toString() } as any).toString()}`}
            className={`px-4 py-2 text-sm font-medium text-white bg-gray-800 rounded-lg transition-colors border border-gray-700 hover:bg-gray-700 ${
              parseInt(page, 10) === 0 ? "opacity-50 pointer-events-none" : ""
            }`}
          >
            Previous
          </Link>
          <span className="text-sm text-gray-400">
            Page <span className="font-medium text-white">{parseInt(page, 10) + 1}</span> of <span className="font-medium text-white">{data.totalPages}</span>
            {" "}({data.totalElements} total logs)
          </span>
          <Link
            href={`/admin/audit?${new URLSearchParams({ ...params, page: Math.min(data.totalPages - 1, parseInt(page, 10) + 1).toString() } as any).toString()}`}
            className={`px-4 py-2 text-sm font-medium text-white bg-gray-800 rounded-lg transition-colors border border-gray-700 hover:bg-gray-700 ${
              parseInt(page, 10) >= data.totalPages - 1 ? "opacity-50 pointer-events-none" : ""
            }`}
          >
            Next
          </Link>
        </div>
      )}
    </div>
  );
}
