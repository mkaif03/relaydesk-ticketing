"use client";

import { useRouter } from "next/navigation";

export default function FilterControls({
  filters,
}: {
  filters: { status: string; riskLevel: string; environment: string; authorEmail: string; q: string };
}) {
  const router = useRouter();

  const handleFilterChange = (e: React.ChangeEvent<HTMLInputElement | HTMLSelectElement>) => {
    const newFilters = { ...filters, [e.target.name]: e.target.value };
    const params = new URLSearchParams();
    
    // We only set page=0 if a filter changes.
    // If we push a new filter without page, the server will handle it (or use default 0).
    params.set("page", "0");
    if (newFilters.status) params.set("status", newFilters.status);
    if (newFilters.riskLevel) params.set("riskLevel", newFilters.riskLevel);
    if (newFilters.environment) params.set("environment", newFilters.environment);
    if (newFilters.authorEmail) params.set("authorEmail", newFilters.authorEmail);
    if (newFilters.q) params.set("q", newFilters.q);

    router.push(`/?${params.toString()}`);
  };

  return (
    <div className="glass-panel p-4 rounded-xl mb-8 flex flex-wrap gap-4 items-center">
      <div className="flex-1 min-w-[200px]">
        <input
          type="text"
          name="q"
          value={filters.q}
          onChange={handleFilterChange}
          placeholder="Search titles..."
          className="w-full bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-[var(--color-primary-500)] transition-colors"
        />
      </div>
      <div className="w-full md:w-auto flex flex-wrap gap-4">
        <select
          name="status"
          value={filters.status}
          onChange={handleFilterChange}
          className="bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-[var(--color-primary-500)] transition-colors"
        >
          <option value="">All Statuses</option>
          <option value="DRAFT">Draft</option>
          <option value="SUBMITTED">Submitted</option>
          <option value="CHANGES_REQUESTED">Changes Requested</option>
          <option value="APPROVED">Approved</option>
          <option value="REJECTED">Rejected</option>
          <option value="SCHEDULED">Scheduled</option>
          <option value="SHIPPED">Shipped</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
        
        <select
          name="riskLevel"
          value={filters.riskLevel}
          onChange={handleFilterChange}
          className="bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-[var(--color-primary-500)] transition-colors"
        >
          <option value="">All Risks</option>
          <option value="LOW">Low</option>
          <option value="MEDIUM">Medium</option>
          <option value="HIGH">High</option>
          <option value="CRITICAL">Critical</option>
        </select>

        <select
          name="environment"
          value={filters.environment}
          onChange={handleFilterChange}
          className="bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-[var(--color-primary-500)] transition-colors"
        >
          <option value="">All Envs</option>
          <option value="PRODUCTION">Production</option>
          <option value="STAGING">Staging</option>
          <option value="DEV">Dev</option>
        </select>

        <input
          type="text"
          name="authorEmail"
          value={filters.authorEmail}
          onChange={handleFilterChange}
          placeholder="Author Email"
          className="bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white focus:outline-none focus:border-[var(--color-primary-500)] transition-colors"
        />
      </div>
    </div>
  );
}
