"use client";

import { useRouter } from "next/navigation";

export default function ReleaseActionButtons({ release }: { release: any }) {
  const router = useRouter();

  const shipRelease = async () => {
    const res = await fetch(`/api/v1/releases/${release.id}/ship?version=${release.version}`, { method: "POST" });
    if (res.ok) router.refresh();
    else alert("Failed to ship: " + (await res.json()).message);
  };

  const rollbackRelease = async () => {
    const res = await fetch(`/api/v1/releases/${release.id}/rollback?version=${release.version}`, { method: "POST" });
    if (res.ok) router.refresh();
    else alert("Failed to rollback: " + (await res.json()).message);
  };

  if (release.status !== "PLANNED" && release.status !== "SHIPPED") return null;

  return (
    <div className="flex gap-4 relative z-10 pt-4 border-t border-white/10">
      {release.status === "PLANNED" && (
        <button
          onClick={shipRelease}
          className="px-6 py-2 bg-[var(--color-primary-600)] hover:bg-[var(--color-primary-500)] text-white font-medium rounded-lg transition-colors shadow-[0_0_15px_rgba(79,70,229,0.3)]"
        >
          Ship Release
        </button>
      )}
      {release.status === "SHIPPED" && (
        <button
          onClick={rollbackRelease}
          className="px-6 py-2 bg-red-500/20 text-red-400 hover:bg-red-500/30 font-medium border border-red-500/50 rounded-lg transition-colors"
        >
          Rollback Release
        </button>
      )}
    </div>
  );
}
