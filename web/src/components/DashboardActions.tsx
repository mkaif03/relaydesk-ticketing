"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";

export default function DashboardActions({
  currentUser,
}: {
  currentUser: any;
}) {
  const router = useRouter();

  return (
    <div className="flex flex-col items-end gap-2">
      <div className="flex gap-4">
        {currentUser?.role === "ROLE_ADMIN" && (
          <Link
            href="/admin/audit"
            className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white glass-panel rounded-lg transition-colors border border-gray-500/30 flex items-center"
          >
            Audit Logs
          </Link>
        )}
        <Link
          href="/cr/new"
          className="px-4 py-2 text-sm font-medium text-white bg-[var(--color-primary-500)] hover:bg-[var(--color-primary-600)] rounded-lg transition-colors shadow-lg shadow-[var(--color-primary-500)]/20 flex items-center"
        >
          New Request
        </Link>
        <button
          onClick={() => {
            fetch("/api/v1/auth/logout", { method: "POST" }).then(() => router.push("/login"));
          }}
          className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white glass-panel rounded-lg transition-colors"
        >
          Sign Out
        </button>
      </div>
      {currentUser && (
        <div className="glass-panel px-3 py-1.5 rounded-lg flex items-center gap-2 border border-gray-500/30">
          <div className="w-2 h-2 rounded-full bg-green-500 shadow-[0_0_8px_rgba(34,197,94,0.8)] animate-pulse"></div>
          <span className="text-xs font-medium text-gray-200">{currentUser.email}</span>
        </div>
      )}
    </div>
  );
}
