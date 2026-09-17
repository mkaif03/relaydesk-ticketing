"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import Link from "next/link";

interface AuditEvent {
  id: number;
  actorEmail: string;
  action: string;
  entityType: string;
  entityId: string;
  payload: string;
  traceId: string;
  createdAt: string;
}

interface PageResponse {
  content: AuditEvent[];
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
}

export default function AuditLogsPage() {
  const router = useRouter();
  const [data, setData] = useState<PageResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [filterAction, setFilterAction] = useState("");
  const [filterEntityType, setFilterEntityType] = useState("");

  const fetchLogs = () => {
    setLoading(true);
    const params = new URLSearchParams({
      page: page.toString(),
      size: "20"
    });
    if (filterAction) params.append("action", filterAction);
    if (filterEntityType) params.append("entityType", filterEntityType);

    fetch(`/api/v1/audit?${params.toString()}`)
      .then((res) => {
        if (res.status === 401 || res.status === 403) {
          router.push("/");
          return null;
        }
        return res.json();
      })
      .then((resData) => {
        if (resData) setData(resData);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchLogs();
  }, [page, filterAction, filterEntityType]);

  if (!data && loading) {
    return <div className="min-h-screen flex items-center justify-center text-white">Loading Audit Logs...</div>;
  }

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

      <div className="glass-panel p-6 rounded-2xl mb-8 flex gap-4 items-end">
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-400 mb-1">Filter by Action</label>
          <input
            type="text"
            className="w-full bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)]"
            placeholder="e.g. CR_CREATED"
            value={filterAction}
            onChange={(e) => {
              setFilterAction(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <div className="flex-1">
          <label className="block text-sm font-medium text-gray-400 mb-1">Filter by Entity Type</label>
          <input
            type="text"
            className="w-full bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)]"
            placeholder="e.g. ChangeRequest"
            value={filterEntityType}
            onChange={(e) => {
              setFilterEntityType(e.target.value);
              setPage(0);
            }}
          />
        </div>
        <button 
          onClick={() => { setFilterAction(""); setFilterEntityType(""); setPage(0); }}
          className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white bg-gray-800 rounded-lg transition-colors border border-gray-700"
        >
          Clear Filters
        </button>
      </div>

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
              {data?.content.map((log) => (
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
              {data?.content.length === 0 && (
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
          <button
            disabled={data.number === 0}
            onClick={() => setPage(p => Math.max(0, p - 1))}
            className="px-4 py-2 text-sm font-medium text-white bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors border border-gray-700 hover:bg-gray-700"
          >
            Previous
          </button>
          <span className="text-sm text-gray-400">
            Page <span className="font-medium text-white">{data.number + 1}</span> of <span className="font-medium text-white">{data.totalPages}</span>
            {" "}({data.totalElements} total logs)
          </span>
          <button
            disabled={data.number >= data.totalPages - 1}
            onClick={() => setPage(p => Math.min(data.totalPages - 1, p + 1))}
            className="px-4 py-2 text-sm font-medium text-white bg-gray-800 disabled:opacity-50 disabled:cursor-not-allowed rounded-lg transition-colors border border-gray-700 hover:bg-gray-700"
          >
            Next
          </button>
        </div>
      )}
    </div>
  );
}
