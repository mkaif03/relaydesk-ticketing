"use client";

import { useRouter } from "next/navigation";

export default function AuditFilterControls({
  filterAction,
  filterEntityType,
}: {
  filterAction: string;
  filterEntityType: string;
}) {
  const router = useRouter();

  const updateFilters = (newAction: string, newEntity: string) => {
    const params = new URLSearchParams();
    params.set("page", "0");
    if (newAction) params.set("action", newAction);
    if (newEntity) params.set("entityType", newEntity);
    router.push(`/admin/audit?${params.toString()}`);
  };

  return (
    <div className="glass-panel p-6 rounded-2xl mb-8 flex gap-4 items-end">
      <div className="flex-1">
        <label className="block text-sm font-medium text-gray-400 mb-1">Filter by Action</label>
        <input
          type="text"
          className="w-full bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)]"
          placeholder="e.g. CR_CREATED"
          value={filterAction}
          onChange={(e) => updateFilters(e.target.value, filterEntityType)}
        />
      </div>
      <div className="flex-1">
        <label className="block text-sm font-medium text-gray-400 mb-1">Filter by Entity Type</label>
        <input
          type="text"
          className="w-full bg-gray-900/50 border border-gray-700 rounded-lg px-4 py-2 text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)]"
          placeholder="e.g. ChangeRequest"
          value={filterEntityType}
          onChange={(e) => updateFilters(filterAction, e.target.value)}
        />
      </div>
      <button 
        onClick={() => { updateFilters("", ""); }}
        className="px-4 py-2 text-sm font-medium text-gray-300 hover:text-white bg-gray-800 rounded-lg transition-colors border border-gray-700"
      >
        Clear Filters
      </button>
    </div>
  );
}
