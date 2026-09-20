"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

export default function CRForm({
  initialData,
  isEdit,
}: {
  initialData?: any;
  isEdit: boolean;
}) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    const formData = new FormData(e.currentTarget);
    const payload: any = {
      title: formData.get("title"),
      description: formData.get("description"),
      service: formData.get("service"),
      environment: formData.get("environment"),
      priority: formData.get("priority"),
      riskLevel: formData.get("riskLevel"),
    };

    if (isEdit) {
      payload.version = initialData.version;
    }

    try {
      const url = isEdit ? `/api/v1/change-requests/${initialData.id}` : "/api/v1/change-requests";
      const method = isEdit ? "PUT" : "POST";

      const res = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      if (!res.ok) {
        throw new Error((await res.text()) || `Failed to ${isEdit ? "update" : "create"} change request`);
      }

      const cr = await res.json();
      router.push(`/cr/${cr.id}`);
      router.refresh(); // Ensure the server component re-fetches
    } catch (err: any) {
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <>
      {error && (
        <div className="mb-6 p-4 rounded-lg bg-red-500/10 border border-red-500/20 text-red-400">{error}</div>
      )}

      <div className="glass-panel p-8 rounded-2xl">
        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-300">Title</label>
            <input
              name="title"
              defaultValue={initialData?.title}
              required
              className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all"
              placeholder="E.g., Upgrade Redis version to 7.0"
            />
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-300">Service</label>
              <input
                name="service"
                defaultValue={initialData?.service}
                required
                className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all"
                placeholder="E.g., auth-service"
              />
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-300">Environment</label>
              <select
                name="environment"
                defaultValue={initialData?.environment || "DEV"}
                required
                className="w-full bg-[#111111] border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all"
              >
                <option value="DEV">DEV</option>
                <option value="STAGING">STAGING</option>
                <option value="PRODUCTION">PRODUCTION</option>
              </select>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-6">
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-300">Priority</label>
              <select
                name="priority"
                defaultValue={initialData?.priority || "P3"}
                required
                className="w-full bg-[#111111] border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all"
              >
                <option value="P0">P0 - Critical</option>
                <option value="P1">P1 - High</option>
                <option value="P2">P2 - Medium</option>
                <option value="P3">P3 - Low</option>
              </select>
            </div>
            <div className="space-y-2">
              <label className="text-sm font-medium text-gray-300">Risk Level</label>
              <select
                name="riskLevel"
                defaultValue={initialData?.riskLevel || "LOW"}
                required
                className="w-full bg-[#111111] border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all"
              >
                <option value="LOW">Low</option>
                <option value="MEDIUM">Medium</option>
                <option value="HIGH">High</option>
                <option value="CRITICAL">Critical</option>
              </select>
            </div>
          </div>

          <div className="space-y-2">
            <label className="text-sm font-medium text-gray-300">Description (Markdown)</label>
            <textarea
              name="description"
              defaultValue={initialData?.description}
              required
              rows={8}
              className="w-full bg-white/5 border border-white/10 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:ring-2 focus:ring-[var(--color-primary-500)] focus:border-transparent transition-all font-mono text-sm"
              placeholder="Describe the changes, rollout plan, and impact..."
            />
          </div>

          <div className="pt-4 flex justify-end">
            <button
              type="submit"
              disabled={loading}
              className="px-6 py-2.5 bg-[var(--color-primary-500)] hover:bg-[var(--color-primary-600)] text-white font-medium rounded-lg shadow-lg shadow-[var(--color-primary-500)]/20 transition-colors disabled:opacity-50"
            >
              {loading ? "Saving..." : isEdit ? "Update Request" : "Create Request"}
            </button>
          </div>
        </form>
      </div>
    </>
  );
}
